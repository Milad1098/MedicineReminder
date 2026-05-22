package com.example.test

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

class AlarmActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val medicine =
            intent.getStringExtra("medicine")
                ?: "دارو"

        mediaPlayer =
            MediaPlayer.create(
                this,
                R.raw.alarm
            )

        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        val vibrator =
            getSystemService(VIBRATOR_MANAGER_SERVICE)
                    as VibratorManager

        vibrator.defaultVibrator.vibrate(
            android.os.VibrationEffect.createOneShot(
                1000,
                255
            )
        )

        setContent {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "زمان مصرف دارو",
                        color = Color.White,
                        fontSize = 32.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = medicine,
                        color = Color(0xFF22C55E),
                        fontSize = 42.sp
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Button(
                        onClick = {

                            mediaPlayer?.stop()
                            finish()
                        }
                    ) {

                        Text("متوقف کردن")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        mediaPlayer?.release()
    }
}
