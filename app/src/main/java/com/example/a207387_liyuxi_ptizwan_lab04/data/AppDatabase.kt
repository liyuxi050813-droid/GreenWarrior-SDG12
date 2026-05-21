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
        ProfileSettingsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // 数据库版本 1 → 2 迁移：新增 profile_settings 表
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

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ecotracker_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}