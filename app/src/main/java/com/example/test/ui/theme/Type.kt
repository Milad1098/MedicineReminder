package com.example.test.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.test.R

val Vazir = FontFamily(
    Font(
        resId = R.font.vazirmatn_regular,
        weight = FontWeight.Normal
    ),
    Font(
        resId = R.font.vazirmatn_bold,
        weight = FontWeight.Bold
    )
)

val Typography = Typography(

    bodyLarge = TextStyle(
        fontFamily = Vazir,
        fontSize = 16.sp
    ),

    titleLarge = TextStyle(
        fontFamily = Vazir,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
    )
)
