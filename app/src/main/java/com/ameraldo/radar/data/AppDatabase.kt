package com.ameraldo.radar.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database for the Radar application.
 *
 * Stores routes and their recorded GPS points.
 * Uses a thread-safe singleton pattern to ensure one instance.
 *
 * @property routeDao Provides access to route and point data operations
 */
@Database(
    entities = [RouteEntity::class, RecordedPointEntity::class],
    version = 1,
    exportSchema = false // Set to true and provide schema export directory for migrations
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routeDao(): RouteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Gets the singleton database instance.
         * Uses [@Volatile] and [synchronized] for thread safety.
         *
         * @param context Application context (uses applicationContext to avoid leaks)
         * @return The singleton AppDatabase instance
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room
                    .databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "radar_database"
                    )
                    .fallbackToDestructiveMigration(true) // Destroys DB on version mismatch (no migrations yet)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}