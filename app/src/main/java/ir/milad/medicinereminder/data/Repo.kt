package ir.milad.medicinereminder.data

import android.content.Context
import ir.milad.medicinereminder.alarm.AlarmScheduler
import ir.milad.medicinereminder.alarm.Notifications
import ir.milad.medicinereminder.domain.Dose
import ir.milad.medicinereminder.domain.DoseState
import ir.milad.medicinereminder.domain.dosesOn
import ir.milad.medicinereminder.domain.toLocalDateTime
import kotlinx.coroutines.flow.first

object Repo {
    fun dao(context: Context) = AppDb.get(context).dao()

    suspend fun save(context: Context, medicine: Medicine, times: List<DoseTime>): Long {
        val dao = dao(context)
        // Existing DoseTimes keep their id so DoseLog history stays linked to them.
        val id = if (medicine.id == 0L) dao.insert(medicine) else medicine.id.also { dao.update(medicine) }
        val old = dao.get(id)?.times.orEmpty()
        old.forEach { AlarmScheduler.cancel(context, it.id) }
        val keep = times.filter { t -> t.id != 0L && old.any { it.id == t.id } }
        dao.deleteTimes(old.map { it.id } - keep.map { it.id }.toSet())
        dao.updateTimes(keep.map { it.copy(medicineId = id) })
        dao.insertTimes(times.filter { it !in keep }.map { it.copy(id = 0, medicineId = id) })
        reschedule(context, id)
        return id
    }

    suspend fun delete(context: Context, id: Long) {
        val dao = dao(context)
        dao.get(id)?.times?.forEach { AlarmScheduler.cancel(context, it.id) }
        dao.delete(id)
    }

    suspend fun setArchived(context: Context, medicine: Medicine, archived: Boolean) {
        dao(context).update(medicine.copy(archived = archived))
        reschedule(context, medicine.id)
    }

    suspend fun take(context: Context, medicineId: Long, doseTimeId: Long?, scheduledAt: Long, amount: Float) {
        val dao = dao(context)
        if (doseTimeId != null && dao.logFor(doseTimeId, scheduledAt)?.status == LogStatus.TAKEN) return
        dao.log(DoseLog(0, medicineId, doseTimeId, scheduledAt, LogStatus.TAKEN, System.currentTimeMillis(), amount))
        dao.adjustStock(medicineId, -amount)
        val m = dao.get(medicineId)?.medicine ?: return
        if (m.stock != null && m.stock <= m.refillAt) Notifications.showRefill(context, m)
    }

    suspend fun skip(context: Context, medicineId: Long, doseTimeId: Long, scheduledAt: Long, amount: Float) {
        val dao = dao(context)
        val old = dao.logFor(doseTimeId, scheduledAt)
        if (old?.status == LogStatus.TAKEN) dao.adjustStock(medicineId, old.amount)
        dao.log(DoseLog(old?.id ?: 0, medicineId, doseTimeId, scheduledAt, LogStatus.SKIPPED, System.currentTimeMillis(), amount))
    }

    suspend fun undo(context: Context, log: DoseLog) {
        val dao = dao(context)
        dao.deleteLog(log.id)
        if (log.status == LogStatus.TAKEN) dao.adjustStock(log.medicineId, log.amount)
    }

    /** Doses that are due now and not answered yet (last [windowMillis]); what the alarm screen shows. */
    suspend fun ringing(context: Context, now: Long, windowMillis: Long = 3 * 3_600_000L): List<Dose> {
        val dao = dao(context)
        val meds = dao.active()
        val logs = dao.observeLogs(now - 2 * 86_400_000L, Long.MAX_VALUE).first()
        val today = now.toLocalDateTime().toLocalDate()
        return listOf(today.minusDays(1), today)
            .flatMap { dosesOn(it, meds, logs, now + 1) }
            .filter { it.state == DoseState.MISSED && it.at >= now - windowMillis }
    }

    suspend fun rescheduleAll(context: Context) =
        dao(context).active().forEach { schedule(context, it) }

    private suspend fun reschedule(context: Context, id: Long) {
        val mwt = dao(context).get(id) ?: return
        if (mwt.medicine.archived) mwt.times.forEach { AlarmScheduler.cancel(context, it.id) }
        else schedule(context, mwt)
    }

    private fun schedule(context: Context, mwt: MedicineWithTimes) =
        mwt.times.forEach { AlarmScheduler.schedule(context, mwt.medicine, it, System.currentTimeMillis()) }
}
