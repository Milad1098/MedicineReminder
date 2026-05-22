```kotlin
package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.test.ui.screens.HomeScreen
import com.example.test.utils.NotificationHelper

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        NotificationHelper
            .createNotificationChannel(this)

        setContent {

            CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Rtl
            ) {

                HomeScreen()
            }
        }
    }
}
```
