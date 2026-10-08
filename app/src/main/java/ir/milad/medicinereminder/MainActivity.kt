package ir.milad.medicinereminder

import android.os.Bundle
import ir.milad.medicinereminder.ui.components.liquidBar
import ir.milad.medicinereminder.ui.components.glass
import ir.milad.medicinereminder.ui.components.MeshBackground
import ir.milad.medicinereminder.ui.components.LocalHaze
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.HazeState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
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
    // null until Room answers: avoids flashing the empty state on launch
    val loaded by remember { dao.observeAll() }.collectAsState(null)
    val meds = loaded.orEmpty()
    val from = remember { LocalDate.now().minusDays(HISTORY_DAYS).atStartOfDay().toMillis() }
    val logs by remember { dao.observeLogs(from, Long.MAX_VALUE) }.collectAsState(emptyList())

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var editing by rememberSaveable { mutableStateOf<Long?>(null) }
    BackHandler(editing != null) { editing = null }

    val haze = remember { HazeState() }
    Box(Modifier.fillMaxSize().background(colors.background)) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = { FloatingNav(tab, haze) { tab = it } },
        ) { padding ->
            // Everything in here is the "content layer" the glass bars blur.
            Box(Modifier.fillMaxSize().hazeSource(haze)) {
                MeshBackground()
                CompositionLocalProvider(LocalHaze provides haze) {
                    if (loaded != null) AnimatedContent(tab, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "tab") { t ->
                        when (t) {
                            0 -> TodayScreen(meds, logs, onAdd = { editing = 0 }, padding)
                            1 -> MedicinesScreen(meds, onOpen = { editing = it }, padding)
                            else -> HistoryScreen(meds, logs, padding)
                        }
                    }
                }
            }
        }
        // Glass status bar: content scrolls under it blurred, like iOS.
        val c = colors
        Box(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars).hazeEffect(haze) {
            blurRadius = 20.dp; backgroundColor = c.background; tints = listOf(HazeTint(c.barTint))
        })

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

/** Liquid Glass floating tab bar with a sliding glass "droplet" under the selected tab. */
@Composable
private fun FloatingNav(tab: Int, haze: HazeState, onSelect: (Int) -> Unit) = Box(
    Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 36.dp, vertical = 10.dp),
    contentAlignment = Alignment.Center,
) {
    BoxWithConstraints(Modifier.fillMaxWidth().liquidBar(RoundedCornerShape(34.dp), haze).padding(6.dp)) {
        val itemW = maxWidth / tabs.size
        val x by animateDpAsState(itemW * tab, spring(dampingRatio = 0.72f, stiffness = 380f), label = "drop")
        Box(
            Modifier.offset(x = x).width(itemW).height(58.dp)
                .glass(RoundedCornerShape(28.dp), elevation = 0.dp, fill = colors.accent.copy(alpha = 0.16f)),
        )
        Row(Modifier.fillMaxWidth()) {
            tabs.forEach { (label, icon, i) ->
                val on = tab == i
                val tint by animateColorAsState(if (on) colors.accent else colors.textSecondary, label = "navc")
                Column(
                    Modifier.weight(1f).height(58.dp).clip(RoundedCornerShape(28.dp)).clickable { onSelect(i) },
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                ) {
                    Icon(icon, null, tint = tint)
                    Text(label, color = tint, fontSize = 12.sp, fontWeight = if (on) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}
