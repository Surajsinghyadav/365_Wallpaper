// WallpaperMinuteWorker.kt
package com.example.a365wallpaper

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class WallpaperMinuteWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val targetStr = inputData.getString(KEY_TARGET) ?: WallpaperTarget.BOTH.name
        val target = runCatching { WallpaperTarget.valueOf(targetStr) }.getOrElse { WallpaperTarget.BOTH }

        return try {
            // Replace this with your Capturable bitmap generation when you want.
            val bmp = WallpaperBitmapGenerator.generateRandomColorBitmap(1080, 2400)

            WallpaperSetter(applicationContext).set(bmp, target)

            // Reschedule itself after 1 minute (testing approach).
            scheduleNext(applicationContext, target)

            Result.success()
        } catch (t: Throwable) {
            // Optional: still reschedule to keep the loop alive while testing
            scheduleNext(applicationContext, target)
            Result.retry()
        }
    }

    private fun scheduleNext(context: Context, target: WallpaperTarget) {
        val next = OneTimeWorkRequestBuilder<WallpaperMinuteWorker>()
            .setInitialDelay(1, TimeUnit.MINUTES)
            .setInputData(workDataOf(KEY_TARGET to target.name))
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork( // enqueue patterns [web:33]
            UNIQUE_NAME,
            ExistingWorkPolicy.REPLACE,
            next
        )
    }

    companion object {
        private const val UNIQUE_NAME = "wallpaper_every_minute_test"
        const val KEY_TARGET = "KEY_TARGET"
    }
}
