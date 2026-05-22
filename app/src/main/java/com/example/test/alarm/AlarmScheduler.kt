package com.example.test.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.test.receiver.AlarmReceiver
import java.util.Calendar

fun scheduleMedicineAlarm(
    context: Context,
    medicineName: String,
    time: String
) {

    val parts = time.split(":")

    val hour = parts[0].toInt()
    val minute = parts[1].toInt()

    val calendar = Calendar.getInstance()

    calendar.set(
        Calendar.HOUR_OF_DAY,
        hour
    )

    calendar.set(
        Calendar.MINUTE,
        minute
    )

    calendar.set(
        Calendar.SECOND,
        0
    )

    if (
        calendar.timeInMillis <
        System.currentTimeMillis()
    ) {

        calendar.add(
            Calendar.DAY_OF_MONTH,
            1
        )
    }

    val alarmManager =
        context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

    val intent = Intent(
        context,
        AlarmReceiver::class.java
    )

    intent.putExtra(
        "medicine",
        medicineName
    )

    val pendingIntent =
        PendingIntent.getBroadcast(
            context,
            medicineName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent
    )

    // reminder 10 min before

    val reminderIntent = Intent(
        context,
        AlarmReceiver::class.java
    )

    reminderIntent.putExtra(
        "medicine",
        medicineName
    )

    reminderIntent.putExtra(
        "isReminder",
        true
    )

    val reminderPendingIntent =
        PendingIntent.getBroadcast(
            context,
            medicineName.hashCode() + 1000,
            reminderIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis - (10 * 60 * 1000),
        reminderPendingIntent
    )
}
