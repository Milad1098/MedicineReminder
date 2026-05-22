package com.example.test.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Medicine(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    val time: String
)
