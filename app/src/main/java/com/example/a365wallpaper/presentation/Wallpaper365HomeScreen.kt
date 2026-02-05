package com.example.a365wallpaper.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.*
import com.example.a365wallpaper.data.GridStyle
import com.example.a365wallpaper.data.Target
import com.example.a365wallpaper.data.WallpaperMode
import com.example.a365wallpaper.ui.theme.AppColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Wallpaper365HomeScreen(
    viewModel: Wallpaper365ViewModel,
//    onSave: (Wallpaper365State) -> Unit,
    onSetWallpaper: () -> Unit,
) {
//    var state by remember { mutableStateOf(Wallpaper365State()) }
    var showTargetSheet by remember { mutableStateOf(false) }

    val mode by viewModel.mode.collectAsState()
    val target by viewModel.setWallpaperTo.collectAsState()
    val gridStyle by viewModel.style.collectAsState()
    val accent by viewModel.selectedAccentColor.collectAsState()
    val showLabel by viewModel.showLabel.collectAsState()
    val showQuote by viewModel.showQuote.collectAsState()

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
                // Title row (like image)
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
                            .background(AppColor.GlassBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.SlidersHorizontal,
                            contentDescription = "Options",
                            tint = AppColor.TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Mode segmented (Year / Month / Goals)
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
//                Spacer(Modifier.height(0.5.dp))

            }
        },
        bottomBar = {
            BottomActionSheet(
                target = target,
                onTargetClick = { newTarget ->
                    viewModel.updateSetWallpaperTo(newTarget)
                },
                onSave = {
//                    onSave(state)
                },
                onSetWallpaper ={
                    onSetWallpaper()
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(6.dp))

            // Preview area (like image)
            PreviewSection(
                mode = mode,
                accent = accent,
                target = target,
                gridStyle = gridStyle,
                showQuote = showQuote
            )

            // Controls (meaningful)
            GlassCard(title = "Style") {
                ChipRow(
                    items = GridStyle.entries,
                    selected = gridStyle,
                    onSelected = { viewModel.updateStyle(it) },
                    label = { it.label }
                )
            }

            GlassCard(title = "Accent color") {
                ColorRow(
                    colors = AppColor.Accents,
                    selected = accent,
                    onSelected = { viewModel.updateAccentColor(color = it) }
                )
            }

            GlassCard(title = "Elements") {
                ToggleRow(
                    icon = PhosphorIcons.Regular.CalendarBlank,
                    title = "Show label",
                    subtitle = "Example: 331d left • 9%",
                    checked = showLabel,
                    onCheckedChange = { viewModel.togleShowLabel() }
                )
//                DividerThin()
//                ToggleRow(
//                    icon = PhosphorIcons.Regular.DotsNine,
//                    title = "Show progress dots",
//                    subtitle = "Track streak/progress visually",
//                    checked = state.showDots,
//                    onCheckedChange = { state = state.copy(showDots = it) }
//                )
                DividerThin()
                ToggleRow(
                    icon = PhosphorIcons.Regular.Quotes,
                    title = "Show quote",
                    subtitle = "Small motivational line",
                    checked = showQuote,
                    onCheckedChange = { viewModel.togleShowQuote() }
                )
            }

            Spacer(Modifier.height(10.dp))
        }
    }

//    if (showTargetSheet) {
//        TargetPickerSheet(
//            current = state.target,
//            onDismiss = { showTargetSheet = false },
//            onSelect = {
//                state = state.copy(target = it)
//                showTargetSheet = false
//            }
//        )
//    }
}

/* ----------------------------- UI pieces ----------------------------- */

//private object AppColor {
//    val RootBg = Color(0xFF0F1117)
//    val CardBg = Color(0x1A_FFFFFF)        // visible glass
//    val CardBorder = Color(0x26_FFFFFF)
//    val GlassBg = Color(0x14_FFFFFF)
//    val GlassBorder = Color(0x1F_FFFFFF)
//    val Divider = Color(0x14_FFFFFF)
//
//    val TextPrimary = Color(0xFFE2E8F0)
//    val TextSecondary = Color(0xFF9CA3AF)
//    val TextMuted = Color(0xFF6B7280)
//
//    val Primary = Color(0xFF6366F1)
//    val Violet = Color(0xFF8B5CF6)
//
//    val Accents = listOf(
//        Color(0xFF6366F1), // indigo
//        Color(0xFF8B5CF6), // violet
//        Color(0xFF10B981), // emerald
//        Color(0xFFF59E0B), // amber
//        Color(0xFFEF4444)  // red
//    )
//}

//private enum class WallpaperMode(val label: String) { Year("Year"), Month("Month"), Goals("Goals") }
//private enum class Target(val label: String) { Home("Home"), Lock("Lock"), Both("Both") }
//private enum class GridStyle(val label: String) { Dots("Dots"), Minimal("Minimal"), Ring("Ring") }

//private data class Wallpaper365State(
//    val mode: WallpaperMode = WallpaperMode.Year,
//    val target: Target = Target.Both,
//    val autoUpdate: Boolean = true,
//    val updateTime: String = "00:05",
//    val gridStyle: GridStyle = GridStyle.Dots,
//    val accent: Color = AppColor.Accents.first(),
//    val showYear: Boolean = true,
//    val showDots: Boolean = true,
//    val showQuote: Boolean = false,
//) {
//    fun updateNote(): String =
//        if (autoUpdate) "Updates daily at $updateTime • Battery saver optimized"
//        else "Auto-update off • Set wallpaper manually"
//}

@Composable
private fun PreviewSection(
    mode: WallpaperMode,
    accent: Color,
    gridStyle: GridStyle,
    target: Target,
    showQuote: Boolean,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Live preview",
                        color = AppColor.TextPrimary,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "${mode.label} calendar wallpaper on ${target.label}",
                        color = AppColor.TextSecondary,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                    )
                    Text(
                        "${gridStyle.label} • ${if (showQuote) "Quote on" else "Quote off"}",
                        color = AppColor.TextMuted,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                    )
                }

            }

            // Phone preview mock
            PhonePreviewMock(
                accent = accent,
                showQuote = showQuote
            )
        }
    }
}

@Composable
private fun PhonePreviewMock(
    accent: Color,
    showYear: Boolean = true,
    showQuote: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        contentAlignment = Alignment.Center
    ) {
        // phone body
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF0B0E14),
            border = BorderStroke(6.dp, Color(0xFF111827))
        ) {
            Column(
                modifier = Modifier
                    .width(190.dp)
                    .height(300.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0E1B33), Color(0xFF0B1222))
                        )
                    )
                    .padding(top = 16.dp)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                if (showYear) {
                    Text(
                        "2026",
                        color = accent,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp
                        )
                    )
                }

                if (showQuote) {
                    Text(
                        "\"One day at a time.\"",
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        maxLines = 2
                    )
                }

                    // simple dot grid
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(18) { r ->
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(10) { AppColor ->
                                    val active = (r * 7 + AppColor) % 5 != 0
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (active) accent else Color.White.copy(
                                                    alpha = 0.20f
                                                )
                                            )
                                    )
                                }
                            }
                        }
                    }


                Spacer(Modifier.weight(1f))

                Text(
                    "streak: 12 days",
                    color = Color.White.copy(alpha = 0.70f),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 7.sp),
                    fontFamily = FontFamily.SansSerif
                )
            }
        }
    }
}

@Composable
private fun BottomActionSheet(
    target: com.example.a365wallpaper.data.Target,
    onTargetClick: (com.example.a365wallpaper.data.Target) -> Unit,
    onSave: () -> Unit,
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
                items = com.example.a365wallpaper.data.Target.entries,
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
                    text = "Save",
                    icon = PhosphorIcons.Regular.DownloadSimple,
                    onClick = onSave
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

/**
 * Segmented control pill. For Target in bottom bar we open a picker sheet when tapped,
 * because changing Home/Lock/Both usually needs a confirmation UX.
 */
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
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(999.dp),
        color = AppColor.GlassBg,
        border = BorderStroke(1.dp, AppColor.GlassBorder)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.forEach { item ->
                val isSel = (overrideSelected ?: selected) == item
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (isSel) AppColor.Primary else Color.Transparent,
                    border = if (isSel) BorderStroke(0.dp, Color.Transparent) else BorderStroke(1.dp, Color.Transparent),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(999.dp))
                        .clickable(enabled = onClick == null) { onSelected(item) }
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label(item),
                            color = if (isSel) Color.White else AppColor.TextSecondary,
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
private fun GlassCard(title: String, content: @Composable () -> Unit) {
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
private fun ColorRow(colors: List<Color>, selected: Color, onSelected: (Color) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        colors.forEach { AppColor ->
            val sel = AppColor == selected
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(AppColor.copy(alpha = 0.95f))
                    .then(
                        if (sel) Modifier
                            .padding(0.dp)
                            .background(Color.Transparent)
                        else Modifier
                    )
                    .clickable { onSelected(AppColor) },
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

@Composable
private fun ToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
private fun DividerThin() {
    HorizontalDivider(thickness = 1.dp, color = AppColor.Divider)
}

@Composable
private fun GlassButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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

///* -------- Target picker bottom sheet (Home / Lock / Both) -------- */
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun TargetPickerSheet(
//    current: Target,
//    onDismiss: () -> Unit,
//    onSelect: (Target) -> Unit
//) {
//    ModalBottomSheet(
//        onDismissRequest = onDismiss,
//        containerColor = Color(0xFF0B0E14),
//        contentColor = AppColor.TextPrimary
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp)
//                .padding(bottom = 18.dp),
//            verticalArrangement = Arrangement.spacedBy(10.dp)
//        ) {
//            Text(
//                "Apply wallpaper to",
//                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
//                color = AppColor.TextPrimary
//            )
//
//            Target.entries.forEach { t ->
//                Surface(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clip(RoundedCornerShape(14.dp))
//                        .clickable { onSelect(t) },
//                    shape = RoundedCornerShape(14.dp),
//                    color = AppColor.CardBg,
//                    border = BorderStroke(1.dp, if (t == current) AppColor.Primary.copy(alpha = 0.6f) else AppColor.CardBorder)
//                ) {
//                    Row(
//                        modifier = Modifier.padding(14.dp),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(12.dp)
//                    ) {
//                        val icon = when (t) {
//                            Target.Home -> PhosphorIcons.Regular.House
//                            Target.Lock -> PhosphorIcons.Regular.Lock
//                            Target.Both -> PhosphorIcons.Regular.SquaresFour
//                        }
//                        Box(
//                            modifier = Modifier
//                                .size(36.dp)
//                                .clip(RoundedCornerShape(10.dp))
//                                .background(AppColor.Primary.copy(alpha = 0.14f)),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Icon(icon, null, tint = AppColor.Primary, modifier = Modifier.size(18.dp))
//                        }
//
//                        Column(modifier = Modifier.weight(1f)) {
//                            Text(
//                                t.label,
//                                color = AppColor.TextPrimary,
//                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
//                            )
//                            Text(
//                                when (t) {
//                                    Target.Home -> "Home screen only"
//                                    Target.Lock -> "Lock screen only (Android 7+)"
//                                    Target.Both -> "Apply to both screens"
//                                },
//                                color = AppColor.TextMuted,
//                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
//                            )
//                        }
//
//                        if (t == current) {
//                            Icon(PhosphorIcons.Regular.Check, null, tint = AppColor.Primary, modifier = Modifier.size(18.dp))
//                        }
//                    }
//                }
//            }
//
//            Spacer(Modifier.height(6.dp))
//        }
//    }
//}
