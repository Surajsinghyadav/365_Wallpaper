package com.example.a365wallpaper.data.database.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.a365wallpaper.data.Local.GridStyle
import com.example.a365wallpaper.data.Local.SpecialDateOfMonth
import com.example.a365wallpaper.data.Local.SpecialDateOfYear
import com.example.a365wallpaper.ui.theme.DotTheme
import com.example.a365wallpaper.ui.theme.DotThemes

@Entity
data class MonthEntity(
    @PrimaryKey(false)
    val id : Int = 1,
    val showLabel : Boolean = true,
    val dotSizeMultiplier : Float = 1f,
    val theme : DotTheme = DotThemes.All.first(),
    val gridStyle: GridStyle = GridStyle.Dots,
    val verticalBias : Float = 0f,
    val specialDates : List<SpecialDateOfMonth> = emptyList(),
    val showNumberInsteadOfDots: Boolean = false,
    val showBothNumberAndDot: Boolean = false,
)