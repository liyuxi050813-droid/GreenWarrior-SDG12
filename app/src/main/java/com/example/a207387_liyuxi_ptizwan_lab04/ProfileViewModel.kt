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

    // 1. Create database and repository instances
    private val dao = AppDatabase.getInstance(application).appDao()
    private val repository = AppRepository(dao)

    // 2. Read activity records from database (Flow → StateFlow)
    private val _activityHistory: StateFlow<List<ActivityRecordEntity>> =
        repository.getAllActivities()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    val activityHistory: StateFlow<List<ActivityRecordEntity>> = _activityHistory

    // 3. Read custom tasks from database
    private val _customTasks: StateFlow<List<CustomTaskEntity>> =
        repository.getAllCustomTasks()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    val customTasks: StateFlow<List<CustomTaskEntity>> = _customTasks

    // 4. Emission logs (filter type == "Log" records from activityHistory)
    //    If you need to keep the original EmissionLog data class, mapping can be done here
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

    // 5. User summary data (maintained in memory, recalculated from DB on init)
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // ===== Weather data =====
    private val _weatherData = MutableStateFlow<WeatherEntity?>(null)
    val weatherData: StateFlow<WeatherEntity?> = _weatherData.asStateFlow()

    private val _isWeatherLoading = MutableStateFlow(false)
    val isWeatherLoading: StateFlow<Boolean> = _isWeatherLoading.asStateFlow()

    private val _weatherError = MutableStateFlow<String?>(null)
    val weatherError: StateFlow<String?> = _weatherError.asStateFlow()

    // ===== Location service =====
    private val locationService = LocationService(application)

    private val _currentLatitude = MutableStateFlow(2.93)   // Default UKM
    private val _currentLongitude = MutableStateFlow(101.78)

    private val _locationText = MutableStateFlow("Bangi, Selangor (UKM Campus)")
    val locationText: StateFlow<String> = _locationText.asStateFlow()

    // ===== Step sensor =====
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

    // Restore all data from database on init
    init {
        viewModelScope.launch {
            // 1) Restore user settings (displayName, targetCO2)
            val savedSettings = repository.getProfileSettings()
            val displayName = savedSettings?.displayName ?: "Green Warrior"
            val targetCO2 = savedSettings?.targetCO2 ?: 100

            // 2) Restore activity summary
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

            // 3) Load cached weather data from Room
            val cachedWeather = repository.getWeatherOnce()
            _weatherData.value = cachedWeather
        }
    }

    // 6. Complete reduction task (preset/custom) + auto sync to Firestore
    fun reduceCO2(amount: Int, taskName: String = "") {
        viewModelScope.launch {
            // Insert into local database
            repository.insertActivity(
                ActivityRecordEntity(
                    type = "Task",
                    description = taskName,
                    co2Change = -amount
                )
            )
            // Update in-memory summary
            _userProfile.update {
                it.copy(
                    totalReduced = it.totalReduced + amount,
                    totalActions = it.totalActions + 1,
                    savedCO2 = it.savedCO2 + amount
                )
            }
            // Sync to Firestore cloud community
            if (taskName.isNotBlank()) {
                syncTaskToCloud(taskName, amount)
            }
        }
    }

    // 7. Add custom task
    fun addCustomTask(name: String, amount: Int) {
        viewModelScope.launch {
            repository.insertCustomTask(
                CustomTaskEntity(name = name, co2Reduction = amount)
            )
        }
    }

    // 8. Add emission log
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
                    savedCO2 = it.savedCO2 - amount   // Emission increase, net carbon reduction decreases
                )
            }
        }
    }

    // 12. Delete activity record
    fun deleteActivity(record: ActivityRecordEntity) {
        viewModelScope.launch {
            repository.deleteActivity(record)
            // Update in-memory stats
            _userProfile.update {
                if (record.co2Change < 0) {
                    // Deleted a reduction record
                    it.copy(
                        totalReduced = it.totalReduced - (-record.co2Change),
                        totalActions = (it.totalActions - 1).coerceAtLeast(0),
                        savedCO2 = it.savedCO2 - (-record.co2Change)
                    )
                } else {
                    // Deleted an emission record
                    it.copy(
                        totalEmitted = it.totalEmitted - record.co2Change,
                        totalActions = (it.totalActions - 1).coerceAtLeast(0),
                        savedCO2 = it.savedCO2 + record.co2Change
                    )
                }
            }
        }
    }

    // 13. Delete custom task
    fun deleteCustomTask(task: CustomTaskEntity) {
        viewModelScope.launch {
            repository.deleteCustomTask(task)
        }
    }

    // 9. Set reduction target (also save to DB)
    fun setTargetCO2(target: Int) {
        _userProfile.update { it.copy(targetCO2 = target) }
        saveSettingsToDb()
    }

    // 10. Change display name (also save to DB)
    fun updateDisplayName(newName: String) {
        _userProfile.update { it.copy(displayName = newName) }
        saveSettingsToDb()
    }

    // 11. Write current settings to database
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

    // ===================== Weather API =====================

    /**
     * Call Open-Meteo API for weather + generate carbon reduction tips
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

                // Generate carbon reduction tips
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

                // Save to Room cache
                repository.saveWeather(weatherEntity)
                _weatherData.value = weatherEntity

            } catch (e: Exception) {
                _weatherError.value = "Failed to load weather: ${e.message}"
                // On load failure, use cached data
                if (_weatherData.value == null) {
                    _weatherData.value = repository.getWeatherOnce()
                }
            } finally {
                _isWeatherLoading.value = false
            }
        }
    }

    /**
     * Generate personalized carbon reduction tips based on weather
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

    // ===================== Location + dynamic weather =====================

    /**
     * Get current device location, auto call fetchWeatherData on success
     * Caller must ensure permission is granted first
     */
    fun fetchWeatherWithLocation() {
        if (!locationService.hasLocationPermission()) {
            _weatherError.value = "Location permission not granted"
            // Fallback: use default coordinates
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
                // Fetch weather after getting location
                fetchWeatherData(lat, lon)
            },
            onFailure = { error ->
                _weatherError.value = error
                // Fallback to default coordinates on failure
                fetchWeatherData()
            }
        )
    }

    /**
     * Check location permission
     */
    fun hasLocationPermission(): Boolean = locationService.hasLocationPermission()

    // ===================== Step sensor =====================

    /**
     * Start step monitoring
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
     * Stop step monitoring (call on Activity pause)
     */
    fun stopStepSensor() {
        stepSensorService.stopListening()
    }

    /**
     * Reset step count
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

    // ===================== Statistics data =====================

    /**
     * Return daily carbon reduction/emission stats for last 7 days
     * Used for StatisticsScreen bar chart
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
    val reduced: Int,       // Daily reduction (positive)
    val emitted: Int,       // Daily emission (positive)
    val net: Int            // Net reduction = reduced - emitted
)