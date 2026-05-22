package com.example.test.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TimePickerDefaults.colors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.ui.theme.Vazir
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {

    val context = LocalContext.current

    var medicineName by remember {
        mutableStateOf("")
    }

    var medicineTime by remember {
        mutableStateOf("")
    }

    val calendar = Calendar.getInstance()

    AlertDialog(
        onDismissRequest = onDismiss,

        shape = RoundedCornerShape(28.dp),

        containerColor = Color(0xFF111827),

        title = {
            Text(
                text = "افزودن داروی جدید",
                color = Color.White,
                fontFamily = Vazir,
                fontSize = 22.sp
            )
        },

        text = {

            Column {

                OutlinedTextField(
                    value = medicineName,

                    onValueChange = {
                        medicineName = it
                    },

                    modifier = Modifier.fillMaxWidth(),

                    label = {
                        Text(
                            "نام دارو",
                            color = Color(0xFFCBD5E1),
                            fontFamily = Vazir
                        )
                    },

                    textStyle = LocalTextStyle.current.copy(
                        color = Color.White,
                        fontFamily = Vazir
                    ),

                    singleLine = true,

                    shape = RoundedCornerShape(18.dp),

                    colors = OutlinedTextFieldDefaults.colors(

                        focusedBorderColor = Color(0xFF22C55E),
                        unfocusedBorderColor = Color(0xFF334155),

                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,

                        cursorColor = Color.White,

                        focusedLabelColor = Color(0xFF22C55E),
                        unfocusedLabelColor = Color(0xFF94A3B8)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {

                        TimePickerDialog(
                            context,
                            { _, hour, minute ->

                                medicineTime =
                                    String.format(
                                        "%02d:%02d",
                                        hour,
                                        minute
                                    )
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    },

                    modifier = Modifier.fillMaxWidth(),

                    shape = RoundedCornerShape(18.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B)
                    )
                ) {

                    Text(
                        text =
                            if (medicineTime.isEmpty())
                                "انتخاب ساعت مصرف"
                            else
                                "⏰ $medicineTime",

                        color = Color.White,

                        fontFamily = Vazir
                    )
                }
            }
        },

        confirmButton = {

            Button(

                onClick = {

                    if (
                        medicineName.isNotBlank() &&
                        medicineTime.isNotBlank()
                    ) {

                        try {

                            onAdd(
                                medicineName.trim(),
                                medicineTime.trim()
                            )

                        } catch (_: Exception) {

                        }
                    }
                },

                shape = RoundedCornerShape(16.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF22C55E)
                )
            ) {

                Text(
                    text = "ثبت دارو",
                    color = Color.White,
                    fontFamily = Vazir
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text(
                    text = "لغو",
                    color = Color(0xFFCBD5E1),
                    fontFamily = Vazir
                )
            }
        }
    )
}
