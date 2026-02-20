package com.example.a365wallpaper.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import java.time.LocalDate

@Composable
fun MockMonthsDots(
    dotTheme: DotTheme,
    gridStyle: GridStyle,
    scale: Float = 1.0f,
    showLabel: Boolean
){
    val currentDate = LocalDate.now()
    val noOfDays = currentDate.lengthOfMonth()
    val today = currentDate.dayOfMonth

    Column(
        modifier = Modifier.padding(horizontal = scale * 8.dp, vertical = scale * 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(scale * 40.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(scale * 12.dp),
            horizontalArrangement = Arrangement.spacedBy(scale * 12.dp)
        ) {
            items(noOfDays) { d ->
                val dotColor = when {
                    d < today -> dotTheme.filled.toColors()
                    d == today -> dotTheme.today.toColors()
                    d >= today -> dotTheme.empty.toColors()
                    else -> dotTheme.empty.toColors()
                }

                Box(
                    modifier = Modifier
                        .size(scale * 16.dp)
                        .clip(gridStyle.shape)
                        .background(dotColor)
                )
            }
        }

        Spacer(Modifier.height(scale * 6.dp))


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


        Spacer(Modifier.height(scale * 10.dp))
    }
}
