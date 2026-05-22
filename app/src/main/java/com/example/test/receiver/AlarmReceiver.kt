package com.example.test.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.test.R

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        val medicineName =
            intent.getStringExtra("medicine")
                ?: "دارو"

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

        val notification =
            NotificationCompat.Builder(
                context,
                channelId
            )
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("زمان مصرف دارو")
                .setContentText(medicineName)
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .build()

        manager.notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }
}
