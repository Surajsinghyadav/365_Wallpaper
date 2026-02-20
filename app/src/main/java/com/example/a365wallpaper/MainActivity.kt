package com.example.a365wallpaper


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
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
                    AppNav(
                        viewModel
                    )
                }
            }
        }
    }
}





