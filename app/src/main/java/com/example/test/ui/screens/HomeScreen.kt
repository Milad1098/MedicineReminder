package com.example.test.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.test.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {

    val context = LocalContext.current
    val dao     = remember { AppDatabase.getDatabase(context).medicineDao() }
    val medicines by dao.getAllMedicines().collectAsState(initial = emptyList())
    val scope   = rememberCoroutineScope()

    var showDialog      by remember { mutableStateOf(false) }
    var editingMedicine by remember { mutableStateOf<Medicine?>(null) }
    var deleteMedicine  by remember { mutableStateOf<Medicine?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to Color(0xFF060D1A),
                        0.4f to Color(0xFF0A1628),
                        1.0f to Color(0xFF050C18)
                    )
                )
            )
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 22.dp, end = 22.dp,
                top = 56.dp, bottom = 130.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── هدر ──────────────────────────────
            item {
                HeaderCard(count = medicines.size)
                Spacer(Modifier.height(8.dp))
            }

            // ── empty state ───────────────────────
            if (medicines.isEmpty()) {
                item { EmptyCard() }
            } else {
                itemsIndexed(medicines, key = { _, m -> m.id }) { index, medicine ->
                    val visible = remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(index * 60L)
                        visible.value = true
                    }
                    AnimatedVisibility(
                        visible = visible.value,
                        enter = fadeIn(tween(300)) +
                                slideInVertically(tween(300)) { it / 2 }
                    ) {
                        MedicineCard(
                            medicine = medicine,
                            onEdit   = { editingMedicine = medicine; showDialog = true },
                            onDelete = { deleteMedicine = medicine }
                        )
                    }
                }
            }
        }

        // ── FAB ───────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        ) {
            // halo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.Center)
                    .background(
                        Green500.copy(alpha = 0.18f),
                        CircleShape
                    )
            )
            FloatingActionButton(
                onClick = { editingMedicine = null; showDialog = true },
                modifier       = Modifier.size(60.dp),
                shape          = CircleShape,
                containerColor = Green500,
                contentColor   = Color.White,
                elevation      = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(26.dp))
            }
        }

        // ── دیالوگ افزودن/ویرایش ─────────────────
        if (showDialog) {
            AddMedicineDialog(
                medicine  = editingMedicine,
                onDismiss = { showDialog = false; editingMedicine = null },
                onAdd     = { name, time ->
                    scope.launch {
                        if (editingMedicine == null) {
                            val m  = Medicine(name = name, time = time)
                            val id = dao.insert(m)
                            AlarmScheduler.scheduleAlarm(context, m.copy(id = id.toInt()))
                        } else {
                            val u = editingMedicine!!.copy(name = name, time = time)
                            dao.update(u)
                            AlarmScheduler.cancelAlarm(context, u.id)
                            AlarmScheduler.scheduleAlarm(context, u)
                        }
                        showDialog = false; editingMedicine = null
                    }
                }
            )
        }

        // ── دیالوگ حذف ───────────────────────────
        deleteMedicine?.let { m ->
            AlertDialog(
                onDismissRequest = { deleteMedicine = null },
                containerColor   = Color(0xFF111D30),
                titleContentColor = Color.White,
                textContentColor  = Slate400,
                shape = RoundedCornerShape(24.dp),
                title = {
                    Text("حذف دارو", fontFamily = Vazir, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(
                        "«${m.name}» از لیست حذف شود؟",
                        fontFamily = Vazir
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                dao.delete(m)
                                AlarmScheduler.cancelAlarm(context, m.id)
                                deleteMedicine = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Red400),
                        shape  = RoundedCornerShape(14.dp)
                    ) {
                        Text("حذف", fontFamily = Vazir, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteMedicine = null }) {
                        Text("لغو", fontFamily = Vazir, color = Slate400)
                    }
                }
            )
        }
    }
}

// ── Header ───────────────────────────────────────────────────────────────────

@Composable
private fun HeaderCard(count: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF0E2240), Color(0xFF112B50))
                )
            )
            .padding(24.dp)
    ) {
        // نوار سبز تزئینی
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(90.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Green500.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Green500.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Medication, null,
                        tint = Green400,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        "یادآور دارو",
                        fontFamily     = Vazir,
                        fontWeight     = FontWeight.Bold,
                        fontSize       = 22.sp,
                        color          = Color.White
                    )
                    Text(
                        "مدیریت هوشمند داروها",
                        fontFamily = Vazir,
                        fontSize   = 13.sp,
                        color      = Slate400
                    )
                }
            }

            if (count > 0) {
                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = Slate700.copy(alpha = 0.6f), thickness = 0.5.dp)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    StatChip(label = "داروها", value = "$count")
                    StatChip(label = "آلارم‌ها", value = "فعال", valueColor = Green400)
                    StatChip(label = "یادآور", value = "۱۰ دقیقه", valueColor = Blue400)
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, valueColor: Color = Color.White) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontFamily = Vazir, fontWeight = FontWeight.Bold,
            fontSize = 16.sp, color = valueColor)
        Text(label, fontFamily = Vazir, fontSize = 11.sp, color = Slate500)
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Green500.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0xFF0E2240), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Medication, null,
                        tint = Green400,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            Text(
                "هنوز دارویی ثبت نشده",
                fontFamily = Vazir, fontWeight = FontWeight.Bold,
                fontSize = 20.sp, color = Color.White
            )
            Text(
                "روی دکمه + بزن تا اولین دارو رو اضافه کنی",
                fontFamily = Vazir, fontSize = 14.sp,
                color = Slate500, textAlign = TextAlign.Center
            )
        }
    }
}

// ── Medicine Card ─────────────────────────────────────────────────────────────

@Composable
private fun MedicineCard(
    medicine: Medicine,
    onEdit:   () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF101D30))
    ) {
        // نوار رنگی چپ
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(3.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Green400, Green600)
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // آیکون
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(Green500.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Medication, null,
                        tint = Green400,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        medicine.name,
                        fontFamily = Vazir,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp,
                        color      = Color.White
                    )
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Blue400, CircleShape)
                        )
                        Text(
                            medicine.time,
                            fontFamily = Vazir,
                            fontSize   = 13.sp,
                            color      = Blue400,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // دکمه‌ها
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick  = onEdit,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Blue400.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.Edit, null,
                        tint = Blue400, modifier = Modifier.size(17.dp))
                }
                IconButton(
                    onClick  = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Red400.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.Delete, null,
                        tint = Red400, modifier = Modifier.size(17.dp))
                }
            }
        }
    }
}
