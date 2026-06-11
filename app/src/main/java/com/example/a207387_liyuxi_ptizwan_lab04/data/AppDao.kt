package com.example.a207387_liyuxi_ptizwan_lab04.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Activity records
    @Insert
    suspend fun insertActivity(record: ActivityRecordEntity)

    @Query("SELECT * FROM activity_records ORDER BY timestamp DESC")
    fun getAllActivities(): Flow<List<ActivityRecordEntity>>

    @Delete
    suspend fun deleteActivity(record: ActivityRecordEntity)

    // Custom tasks
    @Insert
    suspend fun insertCustomTask(task: CustomTaskEntity)

    @Query("SELECT * FROM custom_tasks")
    fun getAllCustomTasks(): Flow<List<CustomTaskEntity>>

    @Delete
    suspend fun deleteCustomTask(task: CustomTaskEntity)

    // User settings (always single row, id=1)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfileSettings(settings: ProfileSettingsEntity)

    @Query("SELECT * FROM profile_settings WHERE id = 1")
    suspend fun getProfileSettings(): ProfileSettingsEntity?

    // ===== Weather cache (Weather API) =====
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWeather(weather: WeatherEntity)

    @Query("SELECT * FROM weather_cache WHERE id = 1")
    fun getWeatherFlow(): Flow<WeatherEntity?>

    @Query("SELECT * FROM weather_cache WHERE id = 1")
    suspend fun getWeatherOnce(): WeatherEntity?
}