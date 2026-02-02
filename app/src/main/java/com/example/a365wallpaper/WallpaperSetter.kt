// WallpaperSetter.kt
package com.example.a365wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build

enum class WallpaperTarget { HOME, LOCK, BOTH }

class WallpaperSetter(private val context: Context) {
    fun set(bitmap: Bitmap, target: WallpaperTarget) {
        val wm = WallpaperManager.getInstance(context) // [web:21]

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            when (target) {
                WallpaperTarget.HOME -> wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                WallpaperTarget.LOCK -> wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                WallpaperTarget.BOTH -> {
                    wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                    wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                }
            }
        } else {
            wm.setBitmap(bitmap)
        }
    }
}
