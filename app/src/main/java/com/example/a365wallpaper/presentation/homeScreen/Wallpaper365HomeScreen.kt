package com.example.a365wallpaper.presentation.homeScreen

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.PlayCircle
import com.adamglin.phosphoricons.fill.StopCircle
import com.adamglin.phosphoricons.regular.*
import com.example.a365wallpaper.BitmapGenerators.Goal
import com.example.a365wallpaper.data.Local.GridStyle
import com.example.a365wallpaper.data.Local.SetWallpaperState
import com.example.a365wallpaper.data.Local.SpecialDateOfGoal
import com.example.a365wallpaper.data.Local.SpecialDateOfMonth
import com.example.a365wallpaper.data.Local.SpecialDateOfYear
import com.example.a365wallpaper.data.Local.WallpaperMode
import com.example.a365wallpaper.data.Local.WallpaperTarget
import com.example.a365wallpaper.presentation.previewComponents.MockGoalsDots
import com.example.a365wallpaper.presentation.previewComponents.MockMonthsDots
import com.example.a365wallpaper.presentation.previewComponents.MockYearsDots
import com.example.a365wallpaper.presentation.reusableComponents.SpecialDatesCard
import com.example.a365wallpaper.presentation.reusableComponents.VerticalPositionSlider
import com.example.a365wallpaper.presentation.reusableComponents.toDisplay
import com.example.a365wallpaper.utils.toColors
import com.example.a365wallpaper.ui.theme.AppColor
import com.example.a365wallpaper.ui.theme.DotTheme
import com.example.a365wallpaper.ui.theme.DotThemes
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun Wallpaper365HomeScreen(
    viewModel: Wallpaper365ViewModel,
    goToDevProfile: () -> Unit
) {
    val isReady by viewModel.isReady.collectAsStateWithLifecycle()
    val mode by viewModel.mode.collectAsStateWithLifecycle()
    val target by viewModel.setWallpaperTo.collectAsStateWithLifecycle()
    val gridStyle by viewModel.style.collectAsStateWithLifecycle()
    val showMiniFloatingPreview by viewModel.showMiniFloatingPreview.collectAsStateWithLifecycle()
    val accent by viewModel.selectedAccentColor.collectAsStateWithLifecycle()
    val showLabel by viewModel.showLabel.collectAsStateWithLifecycle()
    val verticalPosition by viewModel.verticalPosition.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val monthDotSize by viewModel.monthDotSize.collectAsStateWithLifecycle()
    val goalDotSize by viewModel.goalDotSize.collectAsStateWithLifecycle()
    val wallpaperSetEvent by viewModel.wallpaperSetEvent.collectAsStateWithLifecycle()
    val isServiceActive by viewModel.isServiceActive.collectAsStateWithLifecycle()
    val showNumberInsteadOfDots by viewModel.showNumberInsteadOfDots.collectAsStateWithLifecycle()
    val showBothNumberAndDot by viewModel.showBothNumberAndDot.collectAsStateWithLifecycle()
    val specialDatesYear by viewModel.specialDatesOfYear.collectAsStateWithLifecycle()
    val specialDatesMonth by viewModel.specialDatesOfMonth.collectAsStateWithLifecycle()
    val specialDatesGoal by viewModel.specialDatesOfGoal.collectAsStateWithLifecycle()


    var showAddGoalDialog by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()
    val context = LocalContext.current
    val showMiniPreview by remember {
        derivedStateOf {
            showMiniFloatingPreview && (lazyListState.firstVisibleItemIndex > 0 ||
                    lazyListState.firstVisibleItemScrollOffset > 1300)
        }
    }

//    if (!isReady) {
//        Box(modifier = Modifier
//            .fillMaxSize()
//            .background(MaterialTheme.colorScheme.background)
//        )
//        return
//    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = AppColor.RootBg,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 5.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "365 Wallpaper",
                                color = AppColor.TextPrimary,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.4).sp
                                )
                            )
                            Text(
                                "Calendar art for your Home & Lock screens",
                                color = AppColor.TextSecondary,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AppColor.GlassBg)
                                .clickable(onClick = goToDevProfile),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.DevToLogo,
                                contentDescription = "DevProfile",
                                tint = AppColor.TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Mode",
                            color = AppColor.TextSecondary,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold, fontSize = 13.sp
                            )
                        )
                        Spacer(Modifier.width(12.dp))
                        SegmentedPill(
                            items = WallpaperMode.entries,
                            selected = mode,
                            onSelected = { viewModel.updateMode(it) },
                            label = { it.label },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            bottomBar = {
                BottomActionSheet(
                    target = target,
                    onTargetClick = { viewModel.updateSetWallpaperTo(it) },
                    onStopService = {
                        val msg = viewModel.cancelAutoDailyWallpaperUpdate()
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    isServiceActive = isServiceActive,
                    onSetWallpaper = {
                        viewModel.runDailyWallpaperWorker(target)
                        viewModel.scheduleAutoDailyWallpaperUpdate()
                    },
                    isAnimating = wallpaperSetEvent,
                    onAnimationDone = { viewModel.acknowledgeWallpaperSet() }
                )
            }
        ) { padding ->
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // ── Live Preview ──────────────────────────────────────────────
                item {
                    PreviewSection(
                        isReady = isReady,
                        mode = mode,
                        dotTheme = accent,
                        gridStyle = gridStyle,
                        verticalPosition = verticalPosition,
                        showLabel = showLabel,
                        goals = goals,
                        monthDotSize = monthDotSize,
                        goalDotSize = goalDotSize,
                        showNumberInsteadOfDots = showNumberInsteadOfDots,
                        showBothNumberAndDot = showBothNumberAndDot,
                        specialDatesYear = specialDatesYear,
                        specialDatesMonth = specialDatesMonth,
                        specialDatesGoal = specialDatesGoal


                    )


                }

                // ── Accent Color ──────────────────────────────────────────────
                item {
                    GlassCard(title = "Accent color") {
                        ColorRow(
                            colors = DotThemes,
                            selected = accent,
                            onSelected = { viewModel.updateAccentColor(it) }
                        )
                    }
                }

                // ── Positioning ───────────────────────────────────────────────
                item {
                    GlassCard(title = "Positioning") {
                        VerticalPositionSlider(
                            position = verticalPosition,
                            onPositionChange = { viewModel.updateVerticalPosition(it) }
                        )
                    }
                }

                // ── Style ─────────────────────────────────────────────────────
                item {
                    GlassCard(title = "Style") {
                        ChipRow(
                            items = GridStyle.entries,
                            selected = gridStyle,
                            onSelected = { viewModel.updateStyle(it) },
                            label = { it.label }
                        )
                        Spacer(Modifier.height(4.dp))
                        when (mode) {
                            WallpaperMode.Month -> DotSizeSlider(
                                label = "Shape Size",
                                value = monthDotSize,
                                valueRange = 0.25f..1.0f,
                                onValueChange = { viewModel.updateMonthDotSize(it) }
                            )

                            WallpaperMode.Goals -> DotSizeSlider(
                                label = "Shape Size",
                                value = goalDotSize,
                                valueRange = 0.25f..1.0f,
                                onValueChange = { viewModel.updateGoalDotSize(it) }
                            )

                            else -> {}
                        }
                    }
                }

                // ── Goals card ────────────────────────────────────────────────────────────────
                if (mode == WallpaperMode.Goals) {
                    item {
                        GlassCard(title = "Add Goals") {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                goals.forEachIndexed { index, goal ->
                                    GoalRow(goal = goal, onRemove = { viewModel.removeGoal(index) })
                                }
                                if (viewModel.canAddGoal()) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { showAddGoalDialog = true },
                                        shape = RoundedCornerShape(12.dp),
                                        color = AppColor.Primary.copy(alpha = 0.10f),
                                        border = BorderStroke(
                                            1.dp,
                                            AppColor.Primary.copy(alpha = 0.35f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(
                                                horizontal = 14.dp,
                                                vertical = 12.dp
                                            ),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                PhosphorIcons.Regular.Plus,
                                                "Add Goal",
                                                tint = AppColor.Primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "Add Goal",
                                                color = AppColor.Primary,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

// ── Special Dates card — mode-aware ───────────────────────────────────────────
                item {
                    when (mode) {
                        WallpaperMode.Year -> SpecialDatesCard(
                            specialDates = specialDatesYear.map { it.toDisplay() },
                            mode = WallpaperMode.Year,
                            accent = accent,
                            onAdd = { start, end, color, _ ->
                                viewModel.addSpecialDateYear(start, end, color)
                            },
                            onRemove = { viewModel.removeSpecialDateYear(it) },
                        )

                        WallpaperMode.Month -> SpecialDatesCard(
                            specialDates = specialDatesMonth.map { it.toDisplay() },
                            mode = WallpaperMode.Year,
                            accent = accent,
                            onAdd = { start, end, color, _ ->
                                viewModel.addSpecialDateMonth(start, end, color)
                            },
                            onRemove = { viewModel.removeSpecialDateMonth(it) },
                        )

                        WallpaperMode.Goals -> SpecialDatesCard(
                            specialDates = specialDatesGoal.map { it.toDisplay() },
                            mode = WallpaperMode.Goals,
                            accent = accent,
                            goals = goals,
                            onAdd = { start, end, color, goalTitle ->
                                viewModel.addSpecialDateGoal(start, end, color, goalTitle)
                            },
                            onRemove = { viewModel.removeSpecialDateGoal(it) },
                        )
                    }

                }

// ── Elements ──────────────────────────────────────────────────────────────────
                item {
                    GlassCard(title = "Elements") {
                        ToggleRow(
                            icon = PhosphorIcons.Regular.CalendarBlank,
                            title = "Show Progress Stats",
                            subtitle = "Example: 331d left · 9%",
                            checked = showLabel,
                            onCheckedChange = { viewModel.toggleShowLabel(it) }
                        )
                        Spacer(Modifier.height(4.dp))
                        // Show Day Numbers — visible for all modes
                        ToggleRow(
                            icon = PhosphorIcons.Regular.Hash,
                            title = "Show Day Numbers",
                            subtitle = when (mode) {
                                WallpaperMode.Year -> "Replace each dot with its day number (1–365)"
                                WallpaperMode.Month -> "Replace each dot with its day number (1–31)"
                                WallpaperMode.Goals -> "Replace each dot with its day number"
                            },
                            checked = showNumberInsteadOfDots,
                            onCheckedChange = { viewModel.toggleShowNumberInsteadOfDots(it) }
                        )
                        AnimatedVisibility(
                            visible = showNumberInsteadOfDots,
                            enter = fadeIn() + slideInHorizontally { it },
                            exit = fadeOut() + slideOutHorizontally { -it },
                        ) {
                            ToggleRow(
                                icon = PhosphorIcons.Regular.CircleHalf,
                                title = "Show Both",
                                subtitle = "Draw dot with number overlaid on top",
                                checked = showBothNumberAndDot,
                                onCheckedChange = { viewModel.toggleShowBothNumberAndDot(it) }
                            )
                        }
                    }
                }


                item { Spacer(Modifier.height(20.dp)) }
            }
        }

        // ── Mini floating preview ─────────────────────────────────────────────
        AnimatedVisibility(
            visible = showMiniPreview,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 100.dp, start = 16.dp)
        ) {
            MiniFloatingPreview(
                mode = mode,
                dotTheme = accent,
                gridStyle = gridStyle,
                verticalPosition = verticalPosition,
                showLabel = showLabel,
                goals = goals,
                monthDotSize = monthDotSize,
                goalDotSize = goalDotSize,
                showNumberInsteadOfDots = showNumberInsteadOfDots,
                showBothNumberAndDot = showBothNumberAndDot,
                specialDatesYear = specialDatesYear,
                specialDatesMonth = specialDatesMonth,
                specialDatesGoal = specialDatesGoal,
                isReady = isReady
            )
        }

        if (showAddGoalDialog) {
            AddGoalDialog(
                onDismiss = { showAddGoalDialog = false },
                onConfirm = { goal ->
                    viewModel.addGoal(goal)
                    showAddGoalDialog = false
                }
            )
        }
    }
}


// ── PreviewSection ────────────────────────────────────────────────────────────
// ✅ FIX 1: accept and forward the two new params
@Composable
private fun PreviewSection(
    mode: WallpaperMode,
    dotTheme: DotTheme,
    gridStyle: GridStyle,
    showLabel: Boolean,
    verticalPosition: Float,
    goals: List<Goal>,
    monthDotSize: Float,
    goalDotSize: Float,
    showNumberInsteadOfDots: Boolean = false,
    showBothNumberAndDot: Boolean = false,
    specialDatesYear: List<SpecialDateOfYear>,
    specialDatesMonth: List<SpecialDateOfMonth>,
    specialDatesGoal: List<SpecialDateOfGoal>,
    isReady: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = AppColor.CardBg,
        border = BorderStroke(1.dp, AppColor.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Live preview",
                color = AppColor.TextPrimary,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            PhonePreviewMock(
                isReady = isReady,
                mode = mode,
                dotTheme = dotTheme,
                gridStyle = gridStyle,
                verticalPosition = verticalPosition,
                scale = 1.0f,
                showLabel = showLabel,
                goals = goals,
                monthDotSize = monthDotSize,
                goalDotSize = goalDotSize,
                showNumberInsteadOfDots = showNumberInsteadOfDots,
                showBothNumberAndDot = showBothNumberAndDot,
                specialDatesYear = specialDatesYear,
                specialDatesMonth = specialDatesMonth,
                specialDatesGoal = specialDatesGoal
            )
        }
    }
}


// ── MiniFloatingPreview ───────────────────────────────────────────────────────
@Composable
private fun MiniFloatingPreview(
    mode: WallpaperMode,
    dotTheme: DotTheme,
    gridStyle: GridStyle,
    verticalPosition: Float,
    showLabel: Boolean,
    goals: List<Goal>,
    monthDotSize: Float,
    goalDotSize: Float,
    showNumberInsteadOfDots: Boolean = false,
    showBothNumberAndDot: Boolean = false,
    specialDatesYear: List<SpecialDateOfYear>,
    specialDatesMonth: List<SpecialDateOfMonth>,
    specialDatesGoal: List<SpecialDateOfGoal>,
    isReady: Boolean,
) {
    Surface(
        modifier = Modifier.size(width = 110.dp, height = 220.dp),
        shape = RoundedCornerShape(20.dp),
        color = AppColor.CardBg.copy(alpha = 0.95f),
        border = BorderStroke(1.5.dp, AppColor.Primary.copy(alpha = 0.5f)),
        shadowElevation = 8.dp
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            PhonePreviewMock(
                dotTheme = dotTheme,
                mode = mode,
                gridStyle = gridStyle,
                verticalPosition = verticalPosition,
                scale = 0.5f,
                showLabel = showLabel,
                goals = goals,
                monthDotSize = monthDotSize,
                goalDotSize = goalDotSize,
                specialDatesYear = specialDatesYear,
                specialDatesMonth = specialDatesMonth,
                specialDatesGoal = specialDatesGoal,
                showNumberInsteadOfDots = showNumberInsteadOfDots,
                showBothNumberAndDot = showBothNumberAndDot,
                isReady = isReady
            )
        }
    }
}


// ── PhonePreviewMock ──────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PhonePreviewMock(
    dotTheme: DotTheme,
    mode: WallpaperMode,
    gridStyle: GridStyle,
    verticalPosition: Float,
    scale: Float = 1.0f,
    showLabel: Boolean,
    goals: List<Goal>,
    monthDotSize: Float,
    goalDotSize: Float,
    specialDatesYear: List<SpecialDateOfYear>,
    specialDatesMonth: List<SpecialDateOfMonth>,
    specialDatesGoal: List<SpecialDateOfGoal>,
    showNumberInsteadOfDots: Boolean = false,
    showBothNumberAndDot: Boolean = false,
    isReady: Boolean,
) {
    val currentGridStyle by rememberUpdatedState(gridStyle)
    val capsuleShape = RoundedCornerShape(50)
    val phoneWidth = 220 * scale.dp
    val phoneHeight = 440 * scale.dp
    val containerHeight = 450 * scale.dp
    val bezelRadius = 38 * scale.dp
    val bezelWidth = 2 * scale.dp
    val screenPadding = 6 * scale.dp
    val screenRadius = 32 * scale.dp
    val statusPaddingH = 24 * scale.dp
    val statusPaddingV = 12 * scale.dp
    val iconSize = 10 * scale.dp
    val spacerSize = 4 * scale.dp
    val islandWidth = 60 * scale.dp
    val islandHeight = 18 * scale.dp
    val contentTop = 40 * scale.dp
    val contentBottom = 20 * scale.dp
    val clockSize = 52 * scale.sp
    val dateSize = 10 * scale.sp
    val actionPadding = 20 * scale.dp
    val actionButtonSize = 40 * scale.dp
    val actionIconSize = 18 * scale.dp
    val personIconSize = 10 * scale.dp
    val personTextSize = 8 * scale.sp
    val indicatorTop = 12 * scale.dp
    val indicatorWidth = 80 * scale.dp
    val indicatorHeight = 4 * scale.dp
    val dotsPaddingV = 8 * scale.dp
    val dotsPaddingH = 8 * scale.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(containerHeight),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(bezelRadius),
            color = Color(0xFF1A1A1A),
            border = BorderStroke(bezelWidth, Color(0xFF333333)),
            modifier = Modifier.size(width = phoneWidth, height = phoneHeight)
        ) {
            Box(
                modifier = Modifier
                    .padding(screenPadding)
                    .clip(RoundedCornerShape(screenRadius))
                    .background(Color.Black)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(dotTheme.bg.toColors())
                )

                // Status Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = statusPaddingH, vertical = statusPaddingV),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            PhosphorIcons.Regular.LinkedinLogo,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(iconSize)
                        )
                        Spacer(Modifier.width(spacerSize))
                        Icon(
                            PhosphorIcons.Regular.ThreadsLogo,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(iconSize)
                        )
                        Spacer(Modifier.width(spacerSize))
                        Icon(
                            PhosphorIcons.Regular.DevToLogo,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(width = islandWidth, height = islandHeight)
                            .clip(capsuleShape)
                            .background(Color.Black)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            PhosphorIcons.Regular.WifiHigh,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(iconSize)
                        )
                        Spacer(Modifier.width(spacerSize))
                        Icon(
                            PhosphorIcons.Regular.BatteryMedium,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }

                // Dots Grid
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = dotsPaddingV, horizontal = dotsPaddingH),
                    contentAlignment = BiasAlignment(
                        horizontalBias = 0f,
                        verticalBias = verticalPosition
                    )
                ) {
                    if (!isReady) {
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = AppColor.CardBorder
                            )
                        }
                        return@Box
                    }

                    key(currentGridStyle) {
                        when (mode) {
                            WallpaperMode.Year -> MockYearsDots(
                                dotTheme = dotTheme, gridStyle = gridStyle, scale = scale,
                                showLabel = showLabel,
                                showNumberInsteadOfDots = showNumberInsteadOfDots,
                                showBothNumberAndDot = showBothNumberAndDot,
                                specialDates = specialDatesYear,
                            )

                            WallpaperMode.Month -> MockMonthsDots(
                                dotTheme = dotTheme, gridStyle = gridStyle, scale = scale,
                                showLabel = showLabel, dotSizeMultiplier = monthDotSize,
                                showNumberInsteadOfDots = showNumberInsteadOfDots,
                                showBothNumberAndDot = showBothNumberAndDot,
                                specialDates = specialDatesMonth,
                            )

                            WallpaperMode.Goals -> MockGoalsDots(
                                dotTheme = dotTheme,
                                gridStyle = gridStyle,
                                scale = scale,
                                showLabel = showLabel,
                                goals = goals,
                                dotSizeMultiplier = goalDotSize,
                                showNumberInsteadOfDots = showNumberInsteadOfDots,
                                showBothNumberAndDot = showBothNumberAndDot,
                                specialDates = specialDatesGoal,
                            )
                        }
                    }
                }

                // Content Layer
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = contentTop, bottom = contentBottom),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Wed Dec 31", color = Color.White, fontSize = dateSize)
                    Text(
                        "8:00", color = Color.White.copy(alpha = 0.9f), fontSize = clockSize,
                        fontWeight = FontWeight.SemiBold,
                        style = LocalTextStyle.current.copy(
                            shadow = Shadow(
                                Color.Black,
                                offset = Offset(0f, 4f * scale),
                                blurRadius = 8f * scale
                            )
                        )
                    )
                    Spacer(Modifier.weight(1f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = actionPadding),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QuickActionButton(
                            icon = PhosphorIcons.Regular.Flashlight,
                            size = actionButtonSize,
                            iconSize = actionIconSize
                        )
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Person,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(personIconSize)
                                )
                                Text(" Personal", color = Color.White, fontSize = personTextSize)
                            }
                        }
                        QuickActionButton(
                            icon = PhosphorIcons.Regular.Camera,
                            size = actionButtonSize,
                            iconSize = actionIconSize
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(top = indicatorTop)
                            .size(width = indicatorWidth, height = indicatorHeight)
                            .clip(capsuleShape)
                            .background(Color.White)
                    )
                }
            }
        }
    }
}


// ── All unchanged helpers below ───────────────────────────────────────────────

@Composable
private fun QuickActionButton(icon: ImageVector, size: Dp = 40.dp, iconSize: Dp = 18.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(iconSize)) }
}

@Composable
private fun <T> SegmentedPill(
    items: List<T>, selected: T, onSelected: (T) -> Unit,
    label: (T) -> String, modifier: Modifier = Modifier,
    overrideSelected: T? = null, onClick: (() -> Unit)? = null
) {
    val actualSelected = overrideSelected ?: selected
    val selectedIndex = items.indexOf(actualSelected)
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(999.dp), color = AppColor.GlassBg,
        border = BorderStroke(1.dp, AppColor.GlassBorder)
    ) {
        Box(modifier = Modifier.padding(4.dp)) {
            val animatedOffset by animateFloatAsState(
                targetValue = selectedIndex.toFloat(),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "pill_slide"
            )
            Row(
                modifier = Modifier.matchParentSize(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items.forEachIndexed { index, _ ->
                    if (index == selectedIndex) {
                        Surface(
                            shape = RoundedCornerShape(999.dp), color = AppColor.Primary,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .graphicsLayer {
                                    val itemWidth = size.width + 6.dp.toPx()
                                    translationX = (animatedOffset - index) * itemWidth
                                }
                        ) {}
                    } else Spacer(modifier = Modifier.weight(1f))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items.forEach { item ->
                    val isSel = actualSelected == item
                    val textColor by animateColorAsState(
                        targetValue = if (isSel) Color.White else AppColor.TextSecondary,
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                        label = "text_color_animation"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(999.dp))
                            .clickable(enabled = onClick == null) { onSelected(item) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label(item),
                            color = textColor,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GlassCard(title: String, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        color = AppColor.CardBg, border = BorderStroke(1.dp, AppColor.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                title,
                color = AppColor.TextPrimary,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )
            )
            content()
        }
    }
}

@Composable
private fun <T> ChipRow(
    items: List<T>,
    selected: T,
    onSelected: (T) -> Unit,
    label: (T) -> String
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(items) { item ->
            val isSel = item == selected
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (isSel) AppColor.Primary.copy(alpha = 0.18f) else AppColor.GlassBg,
                border = BorderStroke(
                    1.dp,
                    if (isSel) AppColor.Primary.copy(alpha = 0.45f) else AppColor.GlassBorder
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable { onSelected(item) }
            ) {
                Text(
                    label(item), modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    color = if (isSel) AppColor.TextPrimary else AppColor.TextSecondary,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun ColorRow(colors: DotThemes, selected: DotTheme, onSelected: (DotTheme) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(colors.All) { appColor ->
            val sel = appColor == selected
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(appColor.today.toColors())
                    .clickable { onSelected(appColor) },
                contentAlignment = Alignment.Center
            ) {
                if (sel) Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f))
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp), color = AppColor.GlassBg,
        border = BorderStroke(1.dp, AppColor.GlassBorder), modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppColor.Primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = AppColor.Primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    title,
                    color = AppColor.TextPrimary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                )
                Text(
                    subtitle,
                    color = AppColor.TextMuted,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                )
            }
            Switch(
                checked = checked, onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = AppColor.Primary, checkedThumbColor = Color.White,
                    uncheckedTrackColor = AppColor.Divider, uncheckedThumbColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun DotSizeSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0.25f..1.0f,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                color = AppColor.TextSecondary,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            )
            Text(
                "${(value * 100).toInt()}%",
                color = AppColor.TextMuted,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = AppColor.Primary,
                activeTrackColor = AppColor.Primary.copy(alpha = 0.6f),
                inactiveTrackColor = AppColor.GlassBorder
            )
        )
    }
}


@Composable
private fun GlassButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = AppColor.GlassBg,
        border = BorderStroke(1.dp, AppColor.GlassBorder)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = AppColor.TextSecondary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text,
                color = AppColor.TextSecondary,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
private fun BottomActionSheet(
    target: WallpaperTarget,
    onTargetClick: (WallpaperTarget) -> Unit,
    isServiceActive: Boolean,                           // ← add param
    onStopService: () -> Unit,
    onSetWallpaper: () -> Unit,
    isAnimating: Boolean,
    onAnimationDone: () -> Unit,
) {
    var showStopConfirm by remember { mutableStateOf(false) }

    val stopIcon = if (isServiceActive) PhosphorIcons.Fill.StopCircle
    else PhosphorIcons.Fill.PlayCircle
    val stopTint = if (isServiceActive) Color(0xFFFF6B6B)
    else AppColor.TextMuted
    val stopBorderColor = if (isServiceActive) Color(0xFFFF6B6B).copy(alpha = 0.3f)
    else AppColor.GlassBorder
    val stopBg = if (isServiceActive) Color(0xFFFF6B6B).copy(alpha = 0.08f)
    else AppColor.GlassBg

    Surface(
        color = AppColor.CardBg,
        border = BorderStroke(1.dp, AppColor.CardBorder),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SegmentedPill(
                items = WallpaperTarget.entries,
                selected = target,
                onSelected = { onTargetClick(it) },
                label = { it.label },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ── Stop/Inactive indicator button ────────────────────────────
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(stopBg)
                        .border(1.dp, stopBorderColor, RoundedCornerShape(14.dp))
                        .clickable(
                            enabled = isServiceActive,       // ← not tappable when already stopped
                            onClick = { showStopConfirm = true }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        stopIcon,
                        contentDescription = if (isServiceActive) "Stop service" else "Service inactive",
                        tint = stopTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                AnimatedSetWallpaperButton(
                    modifier = Modifier.weight(1f),
                    isAnimating = isAnimating,
                    onAnimationDone = onAnimationDone,
                    onClick = onSetWallpaper
                )
            }
        }
    }

    // ── Confirm dialog (only reachable when isServiceActive = true) ───────────
    if (showStopConfirm) {
        AlertDialog(
            onDismissRequest = { showStopConfirm = false },
            containerColor = AppColor.DialogBg,
            shape = RoundedCornerShape(20.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF6B6B).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        PhosphorIcons.Fill.StopCircle, null,
                        tint = Color(0xFFFF6B6B),
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    "Stop Auto-Update?",
                    color = AppColor.TextPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    "The daily wallpaper service will stop updating automatically.",
                    color = AppColor.TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = { onStopService(); showStopConfirm = false }) {
                    Text("Stop", color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopConfirm = false }) {
                    Text("Cancel", color = AppColor.TextSecondary)
                }
            }
        )
    }
}


@Composable
private fun AnimatedSetWallpaperButton(
    modifier: Modifier = Modifier,
    isAnimating: Boolean,
    onAnimationDone: () -> Unit,
    onClick: () -> Unit,
) {
    var state by remember { mutableStateOf(SetWallpaperState.Idle) }

    // Drive state from isAnimating
    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            state = SetWallpaperState.Loading
            delay(1500)
            state = SetWallpaperState.Done
            onAnimationDone()
        }
    }

    LaunchedEffect(isAnimating) {
        if (!isAnimating && state == SetWallpaperState.Done) {
            delay(2000)   // keep "Updated ✔" visible for 2 seconds then reset
            state = SetWallpaperState.Idle
        }
    }

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = state == SetWallpaperState.Idle, onClick = onClick)
            .background(
                brush = Brush.linearGradient(listOf(AppColor.Primary, AppColor.Violet)),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(200))
            },
            label = "set_wallpaper_btn"
        ) { currentState ->
            when (currentState) {

                SetWallpaperState.Idle -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            PhosphorIcons.Regular.DeviceMobile,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Set Wallpaper",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                SetWallpaperState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp,
                    )
                }

                SetWallpaperState.Done -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            "Updated ✔",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun AddGoalDialog(onDismiss: () -> Unit, onConfirm: (Goal) -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var deadlineDate by remember { mutableStateOf(LocalDate.now().plusDays(30)) }
    var isTitleError by remember { mutableStateOf(false) }
    var isRangeError by remember { mutableStateOf(false) }
    var rangeErrorMsg by remember { mutableStateOf("") }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

    fun openDatePicker(initial: LocalDate, onPicked: (LocalDate) -> Unit) {
        DatePickerDialog(
            context,
            { _, year, month, day -> onPicked(LocalDate.of(year, month + 1, day)) },
            initial.year, initial.monthValue - 1, initial.dayOfMonth
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColor.DialogBg,
        title = {
            Text(
                "Add Goal",
                color = AppColor.TextPrimary,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it; isTitleError = false },
                    label = { Text("Goal title", color = AppColor.TextSecondary) },
                    singleLine = true, isError = isTitleError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColor.Primary,
                        unfocusedBorderColor = AppColor.GlassBorder,
                        focusedTextColor = AppColor.TextPrimary,
                        unfocusedTextColor = AppColor.TextPrimary,
                        cursorColor = AppColor.Primary
                    )
                )
                if (isTitleError) Text(
                    "Please enter a goal title",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                GoalDatePickerRow(
                    "Start date",
                    startDate,
                    dateFormatter
                ) {
                    openDatePicker(startDate) { picked ->
                        startDate = picked; isRangeError = false
                    }
                }
                GoalDatePickerRow("Deadline", deadlineDate, dateFormatter) {
                    openDatePicker(
                        deadlineDate
                    ) { picked -> deadlineDate = picked; isRangeError = false }
                }
                val days = ChronoUnit.DAYS.between(startDate, deadlineDate).toInt()
                Text(
                    text = when {
                        days <= 0 -> "⚠️ Deadline must be after start date"; days > 366 -> "⚠️ Max 366 days allowed"; else -> "Duration: $days day${if (days != 1) "s" else ""}"
                    },
                    color = if (days in 1..366) AppColor.TextMuted else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                )
                if (isRangeError) Text(
                    rangeErrorMsg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isBlank()) {
                    isTitleError = true; return@TextButton
                }
                val totalDays = ChronoUnit.DAYS.between(startDate, deadlineDate).toInt()
                when {
                    totalDays <= 0 -> {
                        rangeErrorMsg = "Deadline must be after start date"; isRangeError =
                            true; return@TextButton
                    }

                    totalDays > 366 -> {
                        rangeErrorMsg = "Goal cannot exceed 366 days (1 year)"; isRangeError =
                            true; Toast.makeText(
                            context,
                            "Cannot set a goal longer than a year",
                            Toast.LENGTH_SHORT
                        ).show(); return@TextButton
                    }
                }
                onConfirm(
                    Goal(
                        title = title.trim(),
                        startDate = startDate,
                        deadline = deadlineDate
                    )
                )
            }) { Text("Add", color = AppColor.Primary, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancel",
                    color = AppColor.TextSecondary
                )
            }
        }
    )
}

@Composable
private fun GoalDatePickerRow(
    label: String,
    date: LocalDate,
    formatter: DateTimeFormatter,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label,
            color = AppColor.TextSecondary,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(10.dp),
            color = AppColor.GlassBg,
            border = BorderStroke(1.dp, AppColor.GlassBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    date.format(formatter),
                    color = AppColor.TextPrimary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Icon(
                    PhosphorIcons.Regular.CalendarBlank,
                    null,
                    tint = AppColor.Primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun GoalRow(goal: Goal, onRemove: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd MMM") }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = AppColor.GlassBg,
        border = BorderStroke(1.dp, AppColor.GlassBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    goal.title,
                    color = AppColor.TextPrimary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    "${goal.startDate.format(formatter)} → ${goal.deadline.format(formatter)}  ·  ${goal.daysLeft}d left  ·  ${goal.percentComplete}%",
                    color = AppColor.TextMuted,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                )
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(
                    PhosphorIcons.Regular.X,
                    "Remove",
                    tint = AppColor.TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
