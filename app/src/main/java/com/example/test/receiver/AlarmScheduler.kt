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
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
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

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            mainPending
        )

        // نوتیف ۱۰ دقیقه قبل
        val reminderCalendar = (calendar.clone() as Calendar).apply {
            add(Calendar.MINUTE, -10)
        }

        if (reminderCalendar.after(Calendar.getInstance())) {

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

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderCalendar.timeInMillis,
                reminderPending
            )
        }
    }

    fun cancelAlarm(context: Context, medicineId: Int) {

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // لغو آلارم اصلی
        val mainIntent = Intent(context, AlarmReceiver::class.java)
        val mainPending = PendingIntent.getBroadcast(
            context,
            medicineId,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(mainPending)

        // لغو نوتیف reminder
        val reminderIntent = Intent(context, AlarmReceiver::class.java)
        val reminderPending = PendingIntent.getBroadcast(
            context,
            medicineId + 10000,
            reminderIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(reminderPending)
    }
}
