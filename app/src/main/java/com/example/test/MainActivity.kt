package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.room.Room
import com.example.test.data.local.AppDatabase
import com.example.test.ui.screens.HomeScreen

class MainActivity : ComponentActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "medicine_db"
        )
            .fallbackToDestructiveMigration()
            .build()

        setContent {

            CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Rtl
            ) {

                HomeScreen(db = db)
            }
        }
    }
}
