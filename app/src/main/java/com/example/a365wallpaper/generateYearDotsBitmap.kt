package com.example.a365wallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.graphics.createBitmap
import kotlin.math.ceil
import androidx.core.graphics.toColorInt

data class YearDotsSpec(
    val totalDays: Int = 365,
    val todayIndex: Int,
    val columns: Int = 15,
    val dotRadiusPx: Float,     // e.g. 12f
    val gapPx: Float,           // e.g. 14f
    val topOffsetPx: Float,     // e.g. 260f
    val bottomTextOffsetPx: Float, // e.g. 240f
    val filled: Int = "#F2F2F2".toColorInt(),
    val empty: Int = "#3D3D3D".toColorInt(),
    val today: Int = "#F36B2C".toColorInt(),
    val bg: Int = "#1F1F1F".toColorInt(),
)

fun generateYearDotsBitmap(
    widthPx: Int,
    heightPx: Int,
    spec: YearDotsSpec
): Bitmap {
    val bmp = createBitmap(widthPx, heightPx)
    val canvas = Canvas(bmp)
    canvas.drawColor(spec.bg)

    val total = spec.totalDays
    val today = spec.todayIndex.coerceIn(0, total - 1)
    val rows = ceil(total / spec.columns.toFloat()).toInt()

    val stepX = (spec.dotRadiusPx * 2f + spec.gapPx)
    val stepY = (spec.dotRadiusPx * 2f + spec.gapPx)

    // Center grid horizontally
    val gridW = spec.columns * stepX - spec.gapPx
    val startX = (widthPx - gridW) / 2f
    val startY = spec.topOffsetPx

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    for (i in 0 until total) {
        val r = i / spec.columns
        val c = i % spec.columns
        val cx = startX + c * stepX + spec.dotRadiusPx
        val cy = startY + r * stepY + spec.dotRadiusPx

        val color = when {
            i == today -> spec.today
            i < today -> spec.filled
            else -> spec.empty
        }
        paint.color = color
        canvas.drawCircle(cx, cy, spec.dotRadiusPx, paint)
    }

    // Bottom text
    val daysLeft = (total - 1) - today
    val percent = ((today + 1) * 100) / total

    val baseY = heightPx - spec.bottomTextOffsetPx
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = (widthPx * 0.055f).coerceIn(34f, 54f)
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    val leftText = "${daysLeft}d left"
    val rightText = "  ·  $percent%"

    // Measure to center the whole line
    textPaint.color = spec.today
    val leftW = textPaint.measureText(leftText)
    textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    textPaint.color = Color.parseColor("#A8A8A8")
    val rightW = textPaint.measureText(rightText)

    val totalW = leftW + rightW
    val startTextX = (widthPx - totalW) / 2f

    // Draw left (orange)
    textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    textPaint.color = spec.today
    canvas.drawText(leftText, startTextX, baseY, textPaint)

    // Draw right (gray)
    textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    textPaint.color = Color.parseColor("#A8A8A8")
    canvas.drawText(rightText, startTextX + leftW, baseY, textPaint)

    return bmp
}
