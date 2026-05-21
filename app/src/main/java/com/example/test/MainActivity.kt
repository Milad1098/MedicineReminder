package com.example.test

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.*
import kotlinx.coroutines.launch
import androidx.work.*
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

@Entity
data class Medicine(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val time: String
)

@Dao
interface MedicineDao {

    @Query("SELECT * FROM Medicine")
    suspend fun getAll(): List<Medicine>

    @Insert
    suspend fun insert(medicine: Medicine)
}

@Database(
    entities = [Medicine::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao
}

@Composable
fun MedicineReminderApp(db: AppDatabase) {

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
            Color(0xFF020617),
            Color(0xFF0F172A),
            Color(0xFF111827)
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

            Text(
                text = "یادآور دارو",
                color = Color.White,
                fontFamily = Vazir,
                fontSize = 34.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "داروهایت را هوشمند مدیریت کن",
                color = Color(0xFF94A3B8),
                fontFamily = Vazir,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            if (medicines.isEmpty()) {

                EmptyState()

            } else {

                LazyColumn {

                    items(medicines) {

                        MedicineCard(it)

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
                .padding(10.dp),
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

        Text(
            text = "هنوز دارویی اضافه نشده",
            color = Color.White,
            fontFamily = Vazir,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "روی دکمه + بزن",
            color = Color.Gray,
            fontFamily = Vazir
        )
    }
}

@Composable
fun MedicineCard(medicine: Medicine) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B)
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

        confirmButton = {

            Button(
                onClick = {
                    onAdd(name, time)
                }
            ) {

                Text(
                    "ثبت",
                    fontFamily = Vazir
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text(
                    "لغو",
                    fontFamily = Vazir
                )
            }
        },

        title = {

            Text(
                text = "افزودن دارو",
                fontFamily = Vazir
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
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

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
                    }
                )
            }
        }
    )
}
