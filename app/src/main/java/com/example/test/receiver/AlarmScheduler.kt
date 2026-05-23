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
        val hour   = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return

        val firstTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }.timeInMillis

        if (medicine.cycleHours > 0) {
            // سیکلی — چند وعده در روز
            val totalDoses = 24 / medicine.cycleHours
            repeat(totalDoses) { i ->
                val doseTime = firstTime + i * medicine.cycleHours * 3600_000L
                val reqCode  = medicine.id * 100 + i

                scheduleOne(context, alarmManager, medicine, doseTime, reqCode)
                scheduleReminders(context, alarmManager, medicine, doseTime, reqCode)
            }
        } else {
            // روزانه یک بار
            scheduleOne(context, alarmManager, medicine, firstTime, medicine.id)
            scheduleReminders(context, alarmManager, medicine, firstTime, medicine.id)
        }
    }

    private fun scheduleOne(
        context: Context,
        alarmManager: AlarmManager,
        medicine: Medicine,
        triggerTime: Long,
        requestCode: Int
    ) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("medicine_id",   medicine.id)
            putExtra("medicine_name", medicine.name)
            putExtra("isReminder",    false)
            putExtra("request_code",  requestCode)
        }
        scheduleExact(context, alarmManager, requestCode, intent, triggerTime)
    }

    private fun scheduleReminders(
        context: Context,
        alarmManager: AlarmManager,
        medicine: Medicine,
        mainTime: Long,
        baseCode: Int
    ) {
        listOf(10 to 1, 5 to 2, 1 to 3).forEach { (mins, offset) ->
            val t = mainTime - mins * 60_000L
            if (t > System.currentTimeMillis()) {
                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("medicine_id",   medicine.id)
                    putExtra("medicine_name", medicine.name)
                    putExtra("isReminder",    true)
                    putExtra("minutes_left",  mins)
                }
                scheduleExact(
                    context, alarmManager,
                    baseCode * 1000 + offset,
                    intent, t
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

    fun cancelAlarm(context: Context, medicine: Medicine) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val totalDoses = if (medicine.cycleHours > 0) 24 / medicine.cycleHours else 1
        repeat(totalDoses) { i ->
            val baseCode = if (medicine.cycleHours > 0) medicine.id * 100 + i else medicine.id
            listOf(baseCode, baseCode * 1000 + 1, baseCode * 1000 + 2, baseCode * 1000 + 3)
                .forEach { code ->
                    alarmManager.cancel(
                        PendingIntent.getBroadcast(
                            context, code,
                            Intent(context, AlarmReceiver::class.java),
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                }
        }
    }

    // overload برای backward compatibility
    fun cancelAlarm(context: Context, medicineId: Int) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        (0..10).forEach { i ->
            val baseCode = medicineId * 100 + i
            listOf(baseCode, baseCode * 1000 + 1, baseCode * 1000 + 2, baseCode * 1000 + 3)
                .forEach { code ->
                    alarmManager.cancel(
                        PendingIntent.getBroadcast(
                            context, code,
                            Intent(context, AlarmReceiver::class.java),
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                }
        }
    }
}
