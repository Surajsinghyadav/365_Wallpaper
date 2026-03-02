package com.example.a365wallpaper.Navigation

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.edit
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.a365wallpaper.presentation.menu.DevProfileScreen
import com.example.a365wallpaper.presentation.menu.LogsScreen
import com.example.a365wallpaper.presentation.homeScreen.Wallpaper365HomeScreen
import com.example.a365wallpaper.presentation.homeScreen.Wallpaper365ViewModel
import com.example.a365wallpaper.presentation.menu.SettingsScreen
import com.example.a365wallpaper.presentation.onboarding.OnboardingScreen

@Composable
fun AppNav(
    prefs: SharedPreferences,
    homeViewModel: Wallpaper365ViewModel
) {
    val hasSeenOnboarding = remember {
        false
//            prefs.getBoolean("onboarding_done", false)
    }
    val backStack = rememberNavBackStack(
        if (hasSeenOnboarding) Wallpaper365HomeScreen else OnboardingScreen
    )
    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
        entryProvider = entryProvider {

            entry<OnboardingScreen> {
                OnboardingScreen(
                    onFinished = {
                            prefs.edit { putBoolean("onboarding_done", true) }
                        backStack.clear()
                        backStack.add(Wallpaper365HomeScreen)
                    }
                )
            }

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
                    onBack = { backStack.removeLastOrNull() },
                    onOpenSettings = { backStack.add(SettingsScreen) },
                    onOpenLogs     = { backStack.add(LogsScreen) },
                )
            }

            entry <SettingsScreen>{
                SettingsScreen(
                    onBack = {
                        backStack.removeLastOrNull()
                    },
                    viewModel = homeViewModel
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
