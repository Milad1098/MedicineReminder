package ir.milad.medicinereminder

import android.os.Bundle
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
            bottomBar = {
                NavigationBar(containerColor = colors.surface) {
                    tabs.forEach { (label, icon, i) ->
                        NavigationBarItem(
                            selected = tab == i, onClick = { tab = i },
                            icon = { Icon(icon, null) }, label = { Text(label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.accent, selectedTextColor = colors.accent,
                                indicatorColor = colors.accent.copy(alpha = 0.12f),
                                unselectedIconColor = colors.textSecondary, unselectedTextColor = colors.textSecondary,
                            ),
                        )
                    }
                }
            },
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
