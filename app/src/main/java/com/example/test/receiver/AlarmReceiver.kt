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

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // ساخت channel آلارم
        val alarmChannel = NotificationChannel(
            "medicine_alarm_channel",
            "آلارم دارو",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
            enableLights(true)
            setBypassDnd(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(alarmChannel)

        // ساخت channel reminder
        val reminderChannel = NotificationChannel(
            "medicine_reminder_channel",
            "یادآور دارو",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
        }
        manager.createNotificationChannel(reminderChannel)

        if (isReminder) {
            // نوتیف ۱۰ دقیقه قبل
            val openIntent = PendingIntent.getActivity(
                context,
                medicineId + 20000,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, "medicine_reminder_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("⏰ یادآور دارو")
                .setContentText("۱۰ دقیقه دیگر وقت مصرف $medicineName است")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(openIntent)
                .build()

            manager.notify(medicineId + 5000, notification)
            return
        }

        // --- آلارم اصلی با Full Screen Intent ---
        val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("medicine_name", medicineName)
            putExtra("medicine_id", medicineId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            medicineId,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "medicine_alarm_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("💊 وقت مصرف دارو")
            .setContentText(medicineName)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()

        manager.notify(medicineId + 1000, notification)

        // هم نوتیف میده هم activity رو باز میکنه
        try {
            context.startActivity(fullScreenIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
