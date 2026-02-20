package com.example.a365wallpaper

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.a365wallpaper.data.GridStyle
import com.example.a365wallpaper.data.WallpaperMode
import com.example.a365wallpaper.data.WallpaperTarget
import com.example.a365wallpaper.data.database.LogDao
import com.example.a365wallpaper.data.database.LogEntity
import com.example.a365wallpaper.ui.theme.DotThemes
import java.time.LocalDate

class DailyWallpaperWorker(
    appContext: Context,
    params: WorkerParameters,
    private val logDao: LogDao
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {

        log("Worker started")

        return try {
            val target = inputData.getString(KEY_TARGET)
                ?.let { WallpaperTarget.valueOf(it) } ?: WallpaperTarget.Both

            val themeId = inputData.getString(KEY_THEME_ID) ?: "classic"
            val selectedTheme = DotThemes.byId(themeId)

            val gridStyle = inputData.getString(KEY_GRID_STYLE)
                ?.let { GridStyle.valueOf(it) } ?: GridStyle.Dots

            val verticalBias = inputData.getFloat(KEY_VERTICAL_BIAS, 0f)

            val mode = inputData.getString(KEY_MODE)
                ?.let { WallpaperMode.valueOf(it) } ?: WallpaperMode.Year

            val showLabel = inputData.getBoolean(KEY_SHOW_LABEL, true)

            val todayIndex = computeTodayIndex()
            val size = getWallpaperSizePx(applicationContext)

            val bmp = when (mode) {
                WallpaperMode.Year -> generateYearDotsBitmap(
                    size.width, size.height,
                    YearDotsSpec(
                        gridStyle = gridStyle,
                        verticalBias = verticalBias,
                        theme = selectedTheme,
                        totalDays = 365,
                        todayIndex = todayIndex,
                        showLabel = showLabel
                    )
                )

                WallpaperMode.Month -> generateMonthDotsBitmap(
                    size.width, size.height,
                    MonthDotsSpec(
                        gridStyle = gridStyle,
                        verticalBias = verticalBias,
                        theme = selectedTheme,
                        showLabel = showLabel
                    )
                )

                WallpaperMode.Goals -> generateGoalsDotsBitmap(
                    size.width, size.height,
                    GoalsDotsSpec(
                        goals = listOf(
                            Goal(
                                "Android Interview Practice",
                                startDate = LocalDate.now(),
                                deadline = LocalDate.of(2026, 3, 15)
                            )
                        ),
                        gridStyle = gridStyle,
                        verticalBias = verticalBias,
                        theme = selectedTheme,
                        showLabel = showLabel
                    )
                )
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

    private fun computeTodayIndex(): Int {
        val today = LocalDate.now()
        val start = LocalDate.of(today.year, 1, 1)
        return (today.toEpochDay() - start.toEpochDay()).toInt()
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
