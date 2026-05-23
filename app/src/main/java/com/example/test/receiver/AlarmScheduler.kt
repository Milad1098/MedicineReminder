package com.example.test.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.test.data.local.Medicine
import java.util.Calendar

object AlarmScheduler {

    fun scheduleAlarm(context: Context, medicine: Medicine) {

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val parts = medicine.time.split(":")
        if (parts.size != 2) return

        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        // آلارم اصلی
        val mainIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("medicine_id", medicine.id)
            putExtra("medicine_name", medicine.name)
            putExtra("isReminder", false)
        }

        val mainPending = PendingIntent.getBroadcast(
            context,
            medicine.id,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                mainPending
            )
        } catch (e: SecurityException) {
            // اگه پرمیشن exact alarm نداشت
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                mainPending
            )
        }

        // reminder ۱۰ دقیقه قبل
        val reminderTime = calendar.timeInMillis - (10 * 60 * 1000)
        if (reminderTime > System.currentTimeMillis()) {

            val reminderIntent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("medicine_id", medicine.id)
                putExtra("medicine_name", medicine.name)
                putExtra("isReminder", true)
            }

            val reminderPending = PendingIntent.getBroadcast(
                context,
                medicine.id + 10000,
                reminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    reminderPending
                )
            } catch (e: SecurityException) {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    reminderPending
                )
            }
        }
    }

    fun cancelAlarm(context: Context, medicineId: Int) {

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        listOf(medicineId, medicineId + 10000).forEach { id ->
            val intent = Intent(context, AlarmReceiver::class.java)
            val pending = PendingIntent.getBroadcast(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pending)
        }
    }
}
