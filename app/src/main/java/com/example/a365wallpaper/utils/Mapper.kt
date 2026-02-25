package com.example.a365wallpaper.utils

import com.example.a365wallpaper.BitmapGenerators.GoalsDotsSpec
import com.example.a365wallpaper.BitmapGenerators.MonthDotsSpec
import com.example.a365wallpaper.BitmapGenerators.YearDotsSpec
import com.example.a365wallpaper.data.database.Entity.GoalsEntity
import com.example.a365wallpaper.data.database.Entity.MonthEntity
import com.example.a365wallpaper.data.database.Entity.YearEntity


fun YearDotsSpec.toEntity(): YearEntity = YearEntity(
    theme                   = theme,
    gridStyle               = gridStyle,
    showLabel               = showLabel,
    verticalBias            = verticalBias,
    specialDates            = specialDates,
    showNumberInsteadOfDots = showNumberInsteadOfDots,
    showBothNumberAndDot    = showBothNumberAndDot,
)

fun YearEntity.toExternalModel(): YearDotsSpec = YearDotsSpec(
    theme                   = theme,
    gridStyle               = gridStyle,
    showLabel               = showLabel,
    verticalBias            = verticalBias,
    specialDates            = specialDates,
    showNumberInsteadOfDots = showNumberInsteadOfDots,
    showBothNumberAndDot    = showBothNumberAndDot,
)

fun MonthDotsSpec.toEntity(): MonthEntity = MonthEntity(
    theme = theme,
    gridStyle = gridStyle,
    showLabel = showLabel,
    verticalBias = verticalBias,
    dotSizeMultiplier = dotSizeMultiplier,
    specialDates            = specialDates,
    showNumberInsteadOfDots = showNumberInsteadOfDots,
    showBothNumberAndDot    = showBothNumberAndDot,
)

fun MonthEntity.toExternalModel(): MonthDotsSpec = MonthDotsSpec(
    theme = theme,
    gridStyle = gridStyle,
    showLabel = showLabel,
    verticalBias = verticalBias,
    dotSizeMultiplier = dotSizeMultiplier,
    specialDates            = specialDates,
    showNumberInsteadOfDots = showNumberInsteadOfDots,
    showBothNumberAndDot    = showBothNumberAndDot,
)


fun GoalsDotsSpec.toEntity(): GoalsEntity = GoalsEntity(
    goal = goals,
    theme = theme,
    gridStyle = gridStyle,
    showLabel = showLabel,
    verticalBias = verticalBias,
    dotSizeMultiplier = dotSizeMultiplier,
    specialDates            = specialDates,
    showNumberInsteadOfDots = showNumberInsteadOfDots,
    showBothNumberAndDot    = showBothNumberAndDot,
)

fun GoalsEntity.toExternalModel(): GoalsDotsSpec = GoalsDotsSpec(
    goals = goal,
    theme = theme,
    gridStyle = gridStyle,
    showLabel = showLabel,
    verticalBias = verticalBias,
    dotSizeMultiplier = dotSizeMultiplier,
    specialDates            = specialDates,
    showNumberInsteadOfDots = showNumberInsteadOfDots,
    showBothNumberAndDot    = showBothNumberAndDot,
)
