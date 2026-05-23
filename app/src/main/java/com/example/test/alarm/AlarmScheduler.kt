package com.example.test.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.test.data.local.Medicine
import java.util.Calendar

object AlarmScheduler {

    fun scheduleAlarm(
        context: Context,
        medicine: Medicine
    ) {

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        val parts =
            medicine.time.split(":")

        val hour =
            parts[0].toInt()

        val minute =
            parts[1].toInt()

        val calendar =
            Calendar.getInstance().apply {

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

            putExtra(
                "medicine_id",
                medicine.id
            )

            putExtra(
                "medicine_name",
                medicine.name
            )
        }

        val pendingIntent =
            PendingIntent.getBroadcast(

                context,
                medicine.id,

                intent,

                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    fun cancelAlarm(
        context: Context,
        medicineId: Int
    ) {

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        val intent = Intent(
            context,
            AlarmReceiver::class.java
        )

        val pendingIntent =
            PendingIntent.getBroadcast(

                context,
                medicineId,

                intent,

                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        alarmManager.cancel(pendingIntent)
    }
}
