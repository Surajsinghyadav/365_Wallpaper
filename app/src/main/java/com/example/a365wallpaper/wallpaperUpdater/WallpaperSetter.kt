package com.example.a365wallpaper.wallpaperUpdater

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import com.example.a365wallpaper.data.Local.WallpaperTarget

class WallpaperSetter(private val context: Context) {
    fun set(bitmap: Bitmap, target: WallpaperTarget, size: WallpaperSizePx) {
        val wm = WallpaperManager.getInstance(context)
        val crop = Rect(0, 0, size.width, size.height)

        when (target) {
            WallpaperTarget.Home ->
                wm.setBitmap(bitmap, crop, true, WallpaperManager.FLAG_SYSTEM)

            WallpaperTarget.Lock ->
                wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)

            WallpaperTarget.Both -> {
                wm.setBitmap(bitmap,  crop, true, WallpaperManager.FLAG_SYSTEM)
                wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
            }
        }
    }
}