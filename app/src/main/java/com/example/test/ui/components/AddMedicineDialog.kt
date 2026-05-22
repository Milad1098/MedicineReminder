```kotlin
package com.example.test.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    var medicineTime by remember {
        mutableStateOf("")
    }

    AlertDialog(

        onDismissRequest = onDismiss,

        containerColor = Color(0xFF111827),

        shape = RoundedCornerShape(28.dp),

        title = {

            Text(
                text = "افزودن دارو",
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

                    singleLine = true,

                    shape = RoundedCornerShape(18.dp),

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
                        unfocusedBorderColor = Color(0xFF334155),

                        focusedLabelColor = Color(0xFF22C55E),
                        unfocusedLabelColor = Color(0xFF94A3B8),

                        cursorColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(

                    onClick = {

                        val calendar = Calendar.getInstance()

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
                                "انتخاب ساعت"
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

                        onAdd(
                            medicineName.trim(),
                            medicineTime.trim()
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
```
