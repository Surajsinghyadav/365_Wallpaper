package com.example.a365wallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import kotlin.random.Random

object WallpaperBitmapGenerator {
    fun generateRandomColorBitmap(w: Int, h: Int): Bitmap {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.rgb(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256)))
        return bmp
    }
}
