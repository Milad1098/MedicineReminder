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

        val channelId = "medicine_channel"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "یادآور دارو",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
            enableLights(true)
        }
        manager.createNotificationChannel(channel)

        if (isReminder) {

            val openIntent = Intent(context, MainActivity::class.java)
            val openPending = PendingIntent.getActivity(
                context,
                medicineId + 20000,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("⏰ یادآور دارو")
                .setContentText("۱۰ دقیقه دیگر وقت مصرف $medicineName است")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(openPending)
                .build()

            manager.notify(medicineId + 5000, notification)
            return
        }

        // آلارم تمام صفحه
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            putExtra("medicine_name", medicineName)
            putExtra("medicine_id", medicineId)
        }

        context.startActivity(alarmIntent)
    }
}
