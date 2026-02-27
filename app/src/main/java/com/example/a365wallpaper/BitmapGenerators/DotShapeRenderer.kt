package com.example.a365wallpaper.BitmapGenerators

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.example.a365wallpaper.data.Local.GridStyle

/**
 * Draws a single dot shape centered at (cx, cy).
 * All shape logic lives here — no duplication across bitmap generators.
 */
fun Canvas.drawDot(
    cx: Float,
    cy: Float,
    radius: Float,
    diameter: Float,
    gridStyle: GridStyle,
    paint: Paint,
) {
    when (gridStyle) {
        GridStyle.Dots -> drawCircle(cx, cy, radius, paint)

        GridStyle.Squares -> {
            val left = cx - radius
            val top  = cy - radius
            drawRect(left, top, left + diameter, top + diameter, paint)
        }

        GridStyle.Rounded -> {
            val left   = cx - radius
            val top    = cy - radius
            val corner = diameter * 0.3f
            drawRoundRect(left, top, left + diameter, top + diameter, corner, corner, paint)
        }

        GridStyle.Diamond -> {
            val path = Path().apply {
                moveTo(cx, cy - radius)
                lineTo(cx + radius, cy)
                lineTo(cx, cy + radius)
                lineTo(cx - radius, cy)
                close()
            }
            drawPath(path, paint)
        }

        GridStyle.Hexagon -> {
            val path = Path().apply {
                val halfWidth = radius * 0.866f // sqrt(3)/2
                moveTo(cx, cy - radius)
                lineTo(cx + halfWidth, cy - radius * 0.5f)
                lineTo(cx + halfWidth, cy + radius * 0.5f)
                lineTo(cx, cy + radius)
                lineTo(cx - halfWidth, cy + radius * 0.5f)
                lineTo(cx - halfWidth, cy - radius * 0.5f)
                close()
            }
            drawPath(path, paint)
        }

        GridStyle.Heart -> {
            val path = Path().apply {
                moveTo(cx, cy + radius * 0.7f)
                cubicTo(cx - radius * 1.2f, cy - radius * 0.3f,
                    cx - radius * 0.5f, cy - radius * 1.2f,
                    cx, cy - radius * 0.2f)
                cubicTo(cx + radius * 0.5f, cy - radius * 1.2f,
                    cx + radius * 1.2f, cy - radius * 0.3f,
                    cx, cy + radius * 0.7f)
            }
            drawPath(path, paint)
        }

        GridStyle.Star -> {
            val path = Path().apply {
                moveTo(cx, cy - radius)
                quadTo(cx, cy, cx + radius, cy)
                quadTo(cx, cy, cx, cy + radius)
                quadTo(cx, cy, cx - radius, cy)
                quadTo(cx, cy, cx, cy - radius)
                close()
            }
            drawPath(path, paint)
        }

        GridStyle.Ring -> {
            val originalStyle = paint.style
            val originalStroke = paint.strokeWidth

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = diameter * 0.15f

            drawCircle(cx, cy, radius * 0.85f, paint)

            paint.style = originalStyle
            paint.strokeWidth = originalStroke
        }

    }
}