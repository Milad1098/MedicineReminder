package ir.milad.medicinereminder.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import androidx.core.app.NotificationCompat
import ir.milad.medicinereminder.MainActivity
import ir.milad.medicinereminder.R
import ir.milad.medicinereminder.data.DoseTime
import ir.milad.medicinereminder.data.Medicine
import ir.milad.medicinereminder.ui.amountNumber
import ir.milad.medicinereminder.ui.amountText
import ir.milad.medicinereminder.ui.instructionText
import ir.milad.medicinereminder.ui.unit

object Notifications {
    private const val CH_ALARM = "dose_alarm"
    private const val CH_REFILL = "refill"
    private const val REFILL_BASE = 2_000_000

    fun createChannels(context: Context) {
        val sound = Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.alarm}")
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        nm(context).createNotificationChannels(listOf(
            NotificationChannel(CH_ALARM, "زنگ وقت دارو", NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(sound, attrs)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 800, 600, 800)
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            },
            NotificationChannel(CH_REFILL, "تمدید دارو", NotificationManager.IMPORTANCE_DEFAULT),
        ))
    }

    fun doseText(m: Medicine, t: DoseTime) =
        listOf(amountText(t.amount, m.form), instructionText(m.instruction)).filter { it.isNotEmpty() }.joinToString(" · ")

    fun showAlarm(context: Context, m: Medicine, t: DoseTime, scheduledAt: Long, attempt: Int) {
        val fullScreen = PendingIntent.getActivity(
            context, t.id.toInt(),
            Intent(context, AlarmActivity::class.java)
                .putExtra(AlarmReceiver.EXTRA_DOSE_TIME, t.id)
                .putExtra(AlarmReceiver.EXTRA_SCHEDULED_AT, scheduledAt)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        fun action(action: String, title: String) = NotificationCompat.Action(
            0, title,
            PendingIntent.getBroadcast(
                context, t.id.toInt(),
                Intent(context, AlarmReceiver::class.java).setAction(action)
                    .putExtra(AlarmReceiver.EXTRA_DOSE_TIME, t.id)
                    .putExtra(AlarmReceiver.EXTRA_SCHEDULED_AT, scheduledAt),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        val n = NotificationCompat.Builder(context, CH_ALARM)
            .setSmallIcon(R.drawable.ic_pill)
            .setContentTitle(if (attempt == 0) "💊 وقت ${m.name}" else "⏰ هنوز ${m.name} را نخورده‌ای")
            .setContentText(doseText(m, t))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreen, true)
            .setContentIntent(fullScreen)
            .setTimeoutAfter(60_000)
            .addAction(action(AlarmReceiver.ACTION_TAKE, "✓ خوردم"))
            .addAction(action(AlarmReceiver.ACTION_SNOOZE, "۱۰ دقیقه بعد"))
            .addAction(action(AlarmReceiver.ACTION_SKIP, "رد کردن"))
            .build()
        n.flags = n.flags or Notification.FLAG_INSISTENT
        runCatching { nm(context).notify(t.id.toInt(), n) }
    }

    fun cancelAlarm(context: Context, doseTimeId: Long) = nm(context).cancel(doseTimeId.toInt())

    fun showRefill(context: Context, m: Medicine) {
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(context, CH_REFILL)
            .setSmallIcon(R.drawable.ic_pill)
            .setContentTitle("${m.name} رو به اتمام است")
            .setContentText("فقط ${amountNumber(m.stock ?: 0f)} ${unit(m.form)} مانده؛ وقت تمدید نسخه است")
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        runCatching { nm(context).notify(REFILL_BASE + m.id.toInt(), n) }
    }

    fun canFullScreen(context: Context) =
        android.os.Build.VERSION.SDK_INT < 34 || nm(context).canUseFullScreenIntent()

    private fun nm(context: Context) = context.getSystemService(NotificationManager::class.java)
}
