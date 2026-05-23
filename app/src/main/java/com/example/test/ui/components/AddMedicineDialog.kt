package com.example.test.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.test.data.local.Medicine
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineDialog(

    medicine: Medicine? = null,

    onDismiss: () -> Unit,

    onAdd: (
        String,
        String
    ) -> Unit
) {

    val context = LocalContext.current

    val sheetState =
        rememberModalBottomSheetState()

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

    ModalBottomSheet(

        onDismissRequest = onDismiss,

        sheetState = sheetState,

        containerColor = Color(0xFF111827),

        dragHandle = null

    ) {

        Column(

            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),

            verticalArrangement =
                Arrangement.spacedBy(16.dp)

        ) {

            Text(

                text =
                    if (medicine == null)
                        "افزودن دارو"
                    else
                        "ویرایش دارو",

                color = Color.White
            )

            OutlinedTextField(

                value = name,

                onValueChange = {
                    name = it
                },

                label = {
                    Text("نام دارو")
                },

                modifier = Modifier
                    .fillMaxWidth(),

                shape = RoundedCornerShape(16.dp)
            )

            OutlinedTextField(

                value = time,

                onValueChange = {},

                readOnly = true,

                label = {
                    Text("زمان مصرف")
                },

                modifier = Modifier
                    .fillMaxWidth(),

                shape = RoundedCornerShape(16.dp)
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
                    .fillMaxWidth(),

                shape = RoundedCornerShape(16.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        Color(0xFF2563EB)
                )

            ) {

                Text("انتخاب ساعت")
            }

            Button(

                onClick = {

                    if (
                        name.isNotBlank() &&
                        time.isNotBlank()
                    ) {

                        onAdd(
                            name.trim(),
                            time.trim()
                        )
                    }
                },

                modifier = Modifier
                    .fillMaxWidth(),

                shape = RoundedCornerShape(16.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        Color(0xFF22C55E)
                )

            ) {

                Text(

                    if (medicine == null)
                        "ثبت دارو"
                    else
                        "ذخیره تغییرات"
                )
            }

            TextButton(

                onClick = onDismiss,

                modifier = Modifier
                    .fillMaxWidth()

            ) {

                Text(
                    "بستن",
                    color = Color.LightGray
                )
            }
        }
    }
}
