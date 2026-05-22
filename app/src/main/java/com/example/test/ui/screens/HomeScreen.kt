package com.example.test.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.room.RoomDatabase
import com.example.test.data.local.AppDatabase
import com.example.test.data.local.Medicine
import com.example.test.ui.components.EmptyState
import com.example.test.ui.components.HeaderSection
import com.example.test.ui.components.MedicineCard
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    db: AppDatabase
) {

    val dao = db.medicineDao()

    var medicines by remember {
        mutableStateOf(listOf<Medicine>())
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(true) {
        medicines = dao.getAll()
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF020617),
            Color(0xFF0F172A),
            Color(0xFF111827)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(20.dp)
    ) {

        Column {

            Spacer(modifier = Modifier.height(40.dp))

            HeaderSection()

            Spacer(modifier = Modifier.height(30.dp))

            if (medicines.isEmpty()) {

                EmptyState(
                    fontFamily = com.example.test.ui.theme.Vazir
                )

            } else {

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    items(medicines) {

                        MedicineCard(
                            medicine = it,
                            fontFamily = com.example.test.ui.theme.Vazir
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {

            },

            modifier = Modifier
                .align(Alignment.BottomStart),

            containerColor = Color(0xFF22C55E)
        ) {

            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}
