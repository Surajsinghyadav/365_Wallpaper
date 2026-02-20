package com.example.a365wallpaper.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
@Database(entities = [DotThemeEntity::class, LogEntity:: class], version = 2, exportSchema = false)
abstract class AppDatabase(): RoomDatabase(){
    abstract fun appDao(): AppDao
    abstract fun logDao(): LogDao

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase{
            return INSTANCE ?: synchronized(this){
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