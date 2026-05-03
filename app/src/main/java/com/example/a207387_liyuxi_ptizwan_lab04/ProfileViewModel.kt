package com.example.a207387_liyuxi_ptizwan_lab04

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel : ViewModel() {

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _customTasks = MutableStateFlow<List<CustomTask>>(emptyList())
    val customTasks: StateFlow<List<CustomTask>> = _customTasks.asStateFlow()

    private val _logList = MutableStateFlow<List<EmissionLog>>(emptyList())
    val logList: StateFlow<List<EmissionLog>> = _logList.asStateFlow()

    // 活动历史
    private val _activityHistory = MutableStateFlow<List<ActivityRecord>>(emptyList())
    val activityHistory: StateFlow<List<ActivityRecord>> = _activityHistory.asStateFlow()

    // 记录一条活动（内部使用）
    private fun addActivityRecord(type: String, description: String, co2Change: Int) {
        val record = ActivityRecord(
            id = _activityHistory.value.size + 1,
            type = type,
            description = description,
            co2Change = co2Change
        )
        _activityHistory.update { it + record }
    }

    // 设置目标
    fun setTargetCO2(target: Int) {
        _userProfile.update { it.copy(targetCO2 = target) }
    }

    // 完成预设任务或自定义任务
    fun reduceCO2(amount: Int, taskName: String = "") {
        _userProfile.update { current ->
            current.copy(
                totalReduced = current.totalReduced + amount,
                totalActions = current.totalActions + 1
            )
        }
        // 记录到活动历史
        addActivityRecord("Task", taskName, -amount)
    }

    // 添加自定义减排任务
    fun addCustomTask(name: String, amount: Int) {
        val newTask = CustomTask(
            id = _customTasks.value.size + 1,
            name = name,
            co2Reduction = amount
        )
        _customTasks.update { it + newTask }
    }

    // 添加排放日志
    fun addEmissionLog(name: String, amount: Int) {
        val newLog = EmissionLog(
            id = _logList.value.size + 1,
            activityName = name,
            emissionAmount = amount
        )
        _logList.update { it + newLog }

        _userProfile.update { profile ->
            profile.copy(
                totalEmitted = profile.totalEmitted + amount,
                totalActions = profile.totalActions + 1
            )
        }
        // 记录到活动历史
        addActivityRecord("Log", name, amount)
    }

    fun updateDisplayName(newName: String) {
        _userProfile.update { it.copy(displayName = newName) }
    }

    fun isTargetAchieved(): Boolean {
        return _userProfile.value.totalReduced >= _userProfile.value.targetCO2
    }
}