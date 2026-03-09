package com.aashu.privatesuite.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aashu.privatesuite.data.local.entities.DailyTargetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyTargetDao {
    @Query("SELECT * FROM daily_targets WHERE date = :date")
    fun getDailyTarget(date: String): Flow<DailyTargetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyTarget(target: DailyTargetEntity)

    // Sync Query
    @Query("SELECT * FROM daily_targets WHERE isDirty = 1")
    suspend fun getDirtyTargets(): List<DailyTargetEntity>
    
    @Query("UPDATE daily_targets SET isSynced = 1, isDirty = 0 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("DELETE FROM daily_targets")
    suspend fun deleteAll()
}
