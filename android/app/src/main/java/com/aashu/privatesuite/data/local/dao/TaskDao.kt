package com.aashu.privatesuite.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aashu.privatesuite.data.local.entities.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE collectionId = :collectionId AND isDeleted = 0 ORDER BY status DESC, addedAt DESC")
    fun getTasksByCollection(collectionId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isDeleted = 0")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id AND isDeleted = 0")
    suspend fun getTaskById(id: String): TaskEntity?
    
    @Query("SELECT * FROM tasks WHERE collectionId = :collectionId AND isDeleted = 0")
    suspend fun getTasksByCollectionSync(collectionId: String): List<TaskEntity>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTask(task: TaskEntity): Long
    
    @Update
    suspend fun updateTask(task: TaskEntity)

    @androidx.room.Transaction
    suspend fun upsertTask(task: TaskEntity) {
        val id = insertTask(task)
        if (id == -1L) {
            updateTask(task)
        }
    }

    @androidx.room.Transaction
    suspend fun upsertTasks(tasks: List<TaskEntity>) {
        tasks.forEach { upsertTask(it) }
    }

    @Query("UPDATE tasks SET isDeleted = 1, isDirty = 1, isSynced = 0 WHERE id = :id")
    suspend fun deleteTask(id: String)

    @Query("UPDATE tasks SET status = :status, completedAt = :completedAt, isDirty = 1, isSynced = 0 WHERE id = :taskId")
    suspend fun updateStatus(taskId: String, status: String, completedAt: java.util.Date?)

    // Sync Queries
    @Query("SELECT * FROM tasks WHERE isDirty = 1")
    suspend fun getDirtyTasks(): List<TaskEntity>

    @Query("UPDATE tasks SET isSynced = 1, isDirty = 0 WHERE id = :id")
    suspend fun markSynced(id: String)
    
    @Query("SELECT COUNT(*) FROM tasks WHERE collectionId = :collectionId AND isDeleted = 0")
    suspend fun getTaskCount(collectionId: String): Int
    
    @Query("SELECT COUNT(*) FROM tasks WHERE collectionId = :collectionId AND status = 'Done' AND isDeleted = 0")
    suspend fun getCompletedTaskCount(collectionId: String): Int
    
    @Query("SELECT * FROM tasks WHERE status = 'Done' AND isDeleted = 0 ORDER BY completedAt DESC")
    fun getAllCompletedTasks(): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE (title LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%') AND isDeleted = 0")
    suspend fun searchTasks(query: String): List<TaskEntity>

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()

    // Daily Targets Selection Queries
    @Query("SELECT * FROM tasks WHERE collectionId = :collectionId AND status != 'Done' AND isDeleted = 0 AND id NOT IN (:excludeIds) ORDER BY addedAt ASC LIMIT 1")
    suspend fun getFirstPendingTask(collectionId: String, excludeIds: List<String>): TaskEntity?

    @Query("SELECT * FROM tasks WHERE collectionId = :collectionId AND status != 'Done' AND isDeleted = 0 AND id NOT IN (:excludeIds) ORDER BY addedAt DESC LIMIT 1")
    suspend fun getLastPendingTask(collectionId: String, excludeIds: List<String>): TaskEntity?

    @Query("SELECT * FROM tasks WHERE id IN (:ids)")
    fun getTasksByIds(ids: List<String>): Flow<List<TaskEntity>>
}
