package com.example.test.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

import com.example.test.data.local.AppDatabase
import com.example.test.data.local.Medicine
import com.example.test.receiver.AlarmScheduler
import com.example.test.ui.components.AddMedicineDialog
import com.example.test.ui.components.EmptyState
import com.example.test.ui.components.HeaderSection
import com.example.test.ui.components.MedicineCard
import com.example.test.ui.theme.Vazir

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {

    val context = LocalContext.current

    val dao = remember {
        AppDatabase
            .getDatabase(context)
            .medicineDao()
    }

    val medicines by dao
        .getAllMedicines()
        .collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()

    var showDialog by remember {
        mutableStateOf(false)
    }

    var editingMedicine by remember {
        mutableStateOf<Medicine?>(null)
    }

    var deleteMedicine by remember {
        mutableStateOf<Medicine?>(null)
    }

    Scaffold(

        floatingActionButton = {

            FloatingActionButton(

                onClick = {

                    editingMedicine = null
                    showDialog = true
                }

            ) {

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            }
        }

    ) { padding ->

        Box(

            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF111827),
                            Color(0xFF1E293B)
                        )
                    )
                )
                .padding(padding)

        ) {

            LazyColumn(

                modifier = Modifier
                    .fillMaxSize(),

                verticalArrangement = Arrangement.spacedBy(12.dp),

                contentPadding = PaddingValues(16.dp)

            ) {

                item {

                    HeaderSection()
                }

                item {

                    Spacer(
                        modifier = Modifier
                            .height(8.dp)
                    )
                }

                if (medicines.isEmpty()) {

                    item {

                        EmptyState()
                    }

                } else {

                    items(medicines) { medicine ->

                        MedicineCard(

                            medicine = medicine,

                            onEdit = {

                                editingMedicine = medicine
                                showDialog = true
                            },

                            onDelete = {

                                deleteMedicine = medicine
                            }
                        )
                    }
                }

                item {

                    Spacer(
                        modifier = Modifier
                            .height(100.dp)
                    )
                }
            }

            if (showDialog) {

                AddMedicineDialog(

                    medicine = editingMedicine,

                    onDismiss = {

                        showDialog = false
                        editingMedicine = null
                    },

                    onAdd = { name, time ->

                        scope.launch {

                            if (editingMedicine == null) {

                                val medicine = Medicine(
                                    name = name,
                                    time = time
                                )

                                val id =
                                    dao.insert(medicine)

                                AlarmScheduler.scheduleAlarm(
                                    context,
                                    medicine.copy(
                                        id = id.toInt()
                                    )
                                )

                            } else {

                                val updatedMedicine =
                                    editingMedicine!!.copy(
                                        name = name,
                                        time = time
                                    )

                                dao.update(updatedMedicine)

                                AlarmScheduler.cancelAlarm(
                                    context,
                                    updatedMedicine.id
                                )

                                AlarmScheduler.scheduleAlarm(
                                    context,
                                    updatedMedicine
                                )
                            }

                            showDialog = false
                            editingMedicine = null
                        }
                    }
                )
            }

            deleteMedicine?.let { medicine ->

                AlertDialog(

                    onDismissRequest = {
                        deleteMedicine = null
                    },

                    title = {
                        Text(
                            text = "حذف دارو",
                            fontFamily = Vazir
                        )
                    },

                    text = {
                        Text(
                            text = "آیا مطمئن هستید؟",
                            fontFamily = Vazir
                        )
                    },

                    confirmButton = {

                        Button(

                            onClick = {

                                scope.launch {

                                    dao.delete(medicine)

                                    AlarmScheduler.cancelAlarm(
                                        context,
                                        medicine.id
                                    )

                                    deleteMedicine = null
                                }
                            }

                        ) {

                            Text(
                                text = "حذف",
                                fontFamily = Vazir,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },

                    dismissButton = {

                        TextButton(

                            onClick = {
                                deleteMedicine = null
                            }

                        ) {

                            Text(
                                text = "لغو",
                                fontFamily = Vazir
                            )
                        }
                    }
                )
            }
        }
    }
}
