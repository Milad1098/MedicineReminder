package ir.milad.medicinereminder.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import ir.milad.medicinereminder.R

/** Tokens: docs/DESIGN.md */
@Immutable
data class AppColors(
    val background: Color, val surface: Color, val text: Color, val textSecondary: Color,
    val separator: Color, val accent: Color, val warning: Color, val danger: Color, val fill: Color,
    /** Liquid Glass: translucent card fill, light-catching top edge, faint bottom edge, bar tint */
    val glass: Color, val glassEdgeTop: Color, val glassEdgeBottom: Color, val barTint: Color,
    /** soft color blobs behind everything, so the glass has something to refract */
    val mesh: List<Color>,
    val dark: Boolean,
)

private val Light = AppColors(
    background = Color(0xFFEEF2F7), surface = Color.White, text = Color(0xFF0B1220),
    textSecondary = Color(0xFF6B7280), separator = Color(0x1F0B1220), accent = Color(0xFF0FA968),
    warning = Color(0xFFE08A00), danger = Color(0xFFE5484D), fill = Color(0x140B1220),
    glass = Color.White.copy(alpha = 0.62f), glassEdgeTop = Color.White.copy(alpha = 0.95f),
    glassEdgeBottom = Color.White.copy(alpha = 0.25f), barTint = Color.White.copy(alpha = 0.55f),
    mesh = listOf(Color(0xFF6EE7B7), Color(0xFF7DD3FC), Color(0xFFC4B5FD), Color(0xFFFDBA74)),
    dark = false,
)
private val Dark = AppColors(
    background = Color(0xFF05080D), surface = Color(0xFF151A22), text = Color(0xFFF5F7FA),
    textSecondary = Color(0xFF9AA3AF), separator = Color(0x1FFFFFFF), accent = Color(0xFF34D399),
    warning = Color(0xFFFBBF24), danger = Color(0xFFFF6B6B), fill = Color(0x1AFFFFFF),
    glass = Color.White.copy(alpha = 0.07f), glassEdgeTop = Color.White.copy(alpha = 0.28f),
    glassEdgeBottom = Color.White.copy(alpha = 0.04f), barTint = Color(0xFF10151C).copy(alpha = 0.55f),
    mesh = listOf(Color(0xFF065F46), Color(0xFF1E3A8A), Color(0xFF4C1D95), Color(0xFF0E7490)),
    dark = true,
)

val LocalColors = staticCompositionLocalOf { Light }
val colors: AppColors @Composable get() = LocalColors.current

val Vazir = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
)

private val base = TextStyle(fontFamily = Vazir)
private val typography = Typography().run {
    copy(
        displaySmall = displaySmall.merge(base), headlineLarge = headlineLarge.merge(base),
        headlineMedium = headlineMedium.merge(base), headlineSmall = headlineSmall.merge(base),
        titleLarge = titleLarge.merge(base), titleMedium = titleMedium.merge(base),
        titleSmall = titleSmall.merge(base), bodyLarge = bodyLarge.merge(base),
        bodyMedium = bodyMedium.merge(base), bodySmall = bodySmall.merge(base),
        labelLarge = labelLarge.merge(base), labelMedium = labelMedium.merge(base),
        labelSmall = labelSmall.merge(base),
    )
}

val LargeTitle = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.Bold, fontSize = 32.sp)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val c = if (dark) Dark else Light
    val scheme = (if (dark) darkColorScheme() else lightColorScheme()).copy(
        primary = c.accent, onPrimary = Color.White, background = c.background, surface = c.surface,
        surfaceContainerHigh = c.surface, onSurface = c.text, onBackground = c.text,
        onSurfaceVariant = c.textSecondary, outline = c.separator, error = c.danger,
    )
    CompositionLocalProvider(LocalColors provides c, LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(colorScheme = scheme, typography = typography, content = content)
    }
}
