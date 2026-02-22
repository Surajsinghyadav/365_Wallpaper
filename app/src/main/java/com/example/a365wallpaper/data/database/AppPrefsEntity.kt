package com.example.a365wallpaper.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.a365wallpaper.data.GridStyle
import com.example.a365wallpaper.ui.theme.DotTheme
import com.example.a365wallpaper.ui.theme.DotThemes

@Entity
data class AppPrefsEntity(
    @PrimaryKey
    val id: Int = 1,
    val theme: DotTheme = DotThemes.All.first(),
    val gridStyle: GridStyle = GridStyle.Dots,
    val showLabel: Boolean = true,
    val verticalBias: Float = 0f,
    val monthDotSize: Float = 1.0f,
    val goalDotSize: Float = 1.0f,
)
