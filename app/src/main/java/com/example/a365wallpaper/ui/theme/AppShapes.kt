package com.example.a365wallpaper.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * Pre-defined shapes for 365 Wallpaper app
 */
object AppShapes {


    val Circle: Shape = CircleShape

    val Square: Shape = RectangleShape


    val RoundedCorner: Shape = RoundedCornerShape(20) // 20% rounding


    val Diamond: Shape = object : Shape {
        override fun createOutline(
            size: Size,
            layoutDirection: LayoutDirection,
            density: Density
        ): Outline {
            val path = Path().apply {
                moveTo(size.width / 2f, 0f)
                lineTo(size.width, size.height / 2f)
                lineTo(size.width / 2f, size.height)
                lineTo(0f, size.height / 2f)
                close()
            }
            return Outline.Generic(path)
        }
    }


    val Rhombus: Shape = Diamond
}