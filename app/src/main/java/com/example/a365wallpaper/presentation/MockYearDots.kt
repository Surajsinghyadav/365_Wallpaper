package com.example.a365wallpaper.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.example.a365wallpaper.data.GridStyle
import com.example.a365wallpaper.toColors
import com.example.a365wallpaper.ui.theme.DotTheme

@Composable
fun MockYearsDots(
    dotTheme: DotTheme,
    gridStyle: GridStyle,
    scale: Float = 1.0f,
    showLabel: Boolean
){
    Column(
        modifier = Modifier.padding(vertical = scale * 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(scale * 6.dp)
    ) {
        repeat(18) { r ->
            Row(horizontalArrangement = Arrangement.spacedBy(scale * 6.dp)) {
                repeat(14) { c ->
                    val dotColor = when {
                        r < 11 -> dotTheme.filled.toColors()
                        r <= 11 && c < 8 -> dotTheme.filled.toColors()
                        r == 11 && c == 8 -> dotTheme.today.toColors()
                        r == 17 && c >= 4 -> Color.Transparent
                        else -> dotTheme.empty.toColors()
                    }

                    Box(
                        modifier = Modifier
                            .size(scale * 6.dp)
                            .clip(gridStyle.shape)
                            .background(dotColor)
                    )
                }
            }
        }

        if (showLabel){
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "68d left ",
                    color = dotTheme.today.toColors(),
                    fontWeight = FontWeight.Bold,
                    fontSize = scale * 8.sp
                )
                Text(
                    "· 83%",
                    color = Color.Gray,
                    fontSize = scale * 8.sp
                )
            }
        }

    }
}
