package com.example.a365wallpaper.presentation.reusableComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowsVertical
import com.example.a365wallpaper.ui.theme.AppColor

// Add to HomeScreen file
@Composable
fun VerticalPositionSlider(
    position: Float, // Range: -1 to +1
    onPositionChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    PhosphorIcons.Regular.ArrowsVertical,
                    contentDescription = null,
                    tint = AppColor.TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Vertical position",
                    color = AppColor.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            // Display numeric value
            Text(
                when {
                    position > 0.01f -> "+${(position * 100).toInt()}"
                    position < -0.01f -> "${(position * 100).toInt()}"
                    else -> "0"
                },
                color = AppColor.Primary,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            )
        }

        // Custom slider with center indicator
        Box(modifier = Modifier.fillMaxWidth()) {
            // Center line indicator
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(2.dp)
                    .height(24.dp)
                    .background(AppColor.GlassBorder)
            )

            Slider(
                value = position,
                onValueChange = onPositionChange,
                valueRange = -1f..1f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = AppColor.Primary,
                    activeTrackColor = AppColor.Primary.copy(alpha = 0.6f),
                    inactiveTrackColor = AppColor.GlassBorder
                )
            )
        }

        // Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Top",
                color = AppColor.TextMuted,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
            )
            Text(
                "Center",
                color = AppColor.TextMuted,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
            )
            Text(
                "Bottom",
                color = AppColor.TextMuted,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
            )
        }
    }
}

