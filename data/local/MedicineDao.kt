package com.example.test.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MedicineDao {

    @Query("SELECT * FROM Medicine")
    suspend fun getAll(): List<Medicine>

    @Insert
    suspend fun insert(medicine: Medicine)
}
