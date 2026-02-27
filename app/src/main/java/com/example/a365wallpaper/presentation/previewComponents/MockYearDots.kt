package com.example.a365wallpaper.presentation.previewComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.example.a365wallpaper.data.Local.GridStyle
import com.example.a365wallpaper.data.Local.SpecialDateOfYear
import com.example.a365wallpaper.utils.toColors
import com.example.a365wallpaper.ui.theme.DotTheme
import java.time.LocalDate

// Total mock dots: 18 rows × 14 cols = 252 cells
// todayIndex = 162 → row 11, col 8 (0-based)
private const val MOCK_COLS       = 14
private const val MOCK_ROWS       = 18
private const val MOCK_TODAY_IDX  = 162
private const val MOCK_TOTAL_DAYS = 252
private const val MOCK_DAYS_LEFT  = 68
private const val MOCK_PERCENT    = 83

@Composable
fun MockYearsDots(
    dotTheme: DotTheme,
    gridStyle: GridStyle,
    scale: Float = 1.0f,
    showLabel: Boolean,
    showNumberInsteadOfDots: Boolean = false,
    showBothNumberAndDot: Boolean = false,
    // ── NEW ──────────────────────────────────────────────────────────────────
    specialDates: List<SpecialDateOfYear> = emptyList(),
) {
    val numberFontSize = remember(scale) { (scale * 4.2f).sp }

    // ── Build special-date index for mock year ──────────────────────────────
    // The mock represents the current year. We map each SpecialDateOfYear's
    // range to 0-based day indices (same as the bitmap generator does).
    val specialColorByIndex: Map<Int, Color> = remember(specialDates) {
        val today       = LocalDate.now()
        val currentYear = today.year
        val yearStart   = LocalDate.ofYearDay(currentYear, 1)
        val yearEnd     = LocalDate.ofYearDay(currentYear, today.lengthOfYear())

        buildMap {
            specialDates.forEach { sd ->
                val rangeStart = maxOf(sd.startDate, yearStart)
                val rangeEnd   = minOf(sd.endDate, yearEnd)
                if (rangeStart.isAfter(rangeEnd)) return@forEach

                var d = rangeStart
                while (!d.isAfter(rangeEnd)) {
                    // Convert real day-of-year (1-based) to mock index (0-based).
                    // Scale: real year has ~365 days, mock has 252 cells.
                    // We map proportionally so the highlighted band appears in the
                    // right visual region of the preview.
                    val realIdx   = d.dayOfYear - 1          // 0-based, 0..364
                    val mockIdx   = (realIdx * MOCK_TOTAL_DAYS / today.lengthOfYear())
                        .coerceIn(0, MOCK_TOTAL_DAYS - 1)

                    if (!containsKey(mockIdx)) put(mockIdx, Color(sd.colorArgb))
                    d = d.plusDays(1)
                }
            }
        }
    }

    Column(
        modifier = Modifier.padding(vertical = scale * 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(scale * 6.dp)
    ) {
        repeat(MOCK_ROWS) { r ->
            Row(horizontalArrangement = Arrangement.spacedBy(scale * 6.dp)) {
                repeat(MOCK_COLS) { c ->
                    val idx       = r * MOCK_COLS + c
                    val isPadding = idx >= MOCK_TOTAL_DAYS

                    // Special color overrides filled/empty/today — first entry wins
                    val specialColor = specialColorByIndex[idx]

                    val dotColor: Color = when {
                        isPadding             -> Color.Transparent
                        specialColor != null  -> specialColor                  // ← special date
                        idx == MOCK_TODAY_IDX -> dotTheme.today.toColors()
                        idx < MOCK_TODAY_IDX  -> dotTheme.filled.toColors()
                        else                  -> dotTheme.empty.toColors()
                    }

                    val dayLabel = (idx + 1).toString()

                    // For number contrast: special + filled + today → bg color on top
                    val numberOnDotColor: Color = when {
                        isPadding                                         -> Color.Transparent
                        specialColor != null || idx <= MOCK_TODAY_IDX     -> dotTheme.bg.toColors()
                        else                                              -> dotColor
                    }

                    when {
                        // Padding cell
                        isPadding -> Box(modifier = Modifier.size(scale * 6.dp))

                        // Mode A: numbers only
                        showNumberInsteadOfDots && !showBothNumberAndDot -> {
                            Box(
                                modifier         = Modifier.size(scale * 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text       = dayLabel,
                                    color      = dotColor,
                                    fontSize   = numberFontSize,
                                    fontWeight = FontWeight.Bold,
                                    textAlign  = TextAlign.Center,
                                    lineHeight = numberFontSize,
                                    maxLines   = 1,
                                )
                            }
                        }

                        // Mode B: dot + number overlaid
                        showNumberInsteadOfDots && showBothNumberAndDot -> {
                            Box(
                                modifier = Modifier
                                    .size(scale * 6.dp)
                                    .clip(gridStyle.shape)
                                    .background(dotColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text       = dayLabel,
                                    color      = numberOnDotColor,
                                    fontSize   = numberFontSize,
                                    fontWeight = FontWeight.Bold,
                                    textAlign  = TextAlign.Center,
                                    lineHeight = numberFontSize,
                                    maxLines   = 1,
                                )
                            }
                        }

                        // Mode C: normal dot
                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(scale * 6.dp)
                                    .clip(gridStyle.shape)
                                    .background(dotColor)
                            )
                        }
                    }
                }
            }
        }

        if (showLabel) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "${MOCK_DAYS_LEFT}d left ",
                    color      = dotTheme.today.toColors(),
                    fontWeight = FontWeight.Bold,
                    fontSize   = scale * 8.sp
                )
                Text(
                    "· $MOCK_PERCENT%",
                    color    = Color.Gray,
                    fontSize = scale * 8.sp
                )
            }
        }
    }
}
