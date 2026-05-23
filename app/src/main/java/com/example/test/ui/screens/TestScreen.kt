package com.example.test.ui.screens

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.test.receiver.AlarmReceiver

@Composable
fun TestScreen() {
    val context = LocalContext.current
    var status by remember { mutableStateOf("منتظر...") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("تست آلارم", style = MaterialTheme.typography.headlineMedium)

        Text(status)

        // تست ۱: نوتیف مستقیم
        Button(onClick = {
            try {
                val manager = context.getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    manager.createNotificationChannel(
                        NotificationChannel(
                            "test_channel",
                            "Test",
                            NotificationManager.IMPORTANCE_HIGH
                        )
                    )
                }

                val notification = androidx.core.app.NotificationCompat
                    .Builder(context, "test_channel")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("تست نوتیف")
                    .setContentText("نوتیف مستقیم کار میکنه!")
                    .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                    .build()

                manager.notify(1, notification)
                status = "نوتیف مستقیم ارسال شد - چیزی دیدی؟"
            } catch (e: Exception) {
                status = "خطا: ${e.message}"
            }
        }) {
            Text("تست ۱: نوتیف مستقیم (الان)")
        }

        // تست ۲: آلارم ۱۵ ثانیه دیگه
        Button(onClick = {
            try {
                val alarmManager = context.getSystemService(
                    Context.ALARM_SERVICE
                ) as AlarmManager

                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("medicine_name", "تست دارو")
                    putExtra("medicine_id", 9999)
                    putExtra("isReminder", false)
                }

                val pending = PendingIntent.getBroadcast(
                    context,
                    9999,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val triggerTime = System.currentTimeMillis() + 15_000

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pending
                )

                status = "آلارم برای ۱۵ ثانیه دیگه set شد - صبر کن..."
            } catch (e: SecurityException) {
                status = "SecurityException - پرمیشن Exact Alarm نداری!\nبرو Settings > Apps > یادآور دارو > Alarms & Reminders"
            } catch (e: Exception) {
                status = "خطا: ${e.message}"
            }
        }) {
            Text("تست ۲: آلارم ۱۵ ثانیه دیگه")
        }

        // تست ۳: receiver مستقیم
        Button(onClick = {
            try {
                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("medicine_name", "تست دارو")
                    putExtra("medicine_id", 9998)
                    putExtra("isReminder", false)
                }
                context.sendBroadcast(intent)
                status = "Broadcast ارسال شد - باید آلارم بیاد"
            } catch (e: Exception) {
                status = "خطا: ${e.message}"
            }
        }) {
            Text("تست ۳: receiver مستقیم (الان)")
        }
    }
}
