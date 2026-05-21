package com.example.a207387_liyuxi_ptizwan_lab04.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // 活动记录
    @Insert
    suspend fun insertActivity(record: ActivityRecordEntity)

    @Query("SELECT * FROM activity_records ORDER BY timestamp DESC")
    fun getAllActivities(): Flow<List<ActivityRecordEntity>>

    @Delete
    suspend fun deleteActivity(record: ActivityRecordEntity)

    // 自定义任务
    @Insert
    suspend fun insertCustomTask(task: CustomTaskEntity)

    @Query("SELECT * FROM custom_tasks")
    fun getAllCustomTasks(): Flow<List<CustomTaskEntity>>

    @Delete
    suspend fun deleteCustomTask(task: CustomTaskEntity)

    // 用户设置（永远只有一行，id=1）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfileSettings(settings: ProfileSettingsEntity)

    @Query("SELECT * FROM profile_settings WHERE id = 1")
    suspend fun getProfileSettings(): ProfileSettingsEntity?
}