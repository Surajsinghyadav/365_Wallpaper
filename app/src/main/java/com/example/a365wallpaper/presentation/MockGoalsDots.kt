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
import com.example.a365wallpaper.data.GridStyle
import com.example.a365wallpaper.toColors
import com.example.a365wallpaper.ui.theme.DotTheme
import kotlin.math.ceil

@Composable
fun MockGoalsDots(
    dotTheme: DotTheme,
    gridStyle: GridStyle,
    scale: Float = 1.0f,
    showLabel: Boolean
) {
    val mockGoals = listOf(
        MockGoal(
            title = "Personal",
            totalDays = 90,
            currentDayIndex = 22,
            daysLeft = 68,
            percentComplete = 83
        ),
        MockGoal(
            title = "Fitness",
            totalDays = 160,
            currentDayIndex = 15,
            daysLeft = 45,
            percentComplete = 25
        )
    )

    val columns = 16

    Column(
        modifier = Modifier
            .padding(horizontal = scale * 8.dp, vertical = scale * 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        mockGoals.forEach { goal ->
            MockSingleGoal(
                goal = goal,
                dotTheme = dotTheme,
                gridStyle = gridStyle,
                columns = columns,
                scale = scale,
                showLabel = showLabel,
                modifier = Modifier
            )
        }
    }
}

@Composable
private fun MockSingleGoal(
    goal: MockGoal,
    dotTheme: DotTheme,
    gridStyle: GridStyle,
    columns: Int,
    scale: Float,
    modifier: Modifier = Modifier,
    showLabel: Boolean
) {
    val rows = ceil(goal.totalDays / columns.toFloat()).toInt()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(scale * 8.dp)
    ) {
        // Title
        Text(
            text = goal.title,
            color = Color.White,
            fontSize = scale * 12.sp,
            fontWeight = FontWeight.Normal
        )

        // Dots Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(scale * 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            repeat(rows) { rowIndex ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(scale * 4.dp)
                ) {
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
                                    .size(scale * 6.dp)
                                    .clip(gridStyle.shape)
                                    .background(dotColor)
                            )
                        } else {
                            // Empty space for alignment
                            Spacer(Modifier.size(scale * 6.dp))
                        }
                    }
                }
            }
        }

        // Stats Text
        if (showLabel){
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${goal.daysLeft}d left ",
                    color = dotTheme.today.toColors(),
                    fontWeight = FontWeight.Bold,
                    fontSize = scale * 8.sp
                )
                Text(
                    text = "· ${goal.percentComplete}%",
                    color = Color.Gray,
                    fontSize = scale * 8.sp
                )
            }
        }
        Spacer(Modifier.height(scale * 4.dp))
    }
}

private data class MockGoal(
    val title: String,
    val totalDays: Int,
    val currentDayIndex: Int,
    val daysLeft: Int,
    val percentComplete: Int
)
