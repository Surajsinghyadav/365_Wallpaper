package com.example.a365wallpaper.utils

import com.example.a365wallpaper.GoalsDotsSpec
import com.example.a365wallpaper.MonthDotsSpec
import com.example.a365wallpaper.YearDotsSpec
import com.example.a365wallpaper.data.database.GoalsEntity
import com.example.a365wallpaper.data.database.MonthEntity
import com.example.a365wallpaper.data.database.YearEntity


fun YearEntity.toExternalModel() = YearDotsSpec(
    gridStyle = this.gridStyle,
    verticalBias = this.verticalBias,
    theme = this.theme,
    showLabel = this.showLabel,
)

fun YearDotsSpec.toEntity() = YearEntity(
    theme = this.theme,
    gridStyle = this.gridStyle,
    verticalBias = this.verticalBias,
    showLabel = this.showLabel
)



fun MonthEntity.toExternalModel() = MonthDotsSpec(
    gridStyle = this.gridStyle,
    verticalBias = this.verticalBias,
    theme = this.theme,
    showLabel = this.showLabel,
    dotSizeMultiplier = this.dotSizeMultiplier
)

fun MonthDotsSpec.toEntity() = MonthEntity(
    showLabel = this.showLabel,
    dotSizeMultiplier = this.dotSizeMultiplier,
    theme = this.theme,
    gridStyle = this.gridStyle,
    verticalBias = this.verticalBias,
)


fun GoalsEntity.toExternalModel() = GoalsDotsSpec(
    goals = this.goal,
    gridStyle = this.gridStyle,
    verticalBias = this.verticalBias,
    theme = this.theme,
    showLabel = this.showLabel,
    dotSizeMultiplier = this.dotSizeMultiplier
)

fun GoalsDotsSpec.toEntity() = GoalsEntity(
    goal = this.goals,
    showLabel = this.showLabel,
    dotSizeMultiplier = this.dotSizeMultiplier,
    theme = this.theme,
    gridStyle = this.gridStyle,
    verticalBias =  this.verticalBias,
)


