package ir.milad.medicinereminder.ui.screens

import androidx.compose.foundation.background
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import ir.milad.medicinereminder.data.Backup
import kotlinx.coroutines.launch
import java.time.LocalDate
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
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding(),
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
        item { BackupSection() }
        item { AboutSection() }
    }
}

/** Export / restore everything as one JSON file (data/Backup.kt). */
@Composable
private fun BackupSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingRestore by remember { mutableStateOf<Uri?>(null) }
    fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_LONG).show()

    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            runCatching { Backup.export(context, uri) }
                .onSuccess { toast("پشتیبان ذخیره شد") }
                .onFailure { toast("ذخیره نشد: ${it.message}") }
        }
    }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { pendingRestore = it }

    SectionHeader("پشتیبان‌گیری")
    Group {
        GroupRow(onClick = { exporter.launch("medicine-backup-${LocalDate.now()}.json") }) {
            Icon(Icons.Rounded.CloudUpload, null, tint = colors.accent)
            Column(Modifier.weight(1f)) {
                Text("ذخیره‌ی پشتیبان", color = colors.text, fontWeight = FontWeight.Bold)
                Text("همه‌ی داروها و سابقه در یک فایل؛ در تلگرام یا گوگل‌درایو نگهش دار",
                    color = colors.textSecondary, fontSize = 13.sp)
            }
        }
        RowDivider(52.dp)
        GroupRow(onClick = { importer.launch(arrayOf("application/json", "application/octet-stream", "text/plain")) }) {
            Icon(Icons.Rounded.CloudDownload, null, tint = colors.accent)
            Column(Modifier.weight(1f)) {
                Text("بازیابی از فایل", color = colors.text, fontWeight = FontWeight.Bold)
                Text("مثلاً بعد از عوض کردن گوشی", color = colors.textSecondary, fontSize = 13.sp)
            }
        }
    }

    pendingRestore?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text("بازیابی پشتیبان؟") },
            text = { Text("داروها و سابقه‌ی فعلی این گوشی با محتوای فایل جایگزین می‌شود.") },
            confirmButton = {
                TextButton(onClick = {
                    pendingRestore = null
                    scope.launch {
                        runCatching { Backup.import(context, uri) }
                            .onSuccess { toast("${it.fa()} دارو بازیابی شد") }
                            .onFailure { toast("این فایل پشتیبانِ یادآور دارو نیست") }
                    }
                }) { Text("بازیابی", color = colors.danger) }
            },
            dismissButton = { TextButton(onClick = { pendingRestore = null }) { Text("انصراف") } },
        )
    }
}

@Composable
private fun AboutSection() {
    val context = LocalContext.current
    val version = remember { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
    SectionHeader("درباره")
    Group {
        GroupRow(onClick = {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Milad1098/MedicineReminder")))
        }) {
            Icon(Icons.Rounded.Code, null, tint = colors.accent)
            Column(Modifier.weight(1f)) {
                Text("کد منبع و گزارش مشکل", color = colors.text, fontWeight = FontWeight.Bold)
                Text("رایگان و متن‌باز · نسخه‌ی ${version.orEmpty().fa()}", color = colors.textSecondary, fontSize = 13.sp)
            }
        }
    }
    Text(
        "این برنامه فقط یادآور است و جایگزین توصیه‌ی پزشک یا داروساز نیست. همه‌ی اطلاعات فقط روی همین گوشی می‌ماند و جایی ارسال نمی‌شود.",
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
        color = colors.textSecondary, fontSize = 12.sp, textAlign = TextAlign.Center,
    )
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
                            .background(if (low) colors.warning.copy(alpha = 0.18f) else colors.fill)
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
