package com.example.test.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Green400 = Color(0xFF4ADE80)
val Green500 = Color(0xFF22C55E)
val Green600 = Color(0xFF16A34A)
val Blue400  = Color(0xFF38BDF8)
val Red400   = Color(0xFFEF4444)
val Slate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)
val Slate700 = Color(0xFF334155)
val Slate500 = Color(0xFF64748B)
val Slate400 = Color(0xFF94A3B8)
val Slate200 = Color(0xFFCBD5E1)

private val DarkColors = darkColorScheme(
    primary          = Green500,
    onPrimary        = Color.White,
    secondary        = Blue400,
    onSecondary      = Color.White,
    background       = Slate900,
    onBackground     = Color.White,
    surface          = Slate800,
    onSurface        = Color.White,
    surfaceVariant   = Slate700,
    onSurfaceVariant = Slate200,
    outline          = Slate700,
    error            = Red400,
    onError          = Color.White,
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography  = Typography,
        content     = content
    )
}
