package com.example.a207387_liyuxi_ptizwan_lab04

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.a207387_liyuxi_ptizwan_lab04.data.ActivityRecordEntity
import com.example.a207387_liyuxi_ptizwan_lab04.data.AppDatabase
import com.example.a207387_liyuxi_ptizwan_lab04.data.AppRepository
import com.example.a207387_liyuxi_ptizwan_lab04.data.CustomTaskEntity
import com.example.a207387_liyuxi_ptizwan_lab04.data.ProfileSettingsEntity
import com.example.a207387_liyuxi_ptizwan_lab04.data.WeatherEntity
import com.example.a207387_liyuxi_ptizwan_lab04.network.ApiClient
import com.example.a207387_liyuxi_ptizwan_lab04.network.WeatherCode
import com.example.a207387_liyuxi_ptizwan_lab04.sensor.LocationService
import com.example.a207387_liyuxi_ptizwan_lab04.firebase.CommunityRecord
import com.example.a207387_liyuxi_ptizwan_lab04.firebase.CommunityStats
import com.example.a207387_liyuxi_ptizwan_lab04.firebase.FirebaseSyncService
import com.example.a207387_liyuxi_ptizwan_lab04.sensor.StepSensorService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    // 1. 创建数据库和仓库实例
    private val dao = AppDatabase.getInstance(application).appDao()
    private val repository = AppRepository(dao)

    // 2. 从数据库读取活动记录（用 Flow 转 StateFlow）
    private val _activityHistory: StateFlow<List<ActivityRecordEntity>> =
        repository.getAllActivities()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    val activityHistory: StateFlow<List<ActivityRecordEntity>> = _activityHistory

    // 3. 从数据库读取自定义任务
    private val _customTasks: StateFlow<List<CustomTaskEntity>> =
        repository.getAllCustomTasks()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    val customTasks: StateFlow<List<CustomTaskEntity>> = _customTasks

    // 4. 排放日志（从 activityHistory 中过滤 type == "Log" 的记录）
    //    如果需要保留原来的 EmissionLog 数据类，可以在这里做映射
    val logList: StateFlow<List<EmissionLog>> = _activityHistory
        .map { records ->
            records
                .filter { it.type == "Log" }
                .map { entity ->
                    EmissionLog(
                        id = entity.id,
                        activityName = entity.description,
                        emissionAmount = entity.co2Change
                    )
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 5. 用户汇总数据（内存中维护，启动时从数据库重新计算）
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // ===== 天气数据 =====
    private val _weatherData = MutableStateFlow<WeatherEntity?>(null)
    val weatherData: StateFlow<WeatherEntity?> = _weatherData.asStateFlow()

    private val _isWeatherLoading = MutableStateFlow(false)
    val isWeatherLoading: StateFlow<Boolean> = _isWeatherLoading.asStateFlow()

    private val _weatherError = MutableStateFlow<String?>(null)
    val weatherError: StateFlow<String?> = _weatherError.asStateFlow()

    // ===== 定位服务 =====
    private val locationService = LocationService(application)

    private val _currentLatitude = MutableStateFlow(2.93)   // 默认 UKM
    private val _currentLongitude = MutableStateFlow(101.78)

    private val _locationText = MutableStateFlow("Bangi, Selangor (UKM Campus)")
    val locationText: StateFlow<String> = _locationText.asStateFlow()

    // ===== 步数传感器 =====
    private val stepSensorService = StepSensorService(application)

    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount.asStateFlow()

    private val _stepCO2Saved = MutableStateFlow(0f)
    val stepCO2Saved: StateFlow<Float> = _stepCO2Saved.asStateFlow()

    private val _isSensorAvailable = MutableStateFlow(false)
    val isSensorAvailable: StateFlow<Boolean> = _isSensorAvailable.asStateFlow()

    // ===== Firebase Firestore Cloud Sync =====
    private val firebaseSync = FirebaseSyncService()

    private val _communityRecords = MutableStateFlow<List<CommunityRecord>>(emptyList())
    val communityRecords: StateFlow<List<CommunityRecord>> = _communityRecords.asStateFlow()

    private val _communityStats = MutableStateFlow(CommunityStats(0, 0, 0))
    val communityStats: StateFlow<CommunityStats> = _communityStats.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    // 初始化时从数据库恢复所有数据
    init {
        viewModelScope.launch {
            // ① 恢复用户设置（displayName、targetCO2）
            val savedSettings = repository.getProfileSettings()
            val displayName = savedSettings?.displayName ?: "Green Warrior"
            val targetCO2 = savedSettings?.targetCO2 ?: 100

            // ② 恢复活动汇总
            val allRecords = repository.getAllActivities().first()
            val reduced = allRecords.filter { it.co2Change < 0 }.sumOf { -it.co2Change }
            val emitted = allRecords.filter { it.co2Change > 0 }.sumOf { it.co2Change }

            _userProfile.update {
                it.copy(
                    displayName = displayName,
                    targetCO2 = targetCO2,
                    totalReduced = reduced,
                    totalEmitted = emitted,
                    totalActions = allRecords.size,
                    savedCO2 = reduced - emitted
                )
            }

            // ③ 从 Room 加载缓存的天气数据
            val cachedWeather = repository.getWeatherOnce()
            _weatherData.value = cachedWeather
        }
    }

    // 6. 完成减排任务（预设/自定义）+ 自动同步到 Firestore
    fun reduceCO2(amount: Int, taskName: String = "") {
        viewModelScope.launch {
            // 插入本地数据库
            repository.insertActivity(
                ActivityRecordEntity(
                    type = "Task",
                    description = taskName,
                    co2Change = -amount
                )
            )
            // 更新内存汇总
            _userProfile.update {
                it.copy(
                    totalReduced = it.totalReduced + amount,
                    totalActions = it.totalActions + 1,
                    savedCO2 = it.savedCO2 + amount
                )
            }
            // 同步到 Firestore 云端社区
            if (taskName.isNotBlank()) {
                syncTaskToCloud(taskName, amount)
            }
        }
    }

    // 7. 添加自定义任务
    fun addCustomTask(name: String, amount: Int) {
        viewModelScope.launch {
            repository.insertCustomTask(
                CustomTaskEntity(name = name, co2Reduction = amount)
            )
        }
    }

    // 8. 添加排放日志
    fun addEmissionLog(name: String, amount: Int) {
        viewModelScope.launch {
            repository.insertActivity(
                ActivityRecordEntity(
                    type = "Log",
                    description = name,
                    co2Change = amount
                )
            )
            _userProfile.update {
                it.copy(
                    totalEmitted = it.totalEmitted + amount,
                    totalActions = it.totalActions + 1,
                    savedCO2 = it.savedCO2 - amount   // ✅ 排放增加，净减碳量减少
                )
            }
        }
    }

    // 12. 删除活动记录
    fun deleteActivity(record: ActivityRecordEntity) {
        viewModelScope.launch {
            repository.deleteActivity(record)
            // 更新内存统计
            _userProfile.update {
                if (record.co2Change < 0) {
                    // 删除的是减排记录
                    it.copy(
                        totalReduced = it.totalReduced - (-record.co2Change),
                        totalActions = (it.totalActions - 1).coerceAtLeast(0),
                        savedCO2 = it.savedCO2 - (-record.co2Change)
                    )
                } else {
                    // 删除的是排放记录
                    it.copy(
                        totalEmitted = it.totalEmitted - record.co2Change,
                        totalActions = (it.totalActions - 1).coerceAtLeast(0),
                        savedCO2 = it.savedCO2 + record.co2Change
                    )
                }
            }
        }
    }

    // 13. 删除自定义任务
    fun deleteCustomTask(task: CustomTaskEntity) {
        viewModelScope.launch {
            repository.deleteCustomTask(task)
        }
    }

    // 9. 设置减排目标（同时存数据库）
    fun setTargetCO2(target: Int) {
        _userProfile.update { it.copy(targetCO2 = target) }
        saveSettingsToDb()
    }

    // 10. 修改显示名称（同时存数据库）
    fun updateDisplayName(newName: String) {
        _userProfile.update { it.copy(displayName = newName) }
        saveSettingsToDb()
    }

    // 11. 将当前设置写入数据库
    private fun saveSettingsToDb() {
        val profile = _userProfile.value
        viewModelScope.launch {
            repository.saveProfileSettings(
                ProfileSettingsEntity(
                    id = 1,
                    displayName = profile.displayName,
                    targetCO2 = profile.targetCO2
                )
            )
        }
    }

    fun isTargetAchieved(): Boolean {
        return _userProfile.value.totalReduced >= _userProfile.value.targetCO2
    }

    // ===================== 天气 API =====================

    /**
     * 调用 Open-Meteo API 获取天气 + 生成碳减排建议
     */
    fun fetchWeatherData(latitude: Double = 2.93, longitude: Double = 101.78) {
        viewModelScope.launch {
            _isWeatherLoading.value = true
            _weatherError.value = null
            try {
                val response = ApiClient.apiService.getWeather(
                    latitude = latitude,
                    longitude = longitude
                )
                val current = response.currentWeather
                val daily = response.daily

                // 生成碳减排建议
                val tip = generateCarbonTip(
                    temp = current.temperature,
                    weatherCode = current.weatherCode,
                    windSpeed = current.windSpeed
                )

                val gson = Gson()
                val weatherEntity = WeatherEntity(
                    id = 1,
                    temperature = current.temperature,
                    windSpeed = current.windSpeed,
                    weatherCode = current.weatherCode,
                    weatherDescription = WeatherCode.toDescription(current.weatherCode),
                    weatherEmoji = WeatherCode.toEmoji(current.weatherCode),
                    dailyMaxTemp = if (daily != null) gson.toJson(daily.temperatureMax) else "",
                    dailyMinTemp = if (daily != null) gson.toJson(daily.temperatureMin) else "",
                    dailyWeatherCodes = if (daily != null) gson.toJson(daily.weatherCode) else "",
                    dailyDates = if (daily != null) gson.toJson(daily.time) else "",
                    carbonTip = tip,
                    lastUpdated = System.currentTimeMillis()
                )

                // 存入 Room 缓存
                repository.saveWeather(weatherEntity)
                _weatherData.value = weatherEntity

            } catch (e: Exception) {
                _weatherError.value = "Failed to load weather: ${e.message}"
                // 加载失败时，使用缓存数据
                if (_weatherData.value == null) {
                    _weatherData.value = repository.getWeatherOnce()
                }
            } finally {
                _isWeatherLoading.value = false
            }
        }
    }

    /**
     * 基于天气数据生成个性化碳减排建议
     */
    private fun generateCarbonTip(temp: Double, weatherCode: Int, windSpeed: Double): String {
        val tips = when {
            temp > 32 -> listOf(
                "High heat today! Try remote working to reduce AC & car emissions.",
                "Skip driving in the heat — walk early morning to save 2kg CO\u2082!",
                "Hot day alert: car AC increases fuel use by 15%. Consider cycling."
            )
            temp < 22 -> listOf(
                "Cool weather! Perfect day to walk or bike to campus.",
                "Mild temperatures — open windows instead of using AC.",
                "Great outdoor weather. Plant-based picnic, anyone?"
            )
            weatherCode in 61..67 || weatherCode in 80..82 -> listOf(
                "Rainy day! Carpool with friends to reduce emissions per person.",
                "Wet roads — public transport is safer and greener today.",
                "Stay dry and green: use the campus shuttle instead of driving."
            )
            weatherCode in 95..99 -> listOf(
                "Thunderstorm warning! Stay indoors, save energy.",
                "Stormy weather — perfect day to work from home and reduce carbon."
            )
            else -> listOf(
                "Pleasant weather! Walk or cycle to save up to 3kg CO\u2082 today.",
                "Nice day for outdoor activities — skip the car!",
                "Green tip: plant-based meals today cut 2.5kg CO\u2082 per meal."
            )
        }
        return tips[(System.currentTimeMillis() % tips.size).toInt()]
    }

    // ===================== 定位 + 动态天气 =====================

    /**
     * 获取当前设备位置，成功后自动调用 fetchWeatherData
     * 需要调用方先确保权限已授予
     */
    fun fetchWeatherWithLocation() {
        if (!locationService.hasLocationPermission()) {
            _weatherError.value = "Location permission not granted"
            // 降级：使用默认坐标
            fetchWeatherData()
            return
        }

        _isWeatherLoading.value = true
        _weatherError.value = null

        locationService.getLastLocation(
            onSuccess = { lat, lon ->
                _currentLatitude.value = lat
                _currentLongitude.value = lon
                _locationText.value = String.format("%.4f, %.4f", lat, lon)
                // 拿到位置后获取天气
                fetchWeatherData(lat, lon)
            },
            onFailure = { error ->
                _weatherError.value = error
                // 失败降级到默认坐标
                fetchWeatherData()
            }
        )
    }

    /**
     * 检查定位权限
     */
    fun hasLocationPermission(): Boolean = locationService.hasLocationPermission()

    // ===================== 步数传感器 =====================

    /**
     * 开始监听步数
     */
    fun startStepSensor() {
        _isSensorAvailable.value = stepSensorService.isAvailable()
        if (!stepSensorService.isAvailable()) return

        stepSensorService.startListening { steps, co2 ->
            _stepCount.value = steps
            _stepCO2Saved.value = co2
        }
    }

    /**
     * 停止监听步数（Activity 暂停时调用）
     */
    fun stopStepSensor() {
        stepSensorService.stopListening()
    }

    /**
     * 重置步数计数
     */
    fun resetStepCounter() {
        stepSensorService.reset()
        _stepCount.value = 0
        _stepCO2Saved.value = 0f
    }

    // ===================== Firebase Cloud Sync =====================

    /**
     * Upload a completed carbon-reduction task to Firestore.
     * Called after the user completes a task (preset or custom).
     */
    fun syncTaskToCloud(taskName: String, co2Reduction: Int) {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncError.value = null
            val userId = _userProfile.value.displayName.ifBlank { "Green Warrior" }
            val location = _locationText.value
            val result = firebaseSync.uploadCompletedTask(userId, taskName, co2Reduction, location)
            result.onFailure { e ->
                _syncError.value = "Cloud sync failed: ${e.message}"
            }
            _isSyncing.value = false
            // Refresh community data after upload
            loadCommunityRecords()
        }
    }

    /**
     * Load community records from Firestore.
     */
    fun loadCommunityRecords() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncError.value = null

            // Load records
            val recordsResult = firebaseSync.loadCommunityRecords()
            recordsResult.onSuccess { records ->
                _communityRecords.value = records
            }.onFailure { e ->
                _syncError.value = "Failed to load community: ${e.message}"
            }

            // Load stats
            val statsResult = firebaseSync.getCommunityStats()
            statsResult.onSuccess { stats ->
                _communityStats.value = stats
            }

            _isSyncing.value = false
        }
    }

    // ===================== 统计数据 =====================

    /**
     * 返回最近 7 天每天的碳减排/排放统计数据
     * 用于 StatisticsScreen 柱状图
     */
    fun getDailyCarbonStats(): Flow<List<DailyCarbonStat>> {
        return _activityHistory.map { records ->
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val calendar = java.util.Calendar.getInstance()
            val today = sdf.format(calendar.time)

            val days = mutableListOf<String>()
            repeat(7) { offset ->
                calendar.time = sdf.parse(today) ?: java.util.Date()
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -6 + offset)
                days.add(sdf.format(calendar.time))
            }

            days.map { day ->
                val dayRecords = records.filter { record ->
                    val recordDay = sdf.format(java.util.Date(record.timestamp))
                    recordDay == day
                }
                val reduced = dayRecords.filter { it.co2Change < 0 }.sumOf { -it.co2Change }
                val emitted = dayRecords.filter { it.co2Change > 0 }.sumOf { it.co2Change }
                DailyCarbonStat(
                    date = day,
                    reduced = reduced,
                    emitted = emitted,
                    net = reduced - emitted
                )
            }
        }
    }
}

data class DailyCarbonStat(
    val date: String,       // "2026-06-11"
    val reduced: Int,       // 当天减排量（正）
    val emitted: Int,       // 当天排放量（正）
    val net: Int            // 净减排 = reduced - emitted
)