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
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
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
import ir.milad.medicinereminder.data.Medicine
import ir.milad.medicinereminder.data.MedicineWithTimes
import ir.milad.medicinereminder.data.Repo
import ir.milad.medicinereminder.domain.Dose
import ir.milad.medicinereminder.domain.DoseState
import ir.milad.medicinereminder.domain.dosesOn
import ir.milad.medicinereminder.domain.nextTrigger
import ir.milad.medicinereminder.domain.toLocalDateTime
import ir.milad.medicinereminder.domain.toMillis
import ir.milad.medicinereminder.ui.amountText
import ir.milad.medicinereminder.ui.components.CheckCircle
import ir.milad.medicinereminder.ui.components.Group
import ir.milad.medicinereminder.ui.components.GroupRow
import ir.milad.medicinereminder.ui.components.MedIcon
import ir.milad.medicinereminder.ui.components.RowDivider
import ir.milad.medicinereminder.ui.components.ScreenTitle
import ir.milad.medicinereminder.ui.components.SectionHeader
import ir.milad.medicinereminder.ui.dayName
import ir.milad.medicinereminder.ui.dayShort
import ir.milad.medicinereminder.ui.fa
import ir.milad.medicinereminder.ui.instructionText
import ir.milad.medicinereminder.ui.persianDate
import ir.milad.medicinereminder.ui.timeText
import ir.milad.medicinereminder.ui.theme.colors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/** Ticks every 30s so MISSED states, countdown and the date roll over without reopening the app. */
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
    val now = rememberNow()
    val today = now.toLocalDateTime().toLocalDate()
    var selectedDay by rememberSaveable { mutableLongStateOf(today.toEpochDay()) }
    val day = LocalDate.ofEpochDay(selectedDay)
    val doses = dosesOn(day, meds, logs, now)
    val active = meds.filter { !it.medicine.archived }
    val asNeeded = active.filter { it.medicine.frequency == Frequency.AS_NEEDED }
    val next = remember(meds, now) { nextDose(active, now) }

    LazyColumn(
        Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp,
            bottom = padding.calculateBottomPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            ScreenTitle(
                if (day == today) "امروز" else dayName(day.dayOfWeek),
                persianDate(day, "d MMMM yyyy"),
            )
        }
        item { WeekStrip(today, day, meds, logs, now) { selectedDay = it.toEpochDay() } }
        item { PermissionsCard() }

        if (active.isEmpty()) {
            item { EmptyState(onAdd) }
            return@LazyColumn
        }

        item {
            HeroCard(doses.count { it.state == DoseState.TAKEN }, doses.size, isToday = day == today, next = next, now = now)
        }

        doses.groupBy { it.time.minuteOfDay }.forEach { (minute, group) ->
            item(key = "h$minute$selectedDay") {
                SectionHeader(timeText(minute), trailing = when {
                    group.all { it.state == DoseState.TAKEN } -> "انجام شد ✓"
                    group.any { it.state == DoseState.MISSED } -> "جا افتاده"
                    else -> null
                })
            }
            group.forEach { d ->
                item(key = "d${d.time.id}_${d.at}") {
                    DoseCard(d, canAct = day <= today,
                        onTake = { scope.launch { AlarmReceiver.act(context, AlarmReceiver.ACTION_TAKE, d.time.id, d.at) } },
                        onSkip = { scope.launch { AlarmReceiver.act(context, AlarmReceiver.ACTION_SKIP, d.time.id, d.at) } },
                        onUndo = { d.log?.let { scope.launch { Repo.undo(context, it) } } })
                }
            }
        }

        if (asNeeded.isNotEmpty() && day == today) {
            item { SectionHeader("در صورت نیاز") }
            asNeeded.forEach { mwt ->
                val m = mwt.medicine
                val amount = mwt.times.firstOrNull()?.amount ?: 1f
                val dayStart = today.atStartOfDay().toMillis()
                val count = logs.count { it.medicineId == m.id && it.doseTimeId == null && it.scheduledAt >= dayStart }
                item(key = "n${m.id}") {
                    Card {
                        MedIcon(m.form, m.color, 52.dp)
                        Column(Modifier.weight(1f)) {
                            Text(m.name, color = colors.text, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text(if (count == 0) "امروز مصرف نشده" else "امروز ${count.fa()} بار مصرف شده",
                                color = colors.textSecondary, fontSize = 13.sp)
                        }
                        Text("+ ${amountText(amount, m.form)}",
                            Modifier.clip(RoundedCornerShape(12.dp)).background(colors.accent.copy(alpha = 0.12f))
                                .clickable { scope.launch { Repo.take(context, m.id, null, System.currentTimeMillis(), amount) } }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            color = colors.accent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        if (doses.isEmpty() && (asNeeded.isEmpty() || day != today)) item {
            Text("برای این روز وعده‌ای ثبت نشده", Modifier.fillMaxWidth().padding(24.dp),
                color = colors.textSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

private data class Next(val medicine: Medicine, val at: LocalDateTime)

private fun nextDose(meds: List<MedicineWithTimes>, now: Long): Next? {
    val from = now.toLocalDateTime()
    return meds.flatMap { mwt -> mwt.times.mapNotNull { t -> mwt.medicine.nextTrigger(t.minuteOfDay, from)?.let { Next(mwt.medicine, it) } } }
        .minByOrNull { it.at }
}

private fun relative(at: LocalDateTime, now: LocalDateTime): String {
    val mins = ChronoUnit.MINUTES.between(now, at)
    val days = ChronoUnit.DAYS.between(now.toLocalDate(), at.toLocalDate())
    return when {
        mins < 1 -> "همین الان"
        mins < 60 -> "${mins.fa()} دقیقه دیگر"
        days == 0L -> "${(mins / 60).fa()} ساعت و ${(mins % 60).fa()} دقیقه دیگر"
        days == 1L -> "فردا ساعت ${timeText(at.hour * 60 + at.minute)}"
        else -> "${dayName(at.dayOfWeek)} ساعت ${timeText(at.hour * 60 + at.minute)}"
    }
}

@Composable
private fun Card(onClick: (() -> Unit)? = null, content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) = Row(
    Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(colors.surface)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(14.dp),
    content = content,
)

/** Saturday-first week containing [today]; each day shows a tiny progress ring. */
@Composable
private fun WeekStrip(today: LocalDate, selected: LocalDate, meds: List<MedicineWithTimes>, logs: List<DoseLog>, now: Long, onSelect: (LocalDate) -> Unit) {
    val saturday = today.minusDays(((today.dayOfWeek.value - DayOfWeek.SATURDAY.value + 7) % 7).toLong())
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        (0L..6L).map { saturday.plusDays(it) }.forEach { date ->
            val doses = dosesOn(date, meds, logs, now)
            val ratio = if (doses.isEmpty()) 0f else doses.count { it.state == DoseState.TAKEN } / doses.size.toFloat()
            val isSel = date == selected
            val accent = colors.accent
            val track = if (isSel) Color.White.copy(alpha = 0.35f) else colors.separator
            Column(
                Modifier.clip(RoundedCornerShape(16.dp)).background(if (isSel) colors.accent else Color.Transparent)
                    .clickable { onSelect(date) }.padding(vertical = 8.dp).width(44.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(dayShort(date.dayOfWeek), color = if (isSel) Color.White else colors.textSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                    if (doses.isNotEmpty()) Canvas(Modifier.size(34.dp)) {
                        val s = Stroke(3.dp.toPx(), cap = StrokeCap.Round)
                        drawArc(track, 0f, 360f, false, style = s)
                        drawArc(if (isSel) Color.White else accent, -90f, 360f * ratio, false, style = s)
                    }
                    Text(persianDate(date, "d"), color = if (isSel) Color.White else if (date == today) colors.accent else colors.text,
                        fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun HeroCard(taken: Int, total: Int, isToday: Boolean, next: Next?, now: Long) {
    val progress by animateFloatAsState(if (total == 0) 0f else taken / total.toFloat(), tween(700), label = "ring")
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF059669), Color(0xFF10B981), Color(0xFF2DD4BF))))
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Box(Modifier.size(92.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(92.dp)) {
                    val s = Stroke(11.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(Color.White.copy(alpha = 0.25f), 0f, 360f, false, style = s)
                    drawArc(Color.White, -90f, 360f * progress, false, style = s)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(taken.fa(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 26.sp)
                    Text("از ${total.fa()}", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    when {
                        total == 0 -> if (isToday) "امروز دارویی نداری" else "دارویی برای این روز نیست"
                        taken == total -> "همه رو خوردی، آفرین! 🎉"
                        else -> "${(total - taken).fa()} وعده‌ی دیگر مانده"
                    },
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                )
                next?.let {
                    Column(Modifier.clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.18f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text("نوبت بعدی: ${it.medicine.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(relative(it.at, now.toLocalDateTime()), color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DoseCard(d: Dose, canAct: Boolean, onTake: () -> Unit, onSkip: () -> Unit, onUndo: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    val m = d.medicine
    val done = d.state == DoseState.TAKEN || d.state == DoseState.SKIPPED
    Box {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(colors.surface)
                .clickable(enabled = canAct) { menu = true }.padding(14.dp).animateContentSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            MedIcon(m.form, m.color, 52.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(m.name, color = if (done) colors.textSecondary else colors.text, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                    textDecoration = if (d.state == DoseState.SKIPPED) androidx.compose.ui.text.style.TextDecoration.LineThrough else null)
                Text(amountText(d.time.amount, m.form), color = colors.textSecondary, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    instructionText(m.instruction).takeIf { it.isNotEmpty() }?.let { Tag(it, colors.textSecondary) }
                    when (d.state) {
                        DoseState.MISSED -> Tag("جا افتاده", colors.danger)
                        DoseState.SKIPPED -> Tag("رد شد", colors.textSecondary)
                        DoseState.TAKEN -> Tag(d.log!!.actionAt.toLocalDateTime().let { "خورده شد " + timeText(it.hour * 60 + it.minute) }, colors.accent)
                        DoseState.UPCOMING -> {}
                    }
                }
            }
            CheckCircle(d.state == DoseState.TAKEN, if (d.state == DoseState.MISSED) colors.danger else colors.accent, enabled = canAct) {
                if (d.log != null) onUndo() else onTake()
            }
        }
        DropdownMenu(menu, { menu = false }) {
            if (d.log == null) {
                DropdownMenuItem({ Text("✓ خوردم") }, { menu = false; onTake() })
                DropdownMenuItem({ Text("رد کردن این وعده") }, { menu = false; onSkip() })
            } else DropdownMenuItem({ Text("برگرداندن") }, { menu = false; onUndo() })
        }
    }
}

@Composable
private fun Tag(text: String, color: Color) = Text(
    text, Modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 2.dp),
    color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold,
)

@Composable
private fun EmptyState(onAdd: () -> Unit) = Column(
    Modifier.fillMaxWidth().padding(top = 48.dp), horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(10.dp),
) {
    Box(Modifier.size(120.dp).clip(CircleShape).background(colors.accent.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
        MedIcon(ir.milad.medicinereminder.data.Form.CAPSULE, 0xFF60A5FA.toInt(), 72.dp)
    }
    Spacer(Modifier.height(8.dp))
    Text("هنوز دارویی اضافه نکرده‌ای", color = colors.text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    Text("دارو، ساعت و مقدار مصرف رو ثبت کن\nتا سر وقت یادت بندازم", color = colors.textSecondary, fontSize = 14.sp,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    Spacer(Modifier.height(8.dp))
    Row(
        Modifier.clip(RoundedCornerShape(16.dp)).background(colors.accent).clickable(onClick = onAdd)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(Icons.Rounded.Add, null, tint = Color.White)
        Text("افزودن اولین دارو", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

private class Perm(val key: String, val title: String, val desc: String, val ok: Boolean, val required: Boolean, val fix: () -> Unit)

/** Shows only what is missing; optional items can be dismissed. Re-checked on every resume. */
@Composable
private fun PermissionsCard() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("ui", Context.MODE_PRIVATE) }
    var tick by remember { mutableIntStateOf(0) }
    LifecycleResumeEffect(Unit) { tick++; onPauseOrDispose { } }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { tick++ }

    val perms = remember(tick) { permissions(context) { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) } }
        .filter { !it.ok && (it.required || !prefs.getBoolean("dismiss_${it.key}", false)) }
    if (perms.isEmpty()) return

    Group(Modifier.padding(top = 4.dp)) {
        perms.forEachIndexed { i, p ->
            if (i > 0) RowDivider(60.dp)
            GroupRow(onClick = p.fix) {
                Box(Modifier.size(32.dp).clip(RoundedCornerShape(10.dp))
                    .background(if (p.required) colors.danger else colors.warning), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.NotificationsActive, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(p.title, color = colors.text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(p.desc, color = colors.textSecondary, fontSize = 13.sp)
                }
                if (p.required) Text("فعال‌سازی", color = colors.accent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                else Icon(Icons.Rounded.Close, "بستن", tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp).clickable { prefs.edit().putBoolean("dismiss_${p.key}", true).apply(); tick++ })
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
        Perm("notif", "اجازه‌ی نوتیفیکیشن", "بدون این، زنگ دارو نمایش داده نمی‌شود",
            Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED,
            true, requestNotif),
        Perm("exact", "آلارم دقیق", "برای زنگ زدن سر همان دقیقه", AlarmScheduler.canScheduleExact(context), true) {
            if (Build.VERSION.SDK_INT >= 31) open(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        },
        Perm("fullscreen", "نمایش روی صفحه‌ی قفل", "برای اینکه آلارم تمام‌صفحه باز شود", Notifications.canFullScreen(context), true) {
            if (Build.VERSION.SDK_INT >= 34) open(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
        },
        Perm("battery", "بهینه‌سازی باتری", "روی شیائومی و سامسونگ خاموشش کن تا آلارم دیر نشود",
            pm.isIgnoringBatteryOptimizations(context.packageName), false) {
            open(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS, withPackage = false)
        },
    )
}
