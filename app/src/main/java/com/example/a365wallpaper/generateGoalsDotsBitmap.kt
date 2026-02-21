package com.example.a365wallpaper


import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import com.example.a365wallpaper.data.GridStyle
import com.example.a365wallpaper.data.LocalDateSerializer
import com.example.a365wallpaper.ui.theme.DotTheme
import com.example.a365wallpaper.ui.theme.DotThemes
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil



@Serializable
data class Goal(
    val title: String,
    @Serializable(with = LocalDateSerializer::class)
    val startDate: LocalDate,
    @Serializable(with = LocalDateSerializer::class)
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
    // ✅ User-controlled multiplier (0.5f = smallest, 2.0f = largest)
    val dotSizeMultiplier: Float = 1.0f,
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

    // ✅ Auto-scale: more total dots = smaller dots
    val totalDots = spec.goals.sumOf { it.totalDays }
    val autoScale = when {
        totalDots > 300 -> 0.75f
        totalDots > 150 -> 0.88f
        else -> 1.0f
    }
    val effectiveMultiplier = (autoScale * spec.dotSizeMultiplier).coerceIn(0.4f, 2.0f)

    val totalGoalsHeight = spec.goals.sumOf { goal ->
        val cols = spec.columns.coerceAtLeast(1)
        val rows = ceil(goal.totalDays / cols.toFloat()).toInt()
        val titleHeight = widthPx * 0.045f * 1.2f
        val statsHeight = widthPx * 0.035f * 1.2f
        val titleToGridGap = heightPx * spec.titleToGridGapFrac
        val gridToTextGap = heightPx * spec.gridToTextGapFrac
        val gridAvailableW = (widthPx - 2f * sidePadding)
        val ratio = spec.gapToDiameterRatio
        val denomW = (cols * (1f + ratio) - ratio)
        val diameter = ((gridAvailableW / denomW) * effectiveMultiplier).coerceIn(6f, 64f)
        val step = diameter * (1f + ratio)
        val gridH = rows * step - diameter * ratio
        (titleHeight + titleToGridGap + gridH + gridToTextGap + statsHeight).toInt()
    }

    val goalSpacing = if (goalCount == 2) heightPx * spec.goalSpacingFrac else 0f
    val totalContentHeight = totalGoalsHeight + goalSpacing
    val freeVerticalSpace = heightPx - totalContentHeight
    val contentVerticalOffset = freeVerticalSpace * (0.5f + spec.verticalBias * 0.5f)

    var currentY = contentVerticalOffset

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
            showLabel = spec.showLabel,
            dotSizeMultiplier = effectiveMultiplier // ✅ pass effective size
        )
        currentY += goalHeight
        if (index < spec.goals.size - 1) currentY += goalSpacing
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
    showLabel: Boolean,
    dotSizeMultiplier: Float = 1.0f // ✅ new param
): Float {
    val cols = columns.coerceAtLeast(1)
    val rows = ceil(goal.totalDays / cols.toFloat()).toInt()

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = (widthPx * 0.045f).coerceIn(24f, 48f)
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        color = "#FFFFFF".toColorInt()
        textAlign = Paint.Align.CENTER
    }
    val titleY = startY + titlePaint.fontMetrics.let { -it.ascent }
    canvas.drawText(goal.title, widthPx / 2f, titleY, titlePaint)
    val titleHeight = titlePaint.fontMetrics.let { it.descent - it.ascent }

    val leftText = "${goal.daysLeft}d left"
    val rightText = " · ${goal.percentComplete}%"

    val statsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = (widthPx * 0.035f).coerceIn(20f, 42f)
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }
    val statsFm = statsPaint.fontMetrics
    val statsHeight = statsFm.descent - statsFm.ascent

    val gridAvailableW = (widthPx - 2f * sidePadding).coerceAtLeast(1f)
    val ratio = gapToDiameterRatio.coerceAtLeast(0f)
    val denomW = (cols * (1f + ratio) - ratio).coerceAtLeast(0.0001f)
    // ✅ Apply dotSizeMultiplier to diameter
    val diameter = ((gridAvailableW / denomW) * dotSizeMultiplier).coerceIn(6f, 64f)
    val radius = diameter / 2f
    val gap = diameter * ratio
    val step = diameter + gap

    val gridW = cols * step - gap
    val gridH = rows * step - gap
    val startX = (widthPx - gridW) / 2f
    val gridStartY = startY + titleHeight + titleToGridGap
    val textBaselineY = gridStartY + gridH + gridToTextGap - statsFm.ascent

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
                val left = cx - radius; val top = cy - radius
                canvas.drawRect(left, top, left + diameter, top + diameter, dotPaint)
            }
            GridStyle.Rounded -> {
                val left = cx - radius; val top = cy - radius
                canvas.drawRoundRect(left, top, left + diameter, top + diameter, diameter * 0.3f, diameter * 0.3f, dotPaint)
            }
            GridStyle.Diamond -> {
                val path = Path().apply {
                    moveTo(cx, cy - radius); lineTo(cx + radius, cy)
                    lineTo(cx, cy + radius); lineTo(cx - radius, cy); close()
                }
                canvas.drawPath(path, dotPaint)
            }
        }
    }

    if (showLabel) {
        statsPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        statsPaint.color = theme.today
        val leftW = statsPaint.measureText(leftText)
        statsPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        statsPaint.color = "#A8A8A8".toColorInt()
        val rightW = statsPaint.measureText(rightText)
        val startTextX = (widthPx - leftW - rightW) / 2f
        statsPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        statsPaint.color = theme.today
        canvas.drawText(leftText, startTextX, textBaselineY, statsPaint)
        statsPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        statsPaint.color = "#A8A8A8".toColorInt()
        canvas.drawText(rightText, startTextX + leftW, textBaselineY, statsPaint)
    }

    return titleHeight + titleToGridGap + gridH + gridToTextGap + statsHeight
}
