package com.example.a365wallpaper.utils

import com.example.a365wallpaper.Goal
import com.example.a365wallpaper.GoalsDotsSpec
import com.example.a365wallpaper.MonthDotsSpec
import com.example.a365wallpaper.YearDotsSpec
import com.example.a365wallpaper.data.database.GoalsEntity
import com.example.a365wallpaper.data.database.MonthEntity
import com.example.a365wallpaper.data.database.YearEntity
import java.time.LocalDate


fun YearDotsSpec.toEntity(): YearEntity = YearEntity(
    theme = theme,
    gridStyle = gridStyle,
    showLabel = showLabel,
    verticalBias = verticalBias,
    specialDates = specialDates )

fun YearEntity.toExternalModel(): YearDotsSpec = YearDotsSpec(
    theme = theme,
    gridStyle = gridStyle,
    showLabel = showLabel,
    verticalBias = verticalBias,
    specialDates = specialDates,
)


fun MonthDotsSpec.toEntity(): MonthEntity = MonthEntity(
    theme = theme,
    gridStyle = gridStyle,
    showLabel = showLabel,
    verticalBias = verticalBias,
    dotSizeMultiplier = dotSizeMultiplier,
)

fun MonthEntity.toExternalModel(): MonthDotsSpec = MonthDotsSpec(
    theme = theme,
    gridStyle = gridStyle,
    showLabel = showLabel,
    verticalBias = verticalBias,
    dotSizeMultiplier = dotSizeMultiplier,
)


fun GoalsDotsSpec.toEntity(): GoalsEntity = GoalsEntity(
    goal = goals,
    theme = theme,
    gridStyle = gridStyle,
    showLabel = showLabel,
    verticalBias = verticalBias,
    dotSizeMultiplier = dotSizeMultiplier,
)

fun GoalsEntity.toExternalModel(): GoalsDotsSpec = GoalsDotsSpec(
    goals = goal,
    theme = theme,
    gridStyle = gridStyle,
    showLabel = showLabel,
    verticalBias = verticalBias,
    dotSizeMultiplier = dotSizeMultiplier,
)
