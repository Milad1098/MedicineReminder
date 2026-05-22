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

        val medicineName =
            intent.getStringExtra("medicine")
                ?: "دارو"

        mediaPlayer =
            MediaPlayer.create(this, R.raw.alarm)

        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        vibrator =
            getSystemService(VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            vibrator?.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 1000, 1000),
                    0
                )
            )

        } else {

            @Suppress("DEPRECATION")
            vibrator?.vibrate(
                longArrayOf(0, 1000, 1000),
                0
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {

            setShowWhenLocked(true)
            setTurnScreenOn(true)

        } else {

            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.setBackgroundColor(
            Color.parseColor("#0F172A")
        )
        root.setPadding(50, 50, 50, 50)

        val title = TextView(this)
        title.text = "⏰ زمان مصرف دارو"
        title.textSize = 28f
        title.setTextColor(Color.WHITE)

        val medicine = TextView(this)
        medicine.text = medicineName
        medicine.textSize = 42f
        medicine.setTextColor(
            Color.parseColor("#22C55E")
        )

        val button = Button(this)
        button.text = "متوجه شدم"

        button.setOnClickListener {

            mediaPlayer?.stop()
            mediaPlayer?.release()

            vibrator?.cancel()

            finish()
        }

        val space1 = Space(this)
        space1.layoutParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                40
            )

        val space2 = Space(this)
        space2.layoutParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                60
            )

        root.addView(title)
        root.addView(space1)
        root.addView(medicine)
        root.addView(space2)
        root.addView(button)

        setContentView(root)
    }

    override fun onDestroy() {

        mediaPlayer?.release()
        vibrator?.cancel()

        super.onDestroy()
    }
}
