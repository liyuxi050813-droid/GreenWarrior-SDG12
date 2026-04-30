package com.example.a207387_liyuxi_ptizwan_lab04

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel : ViewModel() {

    // ==================== 用户资料状态 ====================
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // ==================== 排放日志列表状态 ====================
    private val _logList = MutableStateFlow<List<EmissionLog>>(emptyList())
    val logList: StateFlow<List<EmissionLog>> = _logList.asStateFlow()

    // ==================== 用户资料相关方法 ====================
    fun setTargetCO2(target: Int) {
        _userProfile.update {
            it.copy(targetCO2 = target, currentCO2 = target)
        }
    }

    fun reduceCO2(amount: Int) {
        _userProfile.update { current ->
            val newCurrent = (current.currentCO2 - amount).coerceAtLeast(0)
            current.copy(currentCO2 = newCurrent)
        }
    }

    fun clearCarbon() {
        _userProfile.update { it.copy(currentCO2 = 0) }
    }

    fun updateDisplayName(newName: String) {
        _userProfile.update { it.copy(displayName = newName) }
    }

    fun updateCurrentCO2(newValue: Int) {
        _userProfile.update { it.copy(currentCO2 = newValue.coerceIn(0, it.targetCO2)) }
    }

    fun isTargetAchieved(): Boolean {
        val profile = _userProfile.value
        return profile.savedCO2 >= profile.targetCO2
    }

    fun resetAll() {
        _userProfile.update {
            it.copy(
                targetCO2 = 0,
                currentCO2 = 0,
                displayName = "Green Warrior"
            )
        }
    }

    // ==================== 排放日志相关方法 ====================
    fun addEmissionLog(name: String, amount: Int) {
        val newLog = EmissionLog(
            id = _logList.value.size + 1,
            activityName = name,
            emissionAmount = amount
        )
        _logList.update { it + newLog }
        // 如果希望添加日志时自动增加当前排放量（可选），可以在这里调用：
        // updateCurrentCO2(_userProfile.value.currentCO2 + amount)
    }
}