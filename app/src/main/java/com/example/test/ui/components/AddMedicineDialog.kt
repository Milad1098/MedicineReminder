package com.example.test.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Composable
fun AddMedicineDialog(
    fontFamily: FontFamily,
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {

    var name by remember {
        mutableStateOf("")
    }

    var time by remember {
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
                fontFamily = fontFamily
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
                            color = Color.White,
                            fontFamily = fontFamily
                        )
                    },

                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF22C55E),
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = Color.White
                    ),

                    shape = RoundedCornerShape(18.dp),

                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = time,

                    onValueChange = {
                        time = it
                    },

                    label = {
                        Text(
                            "مثلاً 08:30",
                            color = Color.White,
                            fontFamily = fontFamily
                        )
                    },

                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF22C55E),
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = Color.White
                    ),

                    shape = RoundedCornerShape(18.dp),

                    modifier = Modifier.fillMaxWidth()
                )
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

                shape = RoundedCornerShape(16.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF22C55E)
                )
            ) {

                Text(
                    text = "ثبت",
                    fontFamily = fontFamily
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
                    fontFamily = fontFamily
                )
            }
        }
    )
}
