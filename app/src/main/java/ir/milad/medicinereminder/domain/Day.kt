package ir.milad.medicinereminder.domain

import ir.milad.medicinereminder.data.DoseLog
import ir.milad.medicinereminder.data.DoseTime
import ir.milad.medicinereminder.data.LogStatus
import ir.milad.medicinereminder.data.Medicine
import ir.milad.medicinereminder.data.MedicineWithTimes
import java.time.LocalDate

enum class DoseState { TAKEN, SKIPPED, MISSED, UPCOMING }

data class Dose(val medicine: Medicine, val time: DoseTime, val at: Long, val log: DoseLog?, val state: DoseState)

/**
 * Planned doses of [date] joined with their logs.
 * ponytail: uses the *current* schedule for past days too; editing a medicine rewrites its history view.
 * Fix by snapshotting planned doses into DoseLog if that ever matters.
 */
fun dosesOn(date: LocalDate, meds: List<MedicineWithTimes>, logs: List<DoseLog>, now: Long): List<Dose> =
    meds.filter { !it.medicine.archived && it.medicine.isDueOn(date) }
        .flatMap { mwt ->
            mwt.times.map { t ->
                val at = date.atTime(t.minuteOfDay / 60, t.minuteOfDay % 60).toMillis()
                val log = logs.find { it.doseTimeId == t.id && it.scheduledAt == at }
                val state = when {
                    log?.status == LogStatus.TAKEN -> DoseState.TAKEN
                    log?.status == LogStatus.SKIPPED -> DoseState.SKIPPED
                    at < now -> DoseState.MISSED
                    else -> DoseState.UPCOMING
                }
                Dose(mwt.medicine, t, at, log, state)
            }
        }
        .sortedWith(compareBy({ it.time.minuteOfDay }, { it.medicine.name }))
