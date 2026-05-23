package com.example.test.ui.components

import android.app.TimePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.data.local.Medicine
import com.example.test.ui.theme.*
import java.util.Calendar

private val cycles = listOf(
    0  to "روزانه یک‌بار",
    4  to "هر ۴ ساعت",
    6  to "هر ۶ ساعت",
    8  to "هر ۸ ساعت",
    12 to "هر ۱۲ ساعت"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineDialog(
    medicine:  Medicine? = null,
    onDismiss: () -> Unit,
    onAdd:     (String, String, Int) -> Unit
) {
    val context    = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name       by remember { mutableStateOf(medicine?.name ?: "") }
    var time       by remember { mutableStateOf(medicine?.time ?: "") }
    var cycleHours by remember { mutableIntStateOf(medicine?.cycleHours ?: 0) }

    val isReady = name.isNotBlank() && time.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = Color(0xFF0B1929),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 14.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(Color(0xFF1E3A5F), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 52.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {

            // عنوان
            Text(
                if (medicine == null) "افزودن دارو جدید" else "ویرایش دارو",
                fontFamily = Vazir,
                fontWeight = FontWeight.Bold,
                fontSize   = 20.sp,
                color      = Color.White
            )

            // ── نام دارو ──
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FieldLabel("نام دارو")
                OutlinedTextField(
                    value           = name,
                    onValueChange   = { name = it },
                    placeholder     = {
                        Text("مثلاً: آموکسی‌سیلین",
                            fontFamily = Vazir, color = Color(0xFF2D4A6B))
                    },
                    modifier        = Modifier.fillMaxWidth(),
                    shape           = RoundedCornerShape(16.dp),
                    singleLine      = true,
                    colors          = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = Green500,
                        unfocusedBorderColor    = Color(0xFF1A3050),
                        focusedTextColor        = Color.White,
                        unfocusedTextColor      = Color.White,
                        cursorColor             = Green500,
                        focusedContainerColor   = Color(0xFF0E2040),
                        unfocusedContainerColor = Color(0xFF0E2040)
                    )
                )
            }

            // ── ساعت اولین مصرف ──
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FieldLabel("ساعت اولین مصرف")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0E2040))
                        .border(
                            1.dp,
                            if (time.isEmpty()) Color(0xFF1A3050) else Green500.copy(alpha = 0.5f),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            val cal = Calendar.getInstance()
                            TimePickerDialog(
                                context,
                                android.R.style.Theme_Material_Dialog,
                                { _, h, m -> time = "%02d:%02d".format(h, m) },
                                cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE),
                                true
                            ).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = if (time.isEmpty()) "انتخاب ساعت" else time,
                        fontFamily = Vazir,
                        fontSize   = if (time.isEmpty()) 15.sp else 22.sp,
                        fontWeight = if (time.isEmpty()) FontWeight.Normal else FontWeight.Bold,
                        color      = if (time.isEmpty()) Color(0xFF2D4A6B) else Blue400
                    )
                }
            }

            // ── سیکل مصرف ──
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FieldLabel("سیکل مصرف")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    cycles.forEach { (hours, label) ->
                        CycleOption(
                            label    = label,
                            selected = cycleHours == hours,
                            extraInfo = when {
                                hours == 0  -> null
                                hours == 4  -> "۶ وعده در روز"
                                hours == 6  -> "۴ وعده در روز"
                                hours == 8  -> "۳ وعده در روز"
                                hours == 12 -> "۲ وعده در روز"
                                else        -> null
                            },
                            onClick  = { cycleHours = hours }
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── دکمه ثبت ──
            Button(
                onClick  = { if (isReady) onAdd(name.trim(), time.trim(), cycleHours) },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape    = RoundedCornerShape(18.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = if (isReady) Green500 else Color(0xFF1A3050)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (isReady) 8.dp else 0.dp
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
                Text("بستن", fontFamily = Vazir, color = Color(0xFF2D4A6B))
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, fontFamily = Vazir, fontSize = 13.sp, color = Slate400)
}

@Composable
private fun CycleOption(
    label: String,
    selected: Boolean,
    extraInfo: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (selected) Green500.copy(alpha = 0.1f)
                else Color(0xFF0E2040)
            )
            .border(
                1.dp,
                if (selected) Green500.copy(alpha = 0.4f) else Color(0xFF1A3050),
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(
                        if (selected) Green500 else Color(0xFF1A3050),
                        RoundedCornerShape(9.dp)
                    )
                    .border(
                        1.dp,
                        if (selected) Green500 else Color(0xFF2D4A6B),
                        RoundedCornerShape(9.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.White, RoundedCornerShape(4.dp))
                    )
                }
            }
            Text(label, fontFamily = Vazir, fontSize = 15.sp,
                color = if (selected) Color.White else Slate400,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        }
        if (extraInfo != null) {
            Text(extraInfo, fontFamily = Vazir, fontSize = 11.sp,
                color = if (selected) Green400 else Color(0xFF2D4A6B))
        }
    }
}
