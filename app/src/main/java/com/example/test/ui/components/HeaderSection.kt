package com.example.test.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.ui.theme.Vazir

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
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "مدیریت هوشمند داروها",
                color = Color(0xFF94A3B8),
                fontFamily = Vazir
            )
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0x22FFFFFF)
        ) {

            Box(
                modifier = Modifier.padding(18.dp),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "💊",
                    fontSize = 26.sp
                )
            }
        }
    }
}
