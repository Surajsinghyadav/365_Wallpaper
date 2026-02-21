package com.example.a365wallpaper.data.database

import androidx.room.TypeConverter
import com.example.a365wallpaper.Goal
import com.example.a365wallpaper.data.GridStyle
import com.example.a365wallpaper.ui.theme.DotTheme
import com.example.a365wallpaper.ui.theme.DotThemes
import kotlinx.serialization.json.Json

class Converters {

    @TypeConverter
    fun dotThemeToString(theme: DotTheme): String =
        theme.id   // or theme.name

    @TypeConverter
    fun stringToDotTheme(value: String): DotTheme =
        DotThemes.All.first { it.id == value }


    @TypeConverter
    fun gridStyleToString(style: GridStyle): String =
        style.name

    @TypeConverter
    fun stringToGridStyle(value: String): GridStyle =
        GridStyle.valueOf(value)



    @TypeConverter
    fun goalListToJson(goals: List<Goal>): String =
        Json.Default.encodeToString(goals)

    @TypeConverter
    fun jsonToGoalList(value: String): List<Goal> =
        Json.Default.decodeFromString(value)

}