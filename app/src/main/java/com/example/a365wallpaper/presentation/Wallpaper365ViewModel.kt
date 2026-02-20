//package com.example.a365wallpaper.presentation
//
//import android.app.Application
//import android.content.Context
//import androidx.core.content.edit
//import androidx.lifecycle.ViewModel
//import androidx.work.OneTimeWorkRequestBuilder
//import androidx.work.WorkManager
//import androidx.work.workDataOf
//import com.example.a365wallpaper.DailyWallpaperWorker
//import com.example.a365wallpaper.data.GridStyle
//import com.example.a365wallpaper.data.WallpaperMode
//import com.example.a365wallpaper.data.WallpaperTarget
//import com.example.a365wallpaper.data.database.AppDao
//import com.example.a365wallpaper.ui.theme.DotTheme
//import com.example.a365wallpaper.ui.theme.DotThemes
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.update
//
//class Wallpaper365ViewModel(val appContext: Application, val appDao: AppDao) : ViewModel() {
//
//    private val prefs = appContext.getSharedPreferences("wallpaper_prefs", Context.MODE_PRIVATE)
//
//    private val _mode = MutableStateFlow(WallpaperMode.Year)
//    val mode = _mode.asStateFlow()
//
//    private val _setWallpaperTo = MutableStateFlow(WallpaperTarget.Lock)
//    val setWallpaperTo = _setWallpaperTo.asStateFlow()
//
//    private val _style = MutableStateFlow(GridStyle.Dots)
//    val style = _style.asStateFlow()
//
//    private val _selectedAccentColor = MutableStateFlow(loadSavedTheme())
//    val selectedAccentColor = _selectedAccentColor.asStateFlow()
//
//    private val _showLabel = MutableStateFlow(true)
//    val showLabel = _showLabel.asStateFlow()
//
//    private val _showQuote = MutableStateFlow(true)
//    val showQuote = _showQuote.asStateFlow()
//
//    fun updateMode(mode: WallpaperMode) {
//        _mode.update { mode }
//    }
//
//    fun updateSetWallpaperTo(target: WallpaperTarget) {
//        _setWallpaperTo.update { target }
//    }
//
//    fun updateStyle(style: GridStyle) {
//        _style.update { style }
//    }
//
//    fun updateAccentColor(color: DotTheme) {
//        _selectedAccentColor.update { color }
//    }
//
//    fun togleShowLabel() {
//        _showLabel.update { !it }
//    }
//
//    fun togleShowQuote() {
//        _showQuote.update { !it }
//    }
//
//    // 1 3. Simplified runDailyWallpaperWorker (no redundant prefs read)
//    fun runDailyWallpaperWorker(target: WallpaperTarget) {
//        val selectedTheme = _selectedAccentColor.value
//
//        // Save to prefs when user commits
//        saveThemeToPrefs(selectedTheme)
//
//        val req = OneTimeWorkRequestBuilder<DailyWallpaperWorker>()
//            .setInputData(
//                workDataOf(
//                    DailyWallpaperWorker.KEY_TARGET to target.name,
//                    DailyWallpaperWorker.KEY_THEME_ID to selectedTheme.id
//                )
//            )
//            .build()
//
//        WorkManager.getInstance(appContext).enqueue(req)
//    }
//
//    private fun loadSavedTheme(): DotTheme {
//        val themeId = prefs.getString("selected_theme_id", "classic") ?: "classic"
//        return DotThemes.byId(themeId)
//    }
//
//    private fun saveThemeToPrefs(theme: DotTheme) {
//        prefs.edit {
//            putString("selected_theme_id", theme.id)
//        }
//    }
//
//}


package com.example.a365wallpaper.presentation

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.work.*
import com.example.a365wallpaper.DailyWallpaperWorker
import com.example.a365wallpaper.data.GridStyle
import com.example.a365wallpaper.data.WallpaperMode
import com.example.a365wallpaper.data.WallpaperTarget
import com.example.a365wallpaper.data.database.AppDao
import com.example.a365wallpaper.ui.theme.DotTheme
import com.example.a365wallpaper.ui.theme.DotThemes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class Wallpaper365ViewModel(val appContext: Application, val appDao: AppDao) : ViewModel() {

    private val prefs = appContext.getSharedPreferences("wallpaper_prefs", Context.MODE_PRIVATE)

    private val _mode = MutableStateFlow(WallpaperMode.Year)
    val mode = _mode.asStateFlow()

    private val _setWallpaperTo = MutableStateFlow(loadSavedTarget())
    val setWallpaperTo = _setWallpaperTo.asStateFlow()

    private val _style = MutableStateFlow(GridStyle.Dots)
    val style = _style.asStateFlow()


    private val _selectedAccentColor = MutableStateFlow(loadSavedTheme())
    val selectedAccentColor = _selectedAccentColor.asStateFlow()

    private val _showLabel = MutableStateFlow(loadShowLabelValue())
    val showLabel = _showLabel.asStateFlow()

    private val _showQuote = MutableStateFlow(true)
    val showQuote = _showQuote.asStateFlow()

    fun updateMode(mode: WallpaperMode) {
        _mode.update { mode }
    }

    fun updateSetWallpaperTo(target: WallpaperTarget) {
        _setWallpaperTo.update { target }
    }

    fun updateStyle(style: GridStyle) {
        _style.update { style }
    }

    fun updateAccentColor(color: DotTheme) {
        _selectedAccentColor.update { color }
    }

    fun toggleShowLabel(bool: Boolean) {
        _showLabel.update { bool }
        prefs.edit {
            putBoolean("show_label", bool)
        }
    }

    private fun loadShowLabelValue(): Boolean{
        return prefs.getBoolean("show_label", true)
    }

    fun toggleShowQuote() {
        _showQuote.update { !it }
    }

    // Add to Wallpaper365ViewModel.kt
    private val _verticalPosition = MutableStateFlow(loadSavedVerticalPosition()) // -1 = top, 0 = center, 1.0 = bottom
    val verticalPosition = _verticalPosition.asStateFlow()

    fun updateVerticalPosition(position: Float) {
        _verticalPosition.update { position.coerceIn(-1f, 1f) }
        // Save to prefs for persistence
        prefs.edit {
            putFloat("vertical_position", position)
        }
    }

    private fun loadSavedVerticalPosition(): Float {
        return prefs.getFloat("vertical_position", 0f)
    }



    //  Apply wallpaper immediately when user clicks "Set Wallpaper"
    fun runDailyWallpaperWorker(target: WallpaperTarget) {
        val selectedTheme = _selectedAccentColor.value
        val gridStyle = _style.value
        val verticalBias = _verticalPosition.value
        val mode = _mode.value
        val showLabel = _showLabel.value

        saveThemeToPrefs(selectedTheme)
        saveTargetToPrefs(target)

        val req = OneTimeWorkRequestBuilder<DailyWallpaperWorker>()
            .setInputData(
                workDataOf(
                    DailyWallpaperWorker.KEY_TARGET to target.name,
                    DailyWallpaperWorker.KEY_THEME_ID to selectedTheme.id,
                    DailyWallpaperWorker.KEY_GRID_STYLE to gridStyle.name,
                    DailyWallpaperWorker.KEY_VERTICAL_BIAS to verticalBias,
                    DailyWallpaperWorker.KEY_MODE to mode.name,
                    DailyWallpaperWorker.KEY_SHOW_LABEL to showLabel
                )
            )
            .build()

        WorkManager.getInstance(appContext).enqueue(req)
    }

    /**
     * Schedule automatic daily wallpaper updates at 1:00 AM
     * Note: Android may delay execution by 10-15 minutes for battery optimization
     */
    fun scheduleAutoDailyWallpaperUpdate() {
        val target = _setWallpaperTo.value
        val themeId = _selectedAccentColor.value.id
//        val themeId = DotThemes.All.random().id

        saveThemeToPrefs(_selectedAccentColor.value)
        saveTargetToPrefs(target)

        // 1 Calculate delay until next 1:00 AM
        val initialDelay = calculateDelayUntilNextMorning()

        // 1 Constraints: Don't run if battery is critically low
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true) // Waits if battery < 15%
            .build()

        // 1 Schedule daily updates (24-hour interval starting at 1:00 AM)
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyWallpaperWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        ).setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setBackoffCriteria(BackoffPolicy.LINEAR, duration = Duration.ofMinutes(5))
            .setConstraints(constraints)
            .setInputData(
                workDataOf(
                    DailyWallpaperWorker.KEY_TARGET to target.name,
                    DailyWallpaperWorker.KEY_THEME_ID to themeId
                )
            )
            .addTag("auto_daily_wallpaper")
            .build()

        WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
            "auto_daily_wallpaper",
            ExistingPeriodicWorkPolicy.REPLACE, // Replace old schedule if exists
            dailyWorkRequest
        )

        Log.d("WallpaperViewModel", "Scheduled daily wallpaper updates at 1:00 AM")
        Log.d("WallpaperViewModel", "Next update in ${initialDelay / 1000 / 60} minutes")
    }



    fun isAnyWorkerActive(): Boolean {
        val workInfos = WorkManager.getInstance(appContext)
            .getWorkInfosForUniqueWork("auto_daily_wallpaper")
            .get()

        val hasActiveWorker = workInfos.any { workInfo ->
            workInfo.state == WorkInfo.State.RUNNING ||
                    workInfo.state == WorkInfo.State.ENQUEUED
        }
       return hasActiveWorker
    }

    // Cancel automatic daily updates
    fun cancelAutoDailyWallpaperUpdate() : String {
            if (isAnyWorkerActive()) {
                WorkManager.getInstance(appContext)
                    .cancelUniqueWork("auto_daily_wallpaper")
                Log.d("WallpaperViewModel", "Cancelled automatic daily wallpaper updates")
                return   "Daily updates disabled successfully"
            } else {
                return  "No active update service found"
            }
    }

    /**
     * Calculate milliseconds until next 1:00 AM
     */
    private fun calculateDelayUntilNextMorning(): Long {
        val now = LocalDateTime.now()
        val targetTime = LocalTime.of(1,0) // 1:00 AM

        var nextRun = now.withHour(targetTime.hour)
            .withMinute(targetTime.minute)
            .withSecond(0)
            .withNano(0)

        // If 1:00 AM already passed today, schedule for tomorrow
        if (now.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1)
        }

        return Duration.between(now, nextRun).toMillis()
    }

    private fun loadSavedTheme(): DotTheme {
        val themeId = prefs.getString("selected_theme_id", "classic") ?: "classic"
        return DotThemes.byId(themeId)
    }

    private fun loadSavedTarget(): WallpaperTarget {
        val targetName = prefs.getString("wallpaper_target", "Lock") ?: "Lock"
        return try {
            WallpaperTarget.valueOf(targetName)
        } catch (e: IllegalArgumentException) {
            WallpaperTarget.Lock
        }
    }

    private fun saveThemeToPrefs(theme: DotTheme) {
        prefs.edit {
            putString("selected_theme_id", theme.id)
        }
    }

    private fun saveTargetToPrefs(target: WallpaperTarget) {
        prefs.edit {
            putString("wallpaper_target", target.name)
        }
    }

    private fun calculateDelayUntilNextMorningTest(): Long {
        return TimeUnit.MINUTES.toMillis(2) // TEST: runs in 2 minutes
    }

    //  For rapid testing only
    fun scheduleTestWallpaperUpdates() {
        val target = _setWallpaperTo.value

        // Create 5 workers, each 2 minutes apart
        repeat(5) { index ->
            val themeId = DotThemes.All.random().id

            val req = OneTimeWorkRequestBuilder<DailyWallpaperWorker>()
                .setInitialDelay((index + 1) * 2L, TimeUnit.MINUTES) // 2, 4, 6, 8, 10 minutes
                .setInputData(
                    workDataOf(
                        DailyWallpaperWorker.KEY_TARGET to target.name,
                        DailyWallpaperWorker.KEY_THEME_ID to themeId
                    )
                )
                .addTag("test_wallpaper_$index")
                .build()

            WorkManager.getInstance(appContext).enqueue(req)
            Log.d("Test", "Scheduled test wallpaper #$index in ${(index + 1) * 2} minutes")
        }
    }


}
