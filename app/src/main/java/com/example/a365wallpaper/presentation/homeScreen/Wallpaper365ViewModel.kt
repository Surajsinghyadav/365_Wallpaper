package com.example.a365wallpaper.presentation.homeScreen

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    // FIX 1: SharingStarted.Lazily — only active when first collected,
    // not polling every 5 seconds on idle devices
    val isServiceActive: StateFlow<Boolean> = WorkManager.getInstance(appContext)
        .getWorkInfosForUniqueWorkFlow("DailyWallpaper")
        .map { workInfos ->
            val info = workInfos.firstOrNull()
            info?.state == WorkInfo.State.ENQUEUED || info?.state == WorkInfo.State.RUNNING
        }
        .stateIn(
            viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = false,
        )

    private val _setWallpaperTo = MutableStateFlow(loadSavedTarget())
    val setWallpaperTo = _setWallpaperTo.asStateFlow()

    // ── Visual prefs (Room-backed) ────────────────────────────────────────────
    private val _style                = MutableStateFlow(GridStyle.Dots)
    val style = _style.asStateFlow()

    private val _showMiniFloatingPreview = MutableStateFlow(true)
    val showMiniFloatingPreview = _showMiniFloatingPreview.asStateFlow()

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

    // ── Goals (Room Flow) ─────────────────────────────────────────────────────
    val goals: StateFlow<List<Goal>> = appDao.getGoalsFlow()
        .map { it?.goal ?: emptyList() }
        .stateIn(
            scope         = viewModelScope,
            started       = SharingStarted.WhileSubscribed(5_000),
            initialValue  = emptyList(),
        )

    // ── Special Dates ─────────────────────────────────────────────────────────
    private val _specialDatesOfYear  = MutableStateFlow<List<SpecialDateOfYear>>(emptyList())
    val specialDatesOfYear: StateFlow<List<SpecialDateOfYear>> = _specialDatesOfYear.asStateFlow()

    private val _specialDatesOfMonth = MutableStateFlow<List<SpecialDateOfMonth>>(emptyList())
    val specialDatesOfMonth: StateFlow<List<SpecialDateOfMonth>> = _specialDatesOfMonth.asStateFlow()

    private val _specialDatesOfGoal  = MutableStateFlow<List<SpecialDateOfGoal>>(emptyList())
    val specialDatesOfGoal: StateFlow<List<SpecialDateOfGoal>> = _specialDatesOfGoal.asStateFlow()

    // ── Number toggles ────────────────────────────────────────────────────────
    private val _showNumberInsteadOfDots = MutableStateFlow(false)
    val showNumberInsteadOfDots: StateFlow<Boolean> = _showNumberInsteadOfDots.asStateFlow()

    private val _showBothNumberAndDot = MutableStateFlow(false)
    val showBothNumberAndDot: StateFlow<Boolean> = _showBothNumberAndDot.asStateFlow()

    // ── FIX 2: Debounce jobs — one per persist concern ────────────────────────
    // Slider drags fire 60x/sec. These jobs cancel + restart on each call,
    // so Room only writes ONCE after the user lifts their finger (300ms idle).
    private var prefsDebounceJob:       Job? = null
    private var yearConfigDebounceJob:  Job? = null
    private var monthConfigDebounceJob: Job? = null
    private var goalConfigDebounceJob:  Job? = null
    private var togglesDebounceJob:     Job? = null

    // ── Init: load persisted state ────────────────────────────────────────────
    init {
        viewModelScope.launch {
            // 1. Shared visual prefs
            appDao.getAppPrefs()?.let { p ->
                _selectedAccentColor.value       = p.theme
                _style.value                     = p.gridStyle
                _verticalPosition.value          = p.verticalBias
                _showLabel.value                 = p.showLabel
                _monthDotSize.value              = p.monthDotSize
                _goalDotSize.value               = p.goalDotSize
                _showMiniFloatingPreview.value   = p.showMiniFloatingPreview
            }

            // 2. Year config
            appDao.getYearThemeConfig()?.let { year ->
                _specialDatesOfYear.value      = year.specialDates
                _showNumberInsteadOfDots.value = year.showNumberInsteadOfDots
                _showBothNumberAndDot.value    = year.showBothNumberAndDot
            }

            // 3. Month config
            appDao.getMonthThemeConfig()?.let { month ->
                _specialDatesOfMonth.value = month.specialDates
            }

            // 4. Goals special dates
            appDao.getGoalsThemeConfig()?.let { g ->
                _specialDatesOfGoal.value = g.specialDates
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

    // ── Visual prefs — instant UI update, debounced Room write ───────────────

    fun updateStyle(style: GridStyle) {
        _style.update { style }
        // Style changes are discrete (tap, not drag) — no debounce needed
        persistAppPrefsNow()
    }

    fun updateAccentColor(color: DotTheme) {
        _selectedAccentColor.update { color }
        // Color tap is discrete — no debounce needed
        persistAppPrefsNow()
    }

    fun toggleShowLabel(enabled: Boolean) {
        _showLabel.update { enabled }
        persistAppPrefsNow()
    }

    // FIX 3: verticalPosition is a SLIDER — debounce the Room write
    fun updateVerticalPosition(position: Float) {
        _verticalPosition.update { position.coerceIn(-1f, 1f) }  // instant UI
        prefsDebounceJob?.cancel()
        prefsDebounceJob = viewModelScope.launch {
            delay(300)           // wait 300ms after user stops dragging
            persistAppPrefsNow()
        }
    }

    // FIX 4: dot size sliders — same debounce treatment
    fun updateMonthDotSize(size: Float) {
        _monthDotSize.update { size.coerceIn(0.25f, 1.0f) }
        prefsDebounceJob?.cancel()
        prefsDebounceJob = viewModelScope.launch {
            delay(300)
            persistAppPrefsNow()
        }
    }

    fun updateGoalDotSize(size: Float) {
        _goalDotSize.update { size.coerceIn(0.25f, 1.0f) }
        prefsDebounceJob?.cancel()
        prefsDebounceJob = viewModelScope.launch {
            delay(300)
            persistAppPrefsNow()
        }
    }

    fun toggleMiniFloatingPreview() {
        _showMiniFloatingPreview.update { !it }
        persistAppPrefsNow()
    }

    fun toggleShowNumberInsteadOfDots(enabled: Boolean) {
        _showNumberInsteadOfDots.update { enabled }
        if (!enabled) _showBothNumberAndDot.update { false }
        persistYearTogglesDebounced()
    }

    fun toggleShowBothNumberAndDot(enabled: Boolean) {
        _showBothNumberAndDot.update { enabled }
        persistYearTogglesDebounced()
    }

    // ── Special Dates CRUD ────────────────────────────────────────────────────

    fun addSpecialDateYear(startDate: LocalDate, endDate: LocalDate, colorArgb: Int) {
        _specialDatesOfYear.update {
            it + SpecialDateOfYear(
                id            = System.currentTimeMillis().toInt(),
                startEpochDay = startDate.toEpochDay(),
                endEpochDay   = endDate.toEpochDay(),
                colorArgb     = colorArgb,
            )
        }
        persistYearConfigDebounced()
    }

    fun removeSpecialDateYear(id: Int) {
        _specialDatesOfYear.update { it.filter { sd -> sd.id != id } }
        persistYearConfigDebounced()
    }

    fun addSpecialDateMonth(startDate: LocalDate, endDate: LocalDate, colorArgb: Int) {
        _specialDatesOfMonth.update {
            it + SpecialDateOfMonth(
                id            = System.currentTimeMillis().toInt(),
                startEpochDay = startDate.toEpochDay(),
                endEpochDay   = endDate.toEpochDay(),
                colorArgb     = colorArgb,
            )
        }
        persistMonthConfigDebounced()
    }

    fun removeSpecialDateMonth(id: Int) {
        _specialDatesOfMonth.update { it.filter { sd -> sd.id != id } }
        persistMonthConfigDebounced()
    }

    fun addSpecialDateGoal(
        startDate: LocalDate,
        endDate: LocalDate,
        colorArgb: Int,
        goalTitle: String = "",
    ) {
        _specialDatesOfGoal.update {
            it + SpecialDateOfGoal(
                id            = System.currentTimeMillis().toInt(),
                goalTitle     = goalTitle,
                startEpochDay = startDate.toEpochDay(),
                endEpochDay   = endDate.toEpochDay(),
                colorArgb     = colorArgb,
            )
        }
        persistGoalSpecialDatesDebounced()
    }

    fun removeSpecialDateGoal(id: Int) {
        _specialDatesOfGoal.update { it.filter { sd -> sd.id != id } }
        persistGoalSpecialDatesDebounced()
    }

    // ── Goals CRUD ────────────────────────────────────────────────────────────

    fun canAddGoal(): Boolean = goals.value.size < 2

    fun addGoal(goal: Goal) {
        viewModelScope.launch {
            val current = goals.value
            if (current.size >= 2) return@launch
            val capitalized = goal.copy(
                title = goal.title.trim().replaceFirstChar { it.uppercase() }
            )
            persistGoals(current + capitalized)
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
                    .setInputData(
                        workDataOf(
                            DailyWallpaperWorker.KEY_TARGET to target.name,
                            DailyWallpaperWorker.KEY_MODE   to currentMode.name,
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
            WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
                "DailyWallpaper",
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<DailyWallpaperWorker>(1, TimeUnit.DAYS)
                    .setInitialDelay(calculateDelayUntilNextMorning(), TimeUnit.MILLISECONDS)
                    .setConstraints(
                        Constraints.Builder().setRequiresBatteryNotLow(true).build()
                    )
                    .setInputData(
                        workDataOf(
                            DailyWallpaperWorker.KEY_TARGET to currentTarget.name,
                            DailyWallpaperWorker.KEY_MODE   to currentMode.name,
                        )
                    )
                    .build()
            )
        }
    }

    fun cancelAutoDailyWallpaperUpdate(): String {
        WorkManager.getInstance(appContext).cancelUniqueWork("DailyWallpaper")
        return "Automatic daily updates have been disabled"
    }

    // ── Private: immediate persist (for discrete actions like taps) ───────────

    private fun persistAppPrefsNow() {
        viewModelScope.launch {
            appDao.saveAppPrefs(
                AppPrefsEntity(
                    theme                   = _selectedAccentColor.value,
                    gridStyle               = _style.value,
                    showLabel               = _showLabel.value,
                    verticalBias            = _verticalPosition.value,
                    monthDotSize            = _monthDotSize.value,
                    goalDotSize             = _goalDotSize.value,
                    showMiniFloatingPreview = _showMiniFloatingPreview.value,
                )
            )
        }
    }

    // ── Private: debounced persists (for sliders and rapid changes) ───────────

    private fun persistYearTogglesDebounced() {
        togglesDebounceJob?.cancel()
        togglesDebounceJob = viewModelScope.launch {
            delay(300)
            val existing = appDao.getYearThemeConfig() ?: YearDotsSpec(
                theme        = _selectedAccentColor.value,
                gridStyle    = _style.value,
                showLabel    = _showLabel.value,
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

    private fun persistYearConfigDebounced() {
        yearConfigDebounceJob?.cancel()
        yearConfigDebounceJob = viewModelScope.launch {
            delay(300)
            val existing = appDao.getYearThemeConfig() ?: YearDotsSpec(
                theme        = _selectedAccentColor.value,
                gridStyle    = _style.value,
                showLabel    = _showLabel.value,
                verticalBias = _verticalPosition.value,
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

    private fun persistMonthConfigDebounced() {
        monthConfigDebounceJob?.cancel()
        monthConfigDebounceJob = viewModelScope.launch {
            delay(300)
            val existing = appDao.getMonthThemeConfig() ?: MonthDotsSpec(
                theme             = _selectedAccentColor.value,
                gridStyle         = _style.value,
                showLabel         = _showLabel.value,
                verticalBias      = _verticalPosition.value,
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

    private fun persistGoalSpecialDatesDebounced() {
        goalConfigDebounceJob?.cancel()
        goalConfigDebounceJob = viewModelScope.launch {
            delay(300)
            val existing = appDao.getGoalsThemeConfig() ?: GoalsEntity(
                goal              = goals.value,
                showLabel         = _showLabel.value,
                dotSizeMultiplier = _goalDotSize.value,
                theme             = _selectedAccentColor.value,
                gridStyle         = _style.value,
                verticalBias      = _verticalPosition.value,
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

    private fun persistGoals(updatedList: List<Goal>) {
        // Goals add/remove is a discrete action — write immediately
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

    // ── Private: snapshot all state to Room before worker dispatch ────────────

    private suspend fun saveCurrentModeConfig(mode: WallpaperMode) {
        val theme       = _selectedAccentColor.value
        val gridStyle   = _style.value
        val vertBias    = _verticalPosition.value
        val showLabel   = _showLabel.value
        val showNumbers = _showNumberInsteadOfDots.value
        val showBoth    = _showBothNumberAndDot.value

        when (mode) {
            WallpaperMode.Year -> {
                val existing = appDao.getYearThemeConfig()
                appDao.saveYearThemeConfig(
                    YearDotsSpec(
                        theme                   = theme,
                        gridStyle               = gridStyle,
                        showLabel               = showLabel,
                        verticalBias            = vertBias,
                        specialDates            = _specialDatesOfYear.value.ifEmpty {
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
                        theme                   = theme,
                        gridStyle               = gridStyle,
                        showLabel               = showLabel,
                        verticalBias            = vertBias,
                        dotSizeMultiplier       = _monthDotSize.value,
                        specialDates            = _specialDatesOfMonth.value.ifEmpty {
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
                        goal              = goals.value,
                        showLabel         = showLabel,
                        dotSizeMultiplier = _goalDotSize.value,
                        theme             = theme,
                        gridStyle         = gridStyle,
                        verticalBias      = vertBias,
                    )).copy(
                        goal                    = goals.value,
                        theme                   = theme,
                        gridStyle               = gridStyle,
                        showLabel               = showLabel,
                        verticalBias            = vertBias,
                        dotSizeMultiplier       = _goalDotSize.value,
                        specialDates            = _specialDatesOfGoal.value.ifEmpty {
                            existing?.specialDates ?: emptyList()
                        },
                        showNumberInsteadOfDots = showNumbers,
                        showBothNumberAndDot    = showBoth,
                    )
                )
            }
        }
    }

    // ── Private: helpers ──────────────────────────────────────────────────────

    private fun loadSavedTarget(): WallpaperTarget =
        WallpaperTarget.valueOf(
            prefs.getString("target", WallpaperTarget.Lock.name) ?: WallpaperTarget.Lock.name
        )

    private fun calculateDelayUntilNextMorning(): Long {
        val now  = LocalDateTime.now()
        var next = LocalDateTime.of(now.toLocalDate(), LocalTime.of(1, 0))
        if (!now.isBefore(next)) next = next.plusDays(1)
        return Duration.between(now, next).toMillis()
    }
}
