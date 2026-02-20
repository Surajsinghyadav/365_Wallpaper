package com.example.a365wallpaper.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri

object UrlHandler {

    fun openUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = url.toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            }
            context.startActivity(intent)
        }catch (e: Exception){
            Toast.makeText(context, "Error while opening", Toast.LENGTH_SHORT).show()
        }
    }
}