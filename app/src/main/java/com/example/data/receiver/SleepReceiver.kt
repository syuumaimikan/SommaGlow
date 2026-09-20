package com.example.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.local.SomnaGlowDatabase
import com.example.data.repository.SleepRepositoryImpl
import com.example.domain.model.SleepClassification
import com.google.android.gms.location.SleepClassifyEvent
import com.google.android.gms.location.SleepSegmentEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Android Sleep API (Google Play Services) からのブロードキャストを受信するレシーバー
 *
 * バックグラウンドで配信される 2 種類のイベントを処理します:
 * 1. SleepSegmentEvent:
 *    起床時などに過去のまとまった睡眠セッション（開始時刻、終了時刻、ステータス）が確定した際に配信。
 * 2. SleepClassifyEvent:
 *    およそ10分間隔で現在の睡眠確率（Confidence）、体の動き（Motion）、周囲の明るさ（Light）が配信。
 *
 * Hilt利用時は @AndroidEntryPoint で Repository をインジェクト可能。
 * 本クラスでは goAsync() による非同期コルーチン実行で Room DB へ安全に永続化します。
 */
class SleepReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "SleepReceiver"
        const val ACTION_SLEEP_UPDATE = "com.example.ACTION_SLEEP_UPDATE"
    }

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        val pendingResult = goAsync()

        receiverScope.launch {
            try {
                // 1. SleepSegmentEvent の検知と抽出
                if (SleepSegmentEvent.hasEvents(intent)) {
                    val segmentEvents = SleepSegmentEvent.extractEvents(intent)
                    Log.d(TAG, "SleepSegmentEvents received: count=${segmentEvents.size}")

                    val db = SomnaGlowDatabase.getInstance(context)
                    val repository = SleepRepositoryImpl(db.sleepDao())

                    for (event in segmentEvents) {
                        Log.i(
                            TAG,
                            "Segment: start=${event.startTimeMillis}, end=${event.endTimeMillis}, status=${event.status}"
                        )
                        repository.saveSleepSegment(
                            startTimeMillis = event.startTimeMillis,
                            endTimeMillis = event.endTimeMillis,
                            status = event.status
                        )
                    }
                }

                // 2. SleepClassifyEvent (10分毎の睡眠分類) の検知と抽出
                if (SleepClassifyEvent.hasEvents(intent)) {
                    val classifyEvents = SleepClassifyEvent.extractEvents(intent)
                    Log.d(TAG, "SleepClassifyEvents received: count=${classifyEvents.size}")

                    val db = SomnaGlowDatabase.getInstance(context)
                    val repository = SleepRepositoryImpl(db.sleepDao())

                    for (event in classifyEvents) {
                        Log.v(
                            TAG,
                            "Classify: time=${event.timestampMillis}, confidence=${event.confidence}, motion=${event.motion}, light=${event.light}"
                        )
                        repository.saveSleepClassification(
                            SleepClassification(
                                timestampMillis = event.timestampMillis,
                                confidence = event.confidence,
                                motionState = event.motion,
                                lightLevel = event.light
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing sleep API events", e)
            } finally {
                // 非同期処理完了をOSに通知
                pendingResult.finish()
            }
        }
    }
}
