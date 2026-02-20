package com.example.a365wallpaper.presentation

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.*
import com.example.a365wallpaper.data.GridStyle
import com.example.a365wallpaper.data.WallpaperMode
import com.example.a365wallpaper.data.WallpaperTarget
import com.example.a365wallpaper.toColors
import com.example.a365wallpaper.ui.theme.AppColor
import com.example.a365wallpaper.ui.theme.DotTheme
import com.example.a365wallpaper.ui.theme.DotThemes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Wallpaper365HomeScreen(
    viewModel: Wallpaper365ViewModel,
    goToDevProfile : () -> Unit
) {
    val context = LocalContext.current
    val mode by viewModel.mode.collectAsState()
    val target by viewModel.setWallpaperTo.collectAsState()
    val gridStyle by viewModel.style.collectAsState()
    val accent by viewModel.selectedAccentColor.collectAsState()
    val showLabel by viewModel.showLabel.collectAsState()
    val showQuote by viewModel.showQuote.collectAsState()
    val verticalPosition by viewModel.verticalPosition.collectAsState()

    val lazyListState = rememberLazyListState()
    val showMiniPreview by remember { mutableStateOf(false)
//        derivedStateOf {
//            lazyListState.firstVisibleItemIndex > 0 ||
//                    lazyListState.firstVisibleItemScrollOffset > 1300
//        }
    }

//    Log.d("lazy", "${lazyListState.firstVisibleItemIndex},firstVisibleItemScrollOffset = ${lazyListState.firstVisibleItemScrollOffset } ")

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
                                .clickable(
                                    onClick =  goToDevProfile
                                ),
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
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
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
                    onTargetClick = { newTarget ->
                        viewModel.updateSetWallpaperTo(newTarget)
                    },
                    onStopService = {
                        val toastMessage = viewModel.cancelAutoDailyWallpaperUpdate()
                        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
                    },
                    onSetWallpaper = {
                        viewModel.runDailyWallpaperWorker(target)
                        viewModel.scheduleAutoDailyWallpaperUpdate()
                    },
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
//                Spacer(Modifier.height(6.dp))

                item {
                    PreviewSection(
                        mode = mode,
                        dotTheme = accent,
                        target = target,
                        gridStyle = gridStyle,
                        showQuote = showQuote,
                        verticalPosition = verticalPosition,
                        showLabel = showLabel
                    )
                }
                item {
                    GlassCard(title = "Accent color") {
                        ColorRow(
                            colors = DotThemes,
                            selected = accent,
                            onSelected = { viewModel.updateAccentColor(color = it) }
                        )
                    }
                }

                item {
                    GlassCard(title = "Positioning") {
                        VerticalPositionSlider(
                            position = verticalPosition,
                            onPositionChange = { viewModel.updateVerticalPosition(it) }
                        )
                    }
                }

                item {
                    GlassCard(title = "Style") {
                        ChipRow(
                            items = GridStyle.entries,
                            selected = gridStyle,
                            onSelected = { viewModel.updateStyle(it) },
                            label = { it.label }
                        )
                    }
                }

                item {
                    GlassCard(title = "Add Goal") {
                        ChipRow(
                            items = GridStyle.entries,
                            selected = gridStyle,
                            onSelected = { viewModel.updateStyle(it) },
                            label = { it.label }
                        )
                    }
                }

                item {
                    GlassCard(title = "Elements") {
                        ToggleRow(
                            icon = PhosphorIcons.Regular.CalendarBlank,
                            title = "Show label",
                            subtitle = "Example: 331d left • 9%",
                            checked = showLabel,
                            onCheckedChange = { viewModel.toggleShowLabel(!showLabel) }
                        )
                        ToggleRow(
                            icon = PhosphorIcons.Regular.Quotes,
                            title = "Show quote",
                            subtitle = "Small motivational line",
                            checked = showQuote,
                            onCheckedChange = { viewModel.toggleShowQuote() }
                        )
                    }
                    Spacer(Modifier.height(10.dp))

                }









            }
        }

        // Mini floating preview
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
                showLabel = showLabel
            )
        }
    }
}

// Mini floating preview wrapper
@Composable
private fun MiniFloatingPreview(
    mode: WallpaperMode,
    dotTheme: DotTheme,
    gridStyle: GridStyle,
    verticalPosition: Float,
    showLabel: Boolean
) {
    Surface(
        modifier = Modifier
            .size(width = 110.dp, height = 220.dp),
        shape = RoundedCornerShape(20.dp),
        color = AppColor.CardBg.copy(alpha = 0.95f),
        border = BorderStroke(1.5.dp, AppColor.Primary.copy(alpha = 0.5f)),
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // ✅ Use parametric version with 50% scale
            PhonePreviewMock(
                dotTheme = dotTheme,
                mode = mode,
                gridStyle = gridStyle,
                verticalPosition = verticalPosition,
                scale = 0.5f,
                showLabel = showLabel
            )
        }
    }
}

@Composable
private fun PreviewSection(
    mode: WallpaperMode,
    dotTheme: DotTheme,
    gridStyle: GridStyle,
    target: WallpaperTarget,
    showLabel: Boolean,
    showQuote: Boolean,
    verticalPosition: Float,
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

            // ✅ Full size preview (scale = 1.0f)
            PhonePreviewMock(
                mode = mode,
                dotTheme = dotTheme,
                gridStyle = gridStyle,
                verticalPosition = verticalPosition,
                scale = 1.0f,
                showLabel = showLabel
            )
        }
    }
}

// ✅ Parametric PhonePreviewMock that accepts scale multiplier
@Composable
private fun PhonePreviewMock(
    dotTheme: DotTheme,
    mode: WallpaperMode,
    gridStyle: GridStyle,
    verticalPosition: Float,
    scale: Float = 1.0f,
    showLabel: Boolean
) {
    val currentGridStyle by rememberUpdatedState(gridStyle)
    val capsuleShape = RoundedCornerShape(50)

    // ✅ Scale all dimensions
    val phoneWidth = (220 * scale).dp
    val phoneHeight = (440 * scale).dp
    val containerHeight = (450 * scale).dp
    val bezelRadius = (38 * scale).dp
    val bezelWidth = (2 * scale).dp
    val screenPadding = (6 * scale).dp
    val screenRadius = (32 * scale).dp
    val statusPaddingH = (24 * scale).dp
    val statusPaddingV = (12 * scale).dp
    val iconSize = (10 * scale).dp
    val spacerSize = (4 * scale).dp
    val islandWidth = (60 * scale).dp
    val islandHeight = (18 * scale).dp
    val contentTop = (40 * scale).dp
    val contentBottom = (20 * scale).dp
    val clockSize = (52 * scale).sp
    val dateSize = (10 * scale).sp
    val actionPadding = (20 * scale).dp
    val actionButtonSize = (40 * scale).dp
    val actionIconSize = (18 * scale).dp
    val personIconSize = (10 * scale).dp
    val personTextSize = (8 * scale).sp
    val indicatorTop = (12 * scale).dp
    val indicatorWidth = (80 * scale).dp
    val indicatorHeight = (4 * scale).dp
    val dotsPaddingV = (8 * scale).dp
    val dotsPaddingH = (8 * scale).dp

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
                // Background
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
                        Icon(PhosphorIcons.Regular.LinkedinLogo, null, tint = Color.White, modifier = Modifier.size(iconSize))
                        Spacer(Modifier.width(spacerSize))
                        Icon(PhosphorIcons.Regular.ThreadsLogo, null, tint = Color.White, modifier = Modifier.size(iconSize))
                        Spacer(Modifier.width(spacerSize))
                        Icon(PhosphorIcons.Regular.DevToLogo, null, tint = Color.White, modifier = Modifier.size(iconSize))
                    }

                    Box(
                        modifier = Modifier
                            .size(width = islandWidth, height = islandHeight)
                            .clip(capsuleShape)
                            .background(Color.Black)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(PhosphorIcons.Regular.WifiHigh, null, tint = Color.White, modifier = Modifier.size(iconSize))
                        Spacer(Modifier.width(spacerSize))
                        Icon(PhosphorIcons.Regular.BatteryMedium, null, tint = Color.White, modifier = Modifier.size(iconSize))
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
                    key(currentGridStyle) {
                        when (mode) {
                            WallpaperMode.Year -> MockYearsDots(dotTheme, gridStyle, scale , showLabel)
                            WallpaperMode.Month -> MockMonthsDots(dotTheme, gridStyle, scale , showLabel)
                            WallpaperMode.Goals -> MockGoalsDots(dotTheme, gridStyle, scale , showLabel)
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
                    Text(
                        "Wed Dec 31",
                        color = Color.White,
                        fontSize = dateSize
                    )

                    Text(
                        "8:00",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = clockSize,
                        fontWeight = FontWeight.SemiBold,
                        style = LocalTextStyle.current.copy(
                            shadow = Shadow(Color.Black, offset = Offset(0f, 4f * scale), blurRadius = 8f * scale)
                        )
                    )

                    Spacer(Modifier.weight(1f))

                    // Bottom Actions
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
                                Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(personIconSize))
                                Text(" Personal", color = Color.White, fontSize = personTextSize)
                            }
                        }
                        QuickActionButton(
                            icon = PhosphorIcons.Regular.Camera,
                            size = actionButtonSize,
                            iconSize = actionIconSize
                        )
                    }

                    // Home Indicator
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

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    size: Dp = 40.dp,
    iconSize: Dp = 18.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(iconSize))
    }
}

@Composable
private fun BottomActionSheet(
    target: WallpaperTarget,
    onTargetClick: (WallpaperTarget) -> Unit,
    onStopService: () -> Unit,
    onSetWallpaper: () -> Unit,
) {
    Surface(
        color = AppColor.CardBg,
        border = BorderStroke(1.dp, AppColor.CardBorder),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SegmentedPill(
                items = WallpaperTarget.entries,
                selected = target,
                onSelected = { selectedTarget ->
                    onTargetClick(selectedTarget)
                },
                label = { it.label },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassButton(
                    modifier = Modifier.weight(1f),
                    text = "Stop Service",
                    icon = PhosphorIcons.Regular.Stop,
                    onClick = onStopService
                )
                GradientButton(
                    modifier = Modifier.weight(1f),
                    text = "Set Wallpaper",
                    icon = PhosphorIcons.Regular.DeviceMobile,
                    onClick = onSetWallpaper
                )
            }
        }
    }
}

@Composable
private fun <T> SegmentedPill(
    items: List<T>,
    selected: T,
    onSelected: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    overrideSelected: T? = null,
    onClick: (() -> Unit)? = null
) {
    val actualSelected = overrideSelected ?: selected
    val selectedIndex = items.indexOf(actualSelected)

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(999.dp),
        color = AppColor.GlassBg,
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
                            shape = RoundedCornerShape(999.dp),
                            color = AppColor.Primary,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .graphicsLayer {
                                    val itemWidth = size.width + 6.dp.toPx()
                                    translationX = (animatedOffset - index) * itemWidth
                                }
                        ) {}
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AppColor.CardBg,
        border = BorderStroke(1.dp, AppColor.CardBorder)
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
                border = BorderStroke(1.dp, if (isSel) AppColor.Primary.copy(alpha = 0.45f) else AppColor.GlassBorder),
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable { onSelected(item) }
            ) {
                Text(
                    label(item),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
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
                if (sel) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.85f))
                    )
                }
            }
        }
    }
}


private fun AddGoal(
    onAddClick : () -> Unit

){

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
        shape = RoundedCornerShape(14.dp),
        color = AppColor.GlassBg,
        border = BorderStroke(1.dp, AppColor.GlassBorder),
        modifier = Modifier.fillMaxWidth()
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
                Icon(icon, contentDescription = null, tint = AppColor.Primary, modifier = Modifier.size(18.dp))
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = AppColor.Primary,
                    checkedThumbColor = Color.White,
                    uncheckedTrackColor = AppColor.Divider,
                    uncheckedThumbColor = Color.White
                )
            )
        }
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
private fun GradientButton(
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
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(listOf(AppColor.Primary, AppColor.Violet)),
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
