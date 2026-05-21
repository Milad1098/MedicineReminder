package com.example.test.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Medicine::class],
    version = 1
)
abstract class MedicineDatabase : RoomDatabase() {

    abstract fun medicineDao(): MedicineDao
}
