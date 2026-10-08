package ir.milad.medicinereminder.domain

import ir.milad.medicinereminder.data.Frequency
import ir.milad.medicinereminder.data.Medicine
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** Rules documented in docs/SCHEDULING.md */
fun Medicine.isDueOn(date: LocalDate): Boolean {
    val day = date.toEpochDay()
    if (day < startDate || (endDate != null && day > endDate)) return false
    val sinceStart = day - startDate
    return when (frequency) {
        Frequency.DAILY -> true
        Frequency.WEEKDAYS -> weekdays and (1 shl date.dayOfWeek.ordinal) != 0
        Frequency.EVERY_N_DAYS -> sinceStart % intervalDays.coerceAtLeast(1) == 0L
        Frequency.CYCLE -> sinceStart % (cycleOn + cycleOff).coerceAtLeast(1) < cycleOn
        Frequency.AS_NEEDED -> false
    }
}

/** First due moment strictly after [after], or null if the course has ended. */
fun Medicine.nextTrigger(minuteOfDay: Int, after: LocalDateTime): LocalDateTime? {
    val time = LocalTime.of(minuteOfDay / 60, minuteOfDay % 60)
    var date = after.toLocalDate()
    repeat(400) {
        val at = date.atTime(time)
        if (at.isAfter(after) && isDueOn(date)) return at
        date = date.plusDays(1)
    }
    return null
}

fun weekdayMask(vararg days: DayOfWeek) = days.fold(0) { m, d -> m or (1 shl d.ordinal) }

fun Long.toLocalDateTime(): LocalDateTime =
    java.time.Instant.ofEpochMilli(this).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()

fun LocalDateTime.toMillis(): Long =
    atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
