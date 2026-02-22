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
import com.example.a365wallpaper.data.database.AppPrefsEntity
import com.example.a365wallpaper.data.database.GoalsEntity
import com.example.a365wallpaper.ui.theme.DotTheme
import com.example.a365wallpaper.ui.theme.DotThemes
import com.example.a365wallpaper.utils.toEntity
import com.example.a365wallpaper.utils.toExternalModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class Wallpaper365ViewModel(
    val appContext: Application,
    val appDao: AppDao
) : ViewModel() {

    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()
    private val prefs = appContext.getSharedPreferences("wallpaperprefs", Context.MODE_PRIVATE)

    // ── Mode & Target (SharedPreferences) ────────────────────────────────────
    private val _mode = MutableStateFlow(WallpaperMode.Year)
    val mode = _mode.asStateFlow()

    private val _setWallpaperTo = MutableStateFlow(loadSavedTarget())
    val setWallpaperTo = _setWallpaperTo.asStateFlow()

    // ── All other prefs from Room (initialized with defaults, loaded in init) ─
    private val _style = MutableStateFlow(GridStyle.Dots)
    val style = _style.asStateFlow()

    private val _selectedAccentColor = MutableStateFlow(DotThemes.All.first())
    val selectedAccentColor = _selectedAccentColor.asStateFlow()

    private val _showLabel = MutableStateFlow(true)
    val showLabel = _showLabel.asStateFlow()

    private val _verticalPosition = MutableStateFlow(0f)
    val verticalPosition = _verticalPosition.asStateFlow()

    private val _monthDotSize = MutableStateFlow(1.0f)
    val monthDotSize = _monthDotSize.asStateFlow()

    private val _goalDotSize = MutableStateFlow(1.0f)
    val goalDotSize = _goalDotSize.asStateFlow()

    // ── Wallpaper set animation trigger ───────────────────────────────────────
    private val _wallpaperSetEvent = MutableStateFlow(false)
    val wallpaperSetEvent = _wallpaperSetEvent.asStateFlow()

    // ── Goals from Room via Flow ──────────────────────────────────────────────
    val goals: StateFlow<List<Goal>> = appDao.getGoalsFlow()
        .map { entity -> entity?.goal ?: emptyList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ── Init: load all prefs from Room on startup ─────────────────────────────
    init {
        viewModelScope.launch {
            val prefs = appDao.getAppPrefs()
            if (prefs != null) {
                _selectedAccentColor.value = prefs.theme
                _style.value = prefs.gridStyle
                _verticalPosition.value = prefs.verticalBias
                _showLabel.value = prefs.showLabel
                _monthDotSize.value = prefs.monthDotSize
                _goalDotSize.value = prefs.goalDotSize
            }
            _isReady.value = true

        }
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    fun acknowledgeWallpaperSet() = _wallpaperSetEvent.update { false }

    fun updateMode(mode: WallpaperMode) {
        _mode.update { mode }
        prefs.edit { putString("mode", mode.name) } // ✅ SharedPrefs only for mode
    }

    fun updateSetWallpaperTo(target: WallpaperTarget) {
        _setWallpaperTo.update { target }
        prefs.edit { putString("target", target.name) } // ✅ SharedPrefs only for target
    }

    fun updateStyle(style: GridStyle) {
        _style.update { style }
        persistAppPrefs()
    }

    fun updateAccentColor(color: DotTheme) {
        _selectedAccentColor.update { color }
        persistAppPrefs()
    }

    fun toggleShowLabel(bool: Boolean) {
        _showLabel.update { bool }
        persistAppPrefs()
    }

    fun updateVerticalPosition(position: Float) {
        _verticalPosition.update { position.coerceIn(-1f, 1f) }
        persistAppPrefs()
    }

    fun updateMonthDotSize(size: Float) {
        _monthDotSize.update { size.coerceIn(0.5f, 2.0f) }
        persistAppPrefs()
    }

    fun updateGoalDotSize(size: Float) {
        _goalDotSize.update { size.coerceIn(0.5f, 2.0f) }
        persistAppPrefs()
    }

    // ── Goals CRUD ────────────────────────────────────────────────────────────

    fun addGoal(goal: Goal) {
        viewModelScope.launch {
            val current = goals.value
            if (current.size < 2) {
                val updatedList = current + goal
                val existing = appDao.getGoalsThemeConfig()
                appDao.saveGoalsThemeConfig(
                    (existing ?: GoalsEntity(
                        goal = emptyList(),
                        showLabel = _showLabel.value,
                        dotSizeMultiplier = _goalDotSize.value,
                        theme = _selectedAccentColor.value,
                        gridStyle = _style.value,
                        verticalBias = _verticalPosition.value,
                    )).copy(goal = updatedList)
                )
            }
        }
    }

    fun removeGoal(index: Int) {
        viewModelScope.launch {
            val current = goals.value.toMutableList()
            if (index in current.indices) {
                current.removeAt(index)
                val existing = appDao.getGoalsThemeConfig()
                appDao.saveGoalsThemeConfig(
                    (existing ?: GoalsEntity(
                        goal = emptyList(),
                        showLabel = _showLabel.value,
                        dotSizeMultiplier = _goalDotSize.value,
                        theme = _selectedAccentColor.value,
                        gridStyle = _style.value,
                        verticalBias = _verticalPosition.value,
                    )).copy(goal = current)
                )
            }
        }
    }

    fun canAddGoal(): Boolean = goals.value.size < 2

    // ── Wallpaper worker ──────────────────────────────────────────────────────

    fun runDailyWallpaperWorker(target: WallpaperTarget) {
        val mode = _mode.value
        _wallpaperSetEvent.update { true }

        viewModelScope.launch {
            // ✅ Always save config first, then enqueue — no race condition
            saveCurrentModeConfig(mode)

            val req = OneTimeWorkRequestBuilder<DailyWallpaperWorker>()
                .setInputData(
                    workDataOf(
                        DailyWallpaperWorker.KEY_TARGET to target.name,
                        DailyWallpaperWorker.KEY_MODE to mode.name,
                    )
                ).build()
            WorkManager.getInstance(appContext).enqueue(req)
        }
    }

    fun scheduleAutoDailyWallpaperUpdate() {
        val target = _setWallpaperTo.value
        val mode = _mode.value

        viewModelScope.launch {
            saveCurrentModeConfig(mode)

            val req = PeriodicWorkRequestBuilder<DailyWallpaperWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(calculateDelayUntilNextMorning(), TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder().setRequiresBatteryNotLow(true).build()
                )
                .setInputData(
                    workDataOf(
                        DailyWallpaperWorker.KEY_TARGET to target.name,
                        DailyWallpaperWorker.KEY_MODE to mode.name,
                    )
                ).build()

            WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
                "DailyWallpaper",
                ExistingPeriodicWorkPolicy.UPDATE,
                req
            )
        }
    }

    fun cancelAutoDailyWallpaperUpdate(): String {
        WorkManager.getInstance(appContext).cancelUniqueWork("DailyWallpaper")
        return "Auto wallpaper stopped"
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    // ✅ Persists all non-mode/target prefs to Room DB
    private fun persistAppPrefs() {
        viewModelScope.launch {
            appDao.saveAppPrefs(
                AppPrefsEntity(
                    theme = _selectedAccentColor.value,
                    gridStyle = _style.value,
                    showLabel = _showLabel.value,
                    verticalBias = _verticalPosition.value,
                    monthDotSize = _monthDotSize.value,
                    goalDotSize = _goalDotSize.value,
                )
            )
        }
    }

    // ✅ Saves the current mode's wallpaper config (Year/Month/Goals) to Room
    private suspend fun saveCurrentModeConfig(mode: WallpaperMode) {
        val theme = _selectedAccentColor.value
        val gridStyle = _style.value
        val verticalBias = _verticalPosition.value
        val showLabel = _showLabel.value

        when (mode) {
            WallpaperMode.Year -> appDao.saveYearThemeConfig(
                YearDotsSpec(
                    gridStyle = gridStyle,
                    verticalBias = verticalBias,
                    theme = theme,
                    showLabel = showLabel,
                ).toEntity()
            )

            WallpaperMode.Month -> appDao.saveMonthThemeConfig(
                MonthDotsSpec(
                    gridStyle = gridStyle,
                    verticalBias = verticalBias,
                    theme = theme,
                    showLabel = showLabel,
                    dotSizeMultiplier = _monthDotSize.value,
                ).toEntity()
            )

            WallpaperMode.Goals -> {
                val existing = appDao.getGoalsThemeConfig()
                appDao.saveGoalsThemeConfig(
                    (existing ?: GoalsEntity(
                        goal = goals.value,
                        showLabel = showLabel,
                        dotSizeMultiplier = _goalDotSize.value,
                        theme = theme,
                        gridStyle = gridStyle,
                        verticalBias = verticalBias,
                    )).copy(
                        goal = goals.value,
                        theme = theme,
                        gridStyle = gridStyle,
                        verticalBias = verticalBias,
                        showLabel = showLabel,
                        dotSizeMultiplier = _goalDotSize.value,
                    )
                )
            }
        }
    }

    private fun calculateDelayUntilNextMorning(): Long {
        val now = LocalDateTime.now()
        var next = LocalDateTime.of(now.toLocalDate(), LocalTime.of(1, 0))
        if (!now.isBefore(next)) next = next.plusDays(1)
        return Duration.between(now, next).toMillis()
    }

    // ✅ Only these two remain in SharedPreferences
    private fun loadSavedMode(): WallpaperMode =
        WallpaperMode.valueOf(
            prefs.getString("mode", WallpaperMode.Year.name) ?: WallpaperMode.Year.name
        )

    private fun loadSavedTarget(): WallpaperTarget =
        WallpaperTarget.valueOf(
            prefs.getString("target", WallpaperTarget.Lock.name) ?: WallpaperTarget.Lock.name
        )
}
