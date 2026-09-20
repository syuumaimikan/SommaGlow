package com.example.data.tracking

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.receiver.SleepReceiver
import com.example.domain.model.SleepSession
import com.example.domain.repository.SleepRepository
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.SleepSegmentRequest

/**
 * Android Sleep API の登録・解除、およびステータス管理を行うマネージャークラス
 */
class SleepTrackingManager(
    private val context: Context,
    private val sleepRepository: SleepRepository
) {
    companion object {
        private const val TAG = "SleepTrackingManager"
        private const val REQUEST_CODE_SLEEP_API = 1001
    }

    private val activityRecognitionClient by lazy {
        ActivityRecognition.getClient(context)
    }

    private val sleepPendingIntent: PendingIntent by lazy {
        val intent = Intent(context, SleepReceiver::class.java).apply {
            action = SleepReceiver.ACTION_SLEEP_UPDATE
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        PendingIntent.getBroadcast(context, REQUEST_CODE_SLEEP_API, intent, flags)
    }

    fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun startSleepTracking(
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        if (!hasPermission()) {
            Log.w(TAG, "ACTIVITY_RECOGNITION permission not granted")
            onFailure(SecurityException("Activity Recognition permission required"))
            return
        }

        try {
            val request = SleepSegmentRequest.getDefaultSleepSegmentRequest()
            activityRecognitionClient.requestSleepSegmentUpdates(sleepPendingIntent, request)
                .addOnSuccessListener {
                    Log.i(TAG, "SleepSegment updates registered successfully")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to register SleepSegment updates", e)
                    onFailure(e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception starting sleep tracking", e)
            onFailure(e)
        }
    }

    fun stopSleepTracking(
        onComplete: () -> Unit = {}
    ) {
        try {
            activityRecognitionClient.removeSleepSegmentUpdates(sleepPendingIntent)
                .addOnCompleteListener {
                    Log.i(TAG, "SleepSegment updates unregistered")
                    onComplete()
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception stopping sleep tracking", e)
            onComplete()
        }
    }

    /**
     * 朝の睡眠データ同期（決定論的・固定値アルゴリズム）
     * 何度押してもスコアや時間がランダムに変動せず、安定した同一データを保持します。
     */
    suspend fun syncMorningSleepData(targetSleepHours: Float = 8.0f): SleepSession {
        return sleepRepository.recordDeterministicMorningSleepSession(targetSleepHours)
    }
}
