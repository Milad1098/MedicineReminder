package com.example.test.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
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

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val attributes =
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()

            val channel =
                NotificationChannel(
                    channelId,
                    "Medicine Alarm",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {

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
        }

        val activityIntent =
            Intent(context, AlarmActivity::class.java).apply {

                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP

                putExtra("medicine", medicineName)
            }

        val pendingIntent =
            PendingIntent.getActivity(
                context,
                100,
                activityIntent,
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
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setAutoCancel(false)
                .setFullScreenIntent(
                    pendingIntent,
                    true
                )
                .build()

        manager.notify(999, notification)

        /*
            مهم‌ترین بخش
         */

        context.startActivity(activityIntent)
    }
}
