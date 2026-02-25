package com.example.a365wallpaper.Navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.a365wallpaper.presentation.Menu.DevProfileScreen
import com.example.a365wallpaper.presentation.Menu.LogsScreen
import com.example.a365wallpaper.presentation.HomeScreen.Wallpaper365HomeScreen
import com.example.a365wallpaper.presentation.HomeScreen.Wallpaper365ViewModel

@Composable
fun AppNav(
    homeViewModel: Wallpaper365ViewModel
) {
    val backStack = rememberNavBackStack(Wallpaper365HomeScreen)

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
        entryProvider = entryProvider {
            entry<Wallpaper365HomeScreen> {
                Wallpaper365HomeScreen(
                    viewModel = homeViewModel,
                    goToDevProfile = {
                        backStack.add(DevProfile)
                    }
                )
            }

            entry<DevProfile> {
                DevProfileScreen(
                    onBack = {
                        backStack.removeLastOrNull()
                    },
                    onOpenLogs = {
                        backStack.add(LogsScreen)
                    }
                )
            }

            entry<LogsScreen>{
                LogsScreen(
                    onBack = {
                        backStack.removeLastOrNull()
                    },
                )
            }
        },
        // ✅ FORWARD NAVIGATION (Enter new screen)
        transitionSpec = {
            (slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth }, // Start from right edge
                animationSpec = tween(
                    durationMillis = 400, // Smooth duration
                    easing = FastOutSlowInEasing
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            )) togetherWith (slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth / 3 }, // Exit partially left
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            ))
        },
        // ✅ BACK NAVIGATION (Pop screen)
        popTransitionSpec = {
            (slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth / 3 }, // Enter from left partially
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            )) togetherWith (slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth }, // Exit to right edge
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            ))
        },
        // ✅ PREDICTIVE BACK (Android 13+ gesture)
        predictivePopTransitionSpec = {
            (slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth / 3 },
                animationSpec = tween(
                    durationMillis = 350, // Slightly faster for gesture
                    easing = FastOutSlowInEasing
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = 350,
                    easing = FastOutSlowInEasing
                )
            )) togetherWith (slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(
                    durationMillis = 350,
                    easing = FastOutSlowInEasing
                )
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = 350,
                    easing = FastOutSlowInEasing
                )
            ))
        }
    )
}
