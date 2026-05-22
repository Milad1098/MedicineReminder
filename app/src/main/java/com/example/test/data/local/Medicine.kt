package com.example.test.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Medicine(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    val hour: Int,

    val minute: Int
)
