package com.example.a207387_liyuxi_ptizwan_lab04.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_settings")
data class ProfileSettingsEntity(
    @PrimaryKey
    val id: Int = 1,                // Always single row
    val displayName: String = "Green Warrior",
    val targetCO2: Int = 100
)
