package com.example.a365wallpaper

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable


sealed interface AppRoute : NavKey

@Serializable
data object Wallpaper365HomeScreen: AppRoute

@Serializable
data object DevProfile : AppRoute

@Serializable
data object  LogsScreen : AppRoute

