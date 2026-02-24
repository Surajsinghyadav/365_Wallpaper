package com.example.a365wallpaper.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.a365wallpaper.data.GridStyle
import com.example.a365wallpaper.data.SpecialDateOfYear
import com.example.a365wallpaper.data.WallpaperTarget
import com.example.a365wallpaper.ui.theme.DotTheme
import com.example.a365wallpaper.ui.theme.DotThemes
import java.time.LocalDate

@Entity
data class YearEntity(
    @PrimaryKey(false)
    val id: Int = 1,
    val theme : DotTheme = DotThemes.All.first(),
    val gridStyle: GridStyle = GridStyle.Dots,
    val specialDates : List<SpecialDateOfYear> = emptyList(),
    val showLabel : Boolean = true,
    val verticalBias : Float = 0f,

    )