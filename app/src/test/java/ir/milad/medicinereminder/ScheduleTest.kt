package ir.milad.medicinereminder

import ir.milad.medicinereminder.data.Frequency
import ir.milad.medicinereminder.data.Medicine
import ir.milad.medicinereminder.domain.isDueOn
import ir.milad.medicinereminder.domain.nextTrigger
import ir.milad.medicinereminder.domain.weekdayMask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.LocalDate
import java.time.LocalDateTime

class ScheduleTest {
    // 2026-10-10 is a Saturday
    private val sat = LocalDate.of(2026, 10, 10)
    private fun med(f: Frequency, start: LocalDate = sat) =
        Medicine(name = "x", frequency = f, startDate = start.toEpochDay())

    @Test fun weeklyOnSaturday() {
        val m = med(Frequency.WEEKDAYS).copy(weekdays = weekdayMask(SATURDAY))
        assertTrue(m.isDueOn(sat))
        assertFalse(m.isDueOn(sat.plusDays(1)))
        // Sunday 10:00 → next is following Saturday 22:00
        val next = m.nextTrigger(22 * 60, sat.plusDays(1).atTime(10, 0))
        assertEquals(LocalDateTime.of(2026, 10, 17, 22, 0), next)
    }

    @Test fun twoWeekdays() {
        val m = med(Frequency.WEEKDAYS).copy(weekdays = weekdayMask(SATURDAY, WEDNESDAY))
        assertEquals(LocalDateTime.of(2026, 10, 14, 9, 0), m.nextTrigger(9 * 60, sat.atTime(9, 0)))
    }

    @Test fun sameDayLaterTime() {
        val m = med(Frequency.DAILY)
        assertEquals(sat.atTime(20, 0), m.nextTrigger(20 * 60, sat.atTime(8, 0)))
        assertEquals(sat.plusDays(1).atTime(8, 0), m.nextTrigger(8 * 60, sat.atTime(8, 0)))
    }

    @Test fun everyOtherDay() {
        val m = med(Frequency.EVERY_N_DAYS).copy(intervalDays = 2)
        assertTrue(m.isDueOn(sat))
        assertFalse(m.isDueOn(sat.plusDays(1)))
        assertTrue(m.isDueOn(sat.plusDays(2)))
    }

    @Test fun cycle21on7off() {
        val m = med(Frequency.CYCLE).copy(cycleOn = 21, cycleOff = 7)
        assertTrue(m.isDueOn(sat.plusDays(20)))
        assertFalse(m.isDueOn(sat.plusDays(21)))
        assertFalse(m.isDueOn(sat.plusDays(27)))
        assertTrue(m.isDueOn(sat.plusDays(28)))
    }

    @Test fun startAndEnd() {
        val m = med(Frequency.DAILY).copy(endDate = sat.plusDays(9).toEpochDay())
        assertFalse(m.isDueOn(sat.minusDays(1)))
        assertTrue(m.isDueOn(sat.plusDays(9)))
        assertFalse(m.isDueOn(sat.plusDays(10)))
        assertNull(m.nextTrigger(8 * 60, sat.plusDays(9).atTime(9, 0)))
    }

    @Test fun asNeededNeverRings() {
        assertNull(med(Frequency.AS_NEEDED).nextTrigger(8 * 60, sat.atTime(0, 0)))
    }
}
