package com.example.a365wallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import com.example.a365wallpaper.data.GridStyle
import com.example.a365wallpaper.ui.theme.DotTheme
import com.example.a365wallpaper.ui.theme.DotThemes
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

data class Goal(
    val title: String,
    val startDate: LocalDate,
    val deadline: LocalDate,
) {
    val totalDays: Int = ChronoUnit.DAYS.between(startDate, deadline).toInt() + 1
    val currentDayIndex: Int = ChronoUnit.DAYS.between(startDate, LocalDate.now()).toInt().coerceIn(0, totalDays - 1)
    val daysLeft: Int = (totalDays - 1) - currentDayIndex
    val percentComplete: Int = ((currentDayIndex + 1) * 100) / totalDays
}

data class GoalsDotsSpec(
    val goals: List<Goal>,
    val columns: Int = 15,
    val gridStyle: GridStyle = GridStyle.Dots,
    val verticalBias: Float = 0f,
    val theme: DotTheme = DotThemes.All.first(),

    val topPaddingFrac: Float = 0.12f,
    val bottomPaddingFrac: Float = 0.08f,
    val goalSpacingFrac: Float = 0.08f,
    val titleToGridGapFrac: Float = 0.02f,
    val gridToTextGapFrac: Float = 0.02f,
    val gapToDiameterRatio: Float = 0.55f,
    val sidePaddingFrac: Float = 0.08f,
    val showLabel: Boolean,
)

fun generateGoalsDotsBitmap(
    widthPx: Int,
    heightPx: Int,
    spec: GoalsDotsSpec
): Bitmap {
    require(spec.goals.size in 1..2) { "Must have 1 or 2 goals" }

    val bmp = createBitmap(widthPx, heightPx)
    val canvas = Canvas(bmp)
    canvas.drawColor(spec.theme.bg)

    val goalCount = spec.goals.size
    val sidePadding = widthPx * spec.sidePaddingFrac

    // Calculate total content height for ALL goals
    val totalGoalsHeight = spec.goals.sumOf { goal ->
        val cols = spec.columns.coerceAtLeast(1)
        val rows = ceil(goal.totalDays / cols.toFloat()).toInt()

        // Estimate heights
        val titleHeight = widthPx * 0.045f * 1.2f
        val statsHeight = widthPx * 0.035f * 1.2f
        val titleToGridGap = heightPx * spec.titleToGridGapFrac
        val gridToTextGap = heightPx * spec.gridToTextGapFrac

        // Grid height (approximate)
        val gridAvailableW = (widthPx - 2f * sidePadding)
        val ratio = spec.gapToDiameterRatio
        val denomW = (cols * (1f + ratio) - ratio)
        val diameter = (gridAvailableW / denomW).coerceIn(6f, 64f)
        val step = diameter * (1f + ratio)
        val gridH = rows * step - diameter * ratio

        (titleHeight + titleToGridGap + gridH + gridToTextGap + statsHeight).toInt()
    }

    val goalSpacing = if (goalCount == 2) heightPx * spec.goalSpacingFrac else 0f
    val totalContentHeight = totalGoalsHeight + goalSpacing

    // Apply BiasAlignment to entire content block
    val freeVerticalSpace = heightPx - totalContentHeight
    val contentVerticalOffset = freeVerticalSpace * (0.5f + spec.verticalBias * 0.5f)

    var currentY = contentVerticalOffset

    // Draw each goal
    spec.goals.forEachIndexed { index, goal ->
        val goalHeight = drawSingleGoal(
            canvas = canvas,
            goal = goal,
            gridStyle = spec.gridStyle,
            theme = spec.theme,
            widthPx = widthPx,
            startY = currentY,
            sidePadding = sidePadding,
            columns = spec.columns,
            titleToGridGap = heightPx * spec.titleToGridGapFrac,
            gridToTextGap = heightPx * spec.gridToTextGapFrac,
            gapToDiameterRatio = spec.gapToDiameterRatio,
            showLabel = spec.showLabel
        )

        currentY += goalHeight
        if (index < spec.goals.size - 1) {
            currentY += goalSpacing
        }
    }

    return bmp
}

private fun drawSingleGoal(
    canvas: Canvas,
    goal: Goal,
    gridStyle: GridStyle,
    theme: DotTheme,
    widthPx: Int,
    startY: Float,
    sidePadding: Float,
    columns: Int,
    titleToGridGap: Float,
    gridToTextGap: Float,
    gapToDiameterRatio: Float,
    showLabel: Boolean
): Float {
    val cols = columns.coerceAtLeast(1)
    val rows = ceil(goal.totalDays / cols.toFloat()).toInt()

    // ----------------------------
    // 1) Draw Title
    // ----------------------------
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = (widthPx * 0.045f).coerceIn(24f, 48f)
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        color = "#FFFFFF".toColorInt()
        textAlign = Paint.Align.CENTER
    }

    val titleY = startY + titlePaint.fontMetrics.let { -it.ascent }
    canvas.drawText(goal.title, widthPx / 2f, titleY, titlePaint)
    val titleHeight = titlePaint.fontMetrics.let { it.descent - it.ascent }

    // ----------------------------
    // 2) Stats Text measurements
    // ----------------------------
    val leftText = "${goal.daysLeft}d left"
    val rightText = " · ${goal.percentComplete}%"

    val statsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = (widthPx * 0.035f).coerceIn(20f, 42f)
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    val statsFm = statsPaint.fontMetrics
    val statsHeight = statsFm.descent - statsFm.ascent

    // ----------------------------
    // 3) Compute dot size
    // ----------------------------
    val gridAvailableW = (widthPx - 2f * sidePadding).coerceAtLeast(1f)
    val ratio = gapToDiameterRatio.coerceAtLeast(0f)
    val denomW = (cols * (1f + ratio) - ratio).coerceAtLeast(0.0001f)
    val maxDiameterByW = gridAvailableW / denomW
    val diameter = maxDiameterByW.coerceIn(6f, 64f)

    val radius = diameter / 2f
    val gap = diameter * ratio
    val step = diameter + gap

    // ----------------------------
    // 4) Grid positioning
    // ----------------------------
    val gridW = cols * step - gap
    val gridH = rows * step - gap
    val startX = (widthPx - gridW) / 2f

    val gridStartY = startY + titleHeight + titleToGridGap
    val textBaselineY = gridStartY + gridH + gridToTextGap - statsFm.ascent

    // ----------------------------
    // 5) Draw Dots with GridStyle
    // ----------------------------
    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    for (i in 0 until goal.totalDays) {
        val r = i / cols
        val c = i % cols

        val cx = startX + c * step + radius
        val cy = gridStartY + r * step + radius

        dotPaint.color = when {
            i == goal.currentDayIndex -> theme.today
            i < goal.currentDayIndex -> theme.filled
            else -> theme.empty
        }

        when (gridStyle) {
            GridStyle.Dots -> canvas.drawCircle(cx, cy, radius, dotPaint)
            GridStyle.Squares -> {
                val s = diameter
                val left = cx - s / 2f
                val top = cy - s / 2f
                canvas.drawRect(left, top, left + s, top + s, dotPaint)
            }
            GridStyle.Rounded -> {
                val s = diameter
                val left = cx - s / 2f
                val top = cy - s / 2f
                val corner = s * 0.3f
                canvas.drawRoundRect(left, top, left + s, top + s, corner, corner, dotPaint)
            }
            GridStyle.Diamond -> {
                val path = Path().apply {
                    moveTo(cx, cy - radius)
                    lineTo(cx + radius, cy)
                    lineTo(cx, cy + radius)
                    lineTo(cx - radius, cy)
                    close()
                }
                canvas.drawPath(path, dotPaint)
            }
        }
    }

    // ----------------------------
    // 6) Draw Stats Text
    // ----------------------------
    if (showLabel){
        statsPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        statsPaint.color = theme.today
        val leftW = statsPaint.measureText(leftText)

        statsPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        statsPaint.color = "#A8A8A8".toColorInt()
        val rightW = statsPaint.measureText(rightText)

        val totalW = leftW + rightW
        val startTextX = (widthPx - totalW) / 2f

        statsPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        statsPaint.color = theme.today
        canvas.drawText(leftText, startTextX, textBaselineY, statsPaint)

        statsPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        statsPaint.color = "#A8A8A8".toColorInt()
        canvas.drawText(rightText, startTextX + leftW, textBaselineY, statsPaint)
    }

    // Return total height used
    return titleHeight + titleToGridGap + gridH + gridToTextGap + statsHeight
}
