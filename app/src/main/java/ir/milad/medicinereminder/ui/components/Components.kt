package ir.milad.medicinereminder.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.milad.medicinereminder.data.Form
import ir.milad.medicinereminder.ui.theme.LargeTitle
import ir.milad.medicinereminder.ui.theme.colors

val medicineColors = listOf(
    0xFFFFFFFF, 0xFFFDE68A, 0xFFFDBA74, 0xFFF87171, 0xFFF9A8D4, 0xFFC4B5FD, 0xFF93C5FD, 0xFF86EFAC,
).map { it.toInt() }

/** Medicine identified by shape + color (docs/DESIGN.md #4). */
@Composable
fun PillIcon(form: Form, color: Int, size: Dp = 44.dp) {
    val c = Color(color)
    val outline = if (c.luminance() > 0.85f) Color(0xFFD1D1D6) else c.copy(alpha = 0f)
    Box(
        Modifier.size(size).clip(CircleShape).background(c.copy(alpha = 0.18f).compositeOver(colors.fill)),
        contentAlignment = Alignment.Center,
    ) {
        when (form) {
            Form.PILL -> Canvas(Modifier.size(size * 0.55f)) {
                drawCircle(c)
                drawCircle(outline, style = Stroke(1.dp.toPx()))
                drawLine(Color.Black.copy(alpha = 0.15f), Offset(this.size.width * 0.2f, center.y),
                    Offset(this.size.width * 0.8f, center.y), 1.5.dp.toPx(), StrokeCap.Round)
            }
            Form.CAPSULE -> Canvas(Modifier.size(size * 0.62f)) {
                rotate(-45f) {
                    val w = this.size.width * 0.42f
                    val left = center.x - w / 2
                    val r = CornerRadius(w / 2)
                    clipRect(bottom = center.y) {
                        drawRoundRect(c, Offset(left, 0f), Size(w, this@Canvas.size.height), r)
                    }
                    clipRect(top = center.y) {
                        drawRoundRect(Color.White, Offset(left, 0f), Size(w, this@Canvas.size.height), r)
                    }
                    drawRoundRect(Color(0xFFD1D1D6), Offset(left, 0f), Size(w, this.size.height), r,
                        style = Stroke(1.dp.toPx()))
                }
            }
            else -> Icon(
                when (form) {
                    Form.LIQUID -> Icons.Rounded.LocalDrink; Form.DROPS -> Icons.Filled.WaterDrop
                    Form.INHALER -> Icons.Filled.Air; else -> Icons.Filled.Vaccines
                },
                null, Modifier.size(size * 0.55f),
                tint = if (c.luminance() > 0.85f) colors.textSecondary else c,
            )
        }
    }
}

@Composable
fun ScreenTitle(title: String, subtitle: String? = null, trailing: @Composable RowScope.() -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp), verticalAlignment = Alignment.Bottom) {
        Column(Modifier.weight(1f)) {
            subtitle?.let { Text(it, color = colors.textSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            Text(title, style = LargeTitle, color = colors.text)
        }
        trailing()
    }
}

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) = Text(
    text, modifier.padding(start = 16.dp, top = 20.dp, bottom = 6.dp),
    color = colors.textSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold,
)

/** iOS inset grouped list; separate rows with [RowDivider]. */
@Composable
fun Group(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) = Column(
    modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(colors.surface), content = content,
)

@Composable
fun RowDivider(inset: Dp = 16.dp) =
    HorizontalDivider(Modifier.padding(start = inset), thickness = 0.5.dp, color = colors.separator)

@Composable
fun GroupRow(onClick: (() -> Unit)? = null, content: @Composable RowScope.() -> Unit) = Row(
    Modifier.fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    content = content,
)

/** Round check that pops with a spring when checked. */
@Composable
fun CheckCircle(checked: Boolean, color: Color, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        if (checked) 1f else 0.85f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium), label = "check",
    )
    Box(
        Modifier.size(36.dp).scale(scale).clip(CircleShape)
            .background(if (checked) color else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) Icon(Icons.Rounded.Check, "خوردم", tint = Color.White, modifier = Modifier.size(22.dp))
        else Canvas(Modifier.size(30.dp)) { drawCircle(color.copy(alpha = 0.5f), style = Stroke(2.dp.toPx())) }
    }
}

@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) = Box(
    modifier.clip(RoundedCornerShape(12.dp))
        .background(if (selected) colors.accent else colors.fill)
        .clickable(onClick = onClick)
        .padding(horizontal = 14.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center,
) {
    Text(text, color = if (selected) Color.White else colors.text, fontSize = 14.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
}
