package ir.milad.medicinereminder.ui

import android.icu.text.SimpleDateFormat
import android.icu.util.ULocale
import ir.milad.medicinereminder.data.Form
import ir.milad.medicinereminder.data.Frequency
import ir.milad.medicinereminder.data.Instruction
import ir.milad.medicinereminder.data.Medicine
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

fun String.fa() = map { if (it in '0'..'9') '۰' + (it - '0') else it }.joinToString("")
fun Number.fa() = toString().fa()

fun amountNumber(a: Float) = when {
    a == 0.5f -> "نصف"
    a % 1f == 0f -> a.toInt().fa()
    else -> a.toString().replace('.', '٫').fa()
}

fun unit(form: Form) = when (form) {
    Form.PILL, Form.CAPSULE, Form.INJECTION -> "عدد"
    Form.LIQUID -> "میلی‌لیتر"
    Form.DROPS -> "قطره"
    Form.INHALER -> "پاف"
}

fun amountText(a: Float, form: Form) = "${amountNumber(a)} ${unit(form)}"

fun timeText(minuteOfDay: Int) = "%02d:%02d".format(minuteOfDay / 60, minuteOfDay % 60).fa()

fun formName(f: Form) = when (f) {
    Form.PILL -> "قرص"; Form.CAPSULE -> "کپسول"; Form.LIQUID -> "شربت"
    Form.DROPS -> "قطره"; Form.INHALER -> "اسپری"; Form.INJECTION -> "آمپول"
}

fun instructionText(i: Instruction) = when (i) {
    Instruction.NONE -> ""
    Instruction.BEFORE_FOOD -> "قبل از غذا"
    Instruction.AFTER_FOOD -> "بعد از غذا"
    Instruction.WITH_FOOD -> "همراه غذا"
    Instruction.EMPTY_STOMACH -> "معده‌ی خالی"
}

/** Persian week order: Saturday first */
val persianWeek = listOf(
    DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
)

fun dayName(d: DayOfWeek) = when (d) {
    DayOfWeek.SATURDAY -> "شنبه"; DayOfWeek.SUNDAY -> "یکشنبه"; DayOfWeek.MONDAY -> "دوشنبه"
    DayOfWeek.TUESDAY -> "سه‌شنبه"; DayOfWeek.WEDNESDAY -> "چهارشنبه"
    DayOfWeek.THURSDAY -> "پنجشنبه"; DayOfWeek.FRIDAY -> "جمعه"
}

fun dayShort(d: DayOfWeek) = dayName(d).first().toString()

fun frequencyText(m: Medicine): String = when (m.frequency) {
    Frequency.DAILY -> "هر روز"
    Frequency.WEEKDAYS -> {
        val days = persianWeek.filter { m.weekdays and (1 shl it.ordinal) != 0 }
        when {
            days.size == 7 -> "هر روز"
            days.size == 1 -> dayName(days[0]) + "‌ها"
            else -> days.joinToString("، ") { dayName(it) }
        }
    }
    Frequency.EVERY_N_DAYS -> if (m.intervalDays == 2) "یک روز در میان" else "هر ${m.intervalDays.fa()} روز"
    Frequency.CYCLE -> "${m.cycleOn.fa()} روز مصرف، ${m.cycleOff.fa()} روز استراحت"
    Frequency.AS_NEEDED -> "در صورت نیاز"
}

private val faLocale = ULocale("fa_IR@calendar=persian")

fun persianDate(date: LocalDate, pattern: String = "EEEE d MMMM"): String =
    SimpleDateFormat(pattern, faLocale).format(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()))
