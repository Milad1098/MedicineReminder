package com.example.test.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.test.AlarmActivity
import com.example.test.R

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        val medicineName =
            intent.getStringExtra(
                "medicine"
            ) ?: "دارو"

        val isReminder =
            intent.getBooleanExtra(
                "isReminder",
                false
            )

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        // reminder notification

        if (isReminder) {

            val reminderChannel =
                NotificationChannel(
                    "medicine_reminder",
                    "Medicine Reminder",
                    NotificationManager.IMPORTANCE_HIGH
                )

            manager.createNotificationChannel(
                reminderChannel
            )

            val reminderNotification =
                NotificationCompat.Builder(
                    context,
                    "medicine_reminder"
                )
                    .setSmallIcon(
                        android.R.drawable.ic_dialog_info
                    )
                    .setContentTitle(
                        "یادآوری مصرف دارو"
                    )
                    .setContentText(
                        "۱۰ دقیقه تا مصرف $medicineName باقی مانده"
                    )
                    .setOngoing(true)
                    .build()

            manager.notify(
                medicineName.hashCode(),
                reminderNotification
            )

            return
        }

        // full screen alarm

        val channelId = "medicine_alarm"

        val channel =
            NotificationChannel(
                channelId,
                "Medicine Alarm",
                NotificationManager.IMPORTANCE_HIGH
            )

        manager.createNotificationChannel(channel)

        val fullScreenIntent =
            Intent(
                context,
                AlarmActivity::class.java
            )

        fullScreenIntent.putExtra(
            "medicine",
            medicineName
        )

        fullScreenIntent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP

        val fullScreenPendingIntent =
            PendingIntent.getActivity(
                context,
                999,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val notification =
            NotificationCompat.Builder(
                context,
                channelId
            )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setContentTitle(
                    "زمان مصرف دارو"
                )
                .setContentText(
                    medicineName
                )
                .setPriority(
                    NotificationCompat.PRIORITY_MAX
                )
                .setCategory(
                    NotificationCompat.CATEGORY_ALARM
                )
                .setFullScreenIntent(
                    fullScreenPendingIntent,
                    true
                )
                .setAutoCancel(true)
                .build()

        manager.notify(
            2001,
            notification
        )

        val activityIntent =
            Intent(
                context,
                AlarmActivity::class.java
            )

        activityIntent.putExtra(
            "medicine",
            medicineName
        )

        activityIntent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK

        context.startActivity(activityIntent)
    }
}
