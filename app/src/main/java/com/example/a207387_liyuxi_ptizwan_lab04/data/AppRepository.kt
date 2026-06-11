package com.example.a207387_liyuxi_ptizwan_lab04.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val dao: AppDao) {
    // Activity records
    fun getAllActivities(): Flow<List<ActivityRecordEntity>> = dao.getAllActivities()
    suspend fun insertActivity(record: ActivityRecordEntity) = dao.insertActivity(record)
    suspend fun deleteActivity(record: ActivityRecordEntity) = dao.deleteActivity(record)

    // Custom tasks
    fun getAllCustomTasks(): Flow<List<CustomTaskEntity>> = dao.getAllCustomTasks()
    suspend fun insertCustomTask(task: CustomTaskEntity) = dao.insertCustomTask(task)
    suspend fun deleteCustomTask(task: CustomTaskEntity) = dao.deleteCustomTask(task)

    // User settings
    suspend fun getProfileSettings(): ProfileSettingsEntity? = dao.getProfileSettings()
    suspend fun saveProfileSettings(settings: ProfileSettingsEntity) = dao.saveProfileSettings(settings)

    // ===== Weather cache =====
    suspend fun saveWeather(weather: WeatherEntity) = dao.saveWeather(weather)
    fun getWeatherFlow(): Flow<WeatherEntity?> = dao.getWeatherFlow()
    suspend fun getWeatherOnce(): WeatherEntity? = dao.getWeatherOnce()
}