package com.example.test.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build

object NotificationHelper {

    const val CHANNEL_ID = "medicine_channel"

    fun createNotificationChannel(
        context: Context
    ) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val soundUri = Uri.parse(
                "android.resource://${context.packageName}/raw/alarm"
            )

            val audioAttributes =
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Medicine Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {

                description = "داروها"

                enableVibration(true)

                setSound(
                    soundUri,
                    audioAttributes
                )
            }

            val manager =
                context.getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(channel)
        }
    }
}
