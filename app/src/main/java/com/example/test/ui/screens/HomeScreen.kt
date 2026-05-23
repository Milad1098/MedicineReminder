package com.example.test.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.data.local.AppDatabase
import com.example.test.data.local.Medicine
import com.example.test.receiver.AlarmScheduler
import com.example.test.ui.components.AddMedicineDialog
import com.example.test.ui.theme.Vazir
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {

    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).medicineDao() }
    val medicines by dao.getAllMedicines().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    var editingMedicine by remember { mutableStateOf<Medicine?>(null) }
    var deleteMedicine by remember { mutableStateOf<Medicine?>(null) }
    // موقتی برای تست - بعداً حذف کن
    var showTest by remember { mutableStateOf(false) }
        if (showTest) {
            TestScreen()
            return
        }
Button(onClick = { showTest = true }) { Text("باز کردن صفحه تست") }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF060B18),
                        Color(0xFF0A1628),
                        Color(0xFF0F1F35)
                    )
                )
            )
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 60.dp,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // هدر
            item {
                Column(modifier = Modifier.fillMaxWidth()) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "یادآور دارو",
                                fontFamily = Vazir,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "مدیریت هوشمند داروها",
                                fontFamily = Vazir,
                                fontSize = 14.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(
                                    Color(0xFF22C55E).copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💊", fontSize = 24.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // آمار
                    if (medicines.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF166534).copy(alpha = 0.6f),
                                            Color(0xFF14532D).copy(alpha = 0.4f)
                                        )
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("📋", fontSize = 20.sp)
                                Column {
                                    Text(
                                        text = "${medicines.size} دارو ثبت شده",
                                        fontFamily = Vazir,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF86EFAC),
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "آلارم‌ها فعال هستند",
                                        fontFamily = Vazir,
                                        color = Color(0xFF4ADE80).copy(alpha = 0.7f),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            if (medicines.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(
                                        Color(0xFF1E293B),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Medication,
                                    contentDescription = null,
                                    tint = Color(0xFF22C55E),
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            Text(
                                text = "هنوز دارویی ثبت نشده",
                                fontFamily = Vazir,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "روی دکمه + بزن تا اولین دارو رو اضافه کنی",
                                fontFamily = Vazir,
                                fontSize = 14.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(medicines, key = { it.id }) { medicine ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        MedicineCardNew(
                            medicine = medicine,
                            onEdit = {
                                editingMedicine = medicine
                                showDialog = true
                            },
                            onDelete = { deleteMedicine = medicine }
                        )
                    }
                }
            }
        }

        // دکمه افزودن
        FloatingActionButton(
            onClick = {
                editingMedicine = null
                showDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
                .size(64.dp),
            shape = CircleShape,
            containerColor = Color(0xFF22C55E),
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(12.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
        }

        // دیالوگ افزودن/ویرایش
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
                            val medicine = Medicine(name = name, time = time)
                            val id = dao.insert(medicine)
                            AlarmScheduler.scheduleAlarm(context, medicine.copy(id = id.toInt()))
                        } else {
                            val updated = editingMedicine!!.copy(name = name, time = time)
                            dao.update(updated)
                            AlarmScheduler.cancelAlarm(context, updated.id)
                            AlarmScheduler.scheduleAlarm(context, updated)
                        }
                        showDialog = false
                        editingMedicine = null
                    }
                }
            )
        }

        // دیالوگ حذف
        deleteMedicine?.let { medicine ->
            AlertDialog(
                onDismissRequest = { deleteMedicine = null },
                containerColor = Color(0xFF1E293B),
                titleContentColor = Color.White,
                textContentColor = Color(0xFFCBD5E1),
                title = {
                    Text(
                        text = "حذف دارو",
                        fontFamily = Vazir,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "«${medicine.name}» حذف شود؟",
                        fontFamily = Vazir
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                dao.delete(medicine)
                                AlarmScheduler.cancelAlarm(context, medicine.id)
                                deleteMedicine = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("حذف", fontFamily = Vazir, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteMedicine = null }) {
                        Text("لغو", fontFamily = Vazir, color = Color(0xFF94A3B8))
                    }
                }
            )
        }
    }
}

@Composable
fun MedicineCardNew(
    medicine: Medicine,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF1A2744),
                        Color(0xFF1E2D4A)
                    )
                )
            )
    ) {
        // نوار سبز چپ
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(4.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                .background(Color(0xFF22C55E))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            Color(0xFF22C55E).copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💊", fontSize = 20.sp)
                }

                Column {
                    Text(
                        text = medicine.name,
                        fontFamily = Vazir,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("🕐", fontSize = 12.sp)
                        Text(
                            text = medicine.time,
                            fontFamily = Vazir,
                            fontSize = 13.sp,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            Color(0xFF38BDF8).copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            Color(0xFFEF4444).copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
