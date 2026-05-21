package com.example.a207387_liyuxi_ptizwan_lab04

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.a207387_liyuxi_ptizwan_lab04.data.ActivityRecordEntity
import com.example.a207387_liyuxi_ptizwan_lab04.data.AppDatabase
import com.example.a207387_liyuxi_ptizwan_lab04.data.AppRepository
import com.example.a207387_liyuxi_ptizwan_lab04.data.CustomTaskEntity
import com.example.a207387_liyuxi_ptizwan_lab04.data.ProfileSettingsEntity
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
        }
    }

    // 6. 完成减排任务（预设/自定义）
    fun reduceCO2(amount: Int, taskName: String = "") {
        viewModelScope.launch {
            // 插入数据库
            repository.insertActivity(
                ActivityRecordEntity(
                    type = "Task",
                    description = taskName,
                    co2Change = -amount
                )
            )
            // 更新内存汇总（数据库的 Flow 会自动更新 activityHistory）
            _userProfile.update {
                it.copy(
                    totalReduced = it.totalReduced + amount,
                    totalActions = it.totalActions + 1,
                    savedCO2 = it.savedCO2 + amount   // ✅ 减排任务完成，净减碳量增加
                )
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
}