package com.example.a365wallpaper.presentation

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CalendarBlank
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.X
import com.example.a365wallpaper.data.SpecialDateOfYear
import com.example.a365wallpaper.toColors
import com.example.a365wallpaper.ui.theme.AppColor
import com.example.a365wallpaper.ui.theme.DotTheme
import com.example.a365wallpaper.ui.theme.DotThemes
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SpecialDatesCard(
    specialDates: List<SpecialDateOfYear>,
    accent: DotTheme,
    onAdd: (start: LocalDate, end: LocalDate, colorArgb: Int) -> Unit,
    onRemove: (id: Int) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    GlassCard(title = "Special Dates") {
        // Existing special date rows
        specialDates.forEach { sd ->
            SpecialDateRow(
                entry    = sd,
                onRemove = { onRemove(sd.id) },
            )
        }

        // "+ Add Date" button — always visible
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
                modifier             = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment    = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    PhosphorIcons.Regular.Plus,
                    contentDescription = "Add Date",
                    tint     = AppColor.Primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Add Date",
                    color = AppColor.Primary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }

    if (showDialog) {
        AddSpecialDateDialog(
            accent    = accent,
            onDismiss = { showDialog = false },
            onConfirm = { start, end, color ->
                onAdd(start, end, color)
                showDialog = false
            },
        )
    }
}

@Composable
private fun SpecialDateRow(
    entry: SpecialDateOfYear,
    onRemove: () -> Unit,
) {
    val fmt = remember { DateTimeFormatter.ofPattern("dd MMM") }
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
            // Color indicator dot
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text  = "${entry.startDate.format(fmt)} → ${entry.endDate.format(fmt)}",
                color = AppColor.TextPrimary,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick  = onRemove,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    PhosphorIcons.Regular.X,
                    contentDescription = "Remove",
                    tint     = AppColor.TextMuted,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSpecialDateDialog(
    accent: DotTheme,
    onDismiss: () -> Unit,
    onConfirm: (start: LocalDate, end: LocalDate, colorArgb: Int) -> Unit,
) {
    val context         = LocalContext.current
    val today           = LocalDate.now()
    val yearEnd         = LocalDate.of(today.year, 12, 31)
    val dateFormatter   = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

    var startDate   by remember { mutableStateOf(today) }
    var endDate     by remember { mutableStateOf(today) }
    var selectedTheme by remember { mutableStateOf(accent) }
    var isRangeError by remember { mutableStateOf(false) }

    // Clamp helper: only allow today → Dec 31 of current year
    fun openPicker(initial: LocalDate, onPicked: (LocalDate) -> Unit) {
        DatePickerDialog(
            context,
            { _, year, month, day -> onPicked(LocalDate.of(year, month + 1, day)) },
            initial.year, initial.monthValue - 1, initial.dayOfMonth,
        ).also { dialog ->
            dialog.datePicker.minDate = today
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli()
            dialog.datePicker.maxDate = yearEnd
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli()
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

                // ── Start Date ──────────────────────────────────────────────
                SpecialDatePickerRow(
                    label     = "Start",
                    date      = startDate,
                    formatter = dateFormatter,
                    onClick   = {
                        openPicker(startDate) { picked ->
                            startDate    = picked
                            isRangeError = false
                            // Auto-push end date if it's now before start
                            if (endDate.isBefore(picked)) endDate = picked
                        }
                    },
                )

                // ── End Date ────────────────────────────────────────────────
                SpecialDatePickerRow(
                    label     = "End",
                    date      = endDate,
                    formatter = dateFormatter,
                    onClick   = {
                        openPicker(endDate) { picked ->
                            endDate      = picked
                            isRangeError = false
                        }
                    },
                )

                if (isRangeError) {
                    Text(
                        "End date must be on or after start date",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                // ── Color picker (theme accent dots) ────────────────────────
                Text(
                    "Color",
                    color = AppColor.TextSecondary,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold, fontSize = 12.sp,
                    ),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DotThemes.All.forEach { theme ->
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
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (endDate.isBefore(startDate)) {
                    isRangeError = true
                    return@TextButton
                }
                onConfirm(startDate, endDate, selectedTheme.today)
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
                    PhosphorIcons.Regular.CalendarBlank,
                    contentDescription = null,
                    tint     = AppColor.Primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
