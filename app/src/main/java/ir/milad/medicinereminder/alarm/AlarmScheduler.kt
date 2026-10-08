package ir.milad.medicinereminder.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import ir.milad.medicinereminder.MainActivity
import ir.milad.medicinereminder.data.DoseTime
import ir.milad.medicinereminder.data.Medicine
import ir.milad.medicinereminder.domain.nextTrigger
import ir.milad.medicinereminder.domain.toLocalDateTime
import ir.milad.medicinereminder.domain.toMillis

/** One pending alarm per DoseTime (requestCode = id) + one follow-up slot (id + FOLLOW_UP). */
object AlarmScheduler {
    private const val FOLLOW_UP = 1_000_000

    /** Schedules the first dose strictly after [afterMillis]. */
    fun schedule(context: Context, medicine: Medicine, time: DoseTime, afterMillis: Long) {
        val next = medicine.nextTrigger(time.minuteOfDay, afterMillis.toLocalDateTime())
        if (next == null) { cancel(context, time.id); return }
        set(context, time.id.toInt(), time.id, next.toMillis(), attempt = 0, regular = true, at = next.toMillis())
    }

    /** Re-alert (no answer) or snooze for an already-due dose. */
    fun followUp(context: Context, doseTimeId: Long, scheduledAt: Long, at: Long, attempt: Int) =
        set(context, doseTimeId.toInt() + FOLLOW_UP, doseTimeId, scheduledAt, attempt, regular = false, at = at)

    fun cancelFollowUp(context: Context, doseTimeId: Long) =
        alarmManager(context).cancel(pending(context, doseTimeId.toInt() + FOLLOW_UP, Intent()))

    fun cancel(context: Context, doseTimeId: Long) {
        alarmManager(context).cancel(pending(context, doseTimeId.toInt(), Intent()))
        cancelFollowUp(context, doseTimeId)
    }

    fun canScheduleExact(context: Context) =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager(context).canScheduleExactAlarms()

    private fun set(
        context: Context, code: Int, doseTimeId: Long, scheduledAt: Long,
        attempt: Int, regular: Boolean, at: Long,
    ) {
        val pi = pending(context, code, Intent()
            .putExtra(AlarmReceiver.EXTRA_DOSE_TIME, doseTimeId)
            .putExtra(AlarmReceiver.EXTRA_SCHEDULED_AT, scheduledAt)
            .putExtra(AlarmReceiver.EXTRA_ATTEMPT, attempt)
            .putExtra(AlarmReceiver.EXTRA_REGULAR, regular))
        val am = alarmManager(context)
        if (canScheduleExact(context)) {
            val show = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
            )
            am.setAlarmClock(AlarmManager.AlarmClockInfo(at, show), pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        }
    }

    // Extras don't take part in PendingIntent equality, so cancel() matches with an empty Intent.
    private fun pending(context: Context, code: Int, extras: Intent) = PendingIntent.getBroadcast(
        context, code,
        Intent(context, AlarmReceiver::class.java).setAction(AlarmReceiver.ACTION_FIRE).putExtras(extras),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun alarmManager(context: Context) = context.getSystemService(AlarmManager::class.java)
}
