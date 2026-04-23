package com.example.a207387_liyuxi_ptizwan_lab04

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel : ViewModel() {//3
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()//5ViewModel 里用 StateFlow 保存

    // 设置新目标，同时将当前排放重置为新的目标值（从零开始节省）
    fun setTargetCO2(target: Int) {
        _userProfile.update {
            it.copy(targetCO2 = target, currentCO2 = target)
        }
    }

    // 完成任务减少当前排放量（即增加节省量）
    fun reduceCO2(amount: Int) {
        _userProfile.update { current ->
            val newCurrent = (current.currentCO2 - amount).coerceAtLeast(0)
            current.copy(currentCO2 = newCurrent)
        }
    }

    // 一键清零（排放量设为0，节省量达到目标值）
    fun clearCarbon() {
        _userProfile.update { it.copy(currentCO2 = 0) }
    }

    // 更新显示名称
    fun updateDisplayName(newName: String) {
        _userProfile.update { it.copy(displayName = newName) }
    }

    // 直接设置当前排放量（用于编辑页手动修改）
    fun updateCurrentCO2(newValue: Int) {
        _userProfile.update { it.copy(currentCO2 = newValue.coerceIn(0, it.targetCO2)) }
    }

    // 检查目标是否达成（节省量 >= 目标量）
    fun isTargetAchieved(): Boolean {
        val profile = _userProfile.value
        return profile.savedCO2 >= profile.targetCO2
    }
    fun resetAll() {
        _userProfile.update {
            it.copy(
                targetCO2 = 0,
                currentCO2 = 0,
                displayName = "Green Warrior" // 可选，同时重置名字
            )
        }
    }
}