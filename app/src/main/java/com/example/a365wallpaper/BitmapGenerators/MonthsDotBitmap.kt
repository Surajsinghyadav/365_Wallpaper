package com.example.a365wallpaper.BitmapGenerators

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import com.example.a365wallpaper.data.Local.GridStyle
import com.example.a365wallpaper.data.Local.SpecialDateOfMonth
import com.example.a365wallpaper.ui.theme.DotTheme
import com.example.a365wallpaper.ui.theme.DotThemes
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.ceil

data class MonthDotsSpec(
    val year: Int = LocalDate.now().year,
    val month: Int = LocalDate.now().monthValue,
    val currentDayOfMonth: Int = LocalDate.now().dayOfMonth,
    val columns: Int = 7,
    val gridStyle: GridStyle = GridStyle.Dots,
    val verticalBias: Float = 0f,
    val theme: DotTheme = DotThemes.All.first(),
    val gridToTextGapFrac: Float = 0.03f,
    // Gap-to-diameter ratio — controls spacing between dots
    val gapToDiameterRatio: Float = 0.45f,
    // Side + vertical padding as fraction of screen dimension
    val sidePaddingFrac: Float = 0.06f,
    val verticalPaddingFrac: Float = 0.06f,
    val showLabel: Boolean = true,
    // multiplier range: 0.25f (tightest) → 1.0f (fills available width perfectly)
    val dotSizeMultiplier: Float = 1.0f,
    val specialDates: List<SpecialDateOfMonth> = emptyList(),
    val showNumberInsteadOfDots: Boolean = false,
    val showBothNumberAndDot: Boolean = false,
)

fun generateMonthDotsBitmap(
    widthPx: Int,
    heightPx: Int,
    spec: MonthDotsSpec,
): Bitmap {
    val bmp    = createBitmap(widthPx, heightPx)
    val canvas = Canvas(bmp)
    canvas.drawColor(spec.theme.bg)

    val totalDays  = YearMonth.of(spec.year, spec.month).lengthOfMonth()
    val todayIndex = (spec.currentDayOfMonth - 1).coerceIn(0, totalDays - 1)
    val cols       = spec.columns.coerceAtLeast(1)
    val rows       = ceil(totalDays / cols.toFloat()).toInt()

    // ── Padding ───────────────────────────────────────────────────────────────
    val sidePadding     = widthPx  * spec.sidePaddingFrac
    val verticalPadding = heightPx * spec.verticalPaddingFrac

    // ── Label text sizes ──────────────────────────────────────────────────────
    val daysLeft  = totalDays - spec.currentDayOfMonth
    val percent   = (spec.currentDayOfMonth * 100) / totalDays
    val leftText  = "${daysLeft}d left"
    val rightText = " · $percent%"

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = (widthPx * 0.04f).coerceIn(28f, 56f)
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }
    val textFm        = textPaint.fontMetrics
    val textHeight    = textFm.descent - textFm.ascent
    val gridToTextGap = heightPx * spec.gridToTextGapFrac

    // ── Dot sizing: auto-fit to available width, then apply multiplier ────────
    // "Available width" = screen width minus two side paddings.
    // Max diameter = width that perfectly fills the row with the gap ratio.
    // dotSizeMultiplier then scales it DOWN from that max (1.0 = fills width).
    val gridAvailableW  = (widthPx - 2f * sidePadding).coerceAtLeast(1f)
    val ratio           = spec.gapToDiameterRatio.coerceAtLeast(0f)
    val denomW          = (cols * (1f + ratio) - ratio).coerceAtLeast(0.0001f)
    val maxDiameter     = gridAvailableW / denomW           // fills available width at multiplier=1
    val diameter        = (maxDiameter * spec.dotSizeMultiplier.coerceIn(0.25f, 1.0f))
        .coerceIn(6f, widthPx.toFloat())                    // never smaller than 6px or bigger than screen
    val radius          = diameter / 2f
    val step            = diameter + diameter * ratio

    // ── Grid dimensions ───────────────────────────────────────────────────────
    val gridW = cols * step - diameter * ratio
    val gridH = rows * step - diameter * ratio

    // ── Total content height for BiasAlignment ────────────────────────────────
    val totalContentH  = verticalPadding + gridH + gridToTextGap + textHeight + verticalPadding
    val freeSpace      = heightPx - totalContentH
    val startY         = verticalPadding + freeSpace * (0.5f + spec.verticalBias * 0.5f)
    val startX         = (widthPx - gridW) / 2f
    val textBaselineY  = startY + gridH + gridToTextGap - textFm.ascent

    // ── Special dates index: dayIndex (0-based) → colorArgb ──────────────────
    val specialColorByIndex: Map<Int, Int> = buildMap {
        val monthStart = LocalDate.of(spec.year, spec.month, 1)
        val monthEnd   = monthStart.withDayOfMonth(totalDays)
        spec.specialDates.forEach { sd ->
            val rangeStart = maxOf(sd.startDate, monthStart)
            val rangeEnd   = minOf(sd.endDate, monthEnd)
            if (rangeStart.isAfter(rangeEnd)) return@forEach
            var d = rangeStart
            while (!d.isAfter(rangeEnd)) {
                val idx = d.dayOfMonth - 1  // 0-based
                if (!containsKey(idx)) put(idx, sd.colorArgb)
                d = d.plusDays(1)
            }
        }
    }

    // ── Number paint ──────────────────────────────────────────────────────────
    val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface  = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textSize  = (diameter * 0.52f).coerceIn(6f, 48f)
        textAlign = Paint.Align.CENTER
    }

    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // ── Draw each day ─────────────────────────────────────────────────────────
    for (i in 0 until totalDays) {
        val cx           = startX + (i % cols) * step + radius
        val cy           = startY + (i / cols) * step + radius
        val specialColor = specialColorByIndex[i]
        val dotColor     = specialColor ?: when {
            i == todayIndex -> spec.theme.today
            i < todayIndex  -> spec.theme.filled
            else            -> spec.theme.empty
        }

        when {
            // Mode A: number only
            spec.showNumberInsteadOfDots && !spec.showBothNumberAndDot -> {
                numberPaint.color = dotColor
                val numFm    = numberPaint.fontMetrics
                val baseline = cy - (numFm.ascent + numFm.descent) / 2f
                canvas.drawText((i + 1).toString(), cx, baseline, numberPaint)
            }

            // Mode B: dot + number overlaid
            spec.showNumberInsteadOfDots && spec.showBothNumberAndDot -> {
                dotPaint.color = dotColor
                canvas.drawDot(cx, cy, radius, diameter, spec.gridStyle, dotPaint)
                numberPaint.color = if (i <= todayIndex || specialColor != null)
                    spec.theme.bg else dotColor
                val numFm    = numberPaint.fontMetrics
                val baseline = cy - (numFm.ascent + numFm.descent) / 2f
                canvas.drawText((i + 1).toString(), cx, baseline, numberPaint)
            }

            // Mode C: normal dot
            else -> {
                dotPaint.color = dotColor
                canvas.drawDot(cx, cy, radius, diameter, spec.gridStyle, dotPaint)
            }
        }
    }

    // ── Label ─────────────────────────────────────────────────────────────────
    if (spec.showLabel) {
        textPaint.apply { typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); color = spec.theme.today }
        val leftW = textPaint.measureText(leftText)
        textPaint.apply { typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL); color = "#A8A8A8".toColorInt() }
        val startTextX = (widthPx - leftW - textPaint.measureText(rightText)) / 2f
        textPaint.apply { typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); color = spec.theme.today }
        canvas.drawText(leftText, startTextX, textBaselineY, textPaint)
        textPaint.apply { typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL); color = "#A8A8A8".toColorInt() }
        canvas.drawText(rightText, startTextX + leftW, textBaselineY, textPaint)
    }

    return bmp
}
