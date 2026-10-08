package ir.milad.medicinereminder.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.milad.medicinereminder.data.DoseLog
import ir.milad.medicinereminder.data.MedicineWithTimes
import ir.milad.medicinereminder.domain.DoseState
import ir.milad.medicinereminder.domain.dosesOn
import ir.milad.medicinereminder.domain.toLocalDateTime
import ir.milad.medicinereminder.ui.components.Group
import ir.milad.medicinereminder.ui.components.GroupRow
import ir.milad.medicinereminder.ui.components.MedIcon
import ir.milad.medicinereminder.ui.components.RowDivider
import ir.milad.medicinereminder.ui.components.ScreenTitle
import ir.milad.medicinereminder.ui.components.SectionHeader
import ir.milad.medicinereminder.ui.dayShort
import ir.milad.medicinereminder.ui.fa
import ir.milad.medicinereminder.ui.persianDate
import ir.milad.medicinereminder.ui.timeText
import ir.milad.medicinereminder.ui.theme.colors

const val HISTORY_DAYS = 14L

@Composable
fun HistoryScreen(meds: List<MedicineWithTimes>, logs: List<DoseLog>, padding: PaddingValues) {
    val now = rememberNow()
    val today = now.toLocalDateTime().toLocalDate()
    val days = (0 until HISTORY_DAYS).map { today.minusDays(it) }
        .map { it to dosesOn(it, meds, logs, now).filter { d -> d.state != DoseState.UPCOMING } }
    val week = days.take(7).reversed()
    val weekDue = week.sumOf { it.second.size }
    val weekTaken = week.sumOf { (_, d) -> d.count { it.state == DoseState.TAKEN } }

    LazyColumn(
        Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp,
            bottom = padding.calculateBottomPadding() + 24.dp),
    ) {
        item { ScreenTitle("تاریخچه") }
        item {
            Group(Modifier.padding(top = 8.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text("پایبندی ۷ روز اخیر", color = colors.textSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (weekDue == 0) "—" else "${(weekTaken * 100 / weekDue).fa()}٪",
                        color = colors.text, fontSize = 34.sp, fontWeight = FontWeight.Bold,
                    )
                    Row(Modifier.fillMaxWidth().height(96.dp).padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        week.forEach { (date, doses) ->
                            val ratio = if (doses.isEmpty()) 0f else doses.count { it.state == DoseState.TAKEN } / doses.size.toFloat()
                            val h by animateFloatAsState(ratio, label = "bar")
                            Column(Modifier.fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(Modifier.weight(1f).width(22.dp).clip(RoundedCornerShape(8.dp)).background(colors.fill),
                                    contentAlignment = Alignment.BottomCenter) {
                                    Box(Modifier.fillMaxWidth().fillMaxHeight(h).clip(RoundedCornerShape(8.dp))
                                        .background(if (ratio == 1f) colors.accent else colors.accent.copy(alpha = 0.6f)))
                                }
                                Text(dayShort(date.dayOfWeek), color = if (date == today) colors.accent else colors.textSecondary,
                                    fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        }
        days.filter { it.second.isNotEmpty() }.forEach { (date, doses) ->
            item { SectionHeader(if (date == today) "امروز" else persianDate(date)) }
            item {
                Group {
                    doses.forEachIndexed { i, d ->
                        if (i > 0) RowDivider(72.dp)
                        GroupRow {
                            MedIcon(d.medicine.form, d.medicine.color, 40.dp)
                            Column(Modifier.weight(1f)) {
                                Text(d.medicine.name, color = colors.text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("نوبت ${timeText(d.time.minuteOfDay)}", color = colors.textSecondary, fontSize = 13.sp)
                            }
                            val (label, color) = when (d.state) {
                                DoseState.TAKEN -> d.log!!.actionAt.toLocalDateTime()
                                    .let { "✓ " + timeText(it.hour * 60 + it.minute) } to colors.accent
                                DoseState.SKIPPED -> "رد شد" to colors.textSecondary
                                else -> "جا افتاد" to colors.danger
                            }
                            Text(label, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
