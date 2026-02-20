package com.example.a365wallpaper.utils

import com.example.a365wallpaper.data.database.DotThemeEntity
import com.example.a365wallpaper.ui.theme.DotTheme

fun DotThemeEntity.toExternalModel() = DotTheme(
    id = id,
    name = name,
    bg = bg,
    filled = filled,
    empty = empty,
    today = today,
)

fun DotTheme.toEntity() = DotThemeEntity(
    id = id,
    name = name ,
    bg = bg,
    filled = filled,
    empty = empty,
    today = today
)