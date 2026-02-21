package com.example.a365wallpaper.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [YearEntity::class, MonthEntity::class, GoalsEntity::class, LogEntity:: class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase(): RoomDatabase(){
    abstract fun appDao(): AppDao
    abstract fun logDao(): LogDao

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "App_Database"
                ).fallbackToDestructiveMigration(false).build()
                INSTANCE = instance
                instance
            }
        }
    }


}