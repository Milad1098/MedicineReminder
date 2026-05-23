package com.example.test.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MedicineDao {

    @Insert
    suspend fun insert(
        medicine: Medicine
    ): Long

    @Update
    suspend fun update(
        medicine: Medicine
    )

    @Delete
    suspend fun delete(
        medicine: Medicine
    )

    @Query("SELECT * FROM medicines ORDER BY time ASC")
    suspend fun getAll(): List<Medicine>
}
