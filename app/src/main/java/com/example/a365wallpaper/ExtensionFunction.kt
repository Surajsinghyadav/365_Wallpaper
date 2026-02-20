package com.example.a365wallpaper

import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

fun Int.toColors(): Color = Color(this)

fun Long.getDate(): String {
    val formatedTime = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        .format(Date(this))
    return formatedTime
}

fun Long.getTime(): String {
    val formatedTime = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        .format(Date(this))
    return formatedTime
}
