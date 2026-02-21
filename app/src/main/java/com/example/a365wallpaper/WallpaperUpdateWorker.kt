package com.example.a365wallpaper

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.a365wallpaper.data.GridStyle
import com.example.a365wallpaper.data.WallpaperMode
import com.example.a365wallpaper.data.WallpaperTarget
import com.example.a365wallpaper.data.database.AppDao
import com.example.a365wallpaper.data.database.LogDao
import com.example.a365wallpaper.data.database.LogEntity
import com.example.a365wallpaper.ui.theme.DotThemes
import com.example.a365wallpaper.utils.toExternalModel
import java.time.LocalDate

class DailyWallpaperWorker(
    appContext: Context,
    params: WorkerParameters,
    private val appDao: AppDao,
    private val logDao: LogDao
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {

        log("Worker started")

        return try {
            val target = inputData.getString(KEY_TARGET)
                ?.let { WallpaperTarget.valueOf(it) } ?: WallpaperTarget.Both
//
//            val themeId = inputData.getString(KEY_THEME_ID) ?: "classic"
//            val selectedTheme = DotThemes.byId(themeId)
//
//            val gridStyle = inputData.getString(KEY_GRID_STYLE)
//                ?.let { GridStyle.valueOf(it) } ?: GridStyle.Dots
//
//            val verticalBias = inputData.getFloat(KEY_VERTICAL_BIAS, 0f)
//
            val mode = inputData.getString(KEY_MODE)
                ?.let { WallpaperMode.valueOf(it) } ?: WallpaperMode.Year
//
//            val showLabel = inputData.getBoolean(KEY_SHOW_LABEL, true)
//
            val size = getWallpaperSizePx(applicationContext)


            val bmp = when (mode) {
                WallpaperMode.Year -> {
                    val yearDotsSpec = appDao.getYearThemeConfig().toExternalModel()
                    generateYearDotsBitmap(
                        size.width, size.height,
                        yearDotsSpec
                    )
                }

                WallpaperMode.Month -> {
                    val monthDotsSpec = appDao.getMonthThemeConfig().toExternalModel()
                    generateMonthDotsBitmap(
                        size.width, size.height,
                        monthDotsSpec
                    )
                }

                WallpaperMode.Goals -> {
                    val goalsDotsSpec = appDao.getGoalsThemeConfig().toExternalModel()
                    generateGoalsDotsBitmap(
                        size.width, size.height,
                        goalsDotsSpec
                    )
                }
            }

            WallpaperSetter(applicationContext).set(bmp, target, size)

            log("✅ Wallpaper updated successfully→ \nMode: $mode | Target: $target")

            Result.success()

        } catch (e: Exception) {
            log("Failed to update Wallpaper → \n ${e::class.simpleName}: ${e.message}")
            Result.failure()
        }
    }

    private suspend fun log(message: String) {
        Log.d(TAG, message)
        runCatching { logDao.insertLog(LogEntity(message = message)) }
    }


    companion object {
        private const val TAG = "DailyWallpaperWorker"
        const val KEY_TARGET = "KEY_TARGET"
        const val KEY_THEME_ID = "KEY_THEME_ID"
        const val KEY_GRID_STYLE = "KEY_GRID_STYLE"
        const val KEY_VERTICAL_BIAS = "KEY_VERTICAL_BIAS"
        const val KEY_MODE = "KEY_MODE"
        const val KEY_SHOW_LABEL = "KEY_SHOW_LABEL"
    }
}
