package ir.milad.medicinereminder.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.RemoveCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.milad.medicinereminder.data.DoseTime
import ir.milad.medicinereminder.data.Form
import ir.milad.medicinereminder.data.Frequency
import ir.milad.medicinereminder.data.Instruction
import ir.milad.medicinereminder.data.Medicine
import ir.milad.medicinereminder.data.Repo
import ir.milad.medicinereminder.ui.amountNumber
import ir.milad.medicinereminder.ui.components.Chip
import ir.milad.medicinereminder.ui.components.Group
import ir.milad.medicinereminder.ui.components.GroupRow
import ir.milad.medicinereminder.ui.components.MedIcon
import ir.milad.medicinereminder.ui.components.Stepper
import ir.milad.medicinereminder.ui.components.Wheel
import ir.milad.medicinereminder.ui.components.RowDivider
import ir.milad.medicinereminder.ui.components.SectionHeader
import ir.milad.medicinereminder.ui.components.medicineColors
import ir.milad.medicinereminder.ui.dayShort
import ir.milad.medicinereminder.ui.fa
import ir.milad.medicinereminder.ui.formName
import ir.milad.medicinereminder.ui.instructionText
import ir.milad.medicinereminder.ui.persianDate
import ir.milad.medicinereminder.ui.persianWeek
import ir.milad.medicinereminder.ui.timeText
import ir.milad.medicinereminder.ui.unit
import ir.milad.medicinereminder.ui.theme.colors
import kotlinx.coroutines.launch
import java.time.LocalDate

private val frequencies = listOf(
    Frequency.DAILY to "هر روز", Frequency.WEEKDAYS to "روزهای خاص هفته",
    Frequency.EVERY_N_DAYS to "هر چند روز", Frequency.CYCLE to "دوره‌ای (مثلاً ۲۱/۷)",
    Frequency.AS_NEEDED to "در صورت نیاز",
)

/** [id] = 0 → new medicine. Single grouped form, like iOS "new alarm". */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditorScreen(id: Long, onClose: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val today = LocalDate.now().toEpochDay()

    var m by remember { mutableStateOf(Medicine(name = "", startDate = today)) }
    val times = remember { mutableStateListOf(DoseTime(minuteOfDay = 9 * 60)) }
    var durationDays by remember { mutableStateOf<Int?>(null) }
    var loaded by remember { mutableStateOf(id == 0L) }
    var pickTime by remember { mutableStateOf<Int?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(id) {
        if (id == 0L) return@LaunchedEffect
        Repo.dao(context).get(id)?.let { mwt ->
            m = mwt.medicine
            times.clear(); times.addAll(mwt.times.sortedBy { it.minuteOfDay })
            durationDays = mwt.medicine.endDate?.let { (it - mwt.medicine.startDate + 1).toInt() }
        }
        loaded = true
    }
    if (!loaded) return

    val valid = m.name.isNotBlank() && (m.frequency == Frequency.AS_NEEDED || times.isNotEmpty()) &&
        (m.frequency != Frequency.WEEKDAYS || m.weekdays != 0)

    fun save() = scope.launch {
        val med = m.copy(name = m.name.trim(), endDate = durationDays?.let { m.startDate + it - 1 })
        val t = if (med.frequency == Frequency.AS_NEEDED) times.take(1).ifEmpty { listOf(DoseTime(minuteOfDay = 0)) }
        else times.distinctBy { it.minuteOfDay }
        Repo.save(context, med, t)
        onClose()
    }

    Column(Modifier.fillMaxSize().background(colors.background).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onClose) { Text("انصراف", color = colors.accent, fontSize = 16.sp) }
            Text(if (id == 0L) "داروی جدید" else "ویرایش دارو", Modifier.weight(1f), color = colors.text,
                fontWeight = FontWeight.Bold, fontSize = 17.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            TextButton(onClick = { save() }, enabled = valid) {
                Text("ذخیره", color = if (valid) colors.accent else colors.textSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 40.dp)) {
            // Preview + name
            Column(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                MedIcon(m.form, m.color, 104.dp)
            }
            TextField(
                m.name, { m = m.copy(name = it) }, Modifier.fillMaxWidth(),
                placeholder = { Text("نام دارو", Modifier.fillMaxWidth(), color = colors.textSecondary, fontSize = 26.sp,
                    fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = colors.text),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = colors.accent,
                ),
            )

            SectionHeader("شکل و رنگ")
            Group {
                Row(Modifier.horizontalScroll(rememberScrollState()).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Form.entries.forEach { f -> Chip(formName(f), m.form == f, { m = m.copy(form = f) }) }
                }
                RowDivider()
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    medicineColors.forEach { c ->
                        Box(
                            Modifier.size(32.dp).clip(CircleShape).background(Color(c))
                                .border(if (m.color == c) 3.dp else 1.dp, if (m.color == c) colors.accent else colors.separator, CircleShape)
                                .clickable { m = m.copy(color = c) },
                        )
                    }
                }
            }

            SectionHeader("چند وقت یک‌بار؟")
            Group {
                FlowRow(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    frequencies.forEach { (f, label) -> Chip(label, m.frequency == f, { m = m.copy(frequency = f) }) }
                }
                when (m.frequency) {
                    Frequency.WEEKDAYS -> {
                        RowDivider()
                        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            persianWeek.forEach { d ->
                                val bit = 1 shl d.ordinal
                                val on = m.weekdays and bit != 0
                                Box(
                                    Modifier.size(40.dp).clip(CircleShape).background(if (on) colors.accent else colors.fill)
                                        .clickable { m = m.copy(weekdays = m.weekdays xor bit) },
                                    contentAlignment = Alignment.Center,
                                ) { Text(dayShort(d), color = if (on) Color.White else colors.text, fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                    Frequency.EVERY_N_DAYS -> {
                        RowDivider()
                        StepperRow("هر چند روز", m.intervalDays.toFloat(), 2f) { m = m.copy(intervalDays = it.toInt()) }
                    }
                    Frequency.CYCLE -> {
                        RowDivider()
                        StepperRow("روزهای مصرف", m.cycleOn.toFloat(), 1f) { m = m.copy(cycleOn = it.toInt()) }
                        RowDivider()
                        StepperRow("روزهای استراحت", m.cycleOff.toFloat(), 1f) { m = m.copy(cycleOff = it.toInt()) }
                    }
                    else -> {}
                }
            }

            if (m.frequency == Frequency.AS_NEEDED) {
                SectionHeader("مقدار هر بار مصرف")
                Group {
                    val t = times.firstOrNull() ?: DoseTime(minuteOfDay = 0)
                    StepperRow(unit(m.form), t.amount, 0.5f, amount = true) {
                        if (times.isEmpty()) times.add(t.copy(amount = it)) else times[0] = t.copy(amount = it)
                    }
                }
            } else {
                SectionHeader("ساعت و مقدار هر وعده")
                Group {
                    times.forEachIndexed { i, t ->
                        if (i > 0) RowDivider()
                        GroupRow {
                            IconButton(onClick = { times.removeAt(i) }, Modifier.size(28.dp)) {
                                Icon(Icons.Rounded.RemoveCircle, "حذف", tint = colors.danger)
                            }
                            Text(timeText(t.minuteOfDay), Modifier.clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                                .background(colors.fill).clickable { pickTime = i }.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = colors.text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Box(Modifier.weight(1f))
                            Stepper(t.amount, { times[i] = t.copy(amount = it) })
                            Text(unit(m.form), color = colors.textSecondary, fontSize = 13.sp)
                        }
                    }
                    if (times.isNotEmpty()) RowDivider()
                    GroupRow(onClick = {
                        val last = times.maxOfOrNull { it.minuteOfDay }
                        times.add(DoseTime(minuteOfDay = last?.let { (it + 8 * 60) % (24 * 60) } ?: (9 * 60),
                            amount = times.lastOrNull()?.amount ?: 1f))
                    }) {
                        Icon(Icons.Rounded.Add, null, tint = colors.accent)
                        Text("افزودن ساعت", color = colors.accent, fontWeight = FontWeight.Bold)
                    }
                    if (times.isNotEmpty()) {
                        RowDivider()
                        Row(Modifier.horizontalScroll(rememberScrollState()).padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("میان‌بر:", color = colors.textSecondary, fontSize = 13.sp)
                            listOf(12, 8, 6).forEach { h ->
                                Chip("هر ${h.fa()} ساعت", false, {
                                    val first = times.minOf { it.minuteOfDay }
                                    val amount = times.first().amount
                                    times.clear()
                                    repeat(24 / h) { k -> times.add(DoseTime(minuteOfDay = (first + k * h * 60) % (24 * 60), amount = amount)) }
                                })
                            }
                        }
                    }
                }
            }

            SectionHeader("نحوه‌ی مصرف")
            Group {
                FlowRow(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Instruction.entries.forEach { ins ->
                        Chip(if (ins == Instruction.NONE) "فرقی ندارد" else instructionText(ins), m.instruction == ins,
                            { m = m.copy(instruction = ins) })
                    }
                }
                RowDivider()
                PlainField(m.note, { m = m.copy(note = it) }, "یادداشت (مثلاً با آب زیاد، همراه X نخورید)")
            }

            SectionHeader("مدت درمان")
            Group {
                GroupRow {
                    Text("شروع", Modifier.weight(1f), color = colors.text)
                    Text(persianDate(LocalDate.ofEpochDay(m.startDate), "d MMMM yyyy"), color = colors.textSecondary)
                }
                RowDivider()
                SwitchRow("نامحدود (داروی دائمی)", durationDays == null) { durationDays = if (it) null else 10 }
                durationDays?.let { d ->
                    RowDivider()
                    StepperRow("تعداد روز", d.toFloat(), 1f) { durationDays = it.toInt() }
                }
            }

            SectionHeader("موجودی و تمدید")
            Group {
                SwitchRow("پیگیری تعداد باقی‌مانده", m.stock != null) { m = m.copy(stock = if (it) 30f else null) }
                m.stock?.let { s ->
                    RowDivider()
                    NumberRow("الان چند ${unit(m.form)} داری؟", s) { m = m.copy(stock = it) }
                    RowDivider()
                    NumberRow("هشدار وقتی کمتر از", m.refillAt) { m = m.copy(refillAt = it) }
                }
            }

            if (id != 0L) {
                SectionHeader("")
                Group {
                    GroupRow(onClick = {
                        scope.launch { Repo.setArchived(context, m, !m.archived); onClose() }
                    }) { Text(if (m.archived) "بازگرداندن از آرشیو" else "پایان دوره (انتقال به آرشیو)", color = colors.accent) }
                    RowDivider()
                    GroupRow(onClick = { confirmDelete = true }) { Text("حذف دارو و سابقه‌اش", color = colors.danger) }
                }
            }
        }
    }

    pickTime?.let { i -> TimeSheet(times[i].minuteOfDay, { pickTime = null }) { times[i] = times[i].copy(minuteOfDay = it); pickTime = null } }

    if (confirmDelete) AlertDialog(
        onDismissRequest = { confirmDelete = false },
        title = { Text("حذف «${m.name}»؟") },
        text = { Text("دارو، آلارم‌ها و همه‌ی سابقه‌ی مصرفش پاک می‌شود. اگر دوره تمام شده، «انتقال به آرشیو» بهتر است.") },
        confirmButton = {
            TextButton(onClick = { scope.launch { Repo.delete(context, id); onClose() } }) { Text("حذف", color = colors.danger) }
        },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("انصراف") } },
    )
}

@Composable
private fun PlainField(value: String, onChange: (String) -> Unit, placeholder: String, keyboard: KeyboardType = KeyboardType.Text) =
    TextField(
        value, onChange, Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = colors.textSecondary) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
            cursorColor = colors.accent, focusedTextColor = colors.text, unfocusedTextColor = colors.text,
        ),
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 17.sp),
    )

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) = GroupRow {
    Text(label, Modifier.weight(1f), color = colors.text)
    Switch(checked, onChange, colors = SwitchDefaults.colors(checkedTrackColor = colors.accent))
}

@Composable
private fun StepperRow(label: String, value: Float, min: Float, amount: Boolean = false, onChange: (Float) -> Unit) =
    GroupRow {
        Text(label, Modifier.weight(1f), color = colors.text)
        Stepper(value, onChange, min, integer = !amount)
    }

@Composable
private fun NumberRow(label: String, value: Float, onChange: (Float) -> Unit) = GroupRow {
    Text(label, Modifier.weight(1f), color = colors.text)
    Stepper(value, onChange, 0f, integer = true)
}

/** iOS-style hour/minute wheels in a bottom sheet. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeSheet(minuteOfDay: Int, onDismiss: () -> Unit, onPick: (Int) -> Unit) {
    var h by remember { mutableStateOf(minuteOfDay / 60) }
    var mIdx by remember { mutableStateOf((minuteOfDay % 60) / 5) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = colors.surface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ساعت مصرف", color = colors.text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(Modifier.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Wheel((0..23).map { "%02d".format(it).fa() }, h, { h = it })
                    Text(":", color = colors.text, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Wheel((0..55 step 5).map { "%02d".format(it).fa() }, mIdx, { mIdx = it })
                }
            }
            Box(
                Modifier.fillMaxWidth().clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp)).background(colors.accent)
                    .clickable { onPick(h * 60 + mIdx * 5) }.padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) { Text("تأیید ${timeText(h * 60 + mIdx * 5)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        }
    }
}
