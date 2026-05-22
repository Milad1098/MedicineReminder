package com.example.test.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.example.test.AlarmActivity
import com.example.test.R

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        val medicineName =
            intent.getStringExtra("medicine")
                ?: "دارو"

        val channelId = "medicine_alarm"

        val soundUri = Uri.parse(
            "android.resource://${context.packageName}/${R.raw.alarm}"
        )

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Medicine Alarm",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {

            description = "Medicine alarm channel"

            enableVibration(true)

            vibrationPattern = longArrayOf(
                0,
                1000,
                1000,
                1000
            )

            setSound(soundUri, attributes)

            lockscreenVisibility =
                android.app.Notification.VISIBILITY_PUBLIC
        }

        manager.createNotificationChannel(channel)

        val fullScreenIntent =
            Intent(context, AlarmActivity::class.java).apply {

                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP

                putExtra("medicine", medicineName)
            }

        val fullScreenPendingIntent =
            PendingIntent.getActivity(
                context,
                111,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val notification =
            NotificationCompat.Builder(
                context,
                channelId
            )
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("⏰ زمان مصرف دارو")
                .setContentText("$medicineName را مصرف کن")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setAutoCancel(false)
                .setFullScreenIntent(
                    fullScreenPendingIntent,
                    true
                )
                .build()

        manager.notify(999, notification)
    }
}
