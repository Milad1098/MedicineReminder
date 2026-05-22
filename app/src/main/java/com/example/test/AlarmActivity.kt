package com.example.test

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.ui.theme.TestTheme
import com.example.test.R

class AlarmActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val medicineName =
            intent.getStringExtra("medicine")
                ?: "دارو"

        mediaPlayer = MediaPlayer.create(
            this,
            R.raw.alarm
        )

        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        setContent {

            TestTheme {

                AlarmScreen(
                    medicineName = medicineName,
                    onDismiss = {

                        mediaPlayer?.stop()
                        mediaPlayer?.release()
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        mediaPlayer?.release()
    }
}

@Composable
fun AlarmScreen(
    medicineName: String,
    onDismiss: () -> Unit
) {

    DisposableEffect(Unit) {
        onDispose { }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp),

        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "⏰ زمان مصرف دارو",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = medicineName,
            color = Color(0xFF22C55E),
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(40.dp)
        )

        Button(
            onClick = onDismiss
        ) {

            Text(
                text = "متوجه شدم"
            )
        }
    }
}
