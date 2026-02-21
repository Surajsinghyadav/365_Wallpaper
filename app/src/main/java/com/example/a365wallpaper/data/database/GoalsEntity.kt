package com.example.a365wallpaper.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.a365wallpaper.Goal
import com.example.a365wallpaper.data.GridStyle
import com.example.a365wallpaper.ui.theme.DotTheme
import java.time.LocalDate


@Entity
data class GoalsEntity(
    @PrimaryKey(false)
    val id : Int = 1,
    val goal: List<Goal>,
    val showLabel : Boolean,
    val dotSizeMultiplier : Float,
    val theme : DotTheme,
    val gridStyle: GridStyle,
    val verticalBias : Float,
)