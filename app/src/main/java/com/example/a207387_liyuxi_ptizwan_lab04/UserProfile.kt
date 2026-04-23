package com.example.a207387_liyuxi_ptizwan_lab04

data class UserProfile(//4. 数据类存了目标和当前排放
    val displayName: String = "Green Warrior",
    val targetCO2: Int = 500,      // 目标总量
    val currentCO2: Int = 500      // 当前排放量（初始与目标相同）
) {
    // 计算属性：已节省的碳排放量
    val savedCO2: Int
        get() = (targetCO2 - currentCO2).coerceAtLeast(0)
}