package com.example.test

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.room.Room
import com.example.test.data.local.AppDatabase
import com.example.test.ui.screens.HomeScreen
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val powerManager =
                getSystemService(POWER_SERVICE) as PowerManager

            if (
                !powerManager.isIgnoringBatteryOptimizations(
                    packageName
                )
            ) {

                val intent = Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:$packageName")
                )

                startActivity(intent)
            }
        }

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "medicine_db"
        )
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()

        setContent {

            CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Rtl
            ) {
        
                MaterialTheme {
        
                    Surface {
        
                        HomeScreen(db)
                    }
                }
            }
        }
    }
}
