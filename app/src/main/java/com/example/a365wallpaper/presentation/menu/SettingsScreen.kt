package com.example.a365wallpaper.presentation.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.FrameCorners
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.example.a365wallpaper.presentation.homeScreen.GlassCard
import com.example.a365wallpaper.presentation.homeScreen.Wallpaper365ViewModel
import com.example.a365wallpaper.ui.theme.AppColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: Wallpaper365ViewModel,
) {
    val showMiniFloatingPreview by viewModel.showMiniFloatingPreview.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = AppColor.RootBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColor.RootBg
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── DISPLAY ───────────────────────────────────────────────────────
            GlassCard(title = "Display") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppColor.GlassBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            PhosphorIcons.Fill.FrameCorners,
                            contentDescription = null,
                            tint = AppColor.TextSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            "Mini Floating Preview",
                            color = AppColor.TextPrimary,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            "Show a preview while scrolling",
                            color = AppColor.TextMuted,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                        )
                    }
                    Switch(
                        checked = showMiniFloatingPreview,
                        onCheckedChange = { viewModel.toggleMiniFloatingPreview() },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor   = AppColor.Primary,
                            checkedThumbColor   = Color.White,
                            uncheckedTrackColor = AppColor.Divider,
                            uncheckedThumbColor = Color.White
                        )
                    )
                }
            }
        }
    }
}
