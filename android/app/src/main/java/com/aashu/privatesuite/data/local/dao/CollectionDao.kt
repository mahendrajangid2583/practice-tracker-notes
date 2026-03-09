package com.aashu.privatesuite.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aashu.privatesuite.data.local.entities.CollectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Query("""
        SELECT 
            c.*, 
            (SELECT COUNT(*) FROM tasks t WHERE t.collectionId = c.id AND t.isDeleted = 0) as actualTotal,
            (SELECT COUNT(*) FROM tasks t WHERE t.collectionId = c.id AND t.isDeleted = 0 AND t.status = 'Done') as actualCompleted
        FROM collections c 
        WHERE c.isDeleted = 0 
        ORDER BY c.lastOpenedAt DESC
    """)
    fun getCollectionsWithCounts(): Flow<List<com.aashu.privatesuite.data.local.entities.CollectionWithCounts>>

    @Query("SELECT * FROM collections WHERE isDeleted = 0 ORDER BY lastOpenedAt DESC")
    fun getAllCollections(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collections WHERE id = :id AND isDeleted = 0")
    suspend fun getCollectionById(id: String): CollectionEntity?

    @Query("SELECT * FROM collections WHERE type = :type AND isDeleted = 0 ORDER BY lastOpenedAt DESC")
    fun getCollectionsByType(type: String): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collections WHERE title LIKE '%' || :query || '%' AND isDeleted = 0")
    suspend fun searchCollections(query: String): List<CollectionEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCollection(collection: CollectionEntity): Long

    @Update
    suspend fun updateCollection(collection: CollectionEntity)
    
    @androidx.room.Transaction
    suspend fun upsertCollection(collection: CollectionEntity) {
        val id = insertCollection(collection)
        if (id == -1L) {
            updateCollection(collection)
        }
    }

    @androidx.room.Transaction
    suspend fun upsertCollections(collections: List<CollectionEntity>) {
        collections.forEach { upsertCollection(it) }
    }

    @Query("UPDATE collections SET isDeleted = 1, isDirty = 1 WHERE id = :id")
    suspend fun deleteCollection(id: String)

    @Query("UPDATE collections SET completedTasks = :completed, totalTasks = :total WHERE id = :collectionId")
    suspend fun updateTaskCounts(collectionId: String, completed: Int, total: Int)

    // Sync Queries
    @Query("SELECT * FROM collections WHERE isDirty = 1")
    suspend fun getDirtyCollections(): List<CollectionEntity>

    @Query("UPDATE collections SET isSynced = 1, isDirty = 0 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("UPDATE collections SET lastOpenedAt = :timestamp WHERE id = :id")
    suspend fun updateLastOpened(id: String, timestamp: String)

    @Query("UPDATE collections SET completedTasks = (SELECT COUNT(*) FROM tasks WHERE collectionId = :collectionId AND status = 'Done' AND isDeleted = 0), totalTasks = (SELECT COUNT(*) FROM tasks WHERE collectionId = :collectionId AND isDeleted = 0) WHERE id = :collectionId")
    suspend fun updateCollectionCounts(collectionId: String)

    @Query("DELETE FROM collections")
    suspend fun deleteAll()
}
