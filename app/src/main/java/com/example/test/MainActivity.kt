package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class MedicineItem(
    val name: String,
    val time: String
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Rtl
            ) {

                MedicineReminderApp()
            }
        }
    }
}

val Vazir = FontFamily(
    Font(R.font.vazirmatn_regular)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineReminderApp() {

    var medicines by remember {
        mutableStateOf(listOf<MedicineItem>())
    }

    var showBottomSheet by remember {
        mutableStateOf(false)
    }

    val sheetState = rememberModalBottomSheetState()

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF020617),
            Color(0xFF0F172A),
            Color(0xFF111827)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {

            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "یادآور دارو",
                color = Color.White,
                fontFamily = Vazir,
                fontSize = 34.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "داروهایت را هوشمند مدیریت کن",
                color = Color(0xFF94A3B8),
                fontFamily = Vazir,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            AnimatedVisibility(
                visible = medicines.isEmpty()
            ) {

                EmptyState()
            }

            LazyColumn {

                items(medicines) { medicine ->

                    Spacer(modifier = Modifier.height(16.dp))

                    MedicineCard(
                        name = medicine.name,
                        time = medicine.time
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }

        FloatingActionButton(
            onClick = {
                showBottomSheet = true
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp),
            containerColor = Color(0xFF22C55E)
        ) {

            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = Color.White
            )
        }

        if (showBottomSheet) {

            AddMedicineBottomSheet(
                sheetState = sheetState,
                onDismiss = {
                    showBottomSheet = false
                },
                onAddMedicine = { name, time ->

                    medicines = medicines + MedicineItem(
                        name,
                        time
                    )

                    showBottomSheet = false
                }
            )
        }
    }
}

@Composable
fun EmptyState() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            Icons.Default.Medication,
            contentDescription = null,
            tint = Color(0xFF334155),
            modifier = Modifier.size(90.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "هنوز دارویی ثبت نشده",
            color = Color.White,
            fontFamily = Vazir,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "برای افزودن دارو روی دکمه + بزن",
            color = Color(0xFF94A3B8),
            fontFamily = Vazir,
            fontSize = 14.sp
        )
    }
}

@Composable
fun MedicineCard(
    name: String,
    time: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xAA1E293B)
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
                    .size(64.dp)
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

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = name,
                    color = Color.White,
                    fontFamily = Vazir,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = time,
                        color = Color(0xFF94A3B8),
                        fontFamily = Vazir
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onAddMedicine: (String, String) -> Unit
) {

    var medicineName by remember {
        mutableStateOf("")
    }

    var medicineTime by remember {
        mutableStateOf("")
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {

            Text(
                text = "افزودن دارو",
                color = Color.White,
                fontFamily = Vazir,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = medicineName,
                onValueChange = {
                    medicineName = it
                },
                label = {
                    Text(
                        "نام دارو",
                        fontFamily = Vazir
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = medicineTime,
                onValueChange = {
                    medicineTime = it
                },
                label = {
                    Text(
                        "زمان مصرف",
                        fontFamily = Vazir
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {

                    if (
                        medicineName.isNotBlank() &&
                        medicineTime.isNotBlank()
                    ) {

                        onAddMedicine(
                            medicineName,
                            medicineTime
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF22C55E)
                ),
                shape = RoundedCornerShape(22.dp)
            ) {

                Text(
                    text = "ثبت دارو",
                    fontFamily = Vazir,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
