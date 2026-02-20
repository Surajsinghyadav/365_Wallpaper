package com.example.a365wallpaper.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.a365wallpaper.ui.theme.DotTheme

@Dao
interface AppDao {

    @Query("SELECT * FROM DotThemeEntity LIMIT 1")
    fun getSavedDotTheme(): DotThemeEntity? = null

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDotThemeTODb(dotTheme: DotThemeEntity)
}