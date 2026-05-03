package com.example.a207387_liyuxi_ptizwan_lab04

data class UserProfile(
    val displayName: String = "Green Warrior",
    val targetCO2: Int = 500,
    val totalReduced: Int = 0,    // 累计减排量
    val totalEmitted: Int = 0,    // 累计排放量
    val totalActions: Int = 0     // 总活动次数
) {
    val savedCO2: Int
        get() = totalReduced      // Carbon Saved 就是累计减排量
}