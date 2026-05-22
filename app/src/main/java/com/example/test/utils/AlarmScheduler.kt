package com.example.test.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.test.receiver.AlarmReceiver
import java.util.Calendar

fun scheduleNotification(
    context: Context,
    medicineName: String
) {

    val alarmManager =
        context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

    val intent = Intent(
        context,
        AlarmReceiver::class.java
    )

    intent.putExtra(
        "medicine_name",
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

    val calendar = Calendar.getInstance().apply {

        add(Calendar.MINUTE, 1)
    }

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent
    )
}
