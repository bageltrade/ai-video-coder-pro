package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [VideoEntity::class], version = 2, exportSchema = false)
abstract class VideoDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao

    companion object {
        @Volatile
        private var INSTANCE: VideoDatabase? = null

        fun getInstance(context: Context): VideoDatabase {
            return INSTANCE ?: synchronized(this) {
                try {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        VideoDatabase::class.java,
                        "ai_video_projects.db"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                    instance
                } catch (e: Exception) {
                    try {
                        context.applicationContext.deleteDatabase("ai_video_projects.db")
                    } catch (_: Exception) {}
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        VideoDatabase::class.java,
                        "ai_video_projects.db"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                    instance
                }
            }
        }
    }
}
