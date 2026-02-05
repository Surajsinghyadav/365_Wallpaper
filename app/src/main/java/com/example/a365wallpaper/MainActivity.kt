package com.example.a365wallpaper


import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.a365wallpaper.presentation.Wallpaper365HomeScreen
import com.example.a365wallpaper.presentation.Wallpaper365ViewModel
import com.example.a365wallpaper.ui.theme._365WallpaperTheme
import org.koin.androidx.compose.koinViewModel


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        setContent {
            _365WallpaperTheme {
                val viewModel : Wallpaper365ViewModel = koinViewModel()
                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    Wallpaper365HomeScreen(
                        viewModel = viewModel,
                        onSetWallpaper = {
                            // call schedule + apply here
//                            scheduleDailyWallpaper(
//                                context = this,
//                                target = WallpaperTarget.BOTH
//                            )
                            // optionally also apply immediately (recommended)
                            runDailyWallpaperWorkerNow(
                                context = this,
                                target = WallpaperTarget.BOTH
                            )
                            Log.d("check", "Button Clicked")
                        },
//                        onSave = { /* your save */ }
                    )
                }
            }
        }
    }
}




fun runDailyWallpaperWorkerNow(context: Context, target: WallpaperTarget) {
    val req = OneTimeWorkRequestBuilder<DailyWallpaperWorker>()
        .setInputData(workDataOf(DailyWallpaperWorker.KEY_TARGET to target.name))
        .build()

    WorkManager.getInstance(context).enqueue(req)
}

//fun scheduleDailyWallpaper(context: Context, target: WallpaperTarget) {
//    val delayMinutes = minutesUntilNextWindow(ZoneId.systemDefault())
//    val req = OneTimeWorkRequestBuilder<DailyWallpaperWorker>()
//        .setInitialDelay(delayMinutes.toLong(), TimeUnit.MINUTES)
//        .setInputData(workDataOf(DailyWallpaperWorker.KEY_TARGET to target.name))
//        .build()
//
//    WorkManager.getInstance(context).enqueueUniqueWork(
//        DailyWallpaperWorker.UNIQUE_NAME,
//        ExistingWorkPolicy.REPLACE,
//        req
//    )
//}



