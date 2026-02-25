package com.example.a365wallpaper.presentation.Menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.*
import com.example.a365wallpaper.data.database.Entity.LogEntity
import com.example.a365wallpaper.utils.getDate
import com.example.a365wallpaper.utils.getTime
import com.example.a365wallpaper.ui.theme.AppColor
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    onBack: () -> Unit,
    logsViewModel: LogsViewModel = koinViewModel()
) {
    val logsState by logsViewModel.logsUiState.collectAsState()
    val totalLogsCount by logsViewModel.totalLogsCount.collectAsState()

    Scaffold(
        containerColor = AppColor.RootBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Activity Logs",
                            color = AppColor.TextPrimary,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (logsState.isNotEmpty()) {
                            Text(
                                "$totalLogsCount entries",
                                color = AppColor.TextMuted,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            PhosphorIcons.Regular.ArrowLeft,
                            contentDescription = "Back",
                            tint = AppColor.TextPrimary
                        )
                    }
                },
                actions = {
                    if (logsState.isNotEmpty()) {
                        TextButton(onClick = { logsViewModel.deleteAllLogs() }) {
                            Text(
                                "Clear all",
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColor.RootBg
                )
            )
        }
    ) { padding ->

        if (logsState.isEmpty()) {
            // ── Empty State ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(AppColor.GlassBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            PhosphorIcons.Regular.Scroll,
                            contentDescription = null,
                            tint = AppColor.TextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        "No activity yet",
                        color = AppColor.TextPrimary,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        "Wallpaper updates, worker triggers, and setting changes will appear here.",
                        color = AppColor.TextMuted,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        textAlign = TextAlign.Center
                    )
                }
            }

        } else {
            // ── Log List — no wrapping card ────────────────────────────────────
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(
                    horizontal = 20.dp,
                    vertical = 12.dp
                )
            ) {
                itemsIndexed(
                    items = logsState.sortedByDescending { it.timeStamp },
                    key = { _, log -> log.id }
                ) { index, log ->
                    LogRow(
                        log = log,
                        isFirst = index == 0,
                        isLast = index == logsState.lastIndex
                    )
                }
            }
        }
    }
}

@Composable
private fun LogRow(
    log: LogEntity,
    isFirst: Boolean,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        // ── Timeline track ─────────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(if (isFirst) AppColor.Primary else AppColor.GlassBorder)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .height(38.dp)
                        .background(AppColor.Divider)
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        // ── Log content ────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 4.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = log.message,
                color = if (isFirst) AppColor.TextPrimary else AppColor.TextSecondary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isFirst) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 13.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    PhosphorIcons.Regular.Clock,
                    contentDescription = null,
                    tint = AppColor.TextMuted,
                    modifier = Modifier.size(10.dp)
                )
                Text(
                    "${log.timeStamp.getDate()}  ·  ${log.timeStamp.getTime()}",
                    color = AppColor.TextMuted,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                )
            }
        }
    }
}
