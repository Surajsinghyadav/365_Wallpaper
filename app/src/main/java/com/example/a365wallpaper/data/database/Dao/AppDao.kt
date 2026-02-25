package com.example.a365wallpaper.data.database.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.a365wallpaper.data.database.Entity.AppPrefsEntity
import com.example.a365wallpaper.data.database.Entity.GoalsEntity
import com.example.a365wallpaper.data.database.Entity.MonthEntity
import com.example.a365wallpaper.data.database.Entity.YearEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {


    @Query("SELECT * FROM AppPrefsEntity LIMIT 1")
    suspend fun getAppPrefs(): AppPrefsEntity?

    @Query("SELECT * FROM AppPrefsEntity LIMIT 1")
    fun getAppPrefsFlow(): Flow<AppPrefsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAppPrefs(appPrefsEntity: AppPrefsEntity)

    @Query("SELECT * FROM YearEntity LIMIT 1")
    suspend fun getYearThemeConfig(): YearEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveYearThemeConfig(yearEntity: YearEntity)

    @Query("SELECT * FROM MonthEntity LIMIT 1")
    suspend fun getMonthThemeConfig(): MonthEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMonthThemeConfig(monthEntity: MonthEntity)


    @Query("SELECT * FROM GoalsEntity LIMIT 1")
    suspend fun getGoalsThemeConfig(): GoalsEntity?

    @Query("SELECT * FROM GoalsEntity LIMIT 1")
    fun getGoalsFlow(): Flow<GoalsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGoalsThemeConfig(goalsEntity: GoalsEntity)
}