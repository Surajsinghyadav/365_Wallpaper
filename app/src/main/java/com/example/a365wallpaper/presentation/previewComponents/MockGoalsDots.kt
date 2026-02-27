package com.example.a365wallpaper.presentation.previewComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.example.a365wallpaper.BitmapGenerators.Goal
import com.example.a365wallpaper.data.Local.GridStyle
import com.example.a365wallpaper.data.Local.SpecialDateOfGoal
import com.example.a365wallpaper.ui.theme.DotTheme
import com.example.a365wallpaper.utils.toColors
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

@Composable
fun MockGoalsDots(
    dotTheme: DotTheme,
    gridStyle: GridStyle,
    scale: Float = 1.0f,
    showLabel: Boolean,
    goals: List<Goal> = emptyList(),
    dotSizeMultiplier: Float = 1.0f,
    showNumberInsteadOfDots: Boolean = false,
    showBothNumberAndDot: Boolean = false,
    specialDates: List<SpecialDateOfGoal> = emptyList(),
    availableWidth: Dp = 180.dp,
) {
    val mockGoals = goals.ifEmpty {
        listOf(
            Goal("Personal", java.time.LocalDate.now().minusDays(22), java.time.LocalDate.now().plusDays(68)),
            Goal("Fitness",  java.time.LocalDate.now().minusDays(15), java.time.LocalDate.now().plusDays(145))
        )
    }

    val columns   = 16
    val totalDots = mockGoals.sumOf { it.totalDays }
    val autoScale = when {
        totalDots > 300 -> 0.75f
        totalDots > 150 -> 0.88f
        else            -> 1.0f
    }

    // ── Auto-fit dot size — same formula as generateGoalsDotsBitmap ───────────
    val ratio      = 0.45f
    val denomW     = columns * (1f + ratio) - ratio
    val maxDotSize = availableWidth / denomW * scale
    val dotSize    = (maxDotSize * (autoScale * dotSizeMultiplier.coerceIn(0.25f, 1.0f)).coerceIn(0.1f, 1.0f))
        .coerceIn(3.dp, maxDotSize)
    val gap        = dotSize * ratio

    val numberFontSize = remember(dotSize) {
        (dotSize.value * 0.52f).coerceIn(3f, 12f).sp
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(gap * 2f),   // spacing between goal blocks
    ) {
        mockGoals.forEach { goal ->
            val rows = ceil(goal.totalDays / columns.toFloat()).toInt()

            // ── Special dates index for this goal ─────────────────────────────
            val specialColorByIndex: Map<Int, Color> = remember(specialDates, goal.title) {
                buildMap {
                    specialDates.filter { it.goalTitle == goal.title }.forEach { sd ->
                        val rangeStart = maxOf(sd.startDate, goal.startDate)
                        val rangeEnd   = minOf(sd.endDate, goal.deadline)
                        if (rangeStart.isAfter(rangeEnd)) return@forEach
                        var d = rangeStart
                        while (!d.isAfter(rangeEnd)) {
                            val idx = ChronoUnit.DAYS.between(goal.startDate, d).toInt()
                                .coerceIn(0, goal.totalDays - 1)
                            if (!containsKey(idx)) put(idx, Color(sd.colorArgb))
                            d = d.plusDays(1)
                        }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(gap),
            ) {
                // Title
                Text(goal.title, color = Color.White,
                    fontSize = scale * 10.sp, fontWeight = FontWeight.Normal)

                // Grid
                repeat(rows) { rowIndex ->
                    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                        repeat(columns) { colIndex ->
                            val dayIndex = rowIndex * columns + colIndex

                            if (dayIndex >= goal.totalDays) {
                                Box(Modifier.size(dotSize))   // transparent padding
                            } else {
                                val specialColor = specialColorByIndex[dayIndex]
                                val dotColor: Color = specialColor ?: when {
                                    dayIndex == goal.currentDayIndex -> dotTheme.today.toColors()
                                    dayIndex < goal.currentDayIndex  -> dotTheme.filled.toColors()
                                    else                             -> dotTheme.empty.toColors()
                                }
                                val dayLabel         = (dayIndex + 1).toString()
                                val numberOnDotColor = if (dayIndex <= goal.currentDayIndex || specialColor != null)
                                    dotTheme.bg.toColors() else dotColor

                                when {
                                    showNumberInsteadOfDots && !showBothNumberAndDot -> {
                                        Box(Modifier.size(dotSize), Alignment.Center) {
                                            Text(dayLabel, color = dotColor,
                                                fontSize = numberFontSize, fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center, maxLines = 1,
                                                lineHeight = numberFontSize)
                                        }
                                    }
                                    showNumberInsteadOfDots && showBothNumberAndDot -> {
                                        Box(
                                            Modifier.size(dotSize).clip(gridStyle.shape).background(dotColor),
                                            Alignment.Center
                                        ) {
                                            Text(dayLabel, color = numberOnDotColor,
                                                fontSize = numberFontSize, fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center, maxLines = 1,
                                                lineHeight = numberFontSize)
                                        }
                                    }
                                    else -> Box(Modifier.size(dotSize).clip(gridStyle.shape).background(dotColor))
                                }
                            }
                        }
                    }
                }

                // Stats label
                if (showLabel) {
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Text("${goal.daysLeft}d left ", color = dotTheme.today.toColors(),
                            fontWeight = FontWeight.Bold, fontSize = scale * 8.sp)
                        Text("· ${goal.percentComplete}%", color = Color.Gray, fontSize = scale * 8.sp)
                    }
                }
            }
        }
    }
}
