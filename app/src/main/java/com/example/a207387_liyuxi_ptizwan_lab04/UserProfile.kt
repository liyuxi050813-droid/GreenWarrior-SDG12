package com.example.a207387_liyuxi_ptizwan_lab04

data class UserProfile(
    val displayName: String = "Green Warrior",
    val totalReduced: Int = 0,
    val totalEmitted: Int = 0,
    val totalActions: Int = 0,
    val targetCO2: Int = 100,
    val savedCO2: Int = 0  // If previously used, can ignore now or calculate from totalReduced
)
