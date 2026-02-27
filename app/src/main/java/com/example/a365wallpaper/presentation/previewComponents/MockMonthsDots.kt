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
import com.example.a365wallpaper.data.Local.GridStyle
import com.example.a365wallpaper.data.Local.SpecialDateOfMonth
import com.example.a365wallpaper.ui.theme.DotTheme
import com.example.a365wallpaper.utils.toColors
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun MockMonthsDots(
    dotTheme: DotTheme,
    gridStyle: GridStyle,
    scale: Float = 1.0f,
    showLabel: Boolean,
    dotSizeMultiplier: Float = 1.0f,
    showNumberInsteadOfDots: Boolean = false,
    showBothNumberAndDot: Boolean = false,
    specialDates: List<SpecialDateOfMonth> = emptyList(),
    // availableWidth lets us auto-fit; caller passes the phone screen width in dp
    availableWidth: Dp = 180.dp,
) {
    val currentDate = LocalDate.now()
    val noOfDays    = YearMonth.from(currentDate).lengthOfMonth()
    val todayIndex  = currentDate.dayOfMonth - 1   // 0-based
    val columns     = 7

    // ── Special dates index ───────────────────────────────────────────────────
    val specialColorByIndex: Map<Int, Color> = remember(specialDates, noOfDays) {
        val monthStart = currentDate.withDayOfMonth(1)
        val monthEnd   = currentDate.withDayOfMonth(noOfDays)
        buildMap {
            specialDates.forEach { sd ->
                val rangeStart = maxOf(sd.startDate, monthStart)
                val rangeEnd   = minOf(sd.endDate, monthEnd)
                if (rangeStart.isAfter(rangeEnd)) return@forEach
                var d = rangeStart
                while (!d.isAfter(rangeEnd)) {
                    val idx = d.dayOfMonth - 1
                    if (!containsKey(idx)) put(idx, Color(sd.colorArgb))
                    d = d.plusDays(1)
                }
            }
        }
    }

    // ── Auto-fit dot size to available width ──────────────────────────────────
    // Formula mirrors generateMonthDotsBitmap:
    //   maxDiameter = availableWidth / (cols * (1 + ratio) - ratio)
    //   dotSize     = maxDiameter * multiplier  (clamped)
    val ratio       = 0.45f
    val denomW      = columns * (1f + ratio) - ratio
    val maxDotSize  = availableWidth / denomW * scale  // Dp
    val dotSize     = (maxDotSize * dotSizeMultiplier.coerceIn(0.25f, 1.0f))
        .coerceIn(4.dp, maxDotSize)
    val gap         = dotSize * ratio

    val numberFontSize = remember(dotSize) {
        with(Any()) { (dotSize.value * 0.52f).coerceIn(4f, 14f).sp }
    }

    val rows = (noOfDays + columns - 1) / columns

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        repeat(rows) { rowIndex ->
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                repeat(columns) { colIndex ->
                    val dayIndex = rowIndex * columns + colIndex

                    if (dayIndex >= noOfDays) {
                        // Transparent padding cell — keeps row width consistent
                        Box(modifier = Modifier.size(dotSize))
                    } else {
                        val specialColor = specialColorByIndex[dayIndex]
                        val dotColor: Color = specialColor ?: when {
                            dayIndex == todayIndex -> dotTheme.today.toColors()
                            dayIndex < todayIndex  -> dotTheme.filled.toColors()
                            else                   -> dotTheme.empty.toColors()
                        }
                        val dayLabel           = (dayIndex + 1).toString()
                        val numberOnDotColor   = if (dayIndex <= todayIndex || specialColor != null)
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

        if (showLabel) {
            val daysLeft = noOfDays - currentDate.dayOfMonth
            val percent  = currentDate.dayOfMonth * 100 / noOfDays
            Spacer(Modifier.height(gap))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("${daysLeft}d left ", color = dotTheme.today.toColors(),
                    fontWeight = FontWeight.Bold, fontSize = scale * 8.sp)
                Text("· $percent%", color = Color.Gray, fontSize = scale * 8.sp)
            }
        }
    }
}
