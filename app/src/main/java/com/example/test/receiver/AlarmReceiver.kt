package com.example.test.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.test.AlarmActivity
import com.example.test.MainActivity

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        val medicineName =
            intent.getStringExtra("medicine")
                ?: "دارو"

        val medicineId =
            intent.getIntExtra("medicine_id", 0)

        val isReminder =
            intent.getBooleanExtra(
                "isReminder",
                false
            )

        val channelId = "medicine_channel"

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Medicine Reminder",
            NotificationManager.IMPORTANCE_HIGH
        )

        manager.createNotificationChannel(channel)

        val openIntent =
            Intent(
                context,
                MainActivity::class.java
            )

        val openPendingIntent =
            PendingIntent.getActivity(
                context,
                medicineId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        if (isReminder) {

            val notification =
                NotificationCompat.Builder(
                    context,
                    channelId
                )
                    .setSmallIcon(
                        android.R.drawable.ic_dialog_info
                    )
                    .setContentTitle(
                        "یادآوری مصرف دارو"
                    )
                    .setContentText(
                        "کمتر از 10 دقیقه تا مصرف $medicineName باقی مانده"
                    )
                    .setPriority(
                        NotificationCompat.PRIORITY_HIGH
                    )
                    .setAutoCancel(true)
                    .setOngoing(true)
                    .setContentIntent(
                        openPendingIntent
                    )
                    .build()

            manager.notify(
                medicineId + 5000,
                notification
            )

            return
        }

        val alarmIntent =
            Intent(
                context,
                AlarmActivity::class.java
            ).apply {

                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                )

                putExtra(
                    "medicine",
                    medicineName
                )

                putExtra(
                    "medicine_id",
                    medicineId
                )
            }

        context.startActivity(alarmIntent)
    }
}
