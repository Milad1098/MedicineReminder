package com.example.test

import android.app.Activity
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView

class AlarmActivity : Activity() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val medicineName = intent.getStringExtra("medicine") ?: "دارو"

        keepScreenOn()

        mediaPlayer = MediaPlayer.create(this, R.raw.alarm).apply {
            isLooping = true
            start()
        }

        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        startVibration()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0F172A"))
            setPadding(48, 48, 48, 48)
        }

        val title = TextView(this).apply {
            text = "زمان مصرف دارو"
            setTextColor(Color.WHITE)
            textSize = 30f
            gravity = Gravity.CENTER
        }

        val medicineText = TextView(this).apply {
            text = medicineName
            setTextColor(Color.parseColor("#22C55E"))
            textSize = 40f
            gravity = Gravity.CENTER
        }

        val button = Button(this).apply {
            text = "متوجه شدم"
            setOnClickListener {
                stopAlarm()
                finish()
            }
        }

        val space1 = Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                40
            )
        }

        val space2 = Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                48
            )
        }

        root.addView(title)
        root.addView(space1)
        root.addView(medicineText)
        root.addView(space2)
        root.addView(button)

        setContentView(root)
    }

    private fun keepScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
    }

    private fun startVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 1000, 1000),
                        0
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 1000, 1000), 0)
            }
        } catch (_: Exception) {
        }
    }

    private fun stopAlarm() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
        } catch (_: Exception) {
        }
        mediaPlayer = null

        try {
            vibrator?.cancel()
        } catch (_: Exception) {
        }
    }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }
}
