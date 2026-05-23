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

        val mainTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }.timeInMillis

        // آلارم اصلی
        scheduleExact(
            context, alarmManager,
            medicine.id,
            Intent(context, AlarmReceiver::class.java).apply {
                putExtra("medicine_id", medicine.id)
                putExtra("medicine_name", medicine.name)
                putExtra("isReminder", false)
            },
            mainTime
        )

        // reminder در ۱۰، ۵، ۱ دقیقه قبل
        listOf(10, 5, 1).forEachIndexed { index, minutesBefore ->
            val reminderTime = mainTime - (minutesBefore * 60 * 1000L)
            if (reminderTime > System.currentTimeMillis()) {
                scheduleExact(
                    context, alarmManager,
                    medicine.id + (index + 1) * 10000,
                    Intent(context, AlarmReceiver::class.java).apply {
                        putExtra("medicine_id", medicine.id)
                        putExtra("medicine_name", medicine.name)
                        putExtra("isReminder", true)
                        putExtra("minutes_left", minutesBefore)
                    },
                    reminderTime
                )
            }
        }
    }

    private fun scheduleExact(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int,
        intent: Intent,
        triggerTime: Long
    ) {
        val pending = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerTime, pending
            )
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pending)
        }
    }

    fun cancelAlarm(context: Context, medicineId: Int) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // لغو آلارم اصلی + سه reminder
        listOf(medicineId, medicineId + 10000, medicineId + 20000, medicineId + 30000)
            .forEach { id ->
                val pending = PendingIntent.getBroadcast(
                    context, id,
                    Intent(context, AlarmReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pending)
            }
    }
}
