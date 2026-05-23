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
import com.example.test.ui.theme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineDialog(
    medicine:  Medicine? = null,
    onDismiss: () -> Unit,
    onAdd:     (String, String) -> Unit
) {
    val context    = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf(medicine?.name ?: "") }
    var time by remember { mutableStateOf(medicine?.time ?: "") }

    val isReady = name.isNotBlank() && time.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = Color(0xFF0C1A2E),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(Slate700, RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // عنوان
            Text(
                if (medicine == null) "افزودن دارو" else "ویرایش دارو",
                fontFamily = Vazir,
                fontWeight = FontWeight.Bold,
                fontSize   = 20.sp,
                color      = Color.White
            )

            // نام
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("نام دارو", fontFamily = Vazir,
                    fontSize = 13.sp, color = Slate400)
                OutlinedTextField(
                    value           = name,
                    onValueChange   = { name = it },
                    placeholder     = {
                        Text("مثلاً: قرص فشار",
                            fontFamily = Vazir, color = Slate700)
                    },
                    modifier        = Modifier.fillMaxWidth(),
                    shape           = RoundedCornerShape(16.dp),
                    singleLine      = true,
                    colors          = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Green500,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor     = Color.White,
                        unfocusedTextColor   = Color.White,
                        cursorColor          = Green500,
                        focusedContainerColor   = Color(0xFF101D30),
                        unfocusedContainerColor = Color(0xFF101D30)
                    )
                )
            }

            // زمان
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("زمان مصرف", fontFamily = Vazir,
                    fontSize = 13.sp, color = Slate400)
                Button(
                    onClick = {
                        val cal = Calendar.getInstance()
                        TimePickerDialog(
                            context,
                            android.R.style.Theme_Material_Dialog, // ← این تم
                            { _, h, m -> time = "%02d:%02d".format(h, m) },
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF101D30)
                    )
                ) {
                    Text(
                        if (time.isEmpty()) "انتخاب ساعت" else "🕐  $time",
                        fontFamily = Vazir,
                        fontSize   = 16.sp,
                        fontWeight = if (time.isEmpty()) FontWeight.Normal else FontWeight.Bold,
                        color      = if (time.isEmpty()) Slate500 else Blue400
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // دکمه ثبت
            Button(
                onClick  = { if (isReady) onAdd(name.trim(), time.trim()) },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape    = RoundedCornerShape(18.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = if (isReady) Green500 else Slate700
                )
            ) {
                Text(
                    if (medicine == null) "ثبت دارو" else "ذخیره تغییرات",
                    fontFamily = Vazir,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 17.sp,
                    color      = Color.White
                )
            }

            TextButton(
                onClick  = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("بستن", fontFamily = Vazir, color = Slate500)
            }
        }
    }
}
