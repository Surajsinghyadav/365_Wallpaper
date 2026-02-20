package com.example.a365wallpaper.data

import androidx.compose.ui.graphics.Shape
import com.example.a365wallpaper.AppShapes

//enum class Mode{ Year, Month, Goals }
//enum class Target{ Home, Lock, Both }
//enum class GridStyle{ Dots, Minimal, Ring}

enum class WallpaperMode(val label: String) { Year("Year"), Month("Month"), Goals("Goals") }
enum class WallpaperTarget(val label: String) { Home("Home"), Lock("Lock"), Both("Both") }
enum class GridStyle(val label: String, val shape: Shape) {
    Dots("Dots", AppShapes.Circle),
    Squares("Squares", AppShapes.Square),
    Rounded("Rounded", AppShapes.RoundedCorner),
    Diamond("Diamond", AppShapes.Diamond)
}


