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
import kotlin.math.ceil

data class YearDotsSpec(
    val columns: Int = 15,
    val gridStyle: GridStyle = GridStyle.Dots,
    val verticalBias: Float = 0f,
    val theme: DotTheme = DotThemes.All.first(),
    val showLabel: Boolean,
    val topPaddingFrac: Float = 0.20f,
    val bottomPaddingFrac: Float = 0.10f,
    val gridToTextGapFrac: Float = 0.03f,
    val gapToDiameterRatio: Float = 0.55f,
    val sidePaddingFrac: Float = 0.08f,
)

fun generateYearDotsBitmap(
    widthPx: Int,
    heightPx: Int,
    spec: YearDotsSpec
): Bitmap {
    val date = LocalDate.now()
    val todayIndex = date.dayOfYear - 1
    val totalDays = date.lengthOfYear()
    val bmp = createBitmap(widthPx, heightPx)
    val canvas = Canvas(bmp)
    canvas.drawColor(spec.theme.bg)

    val today = todayIndex.coerceIn(0, totalDays - 1)
    val cols = spec.columns.coerceAtLeast(1)
    val rows = ceil(totalDays / cols.toFloat()).toInt()

    val sidePadding = widthPx * spec.sidePaddingFrac

    // ----------------------------
    // 1) Text measurements (needed for total content height calculation)
    // ----------------------------
    val daysLeft = (totalDays - 1) - today
    val percent = ((today + 1) * 100) / totalDays

    val leftText = "${daysLeft}d left"
    val rightText = " · $percent%"

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = (widthPx * 0.04f).coerceIn(28f, 56f)
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    val textFm = textPaint.fontMetrics
    val textHeight = (textFm.descent - textFm.ascent)
    val gridToTextGap = heightPx * spec.gridToTextGapFrac

    // ----------------------------
    // 2) Compute dot size (use available width)
    // ----------------------------
    val gridAvailableW = (widthPx - 2f * sidePadding).coerceAtLeast(1f)

    val ratio = spec.gapToDiameterRatio.coerceAtLeast(0f)
    val denomW = (cols * (1f + ratio) - ratio).coerceAtLeast(0.0001f)
    val maxDiameterByW = gridAvailableW / denomW

    // Use reasonable max diameter (let height adjust naturally)
    val diameterClamped = maxDiameterByW.coerceIn(6f, 64f)

    val radius = diameterClamped / 2f
    val gap = diameterClamped * ratio
    val step = diameterClamped + gap

    // ----------------------------
    // 3) Calculate TOTAL content height (grid + gap + text)
    // ----------------------------
    val gridW = cols * step - gap
    val gridH = rows * step - gap

    // ✅ FIXED: Total content = grid + gap + text (Column behavior)
    val totalContentHeight = gridH + gridToTextGap + textHeight

    // ----------------------------
    // 4) Apply BiasAlignment to ENTIRE content block
    // ----------------------------
    val totalAvailableHeight = heightPx.toFloat()
    val freeVerticalSpace = totalAvailableHeight - totalContentHeight

    // BiasAlignment: +1=top, 0=center, -1=bottom
    val contentVerticalOffset = freeVerticalSpace * (0.5f + spec.verticalBias * 0.5f)

    // Grid starts at content offset
    val startY = contentVerticalOffset
    val startX = (widthPx - gridW) / 2f

    // ✅ Text sticks below grid with fixed gap
    val textBaselineY = startY + gridH + gridToTextGap - textFm.ascent

    // ----------------------------
    // 5) Draw dots
    // ----------------------------
    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    for (i in 0 until totalDays) {
        val r = i / cols
        val c = i % cols

        val cx = startX + c * step + radius
        val cy = startY + r * step + radius

        dotPaint.color = when {
            i == today -> spec.theme.today
            i < today -> spec.theme.filled
            else -> spec.theme.empty
        }

        when (spec.gridStyle) {
            GridStyle.Dots -> {
                canvas.drawCircle(cx, cy, radius, dotPaint)
            }
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
    // 6) Draw text (sticks below grid)
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

        // Left (accent)
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textPaint.color = spec.theme.today
        canvas.drawText(leftText, startTextX, textBaselineY, textPaint)

        // Right (gray)
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textPaint.color = "#A8A8A8".toColorInt()
        canvas.drawText(rightText, startTextX + leftW, textBaselineY, textPaint)
    }

    return bmp
}
