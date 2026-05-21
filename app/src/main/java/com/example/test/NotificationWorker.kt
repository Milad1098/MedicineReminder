package com.example.test

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {

        val manager =
            applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val channelId = "medicine_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                channelId,
                "Medicine Reminder",
                NotificationManager.IMPORTANCE_HIGH
            )

            manager.createNotificationChannel(channel)
        }

        val medicineName =
            inputData.getString("medicine") ?: "دارو"

        val notification =
            NotificationCompat.Builder(
                applicationContext,
                channelId
            )
                .setContentTitle("زمان مصرف دارو")
                .setContentText("مصرف $medicineName را فراموش نکن")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

        manager.notify(
            System.currentTimeMillis().toInt(),
            notification
        )

        return Result.success()
    }
}
