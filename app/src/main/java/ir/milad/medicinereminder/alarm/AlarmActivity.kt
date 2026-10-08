package ir.milad.medicinereminder.alarm

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.milad.medicinereminder.data.Repo
import ir.milad.medicinereminder.domain.Dose
import ir.milad.medicinereminder.domain.toLocalDateTime
import ir.milad.medicinereminder.ui.components.MedIcon
import ir.milad.medicinereminder.ui.fa
import ir.milad.medicinereminder.ui.theme.AppTheme
import ir.milad.medicinereminder.ui.timeText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Full-screen alarm over the lock screen. Shows every dose that is currently due (two medicines at 08:00 → one screen).
 * Sound comes from the notification channel (see DECISIONS.md).
 */
class AlarmActivity : ComponentActivity() {
    private var reload by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        setContent {
            AppTheme {
                var doses by remember { mutableStateOf<List<Dose>?>(null) }
                LaunchedEffect(Unit) { newAlarm.collect { reload++ } }
                LaunchedEffect(reload) {
                    doses = Repo.ringing(applicationContext, System.currentTimeMillis())
                    if (doses.isNullOrEmpty()) finish()
                }
                LaunchedEffect(Unit) { delay(60_000); finish() } // matches the notification timeout

                doses?.takeIf { it.isNotEmpty() }?.let { list ->
                    AlarmScreen(
                        list,
                        onTakeOne = { d ->
                            act(AlarmReceiver.ACTION_TAKE, listOf(d))
                            doses = list - d
                            if (list.size == 1) finish()
                        },
                        onAll = { action -> act(action, list); finish() },
                    )
                }
            }
        }
    }

    // A second alarm while this screen is open (singleInstance) → refresh the list instead of ignoring it.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        reload++
    }

    companion object {
        /** AlarmReceiver pings this when another dose rings while the screen is already open. */
        val newAlarm = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    }

    private fun act(action: String, doses: List<Dose>) {
        // Not lifecycleScope: it is cancelled by finish()
        CoroutineScope(Dispatchers.IO).launch {
            doses.forEach { AlarmReceiver.act(applicationContext, action, it.time.id, it.at) }
        }
    }
}

private val White80 = Color.White.copy(alpha = 0.8f)

@Composable
private fun AlarmScreen(doses: List<Dose>, onTakeOne: (Dose) -> Unit, onAll: (String) -> Unit) {
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        1f, 1.12f, infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse), label = "s",
    )
    val single = doses.singleOrNull()
    Column(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF10B981))))
            .systemBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))
        Text(doses.first().at.toLocalDateTime().let { timeText(it.hour * 60 + it.minute) },
            color = White80, fontSize = 44.sp, fontWeight = FontWeight.Bold)
        if (single != null) {
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(180.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(180.dp).scale(pulse).background(Color.White.copy(alpha = 0.15f), CircleShape))
                MedIcon(single.medicine.form, single.medicine.color, 120.dp)
            }
            Spacer(Modifier.height(24.dp))
            Text(single.medicine.name, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(Notifications.doseText(single.medicine, single.time), color = White80, fontSize = 18.sp, textAlign = TextAlign.Center)
            if (single.medicine.note.isNotBlank()) Text(single.medicine.note, Modifier.padding(top = 8.dp),
                color = Color.White.copy(alpha = 0.75f), fontSize = 15.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.weight(1f))
        } else {
            Text("وقت ${doses.size.fa()} دارو", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                doses.forEach { d -> DoseRow(d) { onTakeOne(d) } }
            }
            Spacer(Modifier.height(16.dp))
        }
        Buttons(if (single != null) "✓  خوردم" else "✓  همه رو خوردم", onAll)
    }
}

@Composable
private fun DoseRow(d: Dose, onTake: () -> Unit) {
    var visible by remember { mutableStateOf(true) }
    AnimatedVisibility(visible, exit = shrinkVertically() + fadeOut()) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color.White.copy(alpha = 0.14f))
                .border(androidx.compose.foundation.BorderStroke(1.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.6f), Color.White.copy(alpha = 0.08f)))), RoundedCornerShape(24.dp)).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            MedIcon(d.medicine.form, d.medicine.color, 52.dp)
            Column(Modifier.weight(1f)) {
                Text(d.medicine.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(Notifications.doseText(d.medicine, d.time), color = White80, fontSize = 14.sp)
            }
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(Color.White).clickable { visible = false; onTake() },
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Rounded.Check, "خوردم", tint = Color(0xFF047857)) }
        }
    }
}

@Composable
private fun ColumnScope.Buttons(takeLabel: String, onAll: (String) -> Unit) {
    Button(
        onClick = { onAll(AlarmReceiver.ACTION_TAKE) }, modifier = Modifier.fillMaxWidth().height(64.dp),
        shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White),
    ) { Text(takeLabel, color = Color(0xFF047857), fontSize = 20.sp, fontWeight = FontWeight.Bold) }
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf("۱۰ دقیقه بعد" to AlarmReceiver.ACTION_SNOOZE, "رد کردن" to AlarmReceiver.ACTION_SKIP).forEach { (label, action) ->
            Button(
                onClick = { onAll(action) }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.14f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.6f), Color.White.copy(alpha = 0.08f)))),
            ) { Text(label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        }
    }
}
