package com.example.test.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.test.data.local.Medicine
import java.util.Calendar

@Composable
fun AddMedicineDialog(
    medicine: Medicine? = null,
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {

    val context = LocalContext.current

    var name by remember {
        mutableStateOf(
            medicine?.name ?: ""
        )
    }

    var time by remember {
        mutableStateOf(
            medicine?.time ?: ""
        )
    }

    AlertDialog(

        onDismissRequest = onDismiss,

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
                    if (medicine == null)
                        "افزودن"
                    else
                        "ویرایش"
                )
            }
        },

        dismissButton = {

            OutlinedButton(
                onClick = onDismiss
            ) {

                Text("لغو")
            }
        },

        title = {

            Text(
                if (medicine == null)
                    "داروی جدید"
                else
                    "ویرایش دارو"
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
                        Text("نام دارو")
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                )

                OutlinedTextField(
                    value = time,

                    onValueChange = {},

                    readOnly = true,

                    label = {
                        Text("زمان مصرف")
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )

                Button(

                    onClick = {

                        val calendar =
                            Calendar.getInstance()

                        val dialog =
                            TimePickerDialog(
                                context,

                                { _, hour, minute ->

                                    time =
                                        String.format(
                                            "%02d:%02d",
                                            hour,
                                            minute
                                        )
                                },

                                calendar.get(
                                    Calendar.HOUR_OF_DAY
                                ),

                                calendar.get(
                                    Calendar.MINUTE
                                ),

                                true
                            )

                        dialog.show()
                    },

                    modifier = Modifier
                        .padding(top = 12.dp)
                ) {

                    Text("انتخاب ساعت")
                }
            }
        }
    )
}
