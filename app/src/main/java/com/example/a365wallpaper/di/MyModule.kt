package com.example.a365wallpaper.di

import android.app.Application
import com.example.a365wallpaper.DailyWallpaperWorker
import com.example.a365wallpaper.data.database.AppDatabase
import com.example.a365wallpaper.presentation.LogsViewModel
import com.example.a365wallpaper.presentation.Wallpaper365ViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val MyModule = module {
    single { AppDatabase.getDatabase(androidContext()) }
    single { get<AppDatabase>().appDao() }
    single {get<AppDatabase>().logDao() }
    workerOf(::DailyWallpaperWorker)
    viewModel{ Wallpaper365ViewModel(get<Application>(), get())}
    viewModel { LogsViewModel(get()) }

}