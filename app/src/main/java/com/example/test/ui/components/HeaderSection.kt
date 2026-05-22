package com.example.test.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
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
fun HeaderSection() {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column {

            Text(
                text = "یادآور دارو",
                color = Color.White,
                fontFamily = Vazir,
                fontSize = 34.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "مدیریت هوشمند داروها",
                color = Color(0xFFCBD5E1),
                fontFamily = Vazir
            )
        }

        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.08f)),

            contentAlignment = Alignment.Center
        ) {

            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = Color(0xFF22C55E)
            )
        }
    }
}
