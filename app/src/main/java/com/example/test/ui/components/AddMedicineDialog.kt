package com.example.test.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.test.ui.theme.Vazir

@Composable
fun AddMedicineDialog(
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

        shape = RoundedCornerShape(24.dp),

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
                    value = name,

                    onValueChange = {
                        name = it
                    },

                    label = {
                        Text(
                            "نام دارو",
                            color = Color.White
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = time,

                    onValueChange = {
                        time = it
                    },

                    label = {
                        Text(
                            "زمان مصرف",
                            color = Color.White
                        )
                    }
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
                }
            ) {

                Text(
                    text = "ثبت",
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
                    color = Color.White,
                    fontFamily = Vazir
                )
            }
        }
    )
}
