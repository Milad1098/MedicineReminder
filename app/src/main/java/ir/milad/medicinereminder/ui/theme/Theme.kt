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
)

private val Light = AppColors(
    background = Color(0xFFF2F2F7), surface = Color.White, text = Color.Black,
    textSecondary = Color(0xFF8E8E93), separator = Color(0xFFE5E5EA), accent = Color(0xFF10B981),
    warning = Color(0xFFF59E0B), danger = Color(0xFFEF4444), fill = Color(0xFFE9E9EE),
)
private val Dark = AppColors(
    background = Color.Black, surface = Color(0xFF1C1C1E), text = Color.White,
    textSecondary = Color(0xFF8E8E93), separator = Color(0xFF38383A), accent = Color(0xFF34D399),
    warning = Color(0xFFFBBF24), danger = Color(0xFFF87171), fill = Color(0xFF2C2C2E),
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
