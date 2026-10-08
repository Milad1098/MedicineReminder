package ir.milad.medicinereminder.data

import android.content.Context
import ir.milad.medicinereminder.alarm.AlarmScheduler
import ir.milad.medicinereminder.alarm.Notifications

object Repo {
    fun dao(context: Context) = AppDb.get(context).dao()

    suspend fun save(context: Context, medicine: Medicine, times: List<DoseTime>): Long {
        val dao = dao(context)
        val id = if (medicine.id == 0L) dao.insert(medicine) else {
            dao.get(medicine.id)?.times?.forEach { AlarmScheduler.cancel(context, it.id) }
            dao.update(medicine)
            dao.deleteTimes(medicine.id)
            medicine.id
        }
        dao.insertTimes(times.map { it.copy(id = 0, medicineId = id) })
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
