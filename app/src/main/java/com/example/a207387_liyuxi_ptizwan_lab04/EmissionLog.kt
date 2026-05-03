package com.example.a207387_liyuxi_ptizwan_lab04

data class EmissionLog(
    val id: Int,
    val activityName: String,
    val emissionAmount: Int,   // in kg
    val timestamp: Long = System.currentTimeMillis()

)
