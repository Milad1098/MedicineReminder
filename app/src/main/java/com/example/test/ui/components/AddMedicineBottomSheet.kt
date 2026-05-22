package com.example.test.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineBottomSheet(
    fontFamily: FontFamily,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {

    val context = LocalContext.current

    var medicineName by remember {
        mutableStateOf("")
    }

    var medicineTime by remember {
        mutableStateOf("")
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,

        containerColor = Color(0xFF0F172A),

        dragHandle = {

            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .width(60.dp)
                    .height(6.dp)
                    .background(
                        Color.Gray,
                        RoundedCornerShape(20.dp)
                    )
            )
        }
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {

            Text(
                text = "افزودن داروی جدید",
                color = Color.White,
                fontFamily = fontFamily,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = medicineName,

                onValueChange = {
                    medicineName = it
                },

                modifier = Modifier.fillMaxWidth(),

                label = {
                    Text(
                        "نام دارو",
                        fontFamily = fontFamily
                    )
                },

                leadingIcon = {

                    Icon(
                        Icons.Default.Medication,
                        contentDescription = null
                    )
                },

                shape = RoundedCornerShape(22.dp),

                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF22C55E),
                    unfocusedBorderColor = Color.DarkGray,
                    cursorColor = Color.White,
                    focusedLabelColor = Color(0xFF22C55E),
                    unfocusedLabelColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedButton(

                onClick = {

                    val calendar = Calendar.getInstance()

                    val hour = calendar.get(Calendar.HOUR_OF_DAY)

                    val minute = calendar.get(Calendar.MINUTE)

                    TimePickerDialog(
                        context,

                        { _, selectedHour, selectedMinute ->

                            medicineTime =
                                String.format(
                                    "%02d:%02d",
                                    selectedHour,
                                    selectedMinute
                                )
                        },

                        hour,
                        minute,
                        true
                    ).show()
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),

                shape = RoundedCornerShape(22.dp),

                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = if (medicineTime.isBlank())
                            "انتخاب ساعت مصرف"
                        else
                            medicineTime,

                        fontFamily = fontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(

                onClick = {

                    if (
                        medicineName.isNotBlank() &&
                        medicineTime.isNotBlank()
                    ) {

                        onSave(
                            medicineName,
                            medicineTime
                        )
                    }
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),

                shape = RoundedCornerShape(24.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF22C55E)
                )
            ) {

                Text(
                    text = "ثبت دارو",
                    fontFamily = fontFamily,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
