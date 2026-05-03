package com.example.a207387_liyuxi_ptizwan_lab04

import java.util.Date

data class ActivityRecord(
    val id: Int,
    val type: String,          // "Task" 或 "Log"
    val description: String,   // e.g. "Cycle to Work" or "Drove to office"
    val co2Change: Int,        // 负数表示减排，正数表示排放
    val timestamp: Long = System.currentTimeMillis()
)