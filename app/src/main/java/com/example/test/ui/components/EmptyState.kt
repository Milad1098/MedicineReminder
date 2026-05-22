package com.example.test.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmptyState(
    fontFamily: FontFamily
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(90.dp))

        Icon(
            Icons.Default.Medication,
            contentDescription = null,
            tint = Color(0xFF22C55E),
            modifier = Modifier.size(82.dp)
        )

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "هنوز دارویی ثبت نشده",
            color = Color.White,
            fontFamily = fontFamily,
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "برای شروع روی دکمه + بزن",
            color = Color(0xFF94A3B8),
            fontFamily = fontFamily
        )
    }
}
