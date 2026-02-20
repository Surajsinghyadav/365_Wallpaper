package com.example.a365wallpaper.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.*
import com.example.a365wallpaper.data.database.LogEntity
import com.example.a365wallpaper.getDate
import com.example.a365wallpaper.getTime
import com.example.a365wallpaper.ui.theme.AppColor
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.compose.koinViewModel
import kotlin.math.log


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
                    Text(
                        "Activity logs",
                        color = AppColor.TextPrimary,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
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
                                "Clear",
                                color = AppColor.TextSecondary,
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
            // Empty state
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = AppColor.CardBg,
                    border = BorderStroke(1.dp, AppColor.CardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(AppColor.GlassBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                PhosphorIcons.Regular.Scroll,
                                contentDescription = null,
                                tint = AppColor.TextSecondary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Text(
                            "No logs yet",
                            color = AppColor.TextPrimary,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            "When the app schedules wallpapers, runs workers, or changes settings, the activity will show up here.",
                            color = AppColor.TextMuted,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        } else {
            // List of logs
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = 12.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = AppColor.CardBg,
                    border = BorderStroke(1.dp, AppColor.CardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Recent activity",
                                color = AppColor.TextPrimary,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                "$totalLogsCount logs",
                                color = AppColor.TextMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Divider(
                            modifier = Modifier.padding(vertical = 6.dp),
                            thickness = 1.dp,
                            color = AppColor.Divider
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 520.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            contentPadding = PaddingValues(
                                start = 8.dp,
                                end = 8.dp,
                                bottom = 8.dp
                            )
                        ) {
                            itemsIndexed(
                                items = logsState.sortedByDescending { it.timeStamp },
                                key = { _, log -> log.id } // ✅ Stable key prevents jank
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
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Timeline indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFirst) AppColor.Primary else AppColor.GlassBg
                    )
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(22.dp)
                        .background(AppColor.Divider)
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = log.message,
                color = AppColor.TextPrimary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    log.timeStamp.getDate(),
                    color = AppColor.TextMuted,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                )
                Text(
                    "•",
                    color = AppColor.TextMuted,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                )
                Text(
                    log.timeStamp.getTime(),
                    color = AppColor.TextMuted,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                )
            }
        }
    }
}
