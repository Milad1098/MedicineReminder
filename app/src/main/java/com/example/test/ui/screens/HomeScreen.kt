package com.example.test.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.test.alarm.AlarmScheduler
import com.example.test.data.local.AppDatabase
import com.example.test.data.local.Medicine
import com.example.test.ui.components.AddMedicineBottomSheet
import com.example.test.ui.components.EmptyState
import com.example.test.ui.components.HeaderSection
import com.example.test.ui.components.MedicineCard
import com.example.test.ui.theme.Vazir
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    db: AppDatabase
) {

    val context = LocalContext.current

    val dao = db.medicineDao()

    val scope = rememberCoroutineScope()

    var medicines by remember {
        mutableStateOf<List<Medicine>>(emptyList())
    }

    var showBottomSheet by remember {
        mutableStateOf(false)
    }

    var editingMedicine by remember {
        mutableStateOf<Medicine?>(null)
    }

    LaunchedEffect(Unit) {
        medicines = dao.getAll()
    }

    val backgroundGradient =
        Brush.verticalGradient(
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
            .padding(horizontal = 20.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Spacer(
                modifier = Modifier.height(48.dp)
            )

            HeaderSection()

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            if (medicines.isEmpty()) {

                EmptyState(
                    fontFamily = Vazir
                )

            } else {

                LazyColumn(
                    verticalArrangement =
                        Arrangement.spacedBy(14.dp),

                    contentPadding =
                        PaddingValues(bottom = 120.dp)
                ) {

                    items(
                        medicines,
                        key = { it.id }
                    ) { medicine ->

                        MedicineCard(

                            medicine = medicine,

                            fontFamily = Vazir,

                            onDelete = {

                                scope.launch {

                                    AlarmScheduler.cancel(
                                        context = context,
                                        medicineId = medicine.id
                                    )

                                    dao.delete(medicine)

                                    medicines =
                                        dao.getAll()
                                }
                            },

                            onEdit = {

                                editingMedicine = medicine
                                showBottomSheet = true
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(

            onClick = {

                editingMedicine = null
                showBottomSheet = true
            },

            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = 6.dp,
                    bottom = 24.dp
                ),

            containerColor = Color(0xFF22C55E),
            contentColor = Color.White

        ) {

            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
        }
    }

    if (showBottomSheet) {

        AddMedicineBottomSheet(

            medicine = editingMedicine,

            onDismiss = {

                showBottomSheet = false
            },

            onSave = { name, time, hour, minute ->

                scope.launch {

                    try {

                        if (editingMedicine == null) {

                            val insertedId =
                                dao.insert(
                                    Medicine(
                                        name = name.trim(),
                                        time = time.trim()
                                    )
                                ).toInt()

                            AlarmScheduler.schedule(
                                context = context,
                                medicineId = insertedId,
                                medicineName = name,
                                hour = hour,
                                minute = minute
                            )

                        } else {

                            val updatedMedicine =
                                editingMedicine!!.copy(
                                    name = name.trim(),
                                    time = time.trim()
                                )

                            dao.update(updatedMedicine)

                            AlarmScheduler.cancel(
                                context = context,
                                medicineId = updatedMedicine.id
                            )

                            AlarmScheduler.schedule(
                                context = context,
                                medicineId = updatedMedicine.id,
                                medicineName = updatedMedicine.name,
                                hour = hour,
                                minute = minute
                            )
                        }

                        medicines = dao.getAll()

                    } catch (e: Exception) {

                        e.printStackTrace()
                    }
                }

                showBottomSheet = false
            }
        )
    }
}
