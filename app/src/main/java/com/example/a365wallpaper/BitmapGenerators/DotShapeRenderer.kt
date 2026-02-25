// app/src/main/java/com/example/a365wallpaper/DotShapeRenderer.kt
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
    }
}
