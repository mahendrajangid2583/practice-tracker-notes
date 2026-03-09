package com.aashu.privatesuite.data.repository

import com.aashu.privatesuite.data.local.dao.CollectionDao
import com.aashu.privatesuite.data.local.dao.TaskDao
import com.aashu.privatesuite.data.local.dao.DailyTargetDao
import com.aashu.privatesuite.data.local.dao.SyncStateDao
import com.aashu.privatesuite.data.local.entities.CollectionEntity
import com.aashu.privatesuite.data.local.entities.TaskEntity
import com.aashu.privatesuite.data.local.entities.DailyTargetEntity
import com.aashu.privatesuite.data.local.entities.DeletedItemEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstRepositoryImpl @Inject constructor(
    private val collectionDao: CollectionDao,
    private val taskDao: TaskDao,
    private val dailyTargetDao: DailyTargetDao,
    private val syncStateDao: SyncStateDao,
    private val workManager: androidx.work.WorkManager,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : OfflineFirstRepository {

    private fun scheduleSync() {
        val request = androidx.work.OneTimeWorkRequestBuilder<com.aashu.privatesuite.data.worker.SyncWorker>()
            .setConstraints(
                androidx.work.Constraints.Builder()
                    .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                    .build()
            )
            .build()
        workManager.enqueueUniqueWork("ImmediateSync", androidx.work.ExistingWorkPolicy.KEEP, request)
    }

    private fun sendWidgetUpdateBroadcast() {
        val intent = android.content.Intent("com.aashu.privatesuite.action.UPDATE_STREAK_WIDGET")
        intent.setPackage(context.packageName)
        context.sendBroadcast(intent)
    }

    override fun getAllCollections(): Flow<List<CollectionEntity>> = collectionDao.getAllCollections()

    override fun getCollectionsWithCounts(): Flow<List<com.aashu.privatesuite.data.local.entities.CollectionWithCounts>> = collectionDao.getCollectionsWithCounts()

    override fun getCollectionsByType(type: String): Flow<List<CollectionEntity>> = collectionDao.getCollectionsByType(type)

    override suspend fun getCollectionById(id: String): CollectionEntity? = collectionDao.getCollectionById(id)

    override suspend fun createCollection(collection: CollectionEntity) {
        collectionDao.upsertCollection(collection)
        scheduleSync()
    }

    override suspend fun insertCollections(collections: List<CollectionEntity>) {
        if (collections.isEmpty()) return
        
        // CRITICAL: Only insert server data for items that are NOT dirty locally
        collections.forEach { serverCollection ->
            val localCollection = collectionDao.getCollectionById(serverCollection.id)
            
            if (localCollection == null) {
                // New collection from server, safe to insert
                android.util.Log.d("OfflineFirstRepo", "Inserting new collection from server: ${serverCollection.id}")
                collectionDao.upsertCollection(serverCollection)
            } else if (!localCollection.isDirty) {
                // Local copy is clean, safe to replace with server version
                android.util.Log.d("OfflineFirstRepo", "Updating clean collection with server data: ${serverCollection.id}")
                collectionDao.upsertCollection(serverCollection)
            } else {
                // Local copy has unsaved changes, PRESERVE IT
                android.util.Log.w("OfflineFirstRepo", "SKIPPING server update for dirty collection: ${serverCollection.id} (preserving local changes)")
            }
        }
    }

    override suspend fun updateCollection(collection: CollectionEntity) {
        // CRITICAL: isDirty=true MUST set isSynced=false to maintain invariant
        collectionDao.upsertCollection(collection.copy(isDirty = true, isSynced = false))
        scheduleSync()
    }

    override suspend fun updateCollectionLastOpened(id: String) {
        val now = java.time.Instant.now().toString()
        collectionDao.updateLastOpened(id, now)
        // No sync needed for local preference? Or maybe sync last opened?
        // Let's not spam sync for just opening.
    }

    override suspend fun updateCollectionCounts(id: String) {
        collectionDao.updateCollectionCounts(id)
    }

    override suspend fun deleteCollection(id: String) {
        collectionDao.deleteCollection(id)
        scheduleSync()
    }

    override suspend fun searchCollections(query: String): List<CollectionEntity> = collectionDao.searchCollections(query)

    override fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    override fun getTasksByCollection(collectionId: String): Flow<List<TaskEntity>> = taskDao.getTasksByCollection(collectionId)

    override suspend fun getTaskById(id: String): TaskEntity? = taskDao.getTaskById(id)
    
    override suspend fun getTasksByCollectionId(collectionId: String): List<TaskEntity> {
        return taskDao.getTasksByCollectionSync(collectionId)
    }

    override suspend fun createTask(task: TaskEntity) {
        taskDao.upsertTask(task) 
        updateCollectionCounts(task.collectionId)
        if (task.status == "Done") {
            sendWidgetUpdateBroadcast()
        }
        scheduleSync()
    }

    override suspend fun insertTasks(tasks: List<TaskEntity>) {
        if (tasks.isEmpty()) return
        
        // CRITICAL: Only insert server data for items that are NOT dirty locally
        // This prevents overwriting unsaved local changes
        tasks.forEach { serverTask ->
            val localTask = taskDao.getTaskById(serverTask.id)
            
            if (localTask == null) {
                // New task from server, safe to insert
                android.util.Log.d("OfflineFirstRepo", "Inserting new task from server: ${serverTask.id}")
                taskDao.upsertTask(serverTask)
            } else if (!localTask.isDirty) {
                // Local copy is clean, safe to replace with server version
                android.util.Log.d("OfflineFirstRepo", "Updating clean task with server data: ${serverTask.id}")
                taskDao.upsertTask(serverTask)
            } else {
                // Local copy has unsaved changes, PRESERVE IT
                android.util.Log.w("OfflineFirstRepo", "SKIPPING server update for dirty task: ${serverTask.id} (preserving local changes)")
            }
        }
        
        tasks.map { it.collectionId }.distinct().forEach { collectionId ->
            updateCollectionCounts(collectionId)
        }
        sendWidgetUpdateBroadcast()
    }

    override suspend fun updateTask(task: TaskEntity) {
        // CRITICAL: isDirty=true MUST set isSynced=false to maintain invariant
        android.util.Log.d("OfflineFirstRepo", "updateTask: ${task.id} - Setting dirty=true, synced=false")
        taskDao.upsertTask(task.copy(isDirty = true, isSynced = false)) 
        if (task.status == "Done") {
            sendWidgetUpdateBroadcast()
        }
        scheduleSync()
    }

    override suspend fun toggleTaskStatus(taskId: String, currentStatus: String) {
        val newStatus = if (currentStatus == "Done") "Pending" else "Done"
        val completedAt = if (newStatus == "Done") Date() else null
        
        val task = taskDao.getTaskById(taskId)
        task?.let { 
            // CRITICAL: isDirty=true MUST set isSynced=false to maintain invariant
            android.util.Log.d("OfflineFirstRepo", "toggleTaskStatus: ${taskId} - Setting dirty=true, synced=false")
            val updated = it.copy(status = newStatus, completedAt = completedAt, isDirty = true, isSynced = false)
            taskDao.upsertTask(updated)
            updateCollectionCounts(it.collectionId)
            sendWidgetUpdateBroadcast()
        }
        scheduleSync()
    }

    override suspend fun deleteTask(taskId: String) {
        val task = taskDao.getTaskById(taskId)
        task?.let {
            taskDao.deleteTask(taskId)
            syncStateDao.recordDeletion(DeletedItemEntity(id = taskId, entityType = "task"))
            updateCollectionCounts(it.collectionId)
            sendWidgetUpdateBroadcast()
            scheduleSync()
        }
    }

    override fun getAllCompletedTasks(): Flow<List<TaskEntity>> = taskDao.getAllCompletedTasks()

    override suspend fun searchTasks(query: String): List<TaskEntity> = taskDao.searchTasks(query)

    override fun getDailyTarget(date: String): Flow<DailyTargetEntity?> = dailyTargetDao.getDailyTarget(date)

    override suspend fun createDailyTarget(target: DailyTargetEntity) {
        dailyTargetDao.insertDailyTarget(target)
    }

    override suspend fun getDirtyCollections(): List<CollectionEntity> = collectionDao.getDirtyCollections()

    override suspend fun getDirtyTasks(): List<TaskEntity> = taskDao.getDirtyTasks()

    override suspend fun markCollectionSynced(id: String) {
        collectionDao.markSynced(id)
    }

    override suspend fun markTaskSynced(id: String) {
        taskDao.markSynced(id)
    }

    override suspend fun clearAllData() {
        collectionDao.deleteAll()
        taskDao.deleteAll()
        dailyTargetDao.deleteAll()
        syncStateDao.deleteAll()
    }
}
