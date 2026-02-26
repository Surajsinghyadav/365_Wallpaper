package com.example.a365wallpaper.presentation.HomeScreen

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.a365wallpaper.BitmapGenerators.Goal
import com.example.a365wallpaper.BitmapGenerators.MonthDotsSpec
import com.example.a365wallpaper.BitmapGenerators.YearDotsSpec
import com.example.a365wallpaper.Worker.DailyWallpaperWorker
import com.example.a365wallpaper.data.Local.GridStyle
import com.example.a365wallpaper.data.Local.SpecialDateOfGoal
import com.example.a365wallpaper.data.Local.SpecialDateOfMonth
import com.example.a365wallpaper.data.Local.SpecialDateOfYear
import com.example.a365wallpaper.data.Local.WallpaperMode
import com.example.a365wallpaper.data.Local.WallpaperTarget
import com.example.a365wallpaper.data.database.Dao.AppDao
import com.example.a365wallpaper.data.database.Entity.AppPrefsEntity
import com.example.a365wallpaper.data.database.Entity.GoalsEntity
import com.example.a365wallpaper.ui.theme.DotTheme
import com.example.a365wallpaper.ui.theme.DotThemes
import com.example.a365wallpaper.utils.toEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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

    private val _showMiniFloatingPreview  = MutableStateFlow<Boolean>(false)
    val showMiniFloatingPreview = _showMiniFloatingPreview.asStateFlow()

    private val _selectedAccentColor = MutableStateFlow(DotThemes.All.first())
    val selectedAccentColor = _selectedAccentColor.asStateFlow()

    private val _showLabel = MutableStateFlow(true)
    val showLabel = _showLabel.asStateFlow()

    private val _verticalPosition = MutableStateFlow(0f)
    val verticalPosition = _verticalPosition.asStateFlow()

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

    private val _specialDatesOfYear  = MutableStateFlow<List<SpecialDateOfYear>>(emptyList())
    val specialDatesOfYear: StateFlow<List<SpecialDateOfYear>> = _specialDatesOfYear.asStateFlow()

    private val _specialDatesOfMonth = MutableStateFlow<List<SpecialDateOfMonth>>(emptyList())
    val specialDatesOfMonth: StateFlow<List<SpecialDateOfMonth>> = _specialDatesOfMonth.asStateFlow()

    private val _specialDatesOfGoal  = MutableStateFlow<List<SpecialDateOfGoal>>(emptyList())
    val specialDatesOfGoal: StateFlow<List<SpecialDateOfGoal>> = _specialDatesOfGoal.asStateFlow()

    // ── Number toggles (shared across all modes) ──────────────────────────────────
    private val _showNumberInsteadOfDots = MutableStateFlow(false)
    val showNumberInsteadOfDots: StateFlow<Boolean> = _showNumberInsteadOfDots.asStateFlow()

    private val _showBothNumberAndDot = MutableStateFlow(false)
    val showBothNumberAndDot: StateFlow<Boolean> = _showBothNumberAndDot.asStateFlow()

    // ── Dot sizes ─────────────────────────────────────────────────────────────────
    private val _monthDotSize = MutableStateFlow(1.0f)
    val monthDotSize = _monthDotSize.asStateFlow()

    private val _goalDotSize = MutableStateFlow(1.0f)
    val goalDotSize = _goalDotSize.asStateFlow()

    init {
        viewModelScope.launch {
            // 1. Shared visual prefs
            appDao.getAppPrefs()?.let { p ->
                _selectedAccentColor.value = p.theme
                _style.value               = p.gridStyle
                _verticalPosition.value    = p.verticalBias
                _showLabel.value           = p.showLabel
                _monthDotSize.value        = p.monthDotSize
                _goalDotSize.value         = p.goalDotSize
                _showMiniFloatingPreview.value = p.showMiniFloatingPreview
            }

            // 2. Year config — includes toggles + special dates
            appDao.getYearThemeConfig()?.let { year ->
                _specialDatesOfYear.value        = year.specialDates
                _showNumberInsteadOfDots.value   = year.showNumberInsteadOfDots
                _showBothNumberAndDot.value      = year.showBothNumberAndDot
            }

            // 3. Month config — special dates + toggles
            appDao.getMonthThemeConfig()?.let { month ->
                _specialDatesOfMonth.value = month.specialDates
                // showNumberInsteadOfDots is shared; Year wins on first load.
                // If you want per-mode toggles, store them separately.
            }

            // 4. Goals config — special dates
            appDao.getGoalsThemeConfig()?.let { goals ->
                _specialDatesOfGoal.value = goals.specialDates
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

    fun toggleShowNumberInsteadOfDots(enabled: Boolean) {
        _showNumberInsteadOfDots.update { enabled }
        // Turning off parent resets child
        if (!enabled) _showBothNumberAndDot.update { false }
        persistYearToggles()
    }

    fun toggleShowBothNumberAndDot(enabled: Boolean) {
        _showBothNumberAndDot.update { enabled }
        persistYearToggles()
    }
    fun updateMonthDotSize(size: Float) {
        _monthDotSize.update { size.coerceIn(0.25f, 1.0f) }
        persistAppPrefs()
    }

    fun updateGoalDotSize(size: Float) {
        _goalDotSize.update { size.coerceIn(0.25f, 1.0f) }
        persistAppPrefs()
    }


    fun toggleMiniFloatingPreview(){
        _showMiniFloatingPreview.update { !it }
        persistAppPrefs()
    }


    // ── Special Dates CRUD ────────────────────────────────────────────────────

    // ── Year ──────────────────────────────────────────────────────────────────────
    fun addSpecialDateYear(startDate: LocalDate, endDate: LocalDate, colorArgb: Int) {
        val entry = SpecialDateOfYear(
            id = System.currentTimeMillis().toInt(),
            startEpochDay = startDate.toEpochDay(),
            endEpochDay   = endDate.toEpochDay(),
            colorArgb     = colorArgb,
        )
        _specialDatesOfYear.update { it + entry }
        persistYearConfig()
    }

    fun removeSpecialDateYear(id: Int) {
        _specialDatesOfYear.update { it.filter { sd -> sd.id != id } }
        persistYearConfig()
    }

    // ── Month ─────────────────────────────────────────────────────────────────────
    fun addSpecialDateMonth(startDate: LocalDate, endDate: LocalDate, colorArgb: Int) {
        val entry = SpecialDateOfMonth(
            id = System.currentTimeMillis().toInt(),
            startEpochDay = startDate.toEpochDay(),
            endEpochDay   = endDate.toEpochDay(),
            colorArgb     = colorArgb,
        )
        _specialDatesOfMonth.update { it + entry }
        persistMonthConfig()
    }

    fun removeSpecialDateMonth(id: Int) {
        _specialDatesOfMonth.update { it.filter { sd -> sd.id != id } }
        persistMonthConfig()
    }

    // ── Goals ─────────────────────────────────────────────────────────────────────
    fun addSpecialDateGoal(startDate: LocalDate, endDate: LocalDate, colorArgb: Int) {
        val entry = SpecialDateOfGoal(
            id = System.currentTimeMillis().toInt(),
            startEpochDay = startDate.toEpochDay(),
            endEpochDay   = endDate.toEpochDay(),
            colorArgb     = colorArgb,
        )
        _specialDatesOfGoal.update { it + entry }
        persistGoalSpecialDates()
    }

    fun removeSpecialDateGoal(id: Int) {
        _specialDatesOfGoal.update { it.filter { sd -> sd.id != id } }
        persistGoalSpecialDates()
    }


    fun removeSpecialDate(id: Int) {
        _specialDatesOfYear.update { list -> list.filter { it.id != id } }
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
            WorkManager.Companion.getInstance(appContext).enqueue(
                OneTimeWorkRequestBuilder<DailyWallpaperWorker>()
                    .setInputData(
                        workDataOf(
                            DailyWallpaperWorker.Companion.KEY_TARGET to target.name,
                            DailyWallpaperWorker.Companion.KEY_MODE to currentMode.name,
                        )
                    )
                    .build()
            )
        }
    }

    fun scheduleAutoDailyWallpaperUpdate() {
        val currentMode   = _mode.value
        val currentTarget = _setWallpaperTo.value
        viewModelScope.launch {
            saveCurrentModeConfig(currentMode)
            WorkManager.Companion.getInstance(appContext).enqueueUniquePeriodicWork(
                "DailyWallpaper",
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<DailyWallpaperWorker>(1, TimeUnit.DAYS)
                    .setInitialDelay(calculateDelayUntilNextMorning(), TimeUnit.MILLISECONDS)
                    .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                    .setInputData(
                        workDataOf(
                            DailyWallpaperWorker.Companion.KEY_TARGET to currentTarget.name,
                            DailyWallpaperWorker.Companion.KEY_MODE to currentMode.name,
                        )
                    )
                    .build()
            )
        }
    }

    fun cancelAutoDailyWallpaperUpdate(): String {
        WorkManager.Companion.getInstance(appContext).cancelUniqueWork("DailyWallpaper")
        return "Auto wallpaper stopped"
    }

    // ── Private: persist helpers ──────────────────────────────────────────────

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
                    showMiniFloatingPreview = _showMiniFloatingPreview.value
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
                theme = _selectedAccentColor.value,
                gridStyle = _style.value,
                showLabel = _showLabel.value,
                verticalBias = _verticalPosition.value,
            ).toEntity()
            appDao.saveYearThemeConfig(
                existing.copy(specialDates = _specialDatesOfYear.value)
            )
        }
    }

    private fun persistGoals(updatedList: List<Goal>) {
        viewModelScope.launch {
            val existing = appDao.getGoalsThemeConfig() ?: GoalsEntity(
                goal = emptyList(),
                showLabel = _showLabel.value,
                dotSizeMultiplier = _goalDotSize.value,
                theme = _selectedAccentColor.value,
                gridStyle = _style.value,
                verticalBias = _verticalPosition.value,
            )
            appDao.saveGoalsThemeConfig(existing.copy(goal = updatedList))
        }
    }


    private fun persistYearConfig() {
        viewModelScope.launch {
            val existing = appDao.getYearThemeConfig() ?: YearDotsSpec(
                theme = _selectedAccentColor.value, gridStyle = _style.value,
                showLabel = _showLabel.value, verticalBias = _verticalPosition.value,
            ).toEntity()
            appDao.saveYearThemeConfig(
                existing.copy(
                    specialDates            = _specialDatesOfYear.value,
                    showNumberInsteadOfDots = _showNumberInsteadOfDots.value,
                    showBothNumberAndDot    = _showBothNumberAndDot.value,
                )
            )
        }
    }

    private fun persistMonthConfig() {
        viewModelScope.launch {
            val existing = appDao.getMonthThemeConfig() ?: MonthDotsSpec(
                theme = _selectedAccentColor.value, gridStyle = _style.value,
                showLabel = _showLabel.value, verticalBias = _verticalPosition.value,
                dotSizeMultiplier = _monthDotSize.value,
            ).toEntity()
            appDao.saveMonthThemeConfig(
                existing.copy(
                    specialDates            = _specialDatesOfMonth.value,
                    showNumberInsteadOfDots = _showNumberInsteadOfDots.value,
                    showBothNumberAndDot    = _showBothNumberAndDot.value,
                )
            )
        }
    }

    private fun persistGoalSpecialDates() {
        viewModelScope.launch {
            val existing = appDao.getGoalsThemeConfig() ?: GoalsEntity(
                goal = goals.value, showLabel = _showLabel.value,
                dotSizeMultiplier = _goalDotSize.value,
                theme = _selectedAccentColor.value, gridStyle = _style.value,
                verticalBias = _verticalPosition.value,
            )
            appDao.saveGoalsThemeConfig(
                existing.copy(
                    specialDates            = _specialDatesOfGoal.value,
                    showNumberInsteadOfDots = _showNumberInsteadOfDots.value,
                    showBothNumberAndDot    = _showBothNumberAndDot.value,
                )
            )
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
        val showNumbers  = _showNumberInsteadOfDots.value
        val showBoth     = _showBothNumberAndDot.value

        when (mode) {
            WallpaperMode.Year -> {
                val existing = appDao.getYearThemeConfig()
                appDao.saveYearThemeConfig(
                    YearDotsSpec(
                        theme        = theme,
                        gridStyle    = gridStyle,
                        showLabel    = showLabel,
                        verticalBias = verticalBias,
                        specialDates = _specialDatesOfYear.value.ifEmpty {
                            existing?.specialDates ?: emptyList()
                        },
                        showNumberInsteadOfDots = showNumbers,
                        showBothNumberAndDot    = showBoth,
                    ).toEntity()
                )
            }

            WallpaperMode.Month -> {
                val existing = appDao.getMonthThemeConfig()
                appDao.saveMonthThemeConfig(
                    MonthDotsSpec(
                        theme        = theme,
                        gridStyle    = gridStyle,
                        showLabel    = showLabel,
                        verticalBias = verticalBias,
                        dotSizeMultiplier = _monthDotSize.value,
                        specialDates = _specialDatesOfMonth.value.ifEmpty {
                            existing?.specialDates ?: emptyList()
                        },
                        showNumberInsteadOfDots = showNumbers,
                        showBothNumberAndDot    = showBoth,
                    ).toEntity()
                )
            }

            WallpaperMode.Goals -> {
                val existing = appDao.getGoalsThemeConfig()
                appDao.saveGoalsThemeConfig(
                    (existing ?: GoalsEntity(
                        goal = goals.value, showLabel = showLabel,
                        dotSizeMultiplier = _goalDotSize.value,
                        theme = theme, gridStyle = gridStyle, verticalBias = verticalBias,
                    )).copy(
                        goal              = goals.value,
                        theme             = theme,
                        gridStyle         = gridStyle,
                        showLabel         = showLabel,
                        verticalBias      = verticalBias,
                        dotSizeMultiplier = _goalDotSize.value,
                        specialDates      = _specialDatesOfGoal.value.ifEmpty {
                            existing?.specialDates ?: emptyList()
                        },
                        showNumberInsteadOfDots = showNumbers,
                        showBothNumberAndDot    = showBoth,
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
    private fun persistYearToggles() {
        viewModelScope.launch {
            val existing = appDao.getYearThemeConfig() ?: YearDotsSpec(
                theme = _selectedAccentColor.value,
                gridStyle = _style.value,
                showLabel = _showLabel.value,
                verticalBias = _verticalPosition.value,
            ).toEntity()
            appDao.saveYearThemeConfig(
                existing.copy(
                    showNumberInsteadOfDots = _showNumberInsteadOfDots.value,
                    showBothNumberAndDot    = _showBothNumberAndDot.value,
                )
            )
        }
    }

}