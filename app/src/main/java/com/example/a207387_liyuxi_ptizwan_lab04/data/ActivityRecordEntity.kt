package com.example.a207387_liyuxi_ptizwan_lab04.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_records")
data class ActivityRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: String,           // "Task" or "Log"
    val description: String,    // e.g. "Cycle to Work"
    val co2Change: Int,         // negative = reduction, positive = emission
    val timestamp: Long = System.currentTimeMillis()
)