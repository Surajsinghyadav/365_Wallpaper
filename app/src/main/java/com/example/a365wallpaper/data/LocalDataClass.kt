package com.example.a365wallpaper.data

import androidx.core.graphics.toColorInt
import com.example.a365wallpaper.ui.theme.DotThemes
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class SpecialDateOfYear(
    val id: Int = 0,
    val startEpochDay: Long,   // LocalDate.toEpochDay()
    val endEpochDay: Long,     // LocalDate.toEpochDay()
    val colorArgb: Int,        // ARGB Int from DotTheme.today
) {
    val startDate: LocalDate get() = LocalDate.ofEpochDay(startEpochDay)
    val endDate: LocalDate   get() = LocalDate.ofEpochDay(endEpochDay)
}

/** Returns first matching special color for [date], or null. First entry wins on overlap. */
fun List<SpecialDateOfYear>.specialColorFor(date: LocalDate): Int? =
    firstOrNull { !date.isBefore(it.startDate) && !date.isAfter(it.endDate) }?.colorArgb



//data class SpecialDateofMonth(
//    val startDate: Long,
//    val endDate: Long,
//)
//
//data class SpecialDateofGoal(
//    val validForGoalId : Int,
//    val startDate: Long,
//    val endDate: Long,
//)