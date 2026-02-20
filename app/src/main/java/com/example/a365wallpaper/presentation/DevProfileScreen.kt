package com.example.a365wallpaper.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.*
import com.example.a365wallpaper.ui.theme.AppColor
import com.example.a365wallpaper.utils.UrlHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevProfileScreen(
    onBack: () -> Unit,
    onOpenLogs: () -> Unit
) {
    val context = LocalContext.current

    val linkedinUrl = "https://www.linkedin.com/in/surajsinghyadav/"
    val githubUrl = "https://github.com/Surajsinghyadav"
    val portfolioUrl = "https://surajsinghyadav.in"



    Scaffold(
        containerColor = AppColor.RootBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile",
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // DEV INFO CARD
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = AppColor.CardBg,
                border = BorderStroke(1.dp, AppColor.CardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // avatar
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(AppColor.GlassBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.AndroidLogo,
                            contentDescription = null,
                            tint = AppColor.TextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Suraj Singh Yadav",
                            color = AppColor.TextPrimary,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Android Developer",
                            color = AppColor.TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "ysurajsingh56@Gmail.com",
                            color = AppColor.TextMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // LINKS / CONTACT OPTIONS
            GlassCard(title = "Connect") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfileLinkRow(
                        icon = PhosphorIcons.Regular.LinkedinLogo,
                        label = "LinkedIn",
                        value = "linkedin.com/in/surajsinghyadav",
                        onClick = {
                            UrlHandler.openUrl(context, linkedinUrl)
                        }
                    )
                    ProfileLinkRow(
                        icon = PhosphorIcons.Regular.GithubLogo,
                        label = "GitHub",
                        value = "github.com/Surajsinghyadav",
                        onClick = {
                            UrlHandler.openUrl(context, githubUrl)

                        }
                    )
                    ProfileLinkRow(
                        icon = PhosphorIcons.Regular.Globe,
                        label = "Portfolio",
                        value = "surajsinghyadav.in",
                        onClick = {
                            UrlHandler.openUrl(context, portfolioUrl)

                        }
                    )
                }
            }

            // LOGS ENTRY – OPENS LOGS SCREEN
            GlassCard(title = "Activity") {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onOpenLogs() },
                    shape = RoundedCornerShape(14.dp),
                    color = AppColor.GlassBg,
                    border = BorderStroke(1.dp, AppColor.GlassBorder)
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
                                PhosphorIcons.Regular.Scroll,
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
                                "See all logs",
                                color = AppColor.TextPrimary,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            )
                            Text(
                                "Service triggers & wallpaper updates history",
                                color = AppColor.TextMuted,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                            )
                        }

                        Icon(
                            PhosphorIcons.Regular.CaretRight,
                            contentDescription = null,
                            tint = AppColor.TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileLinkRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick : () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(4.dp).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AppColor.GlassBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = AppColor.TextSecondary, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                color = AppColor.TextPrimary,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                value,
                color = AppColor.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
