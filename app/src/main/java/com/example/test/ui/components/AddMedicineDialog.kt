package com.example.test.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.Vazir
import java.util.Calendar

@Composable
fun AddMedicineDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {

    val context = LocalContext.current

    var name by remember {
        mutableStateOf("")
    }

    var time by remember {
        mutableStateOf("")
    }

    AlertDialog(

        onDismissRequest = onDismiss,

        containerColor = Color(0xFF111827),

        shape = RoundedCornerShape(30.dp),

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
                    value = name,

                    onValueChange = {
                        name = it
                    },

                    label = {
                        Text(
                            "نام دارو",
                            color = Color(0xFFCBD5E1),
                            fontFamily = Vazir
                        )
                    },

                    textStyle = TextStyle(
                        color = Color.White,
                        fontFamily = Vazir,
                        fontSize = 16.sp
                    ),

                    shape = RoundedCornerShape(20.dp),

                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF22C55E),
                        unfocusedBorderColor = Color(0xFF475569),
                        cursorColor = Color.White,
                        focusedLabelColor = Color(0xFF22C55E),
                        unfocusedLabelColor = Color(0xFFCBD5E1)
                    ),

                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable {

                            val calendar = Calendar.getInstance()

                            val hour =
                                calendar.get(Calendar.HOUR_OF_DAY)

                            val minute =
                                calendar.get(Calendar.MINUTE)

                            TimePickerDialog(
                                context,

                                { _, selectedHour, selectedMinute ->

                                    time =
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
                        }
                        .padding(20.dp)
                ) {

                    Row {

                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = Color(0xFF22C55E)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text =
                                if (time.isEmpty())
                                    "انتخاب ساعت مصرف"
                                else
                                    time,

                            color =
                                if (time.isEmpty())
                                    Color(0xFF94A3B8)
                                else
                                    Color.White,

                            fontFamily = Vazir,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        },

        confirmButton = {

            Button(

                onClick = {

                    if (
                        name.isNotBlank() &&
                        time.isNotBlank()
                    ) {

                        onAdd(name, time)
                    }
                },

                shape = RoundedCornerShape(18.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF22C55E)
                )
            ) {

                Text(
                    text = "ثبت دارو",
                    fontFamily = Vazir,
                    color = Color.White
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text(
                    text = "لغو",
                    fontFamily = Vazir,
                    color = Color(0xFFCBD5E1)
                )
            }
        }
    )
}
