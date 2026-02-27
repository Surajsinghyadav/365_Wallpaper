package com.example.a365wallpaper.Worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.a365wallpaper.BitmapGenerators.generateGoalsDotsBitmap
import com.example.a365wallpaper.BitmapGenerators.generateMonthDotsBitmap
import com.example.a365wallpaper.BitmapGenerators.generateYearDotsBitmap
import com.example.a365wallpaper.wallpaperUpdater.WallpaperSetter
import com.example.a365wallpaper.data.Local.WallpaperMode
import com.example.a365wallpaper.data.Local.WallpaperTarget
import com.example.a365wallpaper.data.database.Dao.AppDao
import com.example.a365wallpaper.data.database.Entity.GoalsEntity
import com.example.a365wallpaper.data.database.Dao.LogDao
import com.example.a365wallpaper.data.database.Entity.LogEntity
import com.example.a365wallpaper.data.database.Entity.MonthEntity
import com.example.a365wallpaper.data.database.Entity.YearEntity
import com.example.a365wallpaper.wallpaperUpdater.getWallpaperSizePx
import com.example.a365wallpaper.utils.toExternalModel

class DailyWallpaperWorker(
    appContext: Context,
    params: WorkerParameters,
    private val appDao: AppDao,
    private val logDao: LogDao,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        logger("⚙️ Worker started")
        return try {
            val target = inputData.getString(KEY_TARGET)
                ?.let { WallpaperTarget.valueOf(it) } ?: WallpaperTarget.Both

            val mode = inputData.getString(KEY_MODE)
                ?.let { WallpaperMode.valueOf(it) } ?: WallpaperMode.Year

            val size = getWallpaperSizePx(applicationContext)

            val bmp = when (mode) {
                WallpaperMode.Year -> {
                    val spec = (appDao.getYearThemeConfig() ?: YearEntity()).toExternalModel()
                    generateYearDotsBitmap(size.width, size.height, spec)
                }

                WallpaperMode.Month -> {
                    val spec = (appDao.getMonthThemeConfig() ?: MonthEntity()).toExternalModel()
                    generateMonthDotsBitmap(size.width, size.height, spec)
                }

                WallpaperMode.Goals -> {
                    val entity = appDao.getGoalsThemeConfig() ?: GoalsEntity()
                    if (entity.goal.isEmpty()) {
                        logger("⚠️ No goals saved — skipping Goals wallpaper")
                        return Result.success()
                    }
                    val spec = entity.toExternalModel()
                    generateGoalsDotsBitmap(size.width, size.height, spec)
                }
            }

            WallpaperSetter(applicationContext).set(bmp, target, size)
            logger("✅ Wallpaper set → Mode: $mode | Target: $target")
            Result.success()

        } catch (e: Exception) {
            logger("❌ Failed → ${e::class.simpleName}: ${e.message}")
            Result.failure()
        }
    }

    private suspend fun logger(message: String) {
        Log.d(TAG, message)
        runCatching {
            val logCount = logDao.getTotalLogsCount()
            if (logCount > 150 ){
                logDao.deleteAllLogs()
            }
            logDao.insertLog(LogEntity(message = message)) }
    }

    companion object {
        private const val TAG = "DailyWallpaperWorker"
        const val KEY_TARGET = "KEY_TARGET"
        const val KEY_MODE = "KEY_MODE"
        // Keep unused keys so they don't break anything referencing them
        const val KEY_THEME_ID = "KEY_THEME_ID"
        const val KEY_GRID_STYLE = "KEY_GRID_STYLE"
        const val KEY_VERTICAL_BIAS = "KEY_VERTICAL_BIAS"
        const val KEY_SHOW_LABEL = "KEY_SHOW_LABEL"
    }
}
