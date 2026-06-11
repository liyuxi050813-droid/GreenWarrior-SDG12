package com.example.a207387_liyuxi_ptizwan_lab04.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ActivityRecordEntity::class,
        CustomTaskEntity::class,
        ProfileSettingsEntity::class,
        WeatherEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // DB version 1 → 2 migration: add profile_settings table
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS profile_settings (
                        id INTEGER NOT NULL PRIMARY KEY,
                        displayName TEXT NOT NULL DEFAULT 'Green Warrior',
                        targetCO2 INTEGER NOT NULL DEFAULT 100
                    )
                """)
            }
        }

        // DB version 2 → 3 migration: add weather_cache table
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS weather_cache (
                        id INTEGER NOT NULL PRIMARY KEY,
                        temperature REAL NOT NULL,
                        windSpeed REAL NOT NULL,
                        weatherCode INTEGER NOT NULL,
                        weatherDescription TEXT NOT NULL,
                        weatherEmoji TEXT NOT NULL,
                        dailyMaxTemp TEXT NOT NULL DEFAULT '',
                        dailyMinTemp TEXT NOT NULL DEFAULT '',
                        dailyWeatherCodes TEXT NOT NULL DEFAULT '',
                        dailyDates TEXT NOT NULL DEFAULT '',
                        carbonTip TEXT NOT NULL DEFAULT '',
                        lastUpdated INTEGER NOT NULL
                    )
                """)
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ecotracker_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}