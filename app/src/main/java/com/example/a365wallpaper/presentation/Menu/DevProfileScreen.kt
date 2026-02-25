package com.example.a365wallpaper.presentation.Menu

import android.content.Intent
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.AndroidLogo
import com.adamglin.phosphoricons.fill.Bug
import com.adamglin.phosphoricons.fill.GithubLogo
import com.adamglin.phosphoricons.fill.Globe
import com.adamglin.phosphoricons.fill.LinkedinLogo
import com.adamglin.phosphoricons.fill.ListChecks
import com.adamglin.phosphoricons.fill.Scroll
import com.adamglin.phosphoricons.fill.ShareNetwork
import com.adamglin.phosphoricons.fill.ShieldCheck
import com.adamglin.phosphoricons.fill.SquaresFour
import com.adamglin.phosphoricons.regular.*
import com.example.a365wallpaper.ui.theme.AppColor
import com.example.a365wallpaper.utils.UrlHandler
import com.example.a365wallpaper.presentation.HomeScreen.GlassCard

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
    val email = "ysurajsingh56@gmail.com"
    val formUrl = "https://forms.gle/87nPN2XhSsLNTwUU9"
    val playStoreUrl = "https://play.google.com/store/apps/developer?id=Suraj+Singh+Yadav"
    val shareText = "Check out 365 Wallpaper app! $playStoreUrl"

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

            // ── DEV INFO CARD ─────────────────────────────────────────────────
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
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(AppColor.GlassBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Fill.AndroidLogo,
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

            // ── CONNECT ───────────────────────────────────────────────────────
            GlassCard(title = "Connect") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfileLinkRow(
                        icon = PhosphorIcons.Fill.LinkedinLogo,
                        label = "LinkedIn",
                        value = "linkedin.com/in/surajsinghyadav",
                        onClick = { UrlHandler.openUrl(context, linkedinUrl) }
                    )
                    ProfileLinkRow(
                        icon = PhosphorIcons.Fill.GithubLogo,
                        label = "GitHub",
                        value = "github.com/Surajsinghyadav",
                        onClick = { UrlHandler.openUrl(context, githubUrl) }
                    )
                    ProfileLinkRow(
                        icon = PhosphorIcons.Fill.Globe,
                        label = "Portfolio",
                        value = "surajsinghyadav.in",
                        onClick = { UrlHandler.openUrl(context, portfolioUrl) }
                    )
                }
            }

            // ── MORE ──────────────────────────────────────────────────────────
            GlassCard(title = "More") {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    // Share App
                    MenuNavRow(
                        icon = PhosphorIcons.Fill.ShareNetwork,
                        label = "Share App",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share via"))
                        }
                    )

                    MenuDivider()

                    // More Apps
                    MenuNavRow(
                        icon = PhosphorIcons.Fill.SquaresFour,
                        label = "More Apps",
                        onClick = { UrlHandler.openUrl(context, playStoreUrl) }
                    )

                    MenuDivider()

                    // Report a Bug
                    MenuNavRow(
                        icon = PhosphorIcons.Fill.Bug,
                        label = "Report a Bug",
                        onClick = {
                            UrlHandler.openUrl(
                                context,
                                formUrl
                            )
                        }
                    )

                    MenuDivider()

                    // Privacy Policy
                    MenuNavRow(
                        icon = PhosphorIcons.Fill.ShieldCheck,
                        label = "Privacy Policy",
                        onClick = {
                            UrlHandler.openUrl(
                                context,
                                "https://surajsinghyadav.in/365-wallpaper-privacy"
                            )
                        }
                    )

                    MenuDivider()

                    // Terms of Use
                    MenuNavRow(
                        icon = PhosphorIcons.Fill.ListChecks,
                        label = "Terms of Use",
                        onClick = {
                            UrlHandler.openUrl(
                                context,
                                "https://surajsinghyadav.in/365-wallpaper-terms"
                            )
                        }
                    )
                    MenuNavRow(
                        icon = PhosphorIcons.Fill.Scroll,
                        label = "See all logs",
                        onClick = onOpenLogs

                    )
                }
            }

            // ── VERSION ───────────────────────────────────────────────────────
            Text(
                text = "v1.0.0",
                color = AppColor.TextMuted,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}



@Composable
private fun MenuNavRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 12.dp),
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
                icon,
                contentDescription = null,
                tint = AppColor.TextSecondary,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            color = AppColor.TextPrimary,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            ),
            modifier = Modifier.weight(1f)
        )
        Icon(
            PhosphorIcons.Regular.CaretRight,
            contentDescription = null,
            tint = AppColor.TextMuted,
            modifier = Modifier.size(15.dp)
        )
    }
}

@Composable
private fun MenuDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 46.dp),
        thickness = 0.5.dp,
        color = AppColor.GlassBorder
    )
}

@Composable
private fun ProfileLinkRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(4.dp),
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
        Icon(
            PhosphorIcons.Regular.ArrowSquareOut,
            contentDescription = null,
            tint = AppColor.TextMuted,
            modifier = Modifier.size(15.dp)
        )
    }
}
