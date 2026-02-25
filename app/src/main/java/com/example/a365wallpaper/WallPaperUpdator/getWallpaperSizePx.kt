package com.example.a365wallpaper.WallPaperUpdator

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager

data class WallpaperSizePx(val width: Int, val height: Int)

fun getWallpaperSizePx(context: Context): WallpaperSizePx {
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    return if (Build.VERSION.SDK_INT >= 30) {
        val bounds = wm.currentWindowMetrics.bounds
        WallpaperSizePx(bounds.width(), bounds.height())
    } else {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)
        WallpaperSizePx(metrics.widthPixels, metrics.heightPixels)
    }
}
