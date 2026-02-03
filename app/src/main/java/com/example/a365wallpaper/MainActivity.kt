package com.example.a365wallpaper

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.a365wallpaper.ui.theme._365WallpaperTheme
import dev.shreyaspatil.capturable.capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.coroutines.delay


import androidx.compose.foundation.layout.*

import androidx.compose.ui.unit.times
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.a365wallpaper.presentation.Wallpaper365HomeScreen
import com.example.a365wallpaper.presentation.Wallpaper365ViewModel
import org.koin.androidx.viewmodel.ext.android.getViewModel
import kotlin.random.Random



class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Log.d("WP_MAIN", "onCreate() -> enqueue worker now")
//        startWallpaperEveryMinute()

        val viewModel = getViewModel<Wallpaper365ViewModel>()
        setContent {
            _365WallpaperTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    Wallpaper365HomeScreen(
                        viewModel = viewModel,
                        onOpenFilters = {}
                    )
                }
            }
        }
    }

//    private fun startWallpaperEveryMinute() {
//        val req = OneTimeWorkRequestBuilder<WallpaperMinuteWorker>()
//            .setInputData(workDataOf(WallpaperMinuteWorker.KEY_TARGET to WallpaperTarget.BOTH.name))
//            .build()
//
//        Log.d("WP_MAIN", "enqueueUniqueWork id=${req.id}")
//        WorkManager.getInstance(this).enqueueUniqueWork(
//            "wallpaper_every_minute_test",
//            ExistingWorkPolicy.REPLACE,
//            req
//        )
//    }

    private fun stopWallpaperEveryMinute() {
        Log.d("WP_MAIN", "cancelUniqueWork")
        WorkManager.getInstance(this).cancelUniqueWork("wallpaper_every_minute_test")
    }
}




//@OptIn(ExperimentalComposeUiApi::class)
//@Composable
//fun CaptureTestScreen() {
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//    val captureController = rememberCaptureController()
//    val configuration = LocalConfiguration.current
//
//    var lastSavedUri by remember { mutableStateOf<Uri?>(null) }
//
//    Log.d("ScrrenDimenioms", "width = ${configuration.screenWidthDp}, heigh = ${configuration.screenHeightDp}")
//
//    LaunchedEffect(Unit) {
//        delay(4000)
//
//        val imageBitmap = captureController.captureAsync().await()
//        val bmp = imageBitmap.asAndroidBitmap()
////        lastSavedUri = saveBitmapToPictures(
////            context,
////            bmp,
////            "compose_capture_${System.currentTimeMillis()}.png"
////        )
//    }
//
//    Column(Modifier.fillMaxSize().statusBarsPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
//
//        // The composable you want to export as an image
//        Box(
//            modifier = Modifier
//                .width(configuration.screenWidthDp.dp)
//                .height(configuration.screenHeightDp.dp)
//                .capturable(captureController),
//            contentAlignment = Alignment.Center
//        ) {
//            Card(
//                shape = RoundedCornerShape(20.dp),
//                modifier = Modifier.fillMaxSize()
//            ) {
//                Box(
//                    Modifier.fillMaxSize().background(Color(0xff1a1a1a)),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Column(horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.Center) {
//                        repeat(20){
//                            Row() {
//                                repeat(20){
//                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
//
//                                }
//                            }
//
//                        }
//                    }
//                }
//            }
//        }
//
////        Button(onClick = {
////            scope.launch {
////                val imageBitmap = captureController.captureAsync().await() // Capturable API [web:53]
////                val bmp = imageBitmap.asAndroidBitmap()
////                lastSavedUri = saveBitmapToPictures(context, bmp, "compose_capture_${System.currentTimeMillis()}.png")
////            }
////        }) {
////            Text("Capture & Save")
////        }
//
//
//        if (lastSavedUri != null) {
//            Toast.makeText(context, "Image saved", Toast.LENGTH_SHORT).show()
//        }
//    }
//}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun yearDotsWallpaper(
    modifier: Modifier = Modifier,
    totalDays: Int = 365,
    todayIndex: Int = 32,
    columns: Int = 15,
    dotSize: Dp = 14.dp,
    gap: Dp = 12.dp,
    topPadding: Dp = 110.dp,
    bottomPadding: Dp = 80.dp,
    filledColor: Color = Color(0xFFF2F2F2),
    emptyColor: Color = Color(0xFF3D3D3D),
    todayColor: Color = Color(0xFFF36B2C),
    background: Color = Color(0xFF1F1F1F),
) {
    val clampedToday = todayIndex.coerceIn(0, totalDays - 1)
    val daysLeft = (totalDays - 1) - clampedToday
    val percent = ((clampedToday + 1) * 100) / totalDays
    val captureController = rememberCaptureController()
    val configuration = LocalConfiguration.current

    val bgColor = remember { Color(
        Random.nextInt(100,256),
        Random.nextInt(100,256),
        Random.nextInt(100,256),
        alpha = 0xff,
    ) }
    LaunchedEffect(Unit) {
        val imageBitmap = captureController.captureAsync().await()
        val bmp = imageBitmap.asAndroidBitmap()
    }

    Box(
        modifier = modifier
            .padding(top= topPadding, bottom = bottomPadding)
            .width(configuration.screenWidthDp.dp)
            .height(configuration.screenHeightDp.dp)
            .capturable(captureController),
        contentAlignment = Alignment.TopCenter
    ) {
        // Background
        Box(Modifier.fillMaxSize())

        DotsGrid(
            total = totalDays,
            todayIndex = clampedToday,
            columns = columns,
            dotSize = dotSize,
            gap = gap,
            filledColor = filledColor,
            emptyColor = emptyColor,
            todayColor = todayColor,
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(horizontal = 18.dp)
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${daysLeft}d left",
                color = todayColor,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "  ·  $percent%",
                color = Color(0xFFA8A8A8),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }


}

@Composable
private fun DotsGrid(
    total: Int,
    todayIndex: Int,
    columns: Int,
    dotSize: Dp,
    gap: Dp,
    filledColor: Color,
    emptyColor: Color,
    todayColor: Color,
    modifier: Modifier = Modifier
) {
    val rows = (total + columns - 1) / columns
    val height = ((rows * (dotSize + gap)) + gap)

    Canvas(
        modifier = modifier
            .height(height) // give enough vertical space
    ) {
        val r = dotSize.toPx() / 2f
        val stepX = (dotSize + gap).toPx()
        val stepY = (dotSize + gap).toPx()

        // Start a bit inset so dots look centered like the wallpaper
        val startX = gap.toPx()
        val startY = gap.toPx()

        for (i in 0 until total) {
            val row = i / columns
            val col = i % columns

            val cx = startX + col * stepX + r
            val cy = startY + row * stepY + r

            val color = when {
                i == todayIndex -> todayColor
                i < todayIndex  -> filledColor
                else            -> emptyColor
            }

            drawCircle(
                color = color,
                radius = r,
                center = Offset(cx, cy)
            )
        }
    }
}


//private fun saveBitmapToPictures(context: Context, bitmap: Bitmap, fileName: String): Uri? {
//
//    val resolver = context.contentResolver
//    val values = ContentValues().apply {
//        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
//        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
//        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TestCaptures") // scoped storage [web:65]
//    }
//
//    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
//    val out: OutputStream = resolver.openOutputStream(uri) ?: return null
//    out.use { stream ->
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
//    }
//    return uri
//}
