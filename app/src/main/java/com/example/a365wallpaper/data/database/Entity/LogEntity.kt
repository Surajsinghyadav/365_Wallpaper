package com.example.a365wallpaper.data.database.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class LogEntity(
    @PrimaryKey(true)
    val id: Int = 0,
    val message : String,
    val timeStamp : Long = System.currentTimeMillis()
)