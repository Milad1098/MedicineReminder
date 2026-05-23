package com.example.test

import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.MediaPlayer
import android.os.*
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.ui.theme.Vazir

class AlarmActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        val medicineName = intent.getStringExtra("medicine_name") ?: "دارو"

        // ایجاد channel برای نوتیف فراموشی
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            "medicine_alarm",
            "آلارم دارو",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)

        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.alarm)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        vibrator?.vibrate(
            VibrationEffect.createWaveform(longArrayOf(0, 800, 600), 0)
        )

        // بعد از ۶۰ ثانیه نوتیف فراموشی
        Handler(Looper.getMainLooper()).postDelayed({
            stopAlarm()

            val notification = androidx.core.app.NotificationCompat.Builder(this, "medicine_alarm")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("⚠️ دارو فراموش شد")
                .setContentText("$medicineName مصرف نشد")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .build()

            manager.notify(555, notification)
            finish()
        }, 60000)

        setContent {
            AlarmScreen(
                medicineName = medicineName,
                onDismiss = {
                    stopAlarm()
                    finish()
                }
            )
        }
    }

    private fun stopAlarm() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null
        vibrator?.cancel()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(999)
    }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }
}

@Composable
fun AlarmScreen(medicineName: String, onDismiss: () -> Unit) {

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A1A),
                        Color(0xFF0D1B2A),
                        Color(0xFF0A1628)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {

            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(120.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF22C55E).copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "💊", fontSize = 56.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "وقت مصرف دارو",
                fontFamily = Vazir,
                fontSize = 18.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )

            Text(
                text = medicineName,
                fontFamily = Vazir,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF22C55E)
                )
            ) {
                Text(
                    text = "✓ مصرف شد",
                    fontFamily = Vazir,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
