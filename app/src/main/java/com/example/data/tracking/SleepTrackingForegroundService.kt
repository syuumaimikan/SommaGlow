package com.example.data.tracking

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.local.SomnaGlowDatabase
import com.example.data.repository.SleepRepositoryImpl

/**
 * 24時間バックグラウンドで睡眠検知とバイオリズム監視を継続するための Foreground Service
 * システムによるプロセス Kill を防止し、Sleep API のイベント配信を確実に保証します。
 */
class SleepTrackingForegroundService : Service() {

    companion object {
        private const val TAG = "SleepTrackingService"
        const val CHANNEL_ID = "somnaglow_tracking_channel"
        const val NOTIFICATION_ID = 4001

        const val ACTION_START = "com.example.ACTION_START_TRACKING"
        const val ACTION_STOP = "com.example.ACTION_STOP_TRACKING"

        fun startService(context: Context) {
            val intent = Intent(context, SleepTrackingForegroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, SleepTrackingForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private lateinit var trackingManager: SleepTrackingManager

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val db = SomnaGlowDatabase.getInstance(applicationContext)
        val sleepRepo = SleepRepositoryImpl(db.sleepDao())
        trackingManager = SleepTrackingManager(applicationContext, sleepRepo)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                Log.i(TAG, "Stopping SleepTrackingForegroundService")
                trackingManager.stopSleepTracking()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                Log.i(TAG, "Starting SleepTrackingForegroundService in foreground")
                val notification = buildForegroundNotification()
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        startForeground(NOTIFICATION_ID, notification, 0)
                    } else {
                        startForeground(NOTIFICATION_ID, notification)
                    }
                } catch (e: SecurityException) {
                    Log.w(TAG, "SecurityException with FOREGROUND_SERVICE_TYPE_HEALTH, attempting fallback startForeground", e)
                    try {
                        startForeground(NOTIFICATION_ID, notification)
                    } catch (e2: Exception) {
                        Log.e(TAG, "Failed startForeground completely, stopping service", e2)
                        stopSelf()
                        return START_NOT_STICKY
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Unexpected error in startForeground", e)
                    stopSelf()
                    return START_NOT_STICKY
                }

                trackingManager.startSleepTracking(
                    onSuccess = { Log.i(TAG, "Sleep API successfully registered via service") },
                    onFailure = { e -> Log.w(TAG, "Failed registering Sleep API in service", e) }
                )
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildForegroundNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SomnaGlow 睡眠・生体トラッキング稼働中")
            .setContentText("Sleep API 自動検知 & サーカディアンリズム監視中")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SomnaGlow 生体モニタリング",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "バックグラウンド睡眠トラッキングとサーカディアンリズム監視通知"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
