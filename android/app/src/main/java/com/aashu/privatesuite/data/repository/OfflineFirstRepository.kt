package com.aashu.privatesuite.data.repository

import com.aashu.privatesuite.data.local.entities.CollectionEntity
import com.aashu.privatesuite.data.local.entities.TaskEntity
import com.aashu.privatesuite.data.local.entities.DailyTargetEntity
import kotlinx.coroutines.flow.Flow

interface OfflineFirstRepository {
    // Collections
    fun getAllCollections(): Flow<List<CollectionEntity>>
    fun getCollectionsWithCounts(): Flow<List<com.aashu.privatesuite.data.local.entities.CollectionWithCounts>>
    fun getCollectionsByType(type: String): Flow<List<CollectionEntity>>
    suspend fun getCollectionById(id: String): CollectionEntity?
    suspend fun createCollection(collection: CollectionEntity)
    suspend fun insertCollections(collections: List<CollectionEntity>)
    suspend fun updateCollection(collection: CollectionEntity)
    suspend fun updateCollectionLastOpened(id: String)
    suspend fun updateCollectionCounts(id: String)
    suspend fun deleteCollection(id: String)
    suspend fun searchCollections(query: String): List<CollectionEntity>

    // Tasks
    fun getAllTasks(): Flow<List<TaskEntity>>
    fun getTasksByCollection(collectionId: String): Flow<List<TaskEntity>>
    suspend fun getTaskById(id: String): TaskEntity?
    suspend fun getTasksByCollectionId(collectionId: String): List<TaskEntity>
    suspend fun createTask(task: TaskEntity)
    suspend fun insertTasks(tasks: List<TaskEntity>)
    suspend fun updateTask(task: TaskEntity)
    suspend fun toggleTaskStatus(taskId: String, currentStatus: String)
    suspend fun deleteTask(taskId: String)
    fun getAllCompletedTasks(): Flow<List<TaskEntity>>
    suspend fun searchTasks(query: String): List<TaskEntity>

    // Daily Targets
    fun getDailyTarget(date: String): Flow<DailyTargetEntity?>
    suspend fun createDailyTarget(target: DailyTargetEntity)
    
    // Sync (Public methods for WorkManager)
    suspend fun getDirtyCollections(): List<CollectionEntity>
    suspend fun getDirtyTasks(): List<TaskEntity>
    suspend fun markCollectionSynced(id: String)
    suspend fun markTaskSynced(id: String)
    
    // reset
    suspend fun clearAllData()
}
