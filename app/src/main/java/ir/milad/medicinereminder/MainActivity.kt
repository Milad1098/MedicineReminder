package ir.milad.medicinereminder

import android.os.Bundle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.clickable
import androidx.compose.animation.animateColorAsState
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import ir.milad.medicinereminder.alarm.Notifications
import ir.milad.medicinereminder.data.Repo
import ir.milad.medicinereminder.domain.toMillis
import ir.milad.medicinereminder.ui.screens.EditorScreen
import ir.milad.medicinereminder.ui.screens.HISTORY_DAYS
import ir.milad.medicinereminder.ui.screens.HistoryScreen
import ir.milad.medicinereminder.ui.screens.MedicinesScreen
import ir.milad.medicinereminder.ui.screens.TodayScreen
import ir.milad.medicinereminder.ui.theme.AppTheme
import ir.milad.medicinereminder.ui.theme.colors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Notifications.createChannels(this)
        // Self-heal: alarms can be lost (force-stop, OEM killers). Rescheduling is idempotent.
        lifecycleScope.launch(Dispatchers.IO) { Repo.rescheduleAll(this@MainActivity) }
        setContent { AppTheme { App() } }
    }
}

private val tabs = listOf(
    Triple("امروز", Icons.Rounded.Today, 0),
    Triple("داروها", Icons.Rounded.Medication, 1),
    Triple("تاریخچه", Icons.Rounded.BarChart, 2),
)

@Composable
private fun App() {
    val context = LocalContext.current
    val dao = remember { Repo.dao(context) }
    val meds by dao.observeAll().collectAsState(emptyList())
    val from = remember { LocalDate.now().minusDays(HISTORY_DAYS).atStartOfDay().toMillis() }
    val logs by remember { dao.observeLogs(from, Long.MAX_VALUE) }.collectAsState(emptyList())

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var editing by rememberSaveable { mutableStateOf<Long?>(null) }
    BackHandler(editing != null) { editing = null }

    Box(Modifier.fillMaxSize().background(colors.background)) {
        Scaffold(
            containerColor = colors.background,
            bottomBar = { FloatingNav(tab) { tab = it } },
        ) { padding ->
            AnimatedContent(tab, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "tab") { t ->
                when (t) {
                    0 -> TodayScreen(meds, logs, onAdd = { editing = 0 }, padding)
                    1 -> MedicinesScreen(meds, onOpen = { editing = it }, padding)
                    else -> HistoryScreen(meds, logs, padding)
                }
            }
        }

        AnimatedVisibility(
            editing != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
        ) {
            // Keep last id while the exit animation runs
            val id = remember { editing ?: 0 }
            EditorScreen(id, onClose = { editing = null })
        }
    }
}

/** Telegram-style floating pill nav. */
@Composable
private fun FloatingNav(tab: Int, onSelect: (Int) -> Unit) = Box(
    Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 40.dp, vertical = 10.dp),
    contentAlignment = Alignment.Center,
) {
    Row(
        Modifier.fillMaxWidth().shadow(18.dp, RoundedCornerShape(32.dp), spotColor = Color.Black.copy(alpha = 0.25f))
            .clip(RoundedCornerShape(32.dp)).background(colors.surface).padding(6.dp),
    ) {
        tabs.forEach { (label, icon, i) ->
            val on = tab == i
            val bg by animateColorAsState(if (on) colors.accent.copy(alpha = 0.14f) else Color.Transparent, label = "nav")
            Column(
                Modifier.weight(1f).clip(RoundedCornerShape(26.dp)).background(bg).clickable { onSelect(i) }.padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(icon, null, tint = if (on) colors.accent else colors.textSecondary)
                Text(label, color = if (on) colors.accent else colors.textSecondary, fontSize = 12.sp,
                    fontWeight = if (on) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}
