package com.example.a207387_liyuxi_ptizwan_lab04.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "custom_tasks")
data class CustomTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val co2Reduction: Int
)