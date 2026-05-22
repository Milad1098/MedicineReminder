package com.example.test

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class AlarmActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

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

        val vibrator =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {

                val vibratorManager =
                    getSystemService(
                        VIBRATOR_MANAGER_SERVICE
                    ) as VibratorManager

                vibratorManager.defaultVibrator

            } else {

                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }

        vibrator.vibrate(
            longArrayOf(
                0,
                1000,
                1000
            ),
            0
        )

        setContent {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(24.dp),

                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "⏰ زمان مصرف دارو",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )

                androidx.compose.foundation.layout.Spacer(
                    modifier = Modifier.padding(12.dp)
                )

                Text(
                    text = medicineName,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.Green
                )

                androidx.compose.foundation.layout.Spacer(
                    modifier = Modifier.padding(24.dp)
                )

                Button(
                    onClick = {

                        mediaPlayer?.stop()

                        vibrator.cancel()

                        finish()
                    }
                ) {

                    Text("مصرف شد")
                }
            }
        }
    }

    override fun onDestroy() {

        mediaPlayer?.release()

        super.onDestroy()
    }
}
