package ir.milad.medicinereminder.ui.screens

import androidx.compose.foundation.background
import ir.milad.medicinereminder.ui.fa
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.milad.medicinereminder.data.Frequency
import ir.milad.medicinereminder.data.MedicineWithTimes
import ir.milad.medicinereminder.ui.amountNumber
import ir.milad.medicinereminder.ui.amountText
import ir.milad.medicinereminder.ui.frequencyText
import ir.milad.medicinereminder.ui.timeText
import ir.milad.medicinereminder.ui.unit
import ir.milad.medicinereminder.ui.components.Group
import ir.milad.medicinereminder.ui.components.GroupRow
import ir.milad.medicinereminder.ui.components.MedIcon
import ir.milad.medicinereminder.ui.components.RowDivider
import ir.milad.medicinereminder.ui.components.ScreenTitle
import ir.milad.medicinereminder.ui.components.SectionHeader
import ir.milad.medicinereminder.ui.theme.colors

@Composable
fun MedicinesScreen(meds: List<MedicineWithTimes>, onOpen: (Long) -> Unit, padding: PaddingValues) {
    val (archived, active) = meds.partition { it.medicine.archived }
    LazyColumn(
        Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp,
            bottom = padding.calculateBottomPadding() + 24.dp),
    ) {
        item {
            ScreenTitle("داروها") {
                FilledIconButton(
                    onClick = { onOpen(0) },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = colors.accent),
                ) { Icon(Icons.Rounded.Add, "افزودن دارو", tint = Color.White) }
            }
        }
        if (active.isEmpty()) item {
            Text("با دکمه‌ی + اولین دارو را اضافه کن", Modifier.padding(16.dp), color = colors.textSecondary)
        }
        else item { MedList(active, onOpen) }
        if (archived.isNotEmpty()) {
            item { SectionHeader("آرشیو (دوره‌ی تمام‌شده)") }
            item { MedList(archived, onOpen) }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun MedList(meds: List<MedicineWithTimes>, onOpen: (Long) -> Unit) = Group(Modifier.padding(top = 8.dp)) {
    meds.forEachIndexed { i, (m, times) ->
        if (i > 0) RowDivider(72.dp)
        GroupRow(onClick = { onOpen(m.id) }) {
            MedIcon(m.form, m.color, 46.dp)
            Column(Modifier.weight(1f)) {
                Text(m.name, color = colors.text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                val left = m.endDate?.let { it - java.time.LocalDate.now().toEpochDay() + 1 }
                Text(frequencyText(m) + (left?.takeIf { it > 0 }?.let { " · ${it.fa()} روز مانده" } ?: ""),
                    color = colors.textSecondary, fontSize = 13.sp)
                if (m.frequency != Frequency.AS_NEEDED) FlowRow(Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    times.sortedBy { it.minuteOfDay }.forEach {
                        Row(Modifier.clip(RoundedCornerShape(8.dp)).background(colors.fill).padding(horizontal = 8.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(timeText(it.minuteOfDay), color = colors.text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(amountText(it.amount, m.form), color = colors.textSecondary, fontSize = 12.sp)
                        }
                    }
                }
                m.stock?.let { s ->
                    val low = s <= m.refillAt
                    Text(
                        "موجودی: ${amountNumber(s.coerceAtLeast(0f))} ${unit(m.form)}" + if (low) " · تمدید کن" else "",
                        Modifier.padding(top = 4.dp).clip(RoundedCornerShape(8.dp))
                            .background((if (low) colors.warning else colors.fill).copy(alpha = if (low) 0.18f else 1f))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        color = if (low) colors.warning else colors.textSecondary, fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, null, tint = colors.separator)
        }
    }
}
