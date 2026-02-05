package com.example.a365wallpaper

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.time.LocalDate

class DailyWallpaperWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val target = inputData.getString(KEY_TARGET)?.let { WallpaperTarget.valueOf(it) }
            ?: WallpaperTarget.BOTH

        val todayIndex = computeTodayIndex()
        val size = getWallpaperSizePx(applicationContext)
        Log.d("check", "width = ${size.width}")
        Log.d("check", "height = ${size.height}")
        val spec = YearDotsSpec(
            todayIndex = todayIndex,
            dotRadiusPx = (size.width * 0.012f).coerceIn(8f, 16f),
            gapPx = (size.width * 0.010f).coerceIn(8f, 18f),
            topOffsetPx = (size.height * 0.18f),
            bottomTextOffsetPx = (size.height * 0.12f)
        )

        val bmp = generateYearDotsBitmap(size.width, size.height, spec)
        WallpaperSetter(applicationContext).set(bmp,target,size)

//        scheduleNext(applicationContext, target) // schedule tomorrow
        return Result.success()
    }

    private fun computeTodayIndex(): Int {
        val today = LocalDate.now()
        val start = LocalDate.of(today.year, 1, 1)
        return (today.toEpochDay() - start.toEpochDay()).toInt()
    }

    companion object {
        const val KEY_TARGET = "KEY_TARGET"
        const val UNIQUE_NAME = "daily_wallpaper"
    }
}




