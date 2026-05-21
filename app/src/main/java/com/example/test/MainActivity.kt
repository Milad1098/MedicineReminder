package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MedicineReminderApp()
        }
    }
}

@Composable
fun MedicineReminderApp() {

    val medicines = listOf(
        "قرص فشار خون - ۸ صبح",
        "انسولین - ۱ ظهر",
        "ویتامین D - ۹ شب"
    )

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF2563EB),
            secondary = Color(0xFF0EA5E9),
            background = Color(0xFFF8FAFC)
        )
    ) {

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {},
                    containerColor = Color(0xFF2563EB)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            },

            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = true,
                        onClick = {},
                        icon = {
                            Icon(Icons.Default.Home, null)
                        },
                        label = {
                            Text("خانه")
                        }
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = {
                            Icon(Icons.Default.Medication, null)
                        },
                        label = {
                            Text("داروها")
                        }
                    )
                }
            }

        ) { padding ->

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC))
                    .padding(padding)
                    .padding(16.dp)
            ) {

                item {

                    Text(
                        text = "سلام 👋",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "وقت داروی بعدی نزدیکه",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2563EB)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {

                            Text(
                                text = "داروی بعدی",
                                color = Color.White,
                                fontSize = 18.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "قرص فشار خون",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "۸:۰۰ صبح",
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "داروهای امروز",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(medicines) { medicine ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),

                        shape = RoundedCornerShape(20.dp)
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),

                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Icon(
                                Icons.Default.Medication,
                                contentDescription = null,
                                tint = Color(0xFF2563EB)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = medicine,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
