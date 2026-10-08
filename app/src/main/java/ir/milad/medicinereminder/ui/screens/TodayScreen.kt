package ir.milad.medicinereminder.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import ir.milad.medicinereminder.alarm.AlarmReceiver
import ir.milad.medicinereminder.alarm.AlarmScheduler
import ir.milad.medicinereminder.alarm.Notifications
import ir.milad.medicinereminder.data.DoseLog
import ir.milad.medicinereminder.data.Frequency
import ir.milad.medicinereminder.data.MedicineWithTimes
import ir.milad.medicinereminder.data.Repo
import ir.milad.medicinereminder.domain.Dose
import ir.milad.medicinereminder.domain.DoseState
import ir.milad.medicinereminder.domain.dosesOn
import ir.milad.medicinereminder.domain.toLocalDateTime
import ir.milad.medicinereminder.domain.toMillis
import ir.milad.medicinereminder.ui.amountText
import ir.milad.medicinereminder.ui.fa
import ir.milad.medicinereminder.ui.persianDate
import ir.milad.medicinereminder.ui.timeText
import ir.milad.medicinereminder.ui.components.CheckCircle
import ir.milad.medicinereminder.ui.components.Group
import ir.milad.medicinereminder.ui.components.GroupRow
import ir.milad.medicinereminder.ui.components.PillIcon
import ir.milad.medicinereminder.ui.components.RowDivider
import ir.milad.medicinereminder.ui.components.ScreenTitle
import ir.milad.medicinereminder.ui.components.SectionHeader
import ir.milad.medicinereminder.ui.theme.colors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Ticks every 30s so MISSED states and the date roll over without reopening the app. */
@Composable
fun rememberNow(): Long {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { delay(30_000); now = System.currentTimeMillis() } }
    return now
}

@Composable
fun TodayScreen(meds: List<MedicineWithTimes>, logs: List<DoseLog>, onAdd: () -> Unit, padding: PaddingValues) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val now = rememberNow()
    val today = now.toLocalDateTime().toLocalDate()
    val dayStart = today.atStartOfDay().toMillis()
    val doses = dosesOn(today, meds, logs, now)
    val asNeeded = meds.filter { !it.medicine.archived && it.medicine.frequency == Frequency.AS_NEEDED }

    fun act(d: Dose, action: String) = scope.launch {
        if (action == AlarmReceiver.ACTION_TAKE) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        AlarmReceiver.act(context, action, d.time.id, d.at)
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = padding.calculateBottomPadding() + 24.dp,
            top = padding.calculateTopPadding()),
    ) {
        item { ScreenTitle("امروز", persianDate(today)) }
        item { PermissionsCard() }

        if (meds.none { !it.medicine.archived }) {
            item { EmptyState(onAdd) }
            return@LazyColumn
        }

        item {
            ProgressCard(doses.count { it.state == DoseState.TAKEN }, doses.size,
                doses.firstOrNull { it.state == DoseState.UPCOMING })
        }

        doses.groupBy { it.time.minuteOfDay }.forEach { (minute, group) ->
            item(key = "h$minute") { SectionHeader(timeText(minute)) }
            item(key = "g$minute") {
                Group {
                    group.forEachIndexed { i, d ->
                        if (i > 0) RowDivider(72.dp)
                        DoseRow(d, onToggle = {
                            if (d.log != null) scope.launch { Repo.undo(context, d.log) }
                            else act(d, AlarmReceiver.ACTION_TAKE)
                        }, onSkip = { act(d, AlarmReceiver.ACTION_SKIP) },
                            onUndo = { d.log?.let { scope.launch { Repo.undo(context, it) } } })
                    }
                }
            }
        }

        if (asNeeded.isNotEmpty()) {
            item { SectionHeader("در صورت نیاز") }
            item {
                Group {
                    asNeeded.forEachIndexed { i, mwt ->
                        if (i > 0) RowDivider(72.dp)
                        val m = mwt.medicine
                        val amount = mwt.times.firstOrNull()?.amount ?: 1f
                        val count = logs.count { it.medicineId == m.id && it.doseTimeId == null && it.scheduledAt >= dayStart }
                        GroupRow {
                            PillIcon(m.form, m.color)
                            Column(Modifier.weight(1f)) {
                                Text(m.name, color = colors.text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(if (count == 0) "امروز مصرف نشده" else "امروز ${count.fa()} بار",
                                    color = colors.textSecondary, fontSize = 13.sp)
                            }
                            TextButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                scope.launch { Repo.take(context, m.id, null, System.currentTimeMillis(), amount) }
                            }) { Text("ثبت ${amountText(amount, m.form)}", color = colors.accent, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DoseRow(d: Dose, onToggle: () -> Unit, onSkip: () -> Unit, onUndo: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    val m = d.medicine
    Box {
        GroupRow(onClick = { menu = true }) {
            PillIcon(m.form, m.color)
            Column(Modifier.weight(1f)) {
                Text(m.name, color = colors.text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(Notifications.doseText(m, d.time), color = colors.textSecondary, fontSize = 13.sp)
                when (d.state) {
                    DoseState.MISSED -> Text("جا افتاده", color = colors.danger, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    DoseState.SKIPPED -> Text("رد شد", color = colors.textSecondary, fontSize = 12.sp)
                    DoseState.TAKEN -> Text(
                        "خورده شد ساعت " + d.log!!.actionAt.toLocalDateTime().let { timeText(it.hour * 60 + it.minute) },
                        color = colors.accent, fontSize = 12.sp,
                    )
                    DoseState.UPCOMING -> {}
                }
            }
            CheckCircle(d.state == DoseState.TAKEN, if (d.state == DoseState.MISSED) colors.danger else colors.accent, onToggle)
        }
        DropdownMenu(menu, { menu = false }) {
            if (d.log == null) {
                DropdownMenuItem({ Text("خوردم") }, { menu = false; onToggle() })
                DropdownMenuItem({ Text("رد کردن این وعده") }, { menu = false; onSkip() })
            } else DropdownMenuItem({ Text("برگرداندن") }, { menu = false; onUndo() })
        }
    }
}

@Composable
private fun ProgressCard(taken: Int, total: Int, next: Dose?) {
    val progress by animateFloatAsState(if (total == 0) 0f else taken / total.toFloat(), tween(600), label = "ring")
    val accent = colors.accent
    val track = colors.fill
    Group(Modifier.padding(top = 8.dp)) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Box(Modifier.size(84.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(84.dp)) {
                    val s = Stroke(10.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(track, 0f, 360f, false, style = s)
                    drawArc(accent, -90f, 360f * progress, false, style = s)
                }
                Text("${taken.fa()}/${total.fa()}", color = colors.text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    when {
                        total == 0 -> "امروز دارویی نداری"
                        taken == total -> "همه رو خوردی 🎉"
                        else -> "${(total - taken).fa()} وعده‌ی دیگر مانده"
                    },
                    color = colors.text, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                )
                next?.let {
                    Text("بعدی: ${it.medicine.name} · ساعت ${timeText(it.time.minuteOfDay)}",
                        color = colors.textSecondary, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun EmptyState(onAdd: () -> Unit) = Column(
    Modifier.padding(top = 80.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(10.dp),
) {
    PillIcon(ir.milad.medicinereminder.data.Form.CAPSULE, 0xFF93C5FD.toInt(), 96.dp)
    Spacer(Modifier.height(8.dp))
    Text("هنوز دارویی اضافه نکرده‌ای", color = colors.text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    Text("دارو، ساعت و مقدار مصرف رو ثبت کن تا سر وقت یادت بندازم", color = colors.textSecondary, fontSize = 14.sp)
    TextButton(onClick = onAdd) {
        Icon(Icons.Rounded.Add, null, tint = colors.accent)
        Text("افزودن دارو", color = colors.accent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

private class Perm(val title: String, val desc: String, val ok: Boolean, val required: Boolean, val fix: () -> Unit)

/** Shows only what is missing. Re-checked on every resume (user returns from Settings). */
@Composable
private fun PermissionsCard() {
    val context = LocalContext.current
    var tick by remember { mutableIntStateOf(0) }
    LifecycleResumeEffect(Unit) { tick++; onPauseOrDispose { } }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { tick++ }

    val perms = remember(tick) { permissions(context) { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) } }
        .filter { !it.ok }
    if (perms.isEmpty()) return

    Group(Modifier.padding(top = 8.dp)) {
        perms.forEachIndexed { i, p ->
            if (i > 0) RowDivider()
            GroupRow(onClick = p.fix) {
                Icon(Icons.Rounded.WarningAmber, null, tint = if (p.required) colors.danger else colors.warning)
                Column(Modifier.weight(1f)) {
                    Text(p.title, color = colors.text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(p.desc, color = colors.textSecondary, fontSize = 13.sp)
                }
                Text("فعال‌سازی", color = colors.accent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

private fun permissions(context: Context, requestNotif: () -> Unit): List<Perm> {
    fun open(action: String, withPackage: Boolean = true) {
        runCatching {
            context.startActivity(Intent(action).apply { if (withPackage) data = Uri.parse("package:${context.packageName}") })
        }
    }
    val pm = context.getSystemService(PowerManager::class.java)
    return listOf(
        Perm("اجازه‌ی نوتیفیکیشن", "بدون این، زنگ دارو نمایش داده نمی‌شود",
            Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED,
            true, requestNotif),
        Perm("آلارم دقیق", "برای زنگ زدن سر همان دقیقه", AlarmScheduler.canScheduleExact(context), true) {
            if (Build.VERSION.SDK_INT >= 31) open(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        },
        Perm("نمایش تمام‌صفحه", "برای نمایش آلارم روی صفحه‌ی قفل", Notifications.canFullScreen(context), true) {
            if (Build.VERSION.SDK_INT >= 34) open(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
        },
        Perm("بهینه‌سازی باتری", "بعضی گوشی‌ها (شیائومی، سامسونگ) آلارم را دیر می‌زنند",
            pm.isIgnoringBatteryOptimizations(context.packageName), false) {
            open(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS, withPackage = false)
        },
    )
}
