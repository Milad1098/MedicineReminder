package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.room.Room
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.test.data.local.AppDatabase
import com.example.test.data.local.Medicine
import com.example.test.ui.components.AddMedicineDialog
import com.example.test.ui.components.EmptyState
import com.example.test.ui.components.HeaderSection
import com.example.test.ui.components.MedicineCard
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "medicine_db"
        ).build()

        setContent {

            CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Rtl
            ) {

                MedicineReminderApp(db)
            }
        }
    }
}

val Vazir = FontFamily(
    Font(R.font.vazirmatn_regular)
)

@Composable
fun MedicineReminderApp(db: AppDatabase) {

    val context = LocalContext.current

    val dao = db.medicineDao()

    var medicines by remember {
        mutableStateOf(listOf<Medicine>())
    }

    var showDialog by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(true) {

        medicines = dao.getAll()
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF020617),
            Color(0xFF0F172A),
            Color(0xFF111827),
            Color(0xFF1E293B)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {

        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(x = (-60).dp, y = (-20).dp)
                .blur(120.dp)
                .background(Color(0xFF22C55E).copy(alpha = 0.5f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {

            Spacer(modifier = Modifier.height(50.dp))

            HeaderSection()

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = medicines.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {

                EmptyState()
            }

            AnimatedVisibility(
                visible = medicines.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {

                LazyColumn {

                    items(medicines) { medicine ->

                        MedicineCard(medicine)

                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    item {

                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                showDialog = true
            },

            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp),

            shape = RoundedCornerShape(24.dp),

            containerColor = Color(0xFF22C55E)
        ) {

            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = Color.White
            )
        }
    }

    if (showDialog) {

        AddMedicineDialog(

            onDismiss = {
                showDialog = false
            },

            onAdd = { name, time ->

                scope.launch {

                    dao.insert(
                        Medicine(
                            name = name,
                            time = time
                        )
                    )

                    medicines = dao.getAll()

                    scheduleNotification(
                        context,
                        name
                    )
                }

                showDialog = false
            }
        )
    }
}

fun scheduleNotification(
    context: android.content.Context,
    medicineName: String
) {

    val data = Data.Builder()
        .putString("medicine", medicineName)
        .build()

    val request =
        OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(
                10,
                TimeUnit.SECONDS
            )
            .setInputData(data)
            .build()

    WorkManager
        .getInstance(context)
        .enqueue(request)
}
