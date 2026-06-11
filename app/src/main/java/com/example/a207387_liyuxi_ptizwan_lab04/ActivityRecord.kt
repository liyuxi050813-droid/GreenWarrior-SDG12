package com.example.a207387_liyuxi_ptizwan_lab04

import java.util.Date

data class ActivityRecord(
    val id: Int,
    val type: String,          // "Task" or "Log"
    val description: String,   // e.g. "Cycle to Work" or "Drove to office"
    val co2Change: Int,        // Negative = reduction, positive = emission
    val timestamp: Long = System.currentTimeMillis()
)