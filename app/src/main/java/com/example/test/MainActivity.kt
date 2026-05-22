package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.*
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import com.example.test.data.local.AppDatabase
import com.example.test.data.local.Medicine

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

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF030712),
            Color(0xFF111827),
            Color(0xFF1E1B4B)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(20.dp)
    ) {

        Column {

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {

                    Text(
                        text = "یادآور دارو",
                        color = Color.White,
                        fontFamily = Vazir,
                        fontSize = 34.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "مدیریت هوشمند داروها",
                        color = Color(0xFF94A3B8),
                        fontFamily = Vazir
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0x22FFFFFF)
                ) {

                    Text(
                        text = "💊",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            if (medicines.isEmpty()) {

                EmptyState()

            } else {

                LazyColumn {

                    items(medicines) { medicine ->

                        MedicineCard(medicine)

                        Spacer(modifier = Modifier.height(16.dp))
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
                .padding(10.dp)
                .animateContentSize(),

            shape = RoundedCornerShape(22.dp),

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

@Composable
fun EmptyState() {

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(80.dp))

        Icon(
            Icons.Default.Medication,
            contentDescription = null,
            tint = Color(0xFF22C55E),
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "هنوز دارویی اضافه نشده",
            color = Color.White,
            fontFamily = Vazir,
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "روی دکمه + بزن",
            color = Color(0xFF94A3B8),
            fontFamily = Vazir
        )
    }
}

@Composable
fun MedicineCard(medicine: Medicine) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp)),

        shape = RoundedCornerShape(28.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color(0x33FFFFFF)
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(65.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF22C55E)),

                contentAlignment = Alignment.Center
            ) {

                Icon(
                    Icons.Default.Medication,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(18.dp))

            Column {

                Text(
                    text = medicine.name,
                    color = Color.White,
                    fontFamily = Vazir,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = medicine.time,
                    color = Color(0xFF94A3B8),
                    fontFamily = Vazir
                )
            }
        }
    }
}

@Composable
fun AddMedicineDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {

    var name by remember {
        mutableStateOf("")
    }

    var time by remember {
        mutableStateOf("")
    }

    AlertDialog(

        onDismissRequest = onDismiss,

        containerColor = Color(0xFF1E293B),

        shape = RoundedCornerShape(28.dp),

        title = {

            Text(
                text = "افزودن دارو",
                fontFamily = Vazir,
                color = Color.White
            )
        },

        text = {

            Column {

                OutlinedTextField(
                    value = name,

                    onValueChange = {
                        name = it
                    },

                    label = {
                        Text(
                            "نام دارو",
                            fontFamily = Vazir
                        )
                    },

                    shape = RoundedCornerShape(18.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = time,

                    onValueChange = {
                        time = it
                    },

                    label = {
                        Text(
                            "زمان مصرف",
                            fontFamily = Vazir
                        )
                    },

                    shape = RoundedCornerShape(18.dp)
                )
            }
        },

        confirmButton = {

            Button(
                onClick = {

                    if (
                        name.isNotBlank() &&
                        time.isNotBlank()
                    ) {

                        onAdd(name, time)
                    }
                },

                shape = RoundedCornerShape(16.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF22C55E)
                )
            ) {

                Text(
                    text = "ثبت",
                    fontFamily = Vazir
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text(
                    text = "لغو",
                    fontFamily = Vazir,
                    color = Color.White
                )
            }
        }
    )
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
