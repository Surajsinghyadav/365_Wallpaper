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

    val topPaddingFrac: Float = 0.20f,
    val bottomPaddingFrac: Float = 0.10f,
    val gridToTextGapFrac: Float = 0.03f,
    val gapToDiameterRatio: Float = 0.55f,
    val sidePaddingFrac: Float = 0.08f,
    val showLabel: Boolean,
)

fun generateMonthDotsBitmap(
    widthPx: Int,
    heightPx: Int,
    spec: MonthDotsSpec,
): Bitmap {
    val bmp = createBitmap(widthPx, heightPx)
    val canvas = Canvas(bmp)
    canvas.drawColor(spec.theme.bg)

    val totalDays = YearMonth.of(spec.year, spec.month).lengthOfMonth()
    val todayIndex = (spec.currentDayOfMonth - 1).coerceIn(0, totalDays - 1)

    val cols = spec.columns.coerceAtLeast(1)
    val rows = ceil(totalDays / cols.toFloat()).toInt()
    val sidePadding = widthPx * spec.sidePaddingFrac

    // ----------------------------
    // 1) Text measurements
    // ----------------------------
    val daysLeft = totalDays - spec.currentDayOfMonth
    val percent = (spec.currentDayOfMonth * 100) / totalDays
    val leftText = "${daysLeft}d left"
    val rightText = " · $percent%"

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = (widthPx * 0.04f).coerceIn(28f, 56f)
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    val textFm = textPaint.fontMetrics
    val textHeight = textFm.descent - textFm.ascent
    val gridToTextGap = heightPx * spec.gridToTextGapFrac

    // ----------------------------
    // 2) Compute dot size
    // ----------------------------
    val gridAvailableW = (widthPx - 2f * sidePadding).coerceAtLeast(1f)
    val ratio = spec.gapToDiameterRatio.coerceAtLeast(0f)
    val denomW = (cols * (1f + ratio) - ratio).coerceAtLeast(0.0001f)
    val maxDiameterByW = gridAvailableW / denomW
    val diameterClamped = maxDiameterByW.coerceIn(6f, 64f)

    val radius = diameterClamped / 2f
    val gap = diameterClamped * ratio
    val step = diameterClamped + gap

    // ----------------------------
    // 3) Calculate total content height + apply BiasAlignment
    // ----------------------------
    val gridW = cols * step - gap
    val gridH = rows * step - gap
    val totalContentHeight = gridH + gridToTextGap + textHeight

    val totalAvailableHeight = heightPx.toFloat()
    val freeVerticalSpace = totalAvailableHeight - totalContentHeight
    val contentVerticalOffset = freeVerticalSpace * (0.5f + spec.verticalBias * 0.5f)

    val startY = contentVerticalOffset
    val startX = (widthPx - gridW) / 2f
    val textBaselineY = startY + gridH + gridToTextGap - textFm.ascent

    // ----------------------------
    // 4) Draw dots with GridStyle
    // ----------------------------
    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    for (i in 0 until totalDays) {
        val r = i / cols
        val c = i % cols

        val cx = startX + c * step + radius
        val cy = startY + r * step + radius

        dotPaint.color = when {
            i == todayIndex -> spec.theme.today
            i < todayIndex -> spec.theme.filled
            else -> spec.theme.empty
        }

        when (spec.gridStyle) {
            GridStyle.Dots -> canvas.drawCircle(cx, cy, radius, dotPaint)
            GridStyle.Squares -> {
                val s = diameterClamped
                val left = cx - s / 2f
                val top = cy - s / 2f
                canvas.drawRect(left, top, left + s, top + s, dotPaint)
            }
            GridStyle.Rounded -> {
                val s = diameterClamped
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
    // 5) Draw text (sticks below grid)
    // ----------------------------
    if (spec.showLabel){
        textPaint.textSize = (widthPx * 0.04f).coerceIn(28f, 56f)
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textPaint.color = spec.theme.today
        val leftW = textPaint.measureText(leftText)

        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textPaint.color = "#A8A8A8".toColorInt()
        val rightW = textPaint.measureText(rightText)

        val totalW = leftW + rightW
        val startTextX = (widthPx - totalW) / 2f

        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textPaint.color = spec.theme.today
        canvas.drawText(leftText, startTextX, textBaselineY, textPaint)

        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textPaint.color = "#A8A8A8".toColorInt()
        canvas.drawText(rightText, startTextX + leftW, textBaselineY, textPaint)
    }

    return bmp
}
