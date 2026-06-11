package com.example.a207387_liyuxi_ptizwan_lab04.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val dao: AppDao) {
    // 活动记录
    fun getAllActivities(): Flow<List<ActivityRecordEntity>> = dao.getAllActivities()
    suspend fun insertActivity(record: ActivityRecordEntity) = dao.insertActivity(record)
    suspend fun deleteActivity(record: ActivityRecordEntity) = dao.deleteActivity(record)

    // 自定义任务
    fun getAllCustomTasks(): Flow<List<CustomTaskEntity>> = dao.getAllCustomTasks()
    suspend fun insertCustomTask(task: CustomTaskEntity) = dao.insertCustomTask(task)
    suspend fun deleteCustomTask(task: CustomTaskEntity) = dao.deleteCustomTask(task)

    // 用户设置
    suspend fun getProfileSettings(): ProfileSettingsEntity? = dao.getProfileSettings()
    suspend fun saveProfileSettings(settings: ProfileSettingsEntity) = dao.saveProfileSettings(settings)

    // ===== 天气缓存 =====
    suspend fun saveWeather(weather: WeatherEntity) = dao.saveWeather(weather)
    fun getWeatherFlow(): Flow<WeatherEntity?> = dao.getWeatherFlow()
    suspend fun getWeatherOnce(): WeatherEntity? = dao.getWeatherOnce()
}