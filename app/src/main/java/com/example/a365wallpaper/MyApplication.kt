package com.example.a365wallpaper

import android.app.Application
import com.example.a365wallpaper.di.MyModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApplication)
            modules(MyModule)
        }

    }
}