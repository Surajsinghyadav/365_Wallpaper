package com.example.a365wallpaper.data.database.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.a365wallpaper.data.database.Entity.LogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {

    @Query("SELECT * FROM LogEntity ORDER BY timeStamp ASC")
    fun getAllLogs(): Flow<List<LogEntity>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertLog(logEntity: LogEntity)

    @Query("SELECT COUNT(*) FROM logentity")
    fun getTotalLogsCount(): Flow<Int>

    @Query("DELETE FROM LogEntity")
    suspend fun deleteAllLogs()

}