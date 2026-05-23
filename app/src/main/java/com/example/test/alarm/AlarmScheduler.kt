package com.example.test.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.test.receiver.AlarmReceiver
import java.util.Calendar

object AlarmScheduler {

    fun schedule(
        context: Context,
        medicineId: Int,
        medicineName: String,
        hour: Int,
        minute: Int
    ) {

        cancel(context, medicineId)

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        val reminderIntent = Intent(
            context,
            AlarmReceiver::class.java
        ).apply {
            putExtra("medicine", medicineName)
            putExtra("medicine_id", medicineId)
            putExtra("isReminder", true)
        }

        val reminderPendingIntent =
            PendingIntent.getBroadcast(
                context,
                medicineId + 10000,
                reminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val alarmIntent = Intent(
            context,
            AlarmReceiver::class.java
        ).apply {
            putExtra("medicine", medicineName)
            putExtra("medicine_id", medicineId)
            putExtra("isReminder", false)
        }

        val alarmPendingIntent =
            PendingIntent.getBroadcast(
                context,
                medicineId,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val now = Calendar.getInstance()

        val alarmCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)

            if (before(now)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val reminderCalendar =
            alarmCalendar.clone() as Calendar

        reminderCalendar.add(Calendar.MINUTE, -10)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminderCalendar.timeInMillis,
            reminderPendingIntent
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alarmCalendar.timeInMillis,
            alarmPendingIntent
        )
    }

    fun cancel(
        context: Context,
        medicineId: Int
    ) {

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        val reminderIntent =
            Intent(context, AlarmReceiver::class.java)

        val reminderPendingIntent =
            PendingIntent.getBroadcast(
                context,
                medicineId + 10000,
                reminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val alarmIntent =
            Intent(context, AlarmReceiver::class.java)

        val alarmPendingIntent =
            PendingIntent.getBroadcast(
                context,
                medicineId,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        alarmManager.cancel(reminderPendingIntent)
        alarmManager.cancel(alarmPendingIntent)

        reminderPendingIntent.cancel()
        alarmPendingIntent.cancel()
    }
}
