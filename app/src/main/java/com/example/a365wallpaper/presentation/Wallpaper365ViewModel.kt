package com.example.a365wallpaper.presentation

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.a365wallpaper.DailyWallpaperWorker
import com.example.a365wallpaper.Goal
import com.example.a365wallpaper.GoalsDotsSpec
import com.example.a365wallpaper.MonthDotsSpec
import com.example.a365wallpaper.YearDotsSpec
import com.example.a365wallpaper.data.GridStyle
import com.example.a365wallpaper.data.WallpaperMode
import com.example.a365wallpaper.data.WallpaperTarget
import com.example.a365wallpaper.data.database.AppDao
import com.example.a365wallpaper.ui.theme.DotTheme
import com.example.a365wallpaper.ui.theme.DotThemes
import com.example.a365wallpaper.utils.toEntity
import com.example.a365wallpaper.utils.toExternalModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class Wallpaper365ViewModel(val appContext: Application, val appDao: AppDao) : ViewModel() {

    private val prefs = appContext.getSharedPreferences("wallpaperprefs", Context.MODE_PRIVATE)

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

    private val _verticalPosition = MutableStateFlow(loadSavedVerticalPosition())
    val verticalPosition = _verticalPosition.asStateFlow()

    // ✅ Goals state — max 2 goals
    private val _goals = MutableStateFlow<List<Goal>>(loadSavedGoal())
    val goals = _goals.asStateFlow()

    // ✅ Month mode dot size multiplier (0.5 to 2.0)
    private val _monthDotSize = MutableStateFlow(1.0f)
    val monthDotSize = _monthDotSize.asStateFlow()

    // ✅ Goals mode dot size multiplier (0.5 to 2.0)
    private val _goalDotSize = MutableStateFlow(1.0f)
    val goalDotSize = _goalDotSize.asStateFlow()

    // ✅ Wallpaper set animation trigger
    private val _wallpaperSetEvent = MutableStateFlow(false)
    val wallpaperSetEvent = _wallpaperSetEvent.asStateFlow()

    fun acknowledgeWallpaperSet() = _wallpaperSetEvent.update { false }

    fun updateMode(mode: WallpaperMode) = _mode.update { mode }
    fun updateSetWallpaperTo(target: WallpaperTarget) = _setWallpaperTo.update { target }
    fun updateStyle(style: GridStyle) = _style.update { style }
    fun updateAccentColor(color: DotTheme) = _selectedAccentColor.update { color }
    fun toggleShowLabel(bool: Boolean) {
        _showLabel.update { bool }
        prefs.edit { putBoolean("showlabel", bool) }
    }
    fun updateVerticalPosition(position: Float) {
        _verticalPosition.update { position.coerceIn(-1f, 1f) }
        prefs.edit { putFloat("verticalposition", position) }
    }

    fun updateMonthDotSize(size: Float) = _monthDotSize.update { size.coerceIn(0.5f, 2.0f) }
    fun updateGoalDotSize(size: Float) = _goalDotSize.update { size.coerceIn(0.5f, 2.0f) }

    // ✅ Goals management
    fun addGoal(goal: Goal) {
        viewModelScope.launch {
            _goals.update { current ->
                if (current.size < 2) current + goal else current // Max 2 goals
            }
        }

    }

    fun loadSavedGoal(): List<Goal> {
           return appDao.getGoalsFlow()
    }
    fun removeGoal(index: Int) {
        _goals.update { current -> current.toMutableList().also { it.removeAt(index) } }
    }

    fun canAddGoal(): Boolean = _goals.value.size < 2

    fun runDailyWallpaperWorker(target: WallpaperTarget) {
        val selectedTheme = _selectedAccentColor.value
        val gridStyle = _style.value
        val verticalBias = _verticalPosition.value
        val mode = _mode.value
        val showLabel = _showLabel.value
//        saveThemeToPrefs(selectedTheme)
//        saveTargetToPrefs(target)
        _wallpaperSetEvent.update { true } // ✅ trigger animation

        viewModelScope.launch {
            when(mode){
                WallpaperMode.Year -> {
                    appDao.saveYearThemeConfig(
                        YearDotsSpec(
                            gridStyle = gridStyle,
                            verticalBias = verticalBias,
                            theme = selectedTheme,
                            showLabel =showLabel,
                        ).toEntity()
                    )
                }

                WallpaperMode.Month -> {
                    appDao.saveMonthThemeConfig(
                        MonthDotsSpec(
                            gridStyle = gridStyle,
                            verticalBias = verticalBias,
                            theme = selectedTheme,
                            showLabel =showLabel,
                        ).toEntity()
                    )
                }

                WallpaperMode.Goals -> {
                    appDao.saveGoalsThemeConfig(
                        GoalsDotsSpec(
                            goals = goals,
                            gridStyle = gridStyle,
                            verticalBias = verticalBias,
                            theme = selectedTheme,
                            showLabel =showLabel,
                        ).toEntity()
                    )
                }

            }
        }

        val req = OneTimeWorkRequestBuilder<DailyWallpaperWorker>()
            .setInputData(
                workDataOf(
                    DailyWallpaperWorker.KEY_TARGET to target.name,
                    DailyWallpaperWorker.KEY_MODE to mode.name,
//                    DailyWallpaperWorker.KEY_THEME_ID to selectedTheme.id,
//                    DailyWallpaperWorker.KEY_GRID_STYLE to gridStyle.name,
//                    DailyWallpaperWorker.KEY_VERTICAL_BIAS to verticalBias,
//                    DailyWallpaperWorker.KEY_SHOW_LABEL to showLabel
                )
            ).build()
        WorkManager.getInstance(appContext).enqueue(req)
    }

    fun scheduleAutoDailyWallpaperUpdate() {
        val target = _setWallpaperTo.value
        saveThemeToPrefs(_selectedAccentColor.value)
        saveTargetToPrefs(target)
        val initialDelay = calculateDelayUntilNextMorning()
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        val req = PeriodicWorkRequestBuilder<DailyWallpaperWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .setInputData(
                workDataOf(
                    DailyWallpaperWorker.KEY_TARGET to target.name,
                    DailyWallpaperWorker.KEY_THEME_ID to _selectedAccentColor.value.id,
                    DailyWallpaperWorker.KEY_GRID_STYLE to _style.value.name,
                    DailyWallpaperWorker.KEY_VERTICAL_BIAS to _verticalPosition.value,
                    DailyWallpaperWorker.KEY_MODE to _mode.value.name,
                    DailyWallpaperWorker.KEY_SHOW_LABEL to _showLabel.value
                )
            ).build()
        WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
            "DailyWallpaper",
            ExistingPeriodicWorkPolicy.UPDATE,
            req
        )
    }

    fun cancelAutoDailyWallpaperUpdate(): String {
        WorkManager.getInstance(appContext).cancelUniqueWork("DailyWallpaper")
        return "Auto wallpaper stopped"
    }

    private fun calculateDelayUntilNextMorning(): Long {
        val now = LocalDateTime.now()
        var next = LocalDateTime.of(now.toLocalDate(), LocalTime.of(1, 0))
        if (!now.isBefore(next)) next = next.plusDays(1)
        return Duration.between(now, next).toMillis()
    }

    private fun loadShowLabelValue(): Boolean = prefs.getBoolean("showlabel", true)
    private fun loadSavedVerticalPosition(): Float = prefs.getFloat("verticalposition", 0f)
    private fun loadSavedTheme(): DotTheme = DotThemes.byId(prefs.getString("selectedthemeid", "classic") ?: "classic")
    private fun saveThemeToPrefs(theme: DotTheme) = prefs.edit { putString("selectedthemeid", theme.id) }
    private fun loadSavedTarget(): WallpaperTarget = WallpaperTarget.Lock
    private fun saveTargetToPrefs(target: WallpaperTarget) = prefs.edit { putString("target", target.name) }
}
