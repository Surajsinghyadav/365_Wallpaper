package com.example.a365wallpaper.data.Local

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




@Serializable
data class SpecialDateOfMonth(
    val id: Int = 0,
    val startEpochDay: Long,   // LocalDate.toEpochDay()
    val endEpochDay: Long,     // LocalDate.toEpochDay()
    val colorArgb: Int,        // ARGB Int from DotTheme.today
) {
    val startDate: LocalDate get() = LocalDate.ofEpochDay(startEpochDay)
    val endDate: LocalDate   get() = LocalDate.ofEpochDay(endEpochDay)
}


@Serializable
data class SpecialDateOfGoal(
    val id: Int = 0,
    val goalTitle: String = "",
    val startEpochDay: Long,
    val endEpochDay: Long,
    val colorArgb: Int,
){
    val startDate: LocalDate get() = LocalDate.ofEpochDay(startEpochDay)
    val endDate: LocalDate   get() = LocalDate.ofEpochDay(endEpochDay)
}


