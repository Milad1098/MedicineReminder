package com.example.test.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.test.receiver.AlarmReceiver
import java.util.Calendar

class AlarmScheduler(
    private val context: Context
) {

    fun schedule(
        medicineName: String,
        hour: Int,
        minute: Int
    ) {

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        val intent =
            Intent(
                context,
                AlarmReceiver::class.java
            ).apply {

                putExtra(
                    "medicine",
                    medicineName
                )
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                medicineName.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val calendar =
            Calendar.getInstance().apply {

                set(Calendar.HOUR_OF_DAY, hour)

                set(Calendar.MINUTE, minute)

                set(Calendar.SECOND, 0)

                if (before(Calendar.getInstance())) {

                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
}
