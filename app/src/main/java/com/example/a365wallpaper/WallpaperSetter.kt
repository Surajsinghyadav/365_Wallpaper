package com.example.a365wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build

enum class WallpaperTarget { HOME, LOCK, BOTH }

class WallpaperSetter(private val context: Context) {
    fun set(bitmap: Bitmap, target: WallpaperTarget, size: WallpaperSizePx) {
        val wm = WallpaperManager.getInstance(context)
        val crop = Rect(0, 0, size.width, size.height)

        when (target) {
            WallpaperTarget.HOME ->
                wm.setBitmap(bitmap, crop, true, WallpaperManager.FLAG_SYSTEM)

            WallpaperTarget.LOCK ->
                wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)

            WallpaperTarget.BOTH -> {
                wm.setBitmap(bitmap,  crop, true, WallpaperManager.FLAG_SYSTEM)
                wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
            }
        }
    }
}
