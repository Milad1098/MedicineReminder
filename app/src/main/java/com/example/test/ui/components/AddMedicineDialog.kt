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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.ui.theme.Vazir
import java.util.*

@Composable
fun AddMedicineDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Int, Int) -> Unit
) {

    var name by remember {
        mutableStateOf("")
    }

    var hour by remember {
        mutableIntStateOf(8)
    }

    var minute by remember {
        mutableIntStateOf(0)
    }

    val context = LocalContext.current

    val timePicker = TimePickerDialog(
        context,
        { _, h, m ->
            hour = h
            minute = m
        },
        hour,
        minute,
        true
    )

    AlertDialog(

        onDismissRequest = onDismiss,

        containerColor = Color(0xFF111827),

        shape = RoundedCornerShape(28.dp),

        title = {

            Text(
                text = "افزودن دارو",
                fontFamily = Vazir,
                color = Color.White,
                fontWeight = FontWeight.Bold,
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

                    textStyle = LocalTextStyle.current.copy(
                        color = Color.White,
                        fontFamily = Vazir
                    ),

                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF22C55E),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedLabelColor = Color(0xFF22C55E),
                        unfocusedLabelColor = Color(0xFF94A3B8),
                        cursorColor = Color.White
                    ),

                    shape = RoundedCornerShape(18.dp),

                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Box(

                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFF1E293B),
                            RoundedCornerShape(18.dp)
                        )
                        .clickable {
                            timePicker.show()
                        }
                        .padding(18.dp)
                ) {

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text = String.format(
                                "%02d:%02d",
                                hour,
                                minute
                            ),
                            color = Color.White,
                            fontFamily = Vazir,
                            fontSize = 20.sp
                        )

                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = Color(0xFF22C55E)
                        )
                    }
                }
            }
        },

        confirmButton = {

            Button(

                onClick = {

                    if (name.isNotBlank()) {

                        onAdd(
                            name,
                            hour,
                            minute
                        )
                    }
                },

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF22C55E)
                ),

                shape = RoundedCornerShape(18.dp)
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
                    color = Color.White,
                    fontFamily = Vazir
                )
            }
        }
    )
}
