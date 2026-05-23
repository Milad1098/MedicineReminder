package com.example.test.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {

    val context   = LocalContext.current
    val dao       = remember { AppDatabase.getDatabase(context).medicineDao() }
    val medicines by dao.getAllMedicines().collectAsState(initial = emptyList())
    val scope     = rememberCoroutineScope()

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
                        0.5f to Color(0xFF080F1E),
                        1.0f to Color(0xFF050C17)
                    )
                )
            )
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 18.dp, end = 18.dp,
                top = 52.dp, bottom = 130.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            item {
                HeaderCard(count = medicines.size)
                Spacer(Modifier.height(4.dp))
            }

            if (medicines.isEmpty()) {
                item { EmptyCard() }
            } else {
                itemsIndexed(medicines, key = { _, m -> m.id }) { index, medicine ->
                    val visible = remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(index * 60L)
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

        // ── FAB — مرکز دقیق ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 38.dp)
                .wrapContentSize(),
            contentAlignment = Alignment.Center
        ) {
            // دایره halo — دقیقاً وسط FAB
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .background(Green500.copy(alpha = 0.13f), CircleShape)
            )
            FloatingActionButton(
                onClick        = { editingMedicine = null; showDialog = true },
                modifier       = Modifier.size(56.dp),
                shape          = CircleShape,
                containerColor = Green500,
                contentColor   = Color.White,
                elevation      = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(26.dp))
            }
        }

        // ── دیالوگ افزودن/ویرایش ─────────────────────────────────────────────
        if (showDialog) {
            AddMedicineDialog(
                medicine  = editingMedicine,
                onDismiss = { showDialog = false; editingMedicine = null },
                onAdd     = { name, time, cycleHours ->
                    scope.launch {
                        if (editingMedicine == null) {
                            val m  = Medicine(
                                name       = name,
                                time       = time,
                                cycleHours = cycleHours
                            )
                            val id = dao.insert(m)
                            AlarmScheduler.scheduleAlarm(context, m.copy(id = id.toInt()))
                        } else {
                            val u = editingMedicine!!.copy(
                                name       = name,
                                time       = time,
                                cycleHours = cycleHours
                            )
                            dao.update(u)
                            AlarmScheduler.cancelAlarm(context, u)
                            AlarmScheduler.scheduleAlarm(context, u)
                        }
                        showDialog = false
                        editingMedicine = null
                    }
                }
            )
        }

        // ── دیالوگ حذف ───────────────────────────────────────────────────────
        deleteMedicine?.let { m ->
            AlertDialog(
                onDismissRequest  = { deleteMedicine = null },
                containerColor    = Color(0xFF0B1929),
                titleContentColor = Color.White,
                textContentColor  = Slate400,
                shape             = RoundedCornerShape(24.dp),
                title = {
                    Text(
                        "حذف دارو",
                        fontFamily = Vazir,
                        fontWeight = FontWeight.Bold
                    )
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
                                AlarmScheduler.cancelAlarm(context, m)
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

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun HeaderCard(count: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Color(0xFF0A1628))
            .border(1.dp, Color(0xFF1A3050), RoundedCornerShape(26.dp))
            .padding(22.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Green500.copy(alpha = 0.15f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(70.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Blue400.copy(alpha = 0.1f), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        Column {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        "یادآور دارو",
                        fontFamily = Vazir,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 24.sp,
                        color      = Color.White
                    )
                    Text(
                        "مدیریت هوشمند داروها",
                        fontFamily = Vazir,
                        fontSize   = 13.sp,
                        color      = Slate500
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Green500.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        .border(1.dp, Green500.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Medication, null,
                        tint     = Green400,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            if (count > 0) {
                Spacer(Modifier.height(18.dp))
                HorizontalDivider(color = Color(0xFF1A3050), thickness = 0.5.dp)
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(value = "$count", label = "داروها",        color = Color.White)
                    StatDivider()
                    StatItem(value = "فعال",   label = "آلارم‌ها",      color = Green400)
                    StatDivider()
                    StatItem(value = "یادآور", label = "۱۰ دقیقه قبل", color = Blue400)
                }
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontFamily = Vazir, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
        Text(label, fontFamily = Vazir, fontSize = 11.sp, color = Slate500)
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .height(28.dp)
            .width(0.5.dp)
            .background(Color(0xFF1A3050))
    )
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyCard() {
    Box(
        modifier         = Modifier.fillMaxWidth().padding(top = 60.dp),
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
                        Brush.radialGradient(
                            colors = listOf(Green500.copy(alpha = 0.15f), Color.Transparent)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0xFF0A1628), CircleShape)
                        .border(1.dp, Color(0xFF1A3050), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Medication, null,
                        tint     = Green400,
                        modifier = Modifier.size(34.dp)
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
    val cycleLabel = when (medicine.cycleHours) {
        0    -> "روزانه"
        4    -> "هر ۴ ساعت"
        6    -> "هر ۶ ساعت"
        8    -> "هر ۸ ساعت"
        12   -> "هر ۱۲ ساعت"
        else -> "هر ${medicine.cycleHours} ساعت"
    }
    val isCyclic = medicine.cycleHours > 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0A1628))
            .border(1.dp, Color(0xFF1A3050), RoundedCornerShape(20.dp))
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(3.dp)
                .height(50.dp)
                .clip(RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (isCyclic) Blue400 else Green400,
                            if (isCyclic) Blue400.copy(alpha = 0.3f) else Green400.copy(alpha = 0.3f)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            if (isCyclic) Blue400.copy(alpha = 0.1f) else Green500.copy(alpha = 0.1f),
                            RoundedCornerShape(14.dp)
                        )
                        .border(
                            1.dp,
                            if (isCyclic) Blue400.copy(alpha = 0.2f) else Green500.copy(alpha = 0.2f),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Medication, null,
                        tint     = if (isCyclic) Blue400 else Green400,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        medicine.name,
                        fontFamily = Vazir, fontWeight = FontWeight.Bold,
                        fontSize = 16.sp, color = Color.White
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(Blue400, CircleShape)
                            )
                            Text(
                                medicine.time,
                                fontFamily = Vazir, fontSize = 13.sp,
                                fontWeight = FontWeight.Bold, color = Blue400
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    if (isCyclic) Blue400.copy(alpha = 0.1f)
                                    else Green500.copy(alpha = 0.08f),
                                    RoundedCornerShape(6.dp)
                                )
                                .border(
                                    0.5.dp,
                                    if (isCyclic) Blue400.copy(alpha = 0.2f)
                                    else Green500.copy(alpha = 0.15f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                cycleLabel,
                                fontFamily = Vazir, fontSize = 11.sp,
                                color = if (isCyclic) Blue400 else Green400
                            )
                        }
                    }
                }
            }

            // ── دکمه‌ها با فاصله درست ──────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp), // ← فاصله اضافه شد
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)                                  // ← کمی بزرگتر
                        .background(Blue400.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick  = onEdit,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit, null,
                            tint     = Blue400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Red400.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick  = onDelete,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete, null,
                            tint     = Red400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
