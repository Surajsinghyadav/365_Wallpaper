// app/src/main/java/com/example/a365wallpaper/generateYearDotsBitmap.kt
package com.example.a365wallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import com.example.a365wallpaper.data.GridStyle
import com.example.a365wallpaper.data.SpecialDateOfYear
import com.example.a365wallpaper.data.specialColorFor
import com.example.a365wallpaper.ui.theme.DotTheme
import com.example.a365wallpaper.ui.theme.DotThemes
import java.time.LocalDate
import kotlin.math.ceil

data class YearDotsSpec(
    val columns: Int = 15,
    val gridStyle: GridStyle = GridStyle.Dots,
    val verticalBias: Float = 0f,
    val theme: DotTheme = DotThemes.All.first(),
    val showLabel: Boolean = true,
    val topPaddingFrac: Float = 0.20f,
    val bottomPaddingFrac: Float = 0.10f,
    val gridToTextGapFrac: Float = 0.03f,
    val gapToDiameterRatio: Float = 0.55f,
    val sidePaddingFrac: Float = 0.08f,
    // Special dates — empty by default so existing code paths are unaffected
    val specialDates: List<SpecialDateOfYear> = emptyList(),
)

fun generateYearDotsBitmap(
    widthPx: Int,
    heightPx: Int,
    spec: YearDotsSpec,
): Bitmap {
    // ── Temporal context ────────────────────────────────────────────────────
    val today         = LocalDate.now()
    val todayIndex    = (today.dayOfYear - 1).coerceIn(0, today.lengthOfYear() - 1)
    val totalDays     = today.lengthOfYear()
    val currentYear   = today.year

    // ── Canvas ──────────────────────────────────────────────────────────────
    val bmp    = createBitmap(widthPx, heightPx)
    val canvas = Canvas(bmp)
    canvas.drawColor(spec.theme.bg)

    val cols = spec.columns.coerceAtLeast(1)
    val rows = ceil(totalDays / cols.toFloat()).toInt()

    // ── Geometry ─────────────────────────────────────────────────────────────
    val sidePadding     = widthPx * spec.sidePaddingFrac
    val gridAvailableW  = (widthPx - 2f * sidePadding).coerceAtLeast(1f)
    val ratio           = spec.gapToDiameterRatio.coerceAtLeast(0f)
    val denomW          = (cols * (1f + ratio) - ratio).coerceAtLeast(0.0001f)
    val diameter        = (gridAvailableW / denomW).coerceIn(6f, 64f)
    val radius          = diameter / 2f
    val step            = diameter + diameter * ratio   // diameter + gap

    val gridW = cols * step - diameter * ratio
    val gridH = rows * step - diameter * ratio

    // ── Label measurement (done once, used for layout even if hidden) ────────
    val daysLeft  = (totalDays - 1) - todayIndex
    val percent   = ((todayIndex + 1) * 100) / totalDays
    val leftText  = "${daysLeft}d left"
    val rightText = " · $percent%"

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = (widthPx * 0.04f).coerceIn(28f, 56f)
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }
    val textFm      = textPaint.fontMetrics
    val textHeight  = textFm.descent - textFm.ascent
    val gridToTextGap = heightPx * spec.gridToTextGapFrac

    // ── BiasAlignment for the whole content block ────────────────────────────
    val totalContentHeight  = gridH + gridToTextGap + textHeight
    val freeSpace           = heightPx - totalContentHeight
    // verticalBias: -1 = top, 0 = center, +1 = bottom
    val startY              = freeSpace * (0.5f + spec.verticalBias * 0.5f)
    val startX              = (widthPx - gridW) / 2f
    val textBaselineY       = startY + gridH + gridToTextGap - textFm.ascent

    // ── Pre-index special dates by day-of-year for O(1) lookup ───────────────
    // Build a set of (dayIndex → color) for the current year only
    val specialColorByDayIndex: Map<Int, Int> = buildMap {
        spec.specialDates.forEach { sd ->
            // Clamp range to this year; skip if entirely outside
            val rangeStart = maxOf(sd.startDate, LocalDate.ofYearDay(currentYear, 1))
            val rangeEnd   = minOf(sd.endDate,   LocalDate.ofYearDay(currentYear, totalDays))
            if (rangeStart.isAfter(rangeEnd)) return@forEach
            var d = rangeStart
            while (!d.isAfter(rangeEnd)) {
                // putIfAbsent → first special date in list wins for overlapping ranges
                if (!containsKey(d.dayOfYear - 1)) put(d.dayOfYear - 1, sd.colorArgb)
                d = d.plusDays(1)
            }
        }
    }

    // ── Draw dots ────────────────────────────────────────────────────────────
    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    for (i in 0 until totalDays) {
        val cx = startX + (i % cols) * step + radius
        val cy = startY + (i / cols) * step + radius

        val specialColor = specialColorByDayIndex[i]

        dotPaint.color = specialColor ?: when {
            i == todayIndex -> spec.theme.today
            i < todayIndex  -> spec.theme.filled
            else            -> spec.theme.empty
        }

        canvas.drawDot(cx, cy, radius, diameter, spec.gridStyle, dotPaint)
    }

    // ── Draw label ───────────────────────────────────────────────────────────
    if (spec.showLabel) {
        textPaint.apply {
            textSize = (widthPx * 0.04f).coerceIn(28f, 56f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color    = spec.theme.today
        }
        val leftW = textPaint.measureText(leftText)

        textPaint.apply {
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            color    = "#A8A8A8".toColorInt()
        }
        val startTextX = (widthPx - leftW - textPaint.measureText(rightText)) / 2f

        textPaint.apply {
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color    = spec.theme.today
        }
        canvas.drawText(leftText, startTextX, textBaselineY, textPaint)

        textPaint.apply {
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            color    = "#A8A8A8".toColorInt()
        }
        canvas.drawText(rightText, startTextX + leftW, textBaselineY, textPaint)
    }

    return bmp
}
