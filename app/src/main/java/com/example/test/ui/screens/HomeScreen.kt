فایل کامل `HomeScreen.kt` را کامل جایگزین کن:

```kotlin
package com.example.test.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.example.test.data.local.AppDatabase
import com.example.test.data.local.Medicine
import com.example.test.ui.components.AddMedicineDialog
import com.example.test.ui.components.EmptyState
import com.example.test.ui.components.HeaderSection
import com.example.test.ui.components.MedicineCard
import com.example.test.utils.scheduleNotification
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {

    val context = LocalContext.current

    val db = remember {

        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "medicine_db"
        ).build()
    }

    val dao = db.medicineDao()

    var medicines by remember {
        mutableStateOf<List<Medicine>>(emptyList())
    }

    var showDialog by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {

        medicines = dao.getAll()
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF020617),
            Color(0xFF0F172A),
            Color(0xFF111827),
            Color(0xFF1E293B)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(horizontal = 20.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Spacer(modifier = Modifier.height(50.dp))

            HeaderSection()

            Spacer(modifier = Modifier.height(28.dp))

            if (medicines.isEmpty()) {

                EmptyState()

            } else {

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(
                        bottom = 120.dp
                    )
                ) {

                    items(medicines) { medicine ->

                        MedicineCard(
                            medicine = medicine
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {

                showDialog = true
            },

            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = 6.dp,
                    bottom = 24.dp
                ),

            shape = RoundedCornerShape(24.dp),

            containerColor = Color(0xFF22C55E),

            contentColor = Color.White
        ) {

            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
        }
    }

    if (showDialog) {

        AddMedicineDialog(

            onDismiss = {

                showDialog = false
            },

            onAdd = { name, time ->

                scope.launch {

                    dao.insert(

                        Medicine(
                            name = name,
                            time = time
                        )
                    )

                    medicines = dao.getAll()

                    scheduleNotification(
                        context = context,
                        medicineName = name
                    )
                }

                showDialog = false
            }
        )
    }
}
```
