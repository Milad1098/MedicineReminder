package ir.milad.medicinereminder.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ir.milad.medicinereminder.data.Repo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val doseTimeId = intent.getLongExtra(EXTRA_DOSE_TIME, -1)
        if (doseTimeId < 0) return // e.g. stale alarm from v1
        val scheduledAt = intent.getLongExtra(EXTRA_SCHEDULED_AT, 0)
        val action = intent.action ?: return
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (action == ACTION_FIRE) fire(
                    context, doseTimeId, scheduledAt,
                    intent.getIntExtra(EXTRA_ATTEMPT, 0), intent.getBooleanExtra(EXTRA_REGULAR, true)
                )
                else act(context, action, doseTimeId, scheduledAt)
            } finally {
                result.finish()
            }
        }
    }

    private suspend fun fire(context: Context, doseTimeId: Long, scheduledAt: Long, attempt: Int, regular: Boolean) {
        val dao = Repo.dao(context)
        val time = dao.doseTime(doseTimeId) ?: return
        val m = dao.get(time.medicineId)?.medicine ?: return
        if (m.archived) return
        if (regular) AlarmScheduler.schedule(context, m, time, scheduledAt)
        if (dao.logFor(doseTimeId, scheduledAt) != null) return
        Notifications.showAlarm(context, m, time, scheduledAt, attempt)
        AlarmActivity.newAlarm.tryEmit(Unit)
        if (attempt < MAX_FOLLOW_UPS) AlarmScheduler.followUp(
            context, doseTimeId, scheduledAt, System.currentTimeMillis() + 15 * 60_000, attempt + 1
        )
    }

    companion object {
        const val ACTION_FIRE = "ir.milad.medicinereminder.FIRE"
        const val ACTION_TAKE = "ir.milad.medicinereminder.TAKE"
        const val ACTION_SNOOZE = "ir.milad.medicinereminder.SNOOZE"
        const val ACTION_SKIP = "ir.milad.medicinereminder.SKIP"
        const val EXTRA_DOSE_TIME = "doseTimeId"
        const val EXTRA_SCHEDULED_AT = "scheduledAt"
        const val EXTRA_ATTEMPT = "attempt"
        const val EXTRA_REGULAR = "regular"
        private const val MAX_FOLLOW_UPS = 2

        /** Shared by notification actions and AlarmActivity buttons. */
        suspend fun act(context: Context, action: String, doseTimeId: Long, scheduledAt: Long) {
            Notifications.cancelAlarm(context, doseTimeId)
            val time = Repo.dao(context).doseTime(doseTimeId) ?: return
            when (action) {
                ACTION_TAKE -> {
                    AlarmScheduler.cancelFollowUp(context, doseTimeId)
                    Repo.take(context, time.medicineId, doseTimeId, scheduledAt, time.amount)
                }
                ACTION_SKIP -> {
                    AlarmScheduler.cancelFollowUp(context, doseTimeId)
                    Repo.skip(context, time.medicineId, doseTimeId, scheduledAt, time.amount)
                }
                ACTION_SNOOZE -> AlarmScheduler.followUp(
                    context, doseTimeId, scheduledAt, System.currentTimeMillis() + 10 * 60_000, 1
                )
            }
        }
    }
}
