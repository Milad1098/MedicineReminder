package ir.milad.medicinereminder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import ir.milad.medicinereminder.ui.theme.colors

/**
 * Liquid Glass (docs/DESIGN.md):
 * - content layer = frosted translucent cards ([glass]) floating over [MeshBackground]
 * - chrome layer (bottom bar, headers) = real backdrop blur ([liquidBar]) over the scrolling content
 */

/** The HazeState whose source is the scrolling content of the current screen. */
val LocalHaze = compositionLocalOf<HazeState?> { null }

/** Soft color blobs; without something colorful behind it, glass just looks grey. */
@Composable
fun MeshBackground(modifier: Modifier = Modifier) {
    val c = colors
    Box(modifier.fillMaxSize().background(c.background)) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            listOf(
                Offset(w * 0.95f, h * 0.05f) to w * 0.85f,
                Offset(w * 0.0f, h * 0.35f) to w * 0.8f,
                Offset(w * 1.0f, h * 0.7f) to w * 0.9f,
                Offset(w * 0.15f, h * 1.0f) to w * 0.8f,
            ).forEachIndexed { i, (center, r) ->
                val col = c.mesh[i % c.mesh.size]
                drawCircle(
                    Brush.radialGradient(listOf(col.copy(alpha = if (c.dark) 0.6f else 0.75f), col.copy(alpha = 0f)), center, r),
                    r, center,
                )
            }
        }
    }
}

/** Frosted content card: translucent fill + bright top edge fading out + soft drop shadow. */
fun Modifier.glass(shape: Shape, elevation: Dp = 10.dp, fill: Color? = null): Modifier = composed {
    val c = colors
    this
        .shadow(elevation, shape, ambientColor = Color.Black.copy(alpha = 0.08f), spotColor = Color.Black.copy(alpha = if (c.dark) 0.5f else 0.12f))
        .clip(shape)
        .background(fill ?: c.glass)
        .border(1.dp, Brush.verticalGradient(listOf(c.glassEdgeTop, c.glassEdgeBottom)), shape)
}

/** Chrome glass: real blur of whatever scrolls underneath (Android 12+, tinted fallback below). */
fun Modifier.liquidBar(shape: Shape, state: HazeState?): Modifier = composed {
    val c = colors
    val base = this
        .shadow(24.dp, shape, ambientColor = Color.Black.copy(alpha = 0.1f), spotColor = Color.Black.copy(alpha = if (c.dark) 0.6f else 0.18f))
        .clip(shape)
    (if (state != null) base.hazeEffect(state) {
        blurRadius = 28.dp
        backgroundColor = c.background
        tints = listOf(HazeTint(c.barTint))
        noiseFactor = 0.04f
    } else base.background(c.surface))
        .border(1.dp, Brush.verticalGradient(listOf(c.glassEdgeTop, c.glassEdgeBottom)), shape)
}
