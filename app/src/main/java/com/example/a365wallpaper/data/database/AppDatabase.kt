package com.example.a365wallpaper.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.a365wallpaper.data.database.Dao.AppDao
import com.example.a365wallpaper.data.database.Dao.LogDao
import com.example.a365wallpaper.data.database.Entity.AppPrefsEntity
import com.example.a365wallpaper.data.database.Entity.GoalsEntity
import com.example.a365wallpaper.data.database.Entity.LogEntity
import com.example.a365wallpaper.data.database.Entity.MonthEntity
import com.example.a365wallpaper.data.database.Entity.YearEntity

@Database(
    entities = [
        AppPrefsEntity::class,
        YearEntity::class,
        MonthEntity::class,
        GoalsEntity::class,
        LogEntity::class,
    ],
    version = 11,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun logDao(): LogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "AppDatabase"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
