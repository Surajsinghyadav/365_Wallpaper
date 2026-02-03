package com.example.a365wallpaper.presentation

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.a365wallpaper.data.GridStyle
import com.example.a365wallpaper.data.Target
import com.example.a365wallpaper.data.WallpaperMode
import com.example.a365wallpaper.ui.theme.AppColor
import com.example.a365wallpaper.ui.theme.AppColor.Accents
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class Wallpaper365ViewModel(): ViewModel(){
    private val _mode = MutableStateFlow(
        WallpaperMode.Year
    )

    val mode = _mode.asStateFlow()

    fun updateMode(mode: WallpaperMode){
        _mode.update {
            mode
        }
    }

    private val _setWallpaperTo = MutableStateFlow(
        Target.Lock
    )

    val setWallpaperTo = _setWallpaperTo.asStateFlow()

    fun updateSetWallpaperTo(target: Target){
        _setWallpaperTo.update {
            target
        }
    }

    fun onSetWallpaper(){

    }


    private val _style = MutableStateFlow(
        GridStyle.Dots
    )

    val style = _style.asStateFlow()

    fun updateStyle(style: GridStyle){
        _style.update {
            style
        }
    }


    private val _selectedAccentColor = MutableStateFlow(
        Accents.first()
    )


    val selectedAccentColor = _selectedAccentColor.asStateFlow()

    fun updateAccentColor(color: Color){
        _selectedAccentColor.update {
            color
        }
    }

    private val _showLabel = MutableStateFlow(
        true
    )
val showLabel = _showLabel.asStateFlow()

    private val _showQuote = MutableStateFlow(
        true
    )
    val showQuote = _showQuote.asStateFlow()

    fun togleShowLabel(){
        _showLabel.update { showLabel ->
            !showLabel
        }
    }

    fun togleShowQuote(){
        _showQuote.update { showQuote->
            !showQuote
        }
    }


}