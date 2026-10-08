package ir.milad.medicinereminder.alarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.milad.medicinereminder.data.DoseTime
import ir.milad.medicinereminder.data.Medicine
import ir.milad.medicinereminder.data.Repo
import ir.milad.medicinereminder.domain.toLocalDateTime
import ir.milad.medicinereminder.ui.components.PillIcon
import ir.milad.medicinereminder.ui.theme.AppTheme
import ir.milad.medicinereminder.ui.theme.colors
import ir.milad.medicinereminder.ui.timeText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Full-screen alarm over the lock screen. Sound comes from the notification channel (see DECISIONS.md). */
class AlarmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        val doseTimeId = intent.getLongExtra(AlarmReceiver.EXTRA_DOSE_TIME, -1)
        val scheduledAt = intent.getLongExtra(AlarmReceiver.EXTRA_SCHEDULED_AT, 0)

        fun act(action: String) {
            // Not lifecycleScope: it is cancelled by finish()
            CoroutineScope(Dispatchers.IO).launch { AlarmReceiver.act(applicationContext, action, doseTimeId, scheduledAt) }
            finish()
        }

        setContent {
            AppTheme {
                var data by remember { mutableStateOf<Pair<Medicine, DoseTime>?>(null) }
                LaunchedEffect(Unit) {
                    val dao = Repo.dao(this@AlarmActivity)
                    val t = dao.doseTime(doseTimeId)
                    val m = t?.let { dao.get(it.medicineId)?.medicine }
                    if (t == null || m == null) finish() else data = m to t
                    delay(60_000) // matches the notification timeout
                    finish()
                }
                data?.let { (m, t) ->
                    AlarmScreen(m, t, scheduledAt, onTake = { act(AlarmReceiver.ACTION_TAKE) },
                        onSnooze = { act(AlarmReceiver.ACTION_SNOOZE) }, onSkip = { act(AlarmReceiver.ACTION_SKIP) })
                }
            }
        }
    }
}

@Composable
private fun AlarmScreen(m: Medicine, t: DoseTime, scheduledAt: Long, onTake: () -> Unit, onSnooze: () -> Unit, onSkip: () -> Unit) {
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        1f, 1.12f, infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse), label = "s",
    )
    Column(
        Modifier.fillMaxSize().background(colors.background).systemBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Text(scheduledAt.toLocalDateTime().let { timeText(it.hour * 60 + it.minute) },
            color = colors.textSecondary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Box(Modifier.size(180.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(180.dp).scale(pulse).background(colors.accent.copy(alpha = 0.15f), CircleShape))
            PillIcon(m.form, m.color, 120.dp)
        }
        Spacer(Modifier.height(24.dp))
        Text(m.name, color = colors.text, fontSize = 34.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(Notifications.doseText(m, t), color = colors.textSecondary, fontSize = 18.sp, textAlign = TextAlign.Center)
        if (m.note.isNotBlank()) Text(m.note, Modifier.padding(top = 8.dp), color = colors.textSecondary,
            fontSize = 15.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onTake, modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
        ) { Text("✓  خوردم", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("۱۰ دقیقه بعد" to onSnooze, "رد کردن" to onSkip).forEach { (label, click) ->
                Button(
                    onClick = click, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.fill),
                ) { Text(label, color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}
