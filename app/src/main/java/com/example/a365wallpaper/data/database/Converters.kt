package com.example.a365wallpaper.data.database

import androidx.room.TypeConverter
import com.example.a365wallpaper.BitmapGenerators.Goal
import com.example.a365wallpaper.data.Local.GridStyle
import com.example.a365wallpaper.data.Local.SpecialDateOfGoal
import com.example.a365wallpaper.data.Local.SpecialDateOfMonth
import com.example.a365wallpaper.data.Local.SpecialDateOfYear
import com.example.a365wallpaper.ui.theme.DotTheme
import com.example.a365wallpaper.ui.theme.DotThemes
import kotlinx.serialization.json.Json

class Converters {

    @TypeConverter
    fun specialDateOfYearToJson(specialDates: List<SpecialDateOfYear>) : String =
        Json.encodeToString(specialDates)


    @TypeConverter
    fun jsonToSpecialDateOfYear(specialDateString: String) : List<SpecialDateOfYear> =
        Json.decodeFromString(specialDateString)


    @TypeConverter
    fun specialDateOfMonthToJson(specialDates: List<SpecialDateOfMonth>) : String =
        Json.encodeToString(specialDates)


    @TypeConverter
    fun jsonToSpecialDateOfMonth(specialDateString: String) : List<SpecialDateOfMonth> =
        Json.decodeFromString(specialDateString)



    @TypeConverter
    fun specialDateOfGoalToJson(specialDates: List<SpecialDateOfGoal>) : String =
        Json.encodeToString(specialDates)


    @TypeConverter
    fun jsonToSpecialDateOfGoal(specialDateString: String) : List<SpecialDateOfGoal> =
        Json.decodeFromString(specialDateString)

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
        Json.encodeToString(goals)

    @TypeConverter
    fun jsonToGoalList(value: String): List<Goal> =
        Json.decodeFromString(value)

}