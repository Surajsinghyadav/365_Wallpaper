package com.example.a365wallpaper.presentation.reusableComponents

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CalendarBlank
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.X
import com.example.a365wallpaper.BitmapGenerators.Goal
import com.example.a365wallpaper.data.Local.SpecialDateOfGoal
import com.example.a365wallpaper.data.Local.SpecialDateOfMonth
import com.example.a365wallpaper.data.Local.SpecialDateOfYear
import com.example.a365wallpaper.data.Local.WallpaperMode
import com.example.a365wallpaper.presentation.homeScreen.GlassCard
import com.example.a365wallpaper.utils.toColors
import com.example.a365wallpaper.ui.theme.AppColor
import com.example.a365wallpaper.ui.theme.DotTheme
import com.example.a365wallpaper.ui.theme.DotThemes
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Immutable
data class SpecialDateDisplay(
    val id: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val colorArgb: Int,
)

fun SpecialDateOfYear.toDisplay()  = SpecialDateDisplay(id, startDate, endDate, colorArgb)
fun SpecialDateOfMonth.toDisplay() = SpecialDateDisplay(id, startDate, endDate, colorArgb)
fun SpecialDateOfGoal.toDisplay()  = SpecialDateDisplay(id, startDate, endDate, colorArgb)

@Composable
fun SpecialDatesCard(
    specialDates: List<SpecialDateDisplay>,
    mode: WallpaperMode,
    accent: DotTheme,
    goals: List<Goal> = emptyList(),
    onAdd: (start: LocalDate, end: LocalDate, colorArgb: Int, goalTitle: String) -> Unit,
    onRemove: (id: Int) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    GlassCard(title = "Special Dates") {
        specialDates.forEach { sd ->
            SpecialDateRow(entry = sd, onRemove = { onRemove(sd.id) })
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { showDialog = true },
            shape  = RoundedCornerShape(12.dp),
            color  = AppColor.Primary.copy(alpha = 0.10f),
            border = BorderStroke(1.dp, AppColor.Primary.copy(alpha = 0.35f)),
        ) {
            Row(
                modifier              = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(PhosphorIcons.Regular.Plus, "Add Date",
                    tint = AppColor.Primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Add Date", color = AppColor.Primary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }

    if (showDialog) {
        AddSpecialDateDialog(
            accent    = accent,
            mode      = mode,
            goals     = goals,
            onDismiss = { showDialog = false },
            onConfirm = { start, end, color, goalTitle ->
                onAdd(start, end, color, goalTitle)
                showDialog = false
            },
        )
    }
}

// ── Private composables ───────────────────────────────────────────────────────

@Composable
private fun SpecialDateRow(
    entry: SpecialDateDisplay,
    onRemove: () -> Unit,
) {
    val fmt      = remember { DateTimeFormatter.ofPattern("dd MMM") }
    val dotColor = entry.colorArgb.toColors()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        color    = AppColor.GlassBg,
        border   = BorderStroke(1.dp, AppColor.GlassBorder),
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(14.dp).clip(CircleShape).background(dotColor),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text     = "${entry.startDate.format(fmt)} → ${entry.endDate.format(fmt)}",
                color    = AppColor.TextPrimary,
                style    = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(PhosphorIcons.Regular.X, "Remove",
                    tint = AppColor.TextMuted, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun AddSpecialDateDialog(
    accent: DotTheme,
    mode: WallpaperMode,
    goals: List<Goal>,
    onDismiss: () -> Unit,
    onConfirm: (start: LocalDate, end: LocalDate, colorArgb: Int, goalTitle: String) -> Unit,
) {
    val context       = LocalContext.current
    val today         = LocalDate.now()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

    var selectedGoal by remember {
        mutableStateOf(goals.firstOrNull())
    }

    // ── Bounds: derived from selected goal or current year ────────────────────
    val boundsMin: LocalDate
    val boundsMax: LocalDate
    if (mode == WallpaperMode.Goals && selectedGoal != null) {
        boundsMin = selectedGoal!!.startDate
        boundsMax = selectedGoal!!.deadline
    } else {
        boundsMin = LocalDate.of(today.year, 1, 1)
        boundsMax = LocalDate.of(today.year, 12, 31)
    }

    fun toEpochMillis(date: LocalDate): Long =
        date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    var startDate     by remember(boundsMin, boundsMax) {
        val clamped = today.coerceAtLeast(boundsMin).coerceAtMost(boundsMax)
        mutableStateOf(clamped)
    }
    var endDate       by remember(startDate) { mutableStateOf(boundsMax) }
    var selectedTheme by remember { mutableStateOf(accent) }
    var isRangeError  by remember { mutableStateOf(false) }

    fun openStartPicker() {
        val maxForStart = endDate.coerceAtMost(boundsMax)
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val picked = LocalDate.of(year, month + 1, day)
                startDate    = picked
                isRangeError = false
                if (endDate.isBefore(picked)) endDate = picked
            },
            startDate.year, startDate.monthValue - 1, startDate.dayOfMonth,
        ).also { dialog ->
//            dialog.datePicker.minDate = toEpochMillis(boundsMin)
//            dialog.datePicker.maxDate = toEpochMillis(maxForStart)
            dialog.datePicker.minDate = toEpochMillis(startDate)
            dialog.datePicker.maxDate = toEpochMillis(boundsMax)
            dialog.show()
        }
    }

    fun openEndPicker() {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                endDate      = LocalDate.of(year, month + 1, day)
                isRangeError = false
            },
            endDate.year, endDate.monthValue - 1, endDate.dayOfMonth,
        ).also { dialog ->
            dialog.datePicker.minDate = toEpochMillis(startDate)
            dialog.datePicker.maxDate = toEpochMillis(boundsMax)
            dialog.show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = AppColor.DialogBg,
        title = {
            Text(
                "Highlight Date Range",
                color = AppColor.TextPrimary,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

                // ── Goal selector + hint (Goals mode only) ────────────────────
                if (mode == WallpaperMode.Goals && goals.isNotEmpty()) {
                    val fmt = remember { DateTimeFormatter.ofPattern("dd MMM") }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                        // Pill buttons — one per goal
                        Text(
                            "Apply to Goal",
                            color = AppColor.TextSecondary,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold, fontSize = 12.sp
                            ),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            goals.forEach { goal ->
                                val isSelected = goal == selectedGoal
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedGoal = goal },
                                    shape  = RoundedCornerShape(10.dp),
                                    color  = if (isSelected) AppColor.Primary.copy(alpha = 0.15f)
                                    else AppColor.GlassBg,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) AppColor.Primary else AppColor.GlassBorder
                                    ),
                                ) {
                                    Text(
                                        text     = goal.title,
                                        color    = if (isSelected) AppColor.Primary else AppColor.TextSecondary,
                                        style    = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    )
                                }
                            }
                        }

                        // Date range hint for selected goal
//                        selectedGoal?.let { g ->
//                            Row(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .clip(RoundedCornerShape(8.dp))
//                                    .background(AppColor.GlassBg)
//                                    .padding(horizontal = 12.dp, vertical = 8.dp),
//                                horizontalArrangement = Arrangement.SpaceBetween,
//                            ) {
//                                Text(
//                                    g.title,                  // ← g is a Goal, has .title
//                                    color = AppColor.TextSecondary,
//                                    style = MaterialTheme.typography.bodySmall.copy(
//                                        fontWeight = FontWeight.SemiBold
//                                    ),
//                                )
//                                Text(
//                                    "${g.startDate.format(fmt)} → ${g.deadline.format(fmt)}",
//                                    color = AppColor.TextMuted,
//                                    style = MaterialTheme.typography.bodySmall,
//                                )
//                            }
//                        }
                    }
                }

                // ── Start Date ────────────────────────────────────────────────
                SpecialDatePickerRow(
                    label     = "Start",
                    date      = startDate,
                    formatter = dateFormatter,
                    onClick   = { openStartPicker() },
                )

                // ── End Date ──────────────────────────────────────────────────
                SpecialDatePickerRow(
                    label     = "End",
                    date      = endDate,
                    formatter = dateFormatter,
                    onClick   = { openEndPicker() },
                )

                if (isRangeError) {
                    Text(
                        "End date must be on or after start date",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                // ── Color picker ──────────────────────────────────────────────
                Text(
                    "Color",
                    color = AppColor.TextSecondary,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold, fontSize = 12.sp,
                    ),
                )
                val lazyListState  = rememberLazyListState()
                val totalItems     = DotThemes.All.size
                val scrollProgress by remember {
                    derivedStateOf {
                        val lastVisible  = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        val visibleCount = lazyListState.layoutInfo.visibleItemsInfo.size
                        if (totalItems <= visibleCount) 1f
                        else lastVisible / (totalItems - 1).toFloat()
                    }
                }
                LazyRow(
                    state                 = lazyListState,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(DotThemes.All) { theme ->
                        val isSelected = theme == selectedTheme
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(theme.today.toColors())
                                .clickable { selectedTheme = theme },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.85f)),
                                )
                            }
                        }
                    }
                }
                LinearProgressIndicator(
                    progress   = { scrollProgress },
                    modifier   = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color      = AppColor.Primary.copy(alpha = 0.7f),
                    trackColor = AppColor.GlassBorder,
                    strokeCap  = StrokeCap.Round,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (endDate.isBefore(startDate)) { isRangeError = true; return@TextButton }
                onConfirm(
                    startDate,
                    endDate,
                    selectedTheme.today,
                    if (mode == WallpaperMode.Goals) selectedGoal?.title ?: "" else ""
                )
            }) {
                Text("Add", color = AppColor.Primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppColor.TextSecondary)
            }
        },
    )
}

@Composable
private fun SpecialDatePickerRow(
    label: String,
    date: LocalDate,
    formatter: DateTimeFormatter,
    onClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label,
            color = AppColor.TextSecondary,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold, fontSize = 12.sp,
            ),
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onClick),
            shape  = RoundedCornerShape(10.dp),
            color  = AppColor.GlassBg,
            border = BorderStroke(1.dp, AppColor.GlassBorder),
        ) {
            Row(
                modifier              = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    date.format(formatter),
                    color = AppColor.TextPrimary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                Icon(
                    PhosphorIcons.Regular.CalendarBlank, null,
                    tint     = AppColor.Primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
