package com.example.test.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.test.AlarmActivity
import com.example.test.MainActivity

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val medicineName = intent.getStringExtra("medicine_name") ?: "دارو"
        val medicineId = intent.getIntExtra("medicine_id", 0)
        val isReminder = intent.getBooleanExtra("isReminder", false)
        val minutesLeft = intent.getIntExtra("minutes_left", 10)

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        NotificationChannel(
            "medicine_alarm_channel", "آلارم دارو",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
            setBypassDnd(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            manager.createNotificationChannel(this)
        }

        NotificationChannel(
            "medicine_reminder_channel", "یادآور دارو",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
            manager.createNotificationChannel(this)
        }

        if (isReminder) {
            val openPending = PendingIntent.getActivity(
                context, medicineId + 20000,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val text = when (minutesLeft) {
                10 -> "۱۰ دقیقه دیگر وقت مصرف $medicineName است"
                5  -> "⚡ ۵ دقیقه دیگر وقت مصرف $medicineName است"
                1  -> "🔴 ۱ دقیقه دیگر وقت مصرف $medicineName است"
                else -> "$minutesLeft دقیقه دیگر وقت مصرف $medicineName است"
            }

            val notification = NotificationCompat.Builder(context, "medicine_reminder_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("⏰ یادآور دارو")
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(openPending)
                .build()

            manager.notify(medicineId + 5000, notification)
            return
        }

        // آلارم اصلی — Full Screen Intent
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("medicine_name", medicineName)
            putExtra("medicine_id", medicineId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val fullScreenPending = PendingIntent.getActivity(
            context, medicineId,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "medicine_alarm_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("💊 وقت مصرف دارو")
            .setContentText(medicineName)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPending, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        manager.notify(medicineId + 1000, notification)

        // هم نوتیف هم activity مستقیم
        try {
            context.startActivity(alarmIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
