package com.example.a365wallpaper.di

import com.example.a365wallpaper.presentation.Wallpaper365ViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val MyModule = module {
    viewModel{ Wallpaper365ViewModel() }
}