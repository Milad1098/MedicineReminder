package com.example.test.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.data.local.Medicine
import com.example.test.ui.components.AddMedicineDialog
import com.example.test.ui.components.EmptyState
import com.example.test.ui.components.MedicineCard
import com.example.test.ui.components.AddMedicineBottomSheet

@Composable
fun HomeScreen(
    medicines: List<Medicine>,
    fontFamily: FontFamily,
    onAddMedicine: (String, String) -> Unit
) {

    var showDialog by remember {
        mutableStateOf(false)
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
            .padding(horizontal = 18.dp)
    ) {

        Column {

            Spacer(modifier = Modifier.height(42.dp))

            HeaderSection(fontFamily)

            Spacer(modifier = Modifier.height(28.dp))

            if (medicines.isEmpty()) {

                EmptyState(fontFamily)

            } else {

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {

                    items(medicines) { medicine ->

                        MedicineCard(
                            medicine = medicine,
                            fontFamily = fontFamily
                        )

                        Spacer(modifier = Modifier.height(16.dp))
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
                .padding(12.dp)
                .animateContentSize(),

            shape = RoundedCornerShape(22.dp),

            containerColor = Color(0xFF22C55E)
        ) {

            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = Color.White
            )
        }
    }

    if (showDialog) {

        AddMedicineBottomSheet(
    
            fontFamily = fontFamily,
    
            onDismiss = {
                showDialog = false
            },
    
            onSave = { name, time ->
    
                onAddMedicine(name, time)
    
                showDialog = false
            }
        )
    }
}

@Composable
private fun HeaderSection(
    fontFamily: FontFamily
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column {

            Text(
                text = "یادآور دارو",
                color = Color.White,
                fontFamily = fontFamily,
                fontSize = 34.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "مدیریت هوشمند مصرف دارو",
                color = Color(0xFF94A3B8),
                fontFamily = fontFamily
            )
        }

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0x22FFFFFF)),

            contentAlignment = Alignment.Center
        ) {

            Icon(
                Icons.Default.Medication,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}
