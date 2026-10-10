package ir.milad.medicinereminder.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.Vaccines
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.milad.medicinereminder.data.Form
import ir.milad.medicinereminder.ui.amountNumber
import ir.milad.medicinereminder.ui.fa
import ir.milad.medicinereminder.ui.theme.LargeTitle
import ir.milad.medicinereminder.ui.theme.colors
import kotlinx.coroutines.flow.distinctUntilChanged

val medicineColors = listOf(
    0xFFFFFFFF, 0xFFFACC15, 0xFFFB923C, 0xFFF87171, 0xFFF472B6, 0xFFA78BFA, 0xFF60A5FA, 0xFF34D399,
).map { it.toInt() }

private fun isWhite(c: Color) = c.luminance() > 0.9f

/** Medicine identified by shape + color, on a squircle tinted with that color (docs/DESIGN.md #4). */
@Composable
fun MedIcon(form: Form, color: Int, size: Dp = 48.dp) {
    val c = Color(color)
    val white = isWhite(c)
    val bg = if (white) colors.fill else c.copy(alpha = 0.22f).compositeOver(colors.surface)
    val edge = Color(0xFFB8B8C0)
    Box(
        Modifier.size(size).clip(RoundedCornerShape(size * 0.32f)).background(bg),
        contentAlignment = Alignment.Center,
    ) {
        when (form) {
            Form.PILL -> Canvas(Modifier.size(size * 0.5f)) {
                drawCircle(c)
                if (white) drawCircle(edge, style = Stroke(1.2.dp.toPx()))
                drawLine(Color.Black.copy(alpha = 0.18f), Offset(this.size.width * 0.22f, center.y),
                    Offset(this.size.width * 0.78f, center.y), 1.6.dp.toPx(), StrokeCap.Round)
            }
            Form.CAPSULE -> Canvas(Modifier.size(size * 0.62f)) {
                rotate(-45f) {
                    val w = this.size.width * 0.4f
                    val left = center.x - w / 2
                    val h = this.size.height
                    val r = CornerRadius(w / 2)
                    clipRect(bottom = center.y) { drawRoundRect(c, Offset(left, 0f), Size(w, h), r) }
                    clipRect(top = center.y) { drawRoundRect(Color.White, Offset(left, 0f), Size(w, h), r) }
                    drawRoundRect(if (white) edge else c, Offset(left, 0f), Size(w, h), r, style = Stroke(1.2.dp.toPx()))
                }
            }
            else -> Icon(
                when (form) {
                    Form.LIQUID -> Icons.Rounded.LocalDrink; Form.DROPS -> Icons.Rounded.WaterDrop
                    Form.INHALER -> Icons.Rounded.Air; else -> Icons.Rounded.Vaccines
                },
                null, Modifier.size(size * 0.52f), tint = if (white) colors.textSecondary else c,
            )
        }
    }
}

@Composable
fun ScreenTitle(title: String, subtitle: String? = null, trailing: @Composable RowScope.() -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            subtitle?.let { Text(it, color = colors.textSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            Text(title, style = LargeTitle, color = colors.text)
        }
        trailing()
    }
}

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier, trailing: String? = null) = Row(
    modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 14.dp, bottom = 2.dp),
    verticalAlignment = Alignment.CenterVertically,
) {
    Text(text, Modifier.weight(1f), color = colors.text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    trailing?.let { Text(it, color = colors.textSecondary, fontSize = 13.sp) }
}

/** iOS inset grouped list; separate rows with [RowDivider]. */
@Composable
fun Group(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) = Column(
    modifier.fillMaxWidth().glass(RoundedCornerShape(24.dp)), content = content,
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

/** Round check that pops with a spring when checked. [label] is read by TalkBack, e.g. "آموکسی‌سیلین، ساعت ۰۸:۰۰". */
@Composable
fun CheckCircle(checked: Boolean, color: Color, enabled: Boolean = true, label: String = "", onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        if (checked) 1f else 0.88f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium), label = "check",
    )
    Box(
        Modifier.size(44.dp).scale(scale).clip(CircleShape)
            .background(if (checked) color else color.copy(alpha = 0.1f))
            .then(if (checked) Modifier else Modifier.border(2.dp, color.copy(alpha = if (enabled) 0.6f else 0.2f), CircleShape))
            .clickable(enabled, onClickLabel = if (checked) "برگرداندن" else "ثبت مصرف") {
                if (!checked) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .semantics { contentDescription = label + if (checked) "، خورده شد" else "، هنوز نخورده"; role = Role.Checkbox },
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.Check, null,
            tint = if (checked) Color.White else color.copy(alpha = if (enabled) 0.6f else 0.2f),
            modifier = Modifier.size(24.dp))
    }
}

@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) = Box(
    modifier.clip(RoundedCornerShape(14.dp))
        .background(if (selected) colors.accent else colors.fill)
        .clickable(onClick = onClick)
        .padding(horizontal = 16.dp, vertical = 10.dp),
    contentAlignment = Alignment.Center,
) {
    Text(text, color = if (selected) Color.White else colors.text, fontSize = 14.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
}

/** iOS segmented control. */
@Composable
fun <T> Segmented(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit, modifier: Modifier = Modifier) = Row(
    modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.fill).padding(3.dp),
) {
    options.forEach { (v, label) ->
        val on = v == selected
        Box(
            Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                .background(if (on) colors.surface else Color.Transparent)
                .clickable { onSelect(v) }.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) { Text(label, color = colors.text, fontSize = 13.sp, fontWeight = if (on) FontWeight.Bold else FontWeight.Normal) }
    }
}

/** Amount stepper: whole steps, with ½ below 1 (½ → 1 → 2 …). */
fun nextAmount(v: Float, up: Boolean, min: Float) = when {
    up -> if (v < 1f) 1f else v + 1f
    v <= 1f -> maxOf(min, 0.5f)
    else -> v - 1f
}

@Composable
fun Stepper(value: Float, onChange: (Float) -> Unit, min: Float = 0.5f, integer: Boolean = false) = Row(
    Modifier.clip(RoundedCornerShape(12.dp)).background(colors.fill),
    verticalAlignment = Alignment.CenterVertically,
) {
    fun step(up: Boolean) = if (integer) (if (up) value + 1 else maxOf(min, value - 1)) else nextAmount(value, up, min)
    // RTL: first child sits on the right → "+" on the right like Persian iOS
    StepButton("+", "بیشتر") { onChange(step(true)) }
    Text(if (integer) value.toInt().fa() else amountNumber(value),
        Modifier.width(44.dp), color = colors.text, fontWeight = FontWeight.Bold, fontSize = 17.sp,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    StepButton("−", "کمتر") { onChange(step(false)) }
}

@Composable
private fun StepButton(label: String, spoken: String, onClick: () -> Unit) = Box(
    Modifier.size(48.dp).clickable(onClick = onClick).semantics { contentDescription = spoken },
    contentAlignment = Alignment.Center,
) { Text(label, color = colors.accent, fontSize = 22.sp, fontWeight = FontWeight.Bold) }

/** iOS-style wheel. [values] wrap around infinitely. */
@Composable
fun Wheel(values: List<String>, selected: Int, onSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    val itemH = 44.dp
    val loops = 400
    val start = loops / 2 * values.size + selected - 2
    val state = rememberLazyListState(start)
    val half = with(LocalDensity.current) { itemH.toPx() / 2 }
    // index of the row sitting in the middle slot (first visible + 2, rounded by scroll offset)
    fun centered() = state.firstVisibleItemIndex + (if (state.firstVisibleItemScrollOffset > half) 1 else 0) + 2
    LaunchedEffect(state) {
        snapshotFlow { centered() - 2 }
            .distinctUntilChanged()
            .collect { onSelected((it + 2) % values.size) }
    }
    Box(modifier.width(88.dp).height(itemH * 5), contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxWidth().height(itemH).clip(RoundedCornerShape(10.dp)).background(colors.fill))
        LazyColumn(state = state, flingBehavior = rememberSnapFlingBehavior(state), contentPadding = PaddingValues(0.dp)) {
            items(loops * values.size) { i ->
                val center = centered()
                val d = kotlin.math.abs(i - center)
                Box(Modifier.height(itemH).width(88.dp), contentAlignment = Alignment.Center) {
                    Text(values[i % values.size], color = colors.text.copy(alpha = when (d) { 0 -> 1f; 1 -> 0.45f; else -> 0.2f }),
                        fontSize = if (d == 0) 24.sp else 20.sp, fontWeight = if (d == 0) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}
