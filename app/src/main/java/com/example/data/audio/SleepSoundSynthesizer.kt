package com.example.data.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.example.domain.model.SleepSoundType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

/**
 * 100% オンデバイス・完全オフラインの快眠サウンド合成エンジン
 * 外部音声ファイルやネットワーク通信に一切依存せず、AudioTrack 上でリアルタイムに
 * ピンクノイズ、バイノーラルビート(デルタ波)、雨音、波音を数学的に合成・連続再生します。
 */
class SleepSoundSynthesizer(
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "SleepSoundSynthesizer"
        private const val SAMPLE_RATE = 22050
        private const val BUFFER_SECONDS = 2 // 2秒ループバッファ
    }

    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private var timerJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSound = MutableStateFlow<SleepSoundType?>(null)
    val currentSound: StateFlow<SleepSoundType?> = _currentSound.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(0)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    fun playSound(type: SleepSoundType, durationMinutes: Int = 30) {
        stopSound()

        _currentSound.value = type
        _isPlaying.value = true
        _remainingSeconds.value = durationMinutes * 60

        playbackJob = scope.launch(Dispatchers.Default) {
            try {
                val totalFrames = SAMPLE_RATE * BUFFER_SECONDS
                // ステレオ short 配列 (L, R 交互)
                val buffer = ShortArray(totalFrames * 2)

                generateSoundBuffer(type, buffer, totalFrames)

                val minBufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val trackBufferSize = maxOf(minBufferSize, buffer.size * 2)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                            .build()
                    )
                    .setBufferSizeInBytes(trackBufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack = track
                track.play()

                // タイマーループ
                startTimer(durationMinutes)

                while (isActive && _isPlaying.value) {
                    track.write(buffer, 0, buffer.size)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in SleepSoundSynthesizer playback", e)
            } finally {
                cleanupTrack()
            }
        }
    }

    fun stopSound() {
        _isPlaying.value = false
        _currentSound.value = null
        _remainingSeconds.value = 0
        playbackJob?.cancel()
        playbackJob = null
        timerJob?.cancel()
        timerJob = null
        cleanupTrack()
    }

    private fun startTimer(durationMinutes: Int) {
        timerJob?.cancel()
        if (durationMinutes <= 0) return

        timerJob = scope.launch(Dispatchers.Default) {
            while (isActive && _remainingSeconds.value > 0) {
                delay(1000L)
                val next = _remainingSeconds.value - 1
                _remainingSeconds.value = next
                if (next <= 0) {
                    stopSound()
                    break
                }
            }
        }
    }

    private fun cleanupTrack() {
        try {
            audioTrack?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error cleaning up AudioTrack", e)
        }
        audioTrack = null
    }

    /**
     * 音響タイプに応じた波形合成
     */
    private fun generateSoundBuffer(type: SleepSoundType, buffer: ShortArray, totalFrames: Int) {
        when (type) {
            SleepSoundType.DELTA_BINAURAL -> {
                // バイノーラルビート: 左耳 200Hz, 右耳 204Hz -> 脳内で 4Hz のデルタ波（深層ノンレム睡眠波）を共鳴
                val freqL = 200.0
                val freqR = 204.0
                val amplitude = 12000.0 // 穏やかな音量
                for (i in 0 until totalFrames) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val sampleL = (sin(2.0 * Math.PI * freqL * t) * amplitude).toInt().coerceIn(-32767, 32767).toShort()
                    val sampleR = (sin(2.0 * Math.PI * freqR * t) * amplitude).toInt().coerceIn(-32767, 32767).toShort()
                    buffer[i * 2] = sampleL
                    buffer[i * 2 + 1] = sampleR
                }
            }

            SleepSoundType.PINK_NOISE -> {
                // Voss-McCartney アルゴリズムによる近似ピンクノイズ (1/f スペクトル)
                var b0 = 0.0
                var b1 = 0.0
                var b2 = 0.0
                for (i in 0 until totalFrames) {
                    val white = Random.nextDouble(-1.0, 1.0)
                    b0 = 0.99886 * b0 + white * 0.0555179
                    b1 = 0.99332 * b1 + white * 0.0750759
                    b2 = 0.96900 * b2 + white * 0.1538520
                    val pink = (b0 + b1 + b2 + white * 0.5362) * 2500.0
                    val sample = pink.toInt().coerceIn(-32767, 32767).toShort()
                    buffer[i * 2] = sample
                    buffer[i * 2 + 1] = sample
                }
            }

            SleepSoundType.RAIN -> {
                // ローパスフィルタード・ノイズに低頻度のドロップレットを加えた雨音
                var lastSample = 0.0
                for (i in 0 until totalFrames) {
                    val white = Random.nextDouble(-1.0, 1.0)
                    // ローパスフィルター (カットオフ ~1.5kHz)
                    lastSample = 0.92 * lastSample + 0.08 * white
                    var combined = lastSample * 9000.0
                    // ランダムな水滴
                    if (Random.nextDouble() < 0.002) {
                        combined += Random.nextDouble(1000.0, 4000.0)
                    }
                    val s = combined.toInt().coerceIn(-32767, 32767).toShort()
                    buffer[i * 2] = s
                    buffer[i * 2 + 1] = s
                }
            }

            SleepSoundType.OCEAN -> {
                // 波の満ち引き (周期 約6秒の正弦波でピンクノイズを変調)
                var last = 0.0
                for (i in 0 until totalFrames) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val swell = (sin(2.0 * Math.PI * 0.18 * t) + 1.2) * 0.5
                    val white = Random.nextDouble(-1.0, 1.0)
                    last = 0.94 * last + 0.06 * white
                    val s = (last * swell * 11000.0).toInt().coerceIn(-32767, 32767).toShort()
                    buffer[i * 2] = s
                    buffer[i * 2 + 1] = s
                }
            }
        }
    }
}
