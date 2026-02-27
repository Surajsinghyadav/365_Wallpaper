package com.example.a365wallpaper.BitmapGenerators

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import com.example.a365wallpaper.data.Local.GridStyle
import com.example.a365wallpaper.data.Local.SpecialDateOfYear
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
    val specialDates: List<SpecialDateOfYear> = emptyList(),
    // ── NEW ──────────────────────────────────────────────────────────────────
    // Toggle 1: replace every dot with its day-of-year number
    val showNumberInsteadOfDots: Boolean = false,
    // Toggle 2 (child of Toggle 1): draw dot AND number overlaid
    val showBothNumberAndDot: Boolean = false,
)

fun generateYearDotsBitmap(
    widthPx: Int,
    heightPx: Int,
    spec: YearDotsSpec,
): Bitmap {
    val today      = LocalDate.now()
    val todayIndex = (today.dayOfYear - 1).coerceIn(0, today.lengthOfYear() - 1)
    val totalDays  = today.lengthOfYear()
    val currentYear = today.year

    val bmp    = createBitmap(widthPx, heightPx)
    val canvas = Canvas(bmp)
    canvas.drawColor(spec.theme.bg)

    val cols = spec.columns.coerceAtLeast(1)
    val rows = ceil(totalDays / cols.toFloat()).toInt()

    // ── Side padding ──────────────────────────────────────────────────────────
    val sidePadding    = widthPx * spec.sidePaddingFrac
    val gridAvailableW = (widthPx - 2f * sidePadding).coerceAtLeast(1f)
    val ratio          = spec.gapToDiameterRatio.coerceAtLeast(0f)

    // ── Diameter from WIDTH (original formula) ────────────────────────────────
    val colDenominator = (cols * (1f + ratio) - ratio).coerceAtLeast(0.0001f)
    val diameterFromW  = (gridAvailableW / colDenominator).coerceIn(6f, 64f)

    // ── Label height estimation (needed before we know diameter) ──────────────
    // Use a provisional textSize based on width to estimate vertical space needed
    val provisionalTextSize = (widthPx * 0.04f).coerceIn(28f, 56f)
    val provisionalTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = provisionalTextSize
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }
    val provisionalTextFm = provisionalTextPaint.fontMetrics
    val provisionalTextH  = provisionalTextFm.descent - provisionalTextFm.ascent
    val gridToTextGap     = heightPx * spec.gridToTextGapFrac

    // ── Vertical budget: height minus label area and padding ──────────────────
    // We want the grid to fit within a vertical zone with topPad + bottomPad
    val labelReserve    = if (spec.showLabel) provisionalTextH + gridToTextGap else 0f
    val verticalPadding = heightPx * (spec.topPaddingFrac + spec.bottomPaddingFrac)
    val gridAvailableH  = (heightPx - verticalPadding - labelReserve).coerceAtLeast(1f)

    // ── Diameter from HEIGHT ──────────────────────────────────────────────────
    val rowDenominator = (rows * (1f + ratio) - ratio).coerceAtLeast(0.0001f)
    val diameterFromH  = (gridAvailableH / rowDenominator).coerceIn(6f, 64f)

    // ── Use the smaller of the two so grid fits BOTH dimensions ───────────────
    val diameter = minOf(diameterFromW, diameterFromH)
    val radius   = diameter / 2f
    val step     = diameter + diameter * ratio
    val gridW    = cols * step - diameter * ratio
    val gridH    = rows * step - diameter * ratio

    // ── Label paint (final, uses same diameter-independent textSize) ──────────
    val daysLeft  = (totalDays - 1) - todayIndex
    val percent   = ((todayIndex + 1) * 100) / totalDays
    val leftText  = "${daysLeft}d left"
    val rightText = " · $percent%"

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = provisionalTextSize
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }
    val textFm     = textPaint.fontMetrics
    val textHeight = textFm.descent - textFm.ascent

    // ── BiasAlignment — only uses remaining free space, never overflows ───────
    val totalContentH = gridH + (if (spec.showLabel) gridToTextGap + textHeight else 0f)
    val freeSpace     = (heightPx - totalContentH).coerceAtLeast(0f)
    val startY        = freeSpace * (0.5f + spec.verticalBias * 0.5f)
    val startX        = (widthPx - gridW) / 2f
    val textBaselineY = startY + gridH + gridToTextGap - textFm.ascent

    // ── Special dates index ───────────────────────────────────────────────────
    val specialColorByIndex: Map<Int, Int> = buildMap {
        val yearStart = LocalDate.ofYearDay(currentYear, 1)
        val yearEnd   = LocalDate.ofYearDay(currentYear, totalDays)
        spec.specialDates.forEach { sd ->
            val rangeStart = maxOf(sd.startDate, yearStart)
            val rangeEnd   = minOf(sd.endDate, yearEnd)
            if (rangeStart.isAfter(rangeEnd)) return@forEach
            var d = rangeStart
            while (!d.isAfter(rangeEnd)) {
                val idx = d.dayOfYear - 1
                if (!containsKey(idx)) put(idx, sd.colorArgb)
                d = d.plusDays(1)
            }
        }
    }

    // ── Number paint ──────────────────────────────────────────────────────────
    val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface  = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textSize  = (diameter * 0.52f).coerceIn(6f, 36f)
        textAlign = Paint.Align.CENTER
    }

    // ── Draw dots / numbers ───────────────────────────────────────────────────
    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    for (i in 0 until totalDays) {
        val cx = startX + (i % cols) * step + radius
        val cy = startY + (i / cols) * step + radius

        val specialColor = specialColorByIndex[i]
        val dotColor = specialColor ?: when {
            i == todayIndex -> spec.theme.today
            i < todayIndex  -> spec.theme.filled
            else            -> spec.theme.empty
        }

        when {
            spec.showNumberInsteadOfDots && !spec.showBothNumberAndDot -> {
                numberPaint.color = dotColor
                val numFm    = numberPaint.fontMetrics
                val baseline = cy - (numFm.ascent + numFm.descent) / 2f
                canvas.drawText((i + 1).toString(), cx, baseline, numberPaint)
            }
            spec.showNumberInsteadOfDots && spec.showBothNumberAndDot -> {
                dotPaint.color = dotColor
                canvas.drawDot(cx, cy, radius, diameter, spec.gridStyle, dotPaint)
                numberPaint.color = if (i <= todayIndex || specialColor != null)
                    spec.theme.bg else dotColor
                val numFm    = numberPaint.fontMetrics
                val baseline = cy - (numFm.ascent + numFm.descent) / 2f
                canvas.drawText((i + 1).toString(), cx, baseline, numberPaint)
            }
            else -> {
                dotPaint.color = dotColor
                canvas.drawDot(cx, cy, radius, diameter, spec.gridStyle, dotPaint)
            }
        }
    }

    // ── Draw label ────────────────────────────────────────────────────────────
    if (spec.showLabel) {
        textPaint.apply {
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

