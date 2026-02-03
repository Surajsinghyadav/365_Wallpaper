//// WallpaperMinuteWorker.kt
//package com.example.a365wallpaper
//
//import android.content.Context
//import android.util.Log
//import androidx.work.CoroutineWorker
//import androidx.work.ExistingWorkPolicy
//import androidx.work.OneTimeWorkRequestBuilder
//import androidx.work.WorkManager
//import androidx.work.WorkerParameters
//import androidx.work.workDataOf
//import java.util.concurrent.TimeUnit
//
//class WallpaperMinuteWorker(
//    appContext: Context,
//    params: WorkerParameters
//) : CoroutineWorker(appContext, params) {
//
//    override suspend fun doWork(): Result {
//        val targetStr = inputData.getString(KEY_TARGET) ?: WallpaperTarget.BOTH.name
//        val target = runCatching { WallpaperTarget.valueOf(targetStr) }.getOrElse { WallpaperTarget.BOTH }
//
//        Log.d("WP_WORKER", "doWork START id=$id attempt=$runAttemptCount target=$target")
//
//        return try {
//            val bmp = WallpaperBitmapGenerator.generateRandomColorBitmap(1080, 2400)
//            Log.d("WP_WORKER", "bitmap ${bmp.width}x${bmp.height}")
//
//            WallpaperSetter(applicationContext).set(bmp, target)
//            Log.d("WP_WORKER", "wallpaper set OK")
//
//            scheduleNext(target)
//            Log.d("WP_WORKER", "scheduled next in 1 minute")
//
//            Result.success()
//        } catch (t: Throwable) {
//            Log.e("WP_WORKER", "FAILED ${t.message}", t)
//            scheduleNext(target)
//            Result.retry()
//        }
//    }
//
////    private fun scheduleNext(target: WallpaperTarget) {
////        val next = OneTimeWorkRequestBuilder<WallpaperMinuteWorker>()
////            .setInitialDelay(11, TimeUnit.MINUTES)
////            .setInputData(workDataOf(KEY_TARGET to target.name))
////            .build()
////
////        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
////            "wallpaper_every_minute_test",
////            ExistingWorkPolicy.REPLACE,
////            next
////        )
////    }
//
//    companion object {
//        const val KEY_TARGET = "KEY_TARGET"
//    }
//}
