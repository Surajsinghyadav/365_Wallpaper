package com.example.a365wallpaper.presentation


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.example.a365wallpaper.Goal
import com.example.a365wallpaper.data.GridStyle
import com.example.a365wallpaper.toColors
import com.example.a365wallpaper.ui.theme.DotTheme
import kotlin.math.ceil

@Composable
fun MockGoalsDots(
    dotTheme: DotTheme,
    gridStyle: GridStyle,
    scale: Float = 1.0f,
    showLabel: Boolean,
    goals: List<Goal> = emptyList(), // ✅ real goals
    dotSizeMultiplier: Float = 1.0f  // ✅ user-controlled size
) {
    // Fall back to mock data if no real goals yet
    val mockGoals = goals.ifEmpty {
        listOf(
        Goal("Personal", java.time.LocalDate.now().minusDays(22), java.time.LocalDate.now().plusDays(68)),
        Goal("Fitness", java.time.LocalDate.now().minusDays(15), java.time.LocalDate.now().plusDays(145))
    )
    }

    val totalDots = mockGoals.sumOf { it.totalDays }
    val autoScale = when {
        totalDots > 300 -> 0.75f
        totalDots > 150 -> 0.88f
        else -> 1.0f
    }
    val effectiveDotSize = (scale * 6.dp * autoScale * dotSizeMultiplier)

    val columns = 16

    Column(
        modifier = Modifier.padding(horizontal = scale * 8.dp, vertical = scale * 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        mockGoals.forEach { goal ->
            val rows = ceil(goal.totalDays / columns.toFloat()).toInt()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(scale * 6.dp)
            ) {
                Text(
                    text = goal.title,
                    color = Color.White,
                    fontSize = scale * 11.sp,
                    fontWeight = FontWeight.Normal
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(scale * 3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    repeat(rows) { rowIndex ->
                        Row(horizontalArrangement = Arrangement.spacedBy(scale * 3.dp)) {
                            repeat(columns) { colIndex ->
                                val dayIndex = rowIndex * columns + colIndex
                                if (dayIndex < goal.totalDays) {
                                    val dotColor = when {
                                        dayIndex == goal.currentDayIndex -> dotTheme.today.toColors()
                                        dayIndex < goal.currentDayIndex -> dotTheme.filled.toColors()
                                        else -> dotTheme.empty.toColors()
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(effectiveDotSize)
                                            .clip(gridStyle.shape)
                                            .background(dotColor)
                                    )
                                } else {
                                    Spacer(Modifier.size(effectiveDotSize))
                                }
                            }
                        }
                    }
                }
                if (showLabel) {
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Text("${goal.daysLeft}d left ", color = dotTheme.today.toColors(), fontWeight = FontWeight.Bold, fontSize = scale * 8.sp)
                        Text("· ${goal.percentComplete}%", color = Color.Gray, fontSize = scale * 8.sp)
                    }
                }
                Spacer(Modifier.height(scale * 8.dp))
            }
        }
    }
}
