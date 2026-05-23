package com.example.test.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.data.local.Medicine
import com.example.test.ui.theme.Vazir
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineDialog(
    medicine: Medicine? = null,
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf(medicine?.name ?: "") }
    var time by remember { mutableStateOf(medicine?.time ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F1F35),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(Color(0xFF334155), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = if (medicine == null) "افزودن دارو جدید" else "ویرایش دارو",
                fontFamily = Vazir,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // نام دارو
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "نام دارو",
                    fontFamily = Vazir,
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = {
                        Text(
                            "مثلاً: قرص فشار خون",
                            fontFamily = Vazir,
                            color = Color(0xFF475569)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF22C55E),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF22C55E),
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B)
                    )
                )
            }

            // زمان
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "زمان مصرف",
                    fontFamily = Vazir,
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )

                Button(
                    onClick = {
                        val cal = Calendar.getInstance()
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                time = String.format("%02d:%02d", hour, minute)
                            },
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B)
                    )
                ) {
                    Text(
                        text = if (time.isEmpty()) "انتخاب ساعت" else "⏰  $time",
                        fontFamily = Vazir,
                        color = if (time.isEmpty()) Color(0xFF64748B) else Color(0xFF38BDF8),
                        fontSize = 16.sp,
                        fontWeight = if (time.isEmpty()) FontWeight.Normal else FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // دکمه ثبت
            Button(
                onClick = {
                    if (name.isNotBlank() && time.isNotBlank()) {
                        onAdd(name.trim(), time.trim())
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (name.isNotBlank() && time.isNotBlank())
                        Color(0xFF22C55E) else Color(0xFF1E293B)
                )
            ) {
                Text(
                    text = if (medicine == null) "ثبت دارو" else "ذخیره تغییرات",
                    fontFamily = Vazir,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("بستن", fontFamily = Vazir, color = Color(0xFF64748B))
            }
        }
    }
}
