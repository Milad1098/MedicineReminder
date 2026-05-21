package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            MaterialTheme {

                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {

                    HomeScreen()

                }
            }
        }
    }
}

@Composable
fun HomeScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        verticalArrangement = Arrangement.Center,

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Card(
            shape = RoundedCornerShape(28.dp)
        ) {

            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "یادآور دارو",
                    fontSize = 32.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "داروهایت را فراموش نکن 💊",
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { }
                ) {

                    Text("افزودن دارو")

                }
            }
        }
    }
}
