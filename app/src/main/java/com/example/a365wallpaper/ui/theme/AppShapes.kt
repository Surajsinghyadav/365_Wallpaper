package com.example.a365wallpaper.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

object AppShapes {
    val Circle: Shape = CircleShape
    val Square: Shape = RectangleShape
    val RoundedCorner: Shape = RoundedCornerShape(20)

    val Diamond: Shape = object : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
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

    // Modern, Honeycomb vibe
    val Hexagon: Shape = object : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val path = Path().apply {
                val width = size.width
                val height = size.height
                moveTo(width * 0.5f, 0f)
                lineTo(width, height * 0.25f)
                lineTo(width, height * 0.75f)
                lineTo(width * 0.5f, height)
                lineTo(0f, height * 0.75f)
                lineTo(0f, height * 0.25f)
                close()
            }
            return Outline.Generic(path)
        }
    }

    // Twinkle/Constellation vibe
    val Star: Shape = object : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val path = Path().apply {
                val w = size.width
                val h = size.height
                moveTo(w * 0.5f, 0f)
                quadraticBezierTo(w * 0.5f, h * 0.5f, w, h * 0.5f)
                quadraticBezierTo(w * 0.5f, h * 0.5f, w * 0.5f, h)
                quadraticBezierTo(w * 0.5f, h * 0.5f, 0f, h * 0.5f)
                quadraticBezierTo(w * 0.5f, h * 0.5f, w * 0.5f, 0f)
                close()
            }
            return Outline.Generic(path)
        }
    }

    // Heart shape using Bézier curves
    val Heart: Shape = object : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val path = Path().apply {
                val width = size.width
                val height = size.height

                // Start at the top center dip
                moveTo(width / 2f, height * 0.25f)

                // Left curve
                cubicTo(
                    width * 0.1f, -height * 0.1f,
                    -width * 0.1f, height * 0.6f,
                    width / 2f, height * 0.95f // Bottom tip
                )

                // Right curve
                cubicTo(
                    width * 1.1f, height * 0.6f,
                    width * 0.9f, -height * 0.1f,
                    width / 2f, height * 0.25f // Back to top center dip
                )
                close()
            }
            return Outline.Generic(path)
        }
    }

    // Minimalist Hollow Circle
    val Ring: Shape = object : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val path = Path().apply {
                // EvenOdd fill type allows us to punch a hole in the middle
                fillType = PathFillType.EvenOdd

                // Outer boundary
                addOval(Rect(0f, 0f, size.width, size.height))

                // Inner boundary (creates a 15% thickness ring)
                val insetX = size.width * 0.15f
                val insetY = size.height * 0.15f
                addOval(Rect(insetX, insetY, size.width - insetX, size.height - insetY))
            }
            return Outline.Generic(path)
        }
    }
}