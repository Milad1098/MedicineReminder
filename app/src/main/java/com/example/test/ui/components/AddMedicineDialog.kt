package com.example.test.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.test.ui.theme.Vazir
import java.util.Calendar

@Composable
fun AddMedicineDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {

    val context = LocalContext.current

    var medicineName by remember {
        mutableStateOf("")
    }

    var selectedTime by remember {
        mutableStateOf("انتخاب ساعت")
    }

    val calendar = Calendar.getInstance()

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->

            val formattedHour =
                hour.toString().padStart(2, '0')

            val formattedMinute =
                minute.toString().padStart(2, '0')

            selectedTime = "$formattedHour:$formattedMinute"
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    AlertDialog(
        onDismissRequest = onDismiss,

        containerColor = Color(0xFF0F172A),

        shape = RoundedCornerShape(28.dp),

        title = {
            Text(
                text = "افزودن دارو",
                color = Color.White,
                fontFamily = Vazir
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
                            text = "نام دارو",
                            color = Color(0xFFCBD5E1),
                            fontFamily = Vazir
                        )
                    },

                    colors = OutlinedTextFieldDefaults.colors(

                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,

                        focusedBorderColor = Color(0xFF22C55E),
                        unfocusedBorderColor = Color(0xFF475569),

                        focusedLabelColor = Color(0xFF22C55E),
                        unfocusedLabelColor = Color(0xFFCBD5E1),

                        cursorColor = Color.White
                    ),

                    shape = RoundedCornerShape(18.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        timePickerDialog.show()
                    },

                    modifier = Modifier.fillMaxWidth(),

                    shape = RoundedCornerShape(18.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B)
                    )
                ) {

                    Text(
                        text = selectedTime,
                        color = Color.White,
                        fontFamily = Vazir
                    )
                }
            }
        },

        confirmButton = {

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Button(
                    onClick = {

                        if (
                            medicineName.isNotBlank() &&
                            selectedTime != "انتخاب ساعت"
                        ) {

                            onAdd(
                                medicineName,
                                selectedTime
                            )
                        }
                    },

                    shape = RoundedCornerShape(16.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF22C55E)
                    )
                ) {

                    Text(
                        text = "ثبت",
                        color = Color.White,
                        fontFamily = Vazir
                    )
                }

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
        }
    )
}
