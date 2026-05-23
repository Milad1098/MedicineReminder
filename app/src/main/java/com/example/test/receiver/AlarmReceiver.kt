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
import com.example.test.R

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
            intent.getBooleanExtra("isReminder", false)

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

        val openAppIntent =
            Intent(context, MainActivity::class.java)

        val openPendingIntent =
            PendingIntent.getActivity(
                context,
                medicineId,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        if (isReminder) {

            val notification =
                NotificationCompat.Builder(
                    context,
                    channelId
                )
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("یادآوری دارو")
                    .setContentText(
                        "کمتر از 10 دقیقه تا مصرف $medicineName باقی مانده"
                    )
                    .setPriority(
                        NotificationCompat.PRIORITY_HIGH
                    )
                    .setOngoing(true)
                    .setAutoCancel(true)
                    .setContentIntent(openPendingIntent)
                    .build()

            manager.notify(
                medicineId + 5000,
                notification
            )

            return
        }

        val fullScreenIntent =
            Intent(context, AlarmActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                )

                putExtra("medicine", medicineName)
                putExtra("medicine_id", medicineId)
            }

        context.startActivity(fullScreenIntent)
    }
}
