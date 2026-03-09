package com.aashu.privatesuite.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aashu.privatesuite.data.local.entities.Checkpoint
import com.aashu.privatesuite.data.local.entities.DeletedItemEntity

@Dao
interface SyncStateDao {
    @Query("SELECT * FROM sync_state WHERE id = 'global_checkpoint'")
    suspend fun getCheckpoint(): Checkpoint?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCheckpoint(checkpoint: Checkpoint)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordDeletion(item: DeletedItemEntity)

    @Query("SELECT * FROM deleted_items WHERE isSynced = 0")
    suspend fun getUnsyncedDeletions(): List<DeletedItemEntity>

    @Query("UPDATE deleted_items SET isSynced = 1 WHERE id = :id")
    suspend fun markDeletionSynced(id: String)
        
    @Query("DELETE FROM deleted_items WHERE id = :id")
    suspend fun clearDeletion(id: String)

    @Query("DELETE FROM sync_state")
    suspend fun deleteAll()
}
