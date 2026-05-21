package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Rtl
            ) {
                MedicineReminderApp()
            }
        }
    }
}

val Vazir = FontFamily(
    Font(R.font.vazirmatn_regular)
)

@Composable
fun MedicineReminderApp() {

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A),
            Color(0xFF111827),
            Color(0xFF1E293B)
        )
    )

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(20.dp)
        ) {

            Column {

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "یادآور دارو",
                    fontFamily = Vazir,
                    color = Color.White,
                    fontSize = 32.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "مصرف داروهایت را فراموش نکن",
                    fontFamily = Vazir,
                    color = Color(0xFFCBD5E1),
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                MedicineCard(
                    name = "قرص فشار خون",
                    time = "08:00 صبح"
                )

                Spacer(modifier = Modifier.height(20.dp))

                MedicineCard(
                    name = "ویتامین D",
                    time = "09:30 شب"
                )
            }

            FloatingActionButton(
                onClick = { },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                containerColor = Color(0xFF22C55E)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun MedicineCard(
    name: String,
    time: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(28.dp)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B)
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        Color(0xFF22C55E),
                        RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    Icons.Default.Medication,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {

                Text(
                    text = name,
                    color = Color.White,
                    fontFamily = Vazir,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = time,
                    color = Color(0xFF94A3B8),
                    fontFamily = Vazir,
                    fontSize = 15.sp
                )
            }
        }
    }
}
