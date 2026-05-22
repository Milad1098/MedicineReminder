package com.example.test.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.test.AlarmActivity

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        val medicine =
            intent.getStringExtra("medicine")
                ?: "دارو"

        val alarmIntent =
            Intent(
                context,
                AlarmActivity::class.java
            ).apply {

                putExtra("medicine", medicine)

                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
            }

        context.startActivity(alarmIntent)
    }
}
