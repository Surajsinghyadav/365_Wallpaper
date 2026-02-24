// app/src/main/java/com/example/a365wallpaper/presentation/Wallpaper365ViewModel.kt
package com.example.a365wallpaper.presentation

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.a365wallpaper.DailyWallpaperWorker
import com.example.a365wallpaper.Goal
import com.example.a365wallpaper.MonthDotsSpec
import com.example.a365wallpaper.YearDotsSpec
import com.example.a365wallpaper.data.GridStyle
import com.example.a365wallpaper.data.SpecialDateOfYear
import com.example.a365wallpaper.data.WallpaperMode
import com.example.a365wallpaper.data.WallpaperTarget
import com.example.a365wallpaper.data.database.AppDao
import com.example.a365wallpaper.data.database.AppPrefsEntity
import com.example.a365wallpaper.data.database.GoalsEntity
import com.example.a365wallpaper.ui.theme.DotTheme
import com.example.a365wallpaper.ui.theme.DotThemes
import com.example.a365wallpaper.utils.toEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class Wallpaper365ViewModel(
    val appContext: Application,
    val appDao: AppDao,
) : ViewModel() {

    // ── Readiness ─────────────────────────────────────────────────────────────
    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    // ── SharedPreferences (mode + target only) ────────────────────────────────
    private val prefs = appContext.getSharedPreferences("wallpaperprefs", Context.MODE_PRIVATE)

    // ── Mode & Target ─────────────────────────────────────────────────────────
    private val _mode = MutableStateFlow(WallpaperMode.Year)
    val mode = _mode.asStateFlow()

    private val _setWallpaperTo = MutableStateFlow(loadSavedTarget())
    val setWallpaperTo = _setWallpaperTo.asStateFlow()

    // ── Shared visual prefs (Room-backed) ─────────────────────────────────────
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

    // ── Goals (observed from Room via Flow) ───────────────────────────────────
    val goals: StateFlow<List<Goal>> = appDao.getGoalsFlow()
        .map { it?.goal ?: emptyList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    // ── Special Dates (Year mode only, Room-backed via YearEntity) ────────────
    private val _specialDates = MutableStateFlow<List<SpecialDateOfYear>>(emptyList())
    val specialDates: StateFlow<List<SpecialDateOfYear>> = _specialDates.asStateFlow()

    // ── Init: load all persisted state from Room ──────────────────────────────
    init {
        viewModelScope.launch {
            // 1. Load shared visual prefs
            appDao.getAppPrefs()?.let { p ->
                _selectedAccentColor.value = p.theme
                _style.value               = p.gridStyle
                _verticalPosition.value    = p.verticalBias
                _showLabel.value           = p.showLabel
                _monthDotSize.value        = p.monthDotSize
                _goalDotSize.value         = p.goalDotSize
            }

            // 2. Load special dates from YearEntity
            appDao.getYearThemeConfig()?.let { year ->
                _specialDates.value = year.specialDates
            }

            _isReady.value = true
        }
    }

    // ── Mode & Target ─────────────────────────────────────────────────────────

    fun updateMode(mode: WallpaperMode) {
        _mode.update { mode }
        prefs.edit { putString("mode", mode.name) }
    }

    fun updateSetWallpaperTo(target: WallpaperTarget) {
        _setWallpaperTo.update { target }
        prefs.edit { putString("target", target.name) }
    }

    // ── Shared visual prefs ───────────────────────────────────────────────────

    fun updateStyle(style: GridStyle) {
        _style.update { style }
        persistAppPrefs()
    }

    fun updateAccentColor(color: DotTheme) {
        _selectedAccentColor.update { color }
        persistAppPrefs()
    }

    fun toggleShowLabel(enabled: Boolean) {
        _showLabel.update { enabled }
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

    // ── Special Dates CRUD ────────────────────────────────────────────────────

    fun addSpecialDate(startDate: LocalDate, endDate: LocalDate, colorArgb: Int) {
        val entry = SpecialDateOfYear(
            id            = System.currentTimeMillis().toInt(),
            startEpochDay = startDate.toEpochDay(),
            endEpochDay   = endDate.toEpochDay(),
            colorArgb     = colorArgb,
        )
        _specialDates.update { it + entry }
        persistSpecialDates()
    }

    fun removeSpecialDate(id: Int) {
        _specialDates.update { list -> list.filter { it.id != id } }
        persistSpecialDates()
    }

    // ── Goals CRUD ────────────────────────────────────────────────────────────

    fun canAddGoal(): Boolean = goals.value.size < 2

    fun addGoal(goal: Goal) {
        viewModelScope.launch {
            val current = goals.value
            if (current.size >= 2) return@launch
            persistGoals(current + goal)
        }
    }

    fun removeGoal(index: Int) {
        viewModelScope.launch {
            val current = goals.value.toMutableList()
            if (index !in current.indices) return@launch
            current.removeAt(index)
            persistGoals(current)
        }
    }

    // ── Wallpaper worker ──────────────────────────────────────────────────────

    fun acknowledgeWallpaperSet() = _wallpaperSetEvent.update { false }

    fun runDailyWallpaperWorker(target: WallpaperTarget) {
        val currentMode = _mode.value
        _wallpaperSetEvent.update { true }
        viewModelScope.launch {
            saveCurrentModeConfig(currentMode)
            WorkManager.getInstance(appContext).enqueue(
                OneTimeWorkRequestBuilder<DailyWallpaperWorker>()
                    .setInputData(workDataOf(
                        DailyWallpaperWorker.KEY_TARGET to target.name,
                        DailyWallpaperWorker.KEY_MODE   to currentMode.name,
                    ))
                    .build()
            )
        }
    }

    fun scheduleAutoDailyWallpaperUpdate() {
        val currentMode   = _mode.value
        val currentTarget = _setWallpaperTo.value
        viewModelScope.launch {
            saveCurrentModeConfig(currentMode)
            WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
                "DailyWallpaper",
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<DailyWallpaperWorker>(1, TimeUnit.DAYS)
                    .setInitialDelay(calculateDelayUntilNextMorning(), TimeUnit.MILLISECONDS)
                    .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                    .setInputData(workDataOf(
                        DailyWallpaperWorker.KEY_TARGET to currentTarget.name,
                        DailyWallpaperWorker.KEY_MODE   to currentMode.name,
                    ))
                    .build()
            )
        }
    }

    fun cancelAutoDailyWallpaperUpdate(): String {
        WorkManager.getInstance(appContext).cancelUniqueWork("DailyWallpaper")
        return "Auto wallpaper stopped"
    }

    // ── Private: persist helpers ──────────────────────────────────────────────

    private fun persistAppPrefs() {
        viewModelScope.launch {
            appDao.saveAppPrefs(
                AppPrefsEntity(
                    theme        = _selectedAccentColor.value,
                    gridStyle    = _style.value,
                    showLabel    = _showLabel.value,
                    verticalBias = _verticalPosition.value,
                    monthDotSize = _monthDotSize.value,
                    goalDotSize  = _goalDotSize.value,
                )
            )
        }
    }

    /**
     * Writes updated special dates into the existing YearEntity without
     * touching any other year config fields (theme, gridStyle, etc.).
     */
    private fun persistSpecialDates() {
        viewModelScope.launch {
            val existing = appDao.getYearThemeConfig() ?: YearDotsSpec(
                theme        = _selectedAccentColor.value,
                gridStyle    = _style.value,
                showLabel    = _showLabel.value,
                verticalBias = _verticalPosition.value,
            ).toEntity()
            appDao.saveYearThemeConfig(
                existing.copy(specialDates = _specialDates.value)
            )
        }
    }

    private fun persistGoals(updatedList: List<Goal>) {
        viewModelScope.launch {
            val existing = appDao.getGoalsThemeConfig() ?: GoalsEntity(
                goal              = emptyList(),
                showLabel         = _showLabel.value,
                dotSizeMultiplier = _goalDotSize.value,
                theme             = _selectedAccentColor.value,
                gridStyle         = _style.value,
                verticalBias      = _verticalPosition.value,
            )
            appDao.saveGoalsThemeConfig(existing.copy(goal = updatedList))
        }
    }

    /**
     * Called before every worker dispatch. Snapshots current in-memory state
     * into Room so the worker always reads fresh config.
     */
    private suspend fun saveCurrentModeConfig(mode: WallpaperMode) {
        val theme        = _selectedAccentColor.value
        val gridStyle    = _style.value
        val verticalBias = _verticalPosition.value
        val showLabel    = _showLabel.value

        when (mode) {
            WallpaperMode.Year -> {
                // Merge: preserve already-saved specialDates if _specialDates is empty
                // (can happen if init hasn't finished, though isReady guards UI)
                val existing = appDao.getYearThemeConfig()
                appDao.saveYearThemeConfig(
                    YearDotsSpec(
                        theme        = theme,
                        gridStyle    = gridStyle,
                        showLabel    = showLabel,
                        verticalBias = verticalBias,
                        specialDates = _specialDates.value.ifEmpty {
                            existing?.specialDates ?: emptyList()
                        },
                    ).toEntity()
                )
            }

            WallpaperMode.Month -> appDao.saveMonthThemeConfig(
                MonthDotsSpec(
                    theme             = theme,
                    gridStyle         = gridStyle,
                    showLabel         = showLabel,
                    verticalBias      = verticalBias,
                    dotSizeMultiplier = _monthDotSize.value,
                ).toEntity()
            )

            WallpaperMode.Goals -> {
                val existing = appDao.getGoalsThemeConfig()
                appDao.saveGoalsThemeConfig(
                    (existing ?: GoalsEntity(
                        goal              = goals.value,
                        showLabel         = showLabel,
                        dotSizeMultiplier = _goalDotSize.value,
                        theme             = theme,
                        gridStyle         = gridStyle,
                        verticalBias      = verticalBias,
                    )).copy(
                        goal              = goals.value,
                        theme             = theme,
                        gridStyle         = gridStyle,
                        showLabel         = showLabel,
                        verticalBias      = verticalBias,
                        dotSizeMultiplier = _goalDotSize.value,
                    )
                )
            }
        }
    }

    // ── Private: SharedPreferences helpers ───────────────────────────────────

    private fun loadSavedMode(): WallpaperMode =
        WallpaperMode.valueOf(
            prefs.getString("mode", WallpaperMode.Year.name) ?: WallpaperMode.Year.name
        )

    private fun loadSavedTarget(): WallpaperTarget =
        WallpaperTarget.valueOf(
            prefs.getString("target", WallpaperTarget.Lock.name) ?: WallpaperTarget.Lock.name
        )

    // ── Private: timing ───────────────────────────────────────────────────────

    private fun calculateDelayUntilNextMorning(): Long {
        val now  = LocalDateTime.now()
        var next = LocalDateTime.of(now.toLocalDate(), LocalTime.of(1, 0))
        if (!now.isBefore(next)) next = next.plusDays(1)
        return Duration.between(now, next).toMillis()
    }
}
