package com.example.test.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.Vazir

@Composable
fun EmptyState() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f)),

            contentAlignment = Alignment.Center
        ) {

            Icon(
                Icons.Default.Medication,
                contentDescription = null,
                tint = Color(0xFF22C55E),
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "هنوز دارویی ثبت نشده",
            color = Color.White,
            fontFamily = Vazir,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "برای شروع روی دکمه + بزن",
            color = Color(0xFF94A3B8),
            fontFamily = Vazir,
            fontSize = 15.sp
        )
    }
}
