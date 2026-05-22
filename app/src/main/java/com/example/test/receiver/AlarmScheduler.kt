package com.example.test.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object AlarmScheduler {

    fun scheduleAlarm(
        context: Context,
        medicineName: String,
        time: String
    ) {

        try {

            val parts = time.split(":")

            val hour = parts[0].toInt()
            val minute = parts[1].toInt()

            val calendar = Calendar.getInstance().apply {

                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)

                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            val intent = Intent(
                context,
                AlarmReceiver::class.java
            ).apply {

                putExtra("medicine", medicineName)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                medicineName.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager =
                context.getSystemService(
                    Context.ALARM_SERVICE
                ) as AlarmManager

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }
}
