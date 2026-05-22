package com.example.test

import android.app.NotificationManager
import android.media.MediaPlayer
import android.os.*
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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

        val medicineName =
            intent.getStringExtra("medicine")
                ?: "دارو"

        mediaPlayer =
            MediaPlayer.create(
                this,
                R.raw.alarm
            )

        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        vibrator =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                val manager =
                    getSystemService(
                        VIBRATOR_MANAGER_SERVICE
                    ) as VibratorManager

                manager.defaultVibrator

            } else {

                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }

        vibrator?.vibrate(
            VibrationEffect.createWaveform(
                longArrayOf(
                    0,
                    1000,
                    1000
                ),
                0
            )
        )

        Handler(Looper.getMainLooper()).postDelayed({

            stopAlarm()

            val manager =
                getSystemService(
                    NOTIFICATION_SERVICE
                ) as NotificationManager

            val notification =
                androidx.core.app.NotificationCompat.Builder(
                    this,
                    "medicine_alarm"
                )
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentTitle("دارو فراموش شد")
                    .setContentText("$medicineName مصرف نشد")
                    .build()

            manager.notify(555, notification)

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

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        vibrator?.cancel()

        val manager =
            getSystemService(
                NOTIFICATION_SERVICE
            ) as NotificationManager

        manager.cancel(999)
    }

    override fun onDestroy() {

        stopAlarm()

        super.onDestroy()
    }
}

@Composable
fun AlarmScreen(
    medicineName: String,
    onDismiss: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050816))
    ) {

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "⏰ زمان مصرف دارو",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = medicineName,
                style = MaterialTheme.typography.headlineLarge,
                color = Color(0xFF22C55E)
            )

            Spacer(
                modifier = Modifier.height(40.dp)
            )

            Button(
                onClick = onDismiss
            ) {

                Text("مصرف شد")
            }
        }
    }
}
