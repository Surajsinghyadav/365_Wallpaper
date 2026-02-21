package com.example.a365wallpaper.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.a365wallpaper.Goal
import com.example.a365wallpaper.ui.theme.DotTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Dao
interface AppDao {

    @Query("SELECT * FROM YearEntity LIMIT 1")
    fun getYearThemeConfig(): YearEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveYearThemeConfig(yearEntity: YearEntity)


    @Query("SELECT * FROM MonthEntity LIMIT 1")
    fun getMonthThemeConfig(): MonthEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMonthThemeConfig(monthEntity: MonthEntity)


    @Query("SELECT * FROM GoalsEntity LIMIT 1")
    fun getGoalsThemeConfig(): GoalsEntity

    @Query("SELECT * FROM GoalsEntity LIMIT 1")
    fun getGoalsFlow(): List<Goal>



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGoalsThemeConfig(goal: GoalsEntity)


}