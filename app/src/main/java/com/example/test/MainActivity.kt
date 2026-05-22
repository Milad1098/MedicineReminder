package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.room.Room
import com.example.test.data.local.AppDatabase
import com.example.test.data.local.Medicine
import com.example.test.ui.screens.HomeScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "medicine_db"
        ).build()

        setContent {

            CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Rtl
            ) {

                val dao = db.medicineDao()

                var medicines by remember {
                    mutableStateOf(listOf<Medicine>())
                }

                val scope = rememberCoroutineScope()

                LaunchedEffect(true) {
                    medicines = dao.getAll()
                }

                val vazir = FontFamily(
                    Font(R.font.vazirmatn_regular)
                )

                HomeScreen(

                    medicines = medicines,

                    fontFamily = vazir,

                    onAddMedicine = { name, time ->

                        scope.launch {

                            dao.insert(
                                Medicine(
                                    name = name,
                                    time = time
                                )
                            )

                            medicines = dao.getAll()
                        }
                    }
                )
            }
        }
    }
}
