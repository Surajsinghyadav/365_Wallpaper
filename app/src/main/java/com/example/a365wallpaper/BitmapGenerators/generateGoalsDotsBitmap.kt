package com.example.a365wallpaper.BitmapGenerators

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.runtime.Stable
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import com.example.a365wallpaper.data.Local.GridStyle
import com.example.a365wallpaper.data.Local.SpecialDateOfGoal
import com.example.a365wallpaper.data.LocalDateSerializer
import com.example.a365wallpaper.ui.theme.DotTheme
import com.example.a365wallpaper.ui.theme.DotThemes
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
@Stable
@Serializable
data class Goal(
    val title: String,
    @Serializable(with = LocalDateSerializer::class) val startDate: LocalDate,
    @Serializable(with = LocalDateSerializer::class) val deadline: LocalDate,
) {
    val totalDays: Int       = ChronoUnit.DAYS.between(startDate, deadline).toInt() + 1
    val currentDayIndex: Int = ChronoUnit.DAYS.between(startDate, LocalDate.now()).toInt().coerceIn(0, totalDays - 1)
    val daysLeft: Int        = (totalDays - 1) - currentDayIndex
    val percentComplete: Int = ((currentDayIndex + 1) * 100) / totalDays
}

data class GoalsDotsSpec(
    val goals: List<Goal>,
    val columns: Int = 15,
    val gridStyle: GridStyle = GridStyle.Dots,
    val verticalBias: Float = 0f,
    val theme: DotTheme = DotThemes.All.first(),
    val goalSpacingFrac: Float = 0.05f,
    val titleToGridGapFrac: Float = 0.02f,
    val gridToTextGapFrac: Float = 0.02f,
    // Gap-to-diameter ratio
    val gapToDiameterRatio: Float = 0.45f,
    val sidePaddingFrac: Float = 0.06f,
    val verticalPaddingFrac: Float = 0.05f,
    val showLabel: Boolean = true,
    // multiplier range: 0.25f (tightest) → 1.0f (fills available width perfectly)
    val dotSizeMultiplier: Float = 1.0f,
    val specialDates: List<SpecialDateOfGoal> = emptyList(),
    val showNumberInsteadOfDots: Boolean = false,
    val showBothNumberAndDot: Boolean = false,
)

fun generateGoalsDotsBitmap(
    widthPx: Int,
    heightPx: Int,
    spec: GoalsDotsSpec,
): Bitmap {
    require(spec.goals.size in 1..2) { "Must have 1 or 2 goals" }

    val bmp    = createBitmap(widthPx, heightPx)
    val canvas = Canvas(bmp)
    canvas.drawColor(spec.theme.bg)

    val goalCount       = spec.goals.size
    val sidePadding     = widthPx  * spec.sidePaddingFrac
    val verticalPadding = heightPx * spec.verticalPaddingFrac

    // ── Auto-scale: more total dots → smaller base diameter ───────────────────
    // Then dotSizeMultiplier scales down from perfect-fit (1.0 = fills width).
    val totalDots = spec.goals.sumOf { it.totalDays }
    val autoScale = when {
        totalDots > 300 -> 0.75f
        totalDots > 150 -> 0.88f
        else            -> 1.0f
    }
    val effectiveMultiplier = (autoScale * spec.dotSizeMultiplier.coerceIn(0.25f, 1.0f))
        .coerceIn(0.1f, 1.0f)

    // ── Shared geometry (same dot size for all goals, consistent visual) ──────
    val cols           = spec.columns.coerceAtLeast(1)
    val gridAvailableW = (widthPx - 2f * sidePadding).coerceAtLeast(1f)
    val ratio          = spec.gapToDiameterRatio.coerceAtLeast(0f)
    val denomW         = (cols * (1f + ratio) - ratio).coerceAtLeast(0.0001f)
    val maxDiameter    = gridAvailableW / denomW
    val diameter       = (maxDiameter * effectiveMultiplier).coerceIn(6f, widthPx.toFloat())
    val radius         = diameter / 2f
    val step           = diameter + diameter * ratio

    // ── Measure label text sizes ──────────────────────────────────────────────
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize  = (widthPx * 0.045f).coerceIn(24f, 56f)
        typeface  = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        color     = "#FFFFFF".toColorInt()
        textAlign = Paint.Align.CENTER
    }
    val titleH = titlePaint.fontMetrics.let { it.descent - it.ascent }

    val statsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = (widthPx * 0.035f).coerceIn(20f, 48f)
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }
    val statsFm = statsPaint.fontMetrics
    val statsH  = statsFm.descent - statsFm.ascent

    val titleToGridGap = heightPx * spec.titleToGridGapFrac
    val gridToTextGap  = heightPx * spec.gridToTextGapFrac

    // ── Per-goal heights ──────────────────────────────────────────────────────
    fun goalBlockH(goal: Goal): Float {
        val rows  = ceil(goal.totalDays / cols.toFloat()).toInt()
        val gridH = rows * step - diameter * ratio
        return titleH + titleToGridGap + gridH + gridToTextGap + statsH
    }

    val goalSpacing       = if (goalCount == 2) heightPx * spec.goalSpacingFrac else 0f
    val totalContentH     = verticalPadding +
            spec.goals.sumOf { goalBlockH(it).toDouble() }.toFloat() +
            (goalCount - 1) * goalSpacing +
            verticalPadding
    val freeSpace         = heightPx - totalContentH
    var currentY          = verticalPadding + freeSpace * (0.5f + spec.verticalBias * 0.5f)

    // ── Number paint ──────────────────────────────────────────────────────────
    val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface  = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textSize  = (diameter * 0.52f).coerceIn(6f, 48f)
        textAlign = Paint.Align.CENTER
    }

    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // ── Draw each goal block ──────────────────────────────────────────────────
    spec.goals.forEachIndexed { goalIndex, goal ->

        // Special dates index for this goal: dayIndex (0-based) → colorArgb
        val specialColorByIndex: Map<Int, Int> = buildMap {
            spec.specialDates.filter { it.goalTitle == goal.title }.forEach { sd ->
                val rangeStart = maxOf(sd.startDate, goal.startDate)
                val rangeEnd   = minOf(sd.endDate,   goal.deadline)
                if (rangeStart.isAfter(rangeEnd)) return@forEach
                var d = rangeStart
                while (!d.isAfter(rangeEnd)) {
                    val idx = ChronoUnit.DAYS.between(goal.startDate, d).toInt()
                        .coerceIn(0, goal.totalDays - 1)
                    if (!containsKey(idx)) put(idx, sd.colorArgb)
                    d = d.plusDays(1)
                }
            }
        }

        // Title
        val titleY = currentY - titlePaint.fontMetrics.ascent
        canvas.drawText(goal.title, widthPx / 2f, titleY, titlePaint)
        currentY += titleH + titleToGridGap

        // Grid
        val rows       = ceil(goal.totalDays / cols.toFloat()).toInt()
        val gridW      = cols * step - diameter * ratio
        val gridH      = rows * step - diameter * ratio
        val startX     = (widthPx - gridW) / 2f
        val gridStartY = currentY

        for (i in 0 until goal.totalDays) {
            val cx           = startX + (i % cols) * step + radius
            val cy           = gridStartY + (i / cols) * step + radius
            val specialColor = specialColorByIndex[i]
            val dotColor     = specialColor ?: when {
                i == goal.currentDayIndex -> spec.theme.today
                i < goal.currentDayIndex  -> spec.theme.filled
                else                      -> spec.theme.empty
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
                    numberPaint.color = if (i <= goal.currentDayIndex || specialColor != null)
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
        currentY += gridH + gridToTextGap

        // Stats label
        if (spec.showLabel) {
            val leftText  = "${goal.daysLeft}d left"
            val rightText = " · ${goal.percentComplete}%"
            val textBaselineY = currentY - statsFm.ascent
            statsPaint.apply { typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); color = spec.theme.today }
            val leftW = statsPaint.measureText(leftText)
            statsPaint.apply { typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL); color = "#A8A8A8".toColorInt() }
            val startTextX = (widthPx - leftW - statsPaint.measureText(rightText)) / 2f
            statsPaint.apply { typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); color = spec.theme.today }
            canvas.drawText(leftText, startTextX, textBaselineY, statsPaint)
            statsPaint.apply { typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL); color = "#A8A8A8".toColorInt() }
            canvas.drawText(rightText, startTextX + leftW, textBaselineY, statsPaint)
        }
        currentY += statsH

        // Inter-goal spacing
        if (goalIndex < spec.goals.size - 1) currentY += goalSpacing
    }

    return bmp
}
