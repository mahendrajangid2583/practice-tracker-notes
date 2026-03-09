package com.aashu.privatesuite.data.repository

import com.aashu.privatesuite.data.local.SyncPreferences
import com.aashu.privatesuite.data.remote.PrivateSuiteApi
import com.aashu.privatesuite.data.remote.dto.toDto
import com.aashu.privatesuite.data.remote.dto.toEntity
import com.aashu.privatesuite.data.remote.dto.SyncPushRequest
import com.aashu.privatesuite.data.remote.dto.SyncChanges
import com.aashu.privatesuite.data.remote.dto.EntityChanges
import com.aashu.privatesuite.data.remote.dto.SyncPullRequest
import com.aashu.privatesuite.data.remote.dto.toDomain
import com.aashu.privatesuite.data.remote.dto.toDto

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val localRepository: OfflineFirstRepository,
    private val api: PrivateSuiteApi,
    private val syncPreferences: SyncPreferences
) : SyncRepository {
    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing = _isSyncing.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    override val syncError = _syncError.asStateFlow()

    private suspend fun pushDirtyData(): Boolean {
        try {
            val dirtyCollections = localRepository.getDirtyCollections()
            val dirtyTasks = localRepository.getDirtyTasks()
            
            if (dirtyCollections.isNotEmpty() || dirtyTasks.isNotEmpty()) {
                Log.d("SyncRepository", "===== PUSH DIRTY DATA START =====")
                Log.d("SyncRepository", "Pushing changes: ${dirtyCollections.size} collections, ${dirtyTasks.size} tasks")
                
                // Log each dirty task for debugging
                dirtyTasks.forEach { task ->
                    // SAFETY GUARD: Detect invalid state
                    if (task.isDirty && task.isSynced) {
                        Log.e("SyncRepository", "❌ INVALID STATE DETECTED: Task ${task.id} is BOTH dirty AND synced! This should never happen.")
                        Log.e("SyncRepository", "  Title: ${task.title.take(30)}...")
                        Log.e("SyncRepository", "  This task will be pushed but needs investigation.")
                    }
                    Log.d("SyncRepository", "  Dirty task: ${task.id} - ${task.title.take(30)}... (dirty=${task.isDirty}, synced=${task.isSynced})")
                }
                
                val request = SyncPushRequest(
                    changes = SyncChanges(
                        collections = EntityChanges(
                             updated = dirtyCollections.filter { !it.isDeleted }.map { it.toDto() },
                             deleted = dirtyCollections.filter { it.isDeleted }.map { it.id }
                        ),
                        tasks = EntityChanges(
                             updated = dirtyTasks.filter { !it.isSynced && !it.isDeleted }.map { it.toDto() },
                             deleted = dirtyTasks.filter { it.isDeleted }.map { it.id }
                        )
                    )
                )

                Log.d("SyncRepository", "Calling API syncPush...")
                val response = api.syncPush(request)
                Log.d("SyncRepository", "API response: success=${response.success}, applied=${response.results?.applied}")
                if (response.success) {
                    // CRITICAL: Check for individual item errors
                    val errors = response.results?.errors ?: emptyList()
                    val failedIds = errors.map { it.id }.toSet()
                    
                    if (errors.isNotEmpty()) {
                        Log.e("SyncRepository", "Push had ${errors.size} errors:")
                        errors.forEach { err ->
                            Log.e("SyncRepository", "  ${err.type} ${err.id}: ${err.error}")
                        }
                    }
                    
                    // Only mark items as synced if they didn't have errors
                    dirtyCollections.forEach { 
                        if (it.id !in failedIds) {
                            localRepository.markCollectionSynced(it.id)
                        } else {
                            Log.e("SyncRepository", "NOT marking collection ${it.id} as synced due to server error")
                        }
                    }
                    dirtyTasks.forEach { task ->
                        if (task.id !in failedIds) {
                            Log.d("SyncRepository", "✓ Marking task ${task.id} as synced (dirty→false, synced→true)")
                            localRepository.markTaskSynced(task.id)
                        } else {
                            Log.e("SyncRepository", "NOT marking task ${task.id} as synced due to server error")
                        }
                    }
                    
                    if (errors.isNotEmpty()) {
                        _syncError.value = "Sync completed with ${errors.size} errors. Check logs."
                        Log.d("SyncRepository", "Push completed with errors.")
                        return false  // Return false so we don't proceed with pull
                    }
                    
                    Log.d("SyncRepository", "Push successful.")
                    return true
                } else {
                    Log.e("SyncRepository", "Push failed.")
                    _syncError.value = "Sync Push failed"
                    return false
                }
            }
            return true // Nothing to push, consider success
        } catch (e: Exception) {
            Log.e("SyncRepository", "Push failed with exception", e)
            _syncError.value = "Sync Push error: ${e.message}"
            return false
        }
    }

    override suspend fun refreshCollections() {
        try {
            if (!pushDirtyData()) {
                Log.e("SyncRepository", "Aborting refreshCollections because Push failed. Preserving local data.")
                return 
            }
            
            Log.d("SyncRepository", "Fetching collections from API...")
            val response = api.getCollections()
            val entities = response.collections.map { it.toEntity() }
            
            Log.d("SyncRepository", "Fetched ${entities.size} collections. Saving to local DB...")
            localRepository.insertCollections(entities)
            Log.d("SyncRepository", "Collections saved successfully.")
        } catch (e: Exception) {
            Log.e("SyncRepository", "Error fetching collections", e)
            _syncError.value = "Failed to refresh collections: ${e.message}"
        }
    }

    override suspend fun refreshCollection(id: String) {
        try {
            if (!pushDirtyData()) {
                Log.e("SyncRepository", "Aborting refreshCollection because Push failed. Preserving local data.")
                return
            }
            
            Log.d("SyncRepository", "Refreshing collection $id...")
            val collectionDto = api.getCollectionDetails(id)
            val collectionEntity = collectionDto.toEntity()
            
            localRepository.insertCollections(listOf(collectionEntity))
            
            collectionDto.tasks?.let { serverTasks ->
                Log.d("SyncRepository", "Fetched ${serverTasks.size} tasks for collection $id")
                val taskEntities = serverTasks.map { it.toEntity() }
                localRepository.insertTasks(taskEntities)
                
                // CRITICAL: Handle deletions - remove local tasks that no longer exist on server
                val serverTaskIds = serverTasks.map { it.id }.toSet()
                val localTasks = localRepository.getTasksByCollectionId(id)
                localTasks.forEach { localTask ->
                    // Only delete clean tasks that are missing from server
                    // NEVER delete dirty tasks (unsaved local changes)
                    if (!localTask.isDirty && localTask.id !in serverTaskIds) {
                        Log.w("SyncRepository", "Task ${localTask.id} exists locally but not on server. Deleting locally (was deleted on server).")
                        localRepository.deleteTask(localTask.id)
                    } else if (localTask.isDirty && localTask.id !in serverTaskIds) {
                        Log.e("SyncRepository", "WARNING: Task ${localTask.id} is dirty locally but missing on server. PRESERVING local version.")
                    }
                }
            }
            
            Log.d("SyncRepository", "Collection $id refreshed successfully.")
        } catch (e: Exception) {
            Log.e("SyncRepository", "Failed to refresh collection $id", e)
        }
    }

    override suspend fun sync() {
        if (_isSyncing.value) return
        _isSyncing.value = true
        _syncError.value = null
        
        try {
            // 1. Push changes
            if (!pushDirtyData()) {
                 Log.e("SyncRepository", "Aborting Sync because Push failed. Preserving local data.")
                 return
            }

            // 2. Pull changes
            val lastSync = syncPreferences.getLastSyncTimestamp()
            val pullResponse = api.syncPull(SyncPullRequest(lastSync))
            
            if (pullResponse.changes.collections.updated.isNotEmpty()) {
                val entities = pullResponse.changes.collections.updated.map { it.toEntity() }
                localRepository.insertCollections(entities)
            }
             // Handle tasks pull similarly
             // Handle deleted collections
             if (pullResponse.changes.collections.deleted.isNotEmpty()) {
                 pullResponse.changes.collections.deleted.forEach { id ->
                     try {
                         localRepository.deleteCollection(id)
                         localRepository.markCollectionSynced(id)
                     } catch (e: Exception) {
                         Log.e("SyncRepository", "Failed to delete collection $id", e)
                     }
                 }
             }

             if (pullResponse.changes.tasks.updated.isNotEmpty()) {
                 pullResponse.changes.tasks.updated.forEach { taskDto ->
                     try {
                        localRepository.createTask(taskDto.toEntity()) // createTask usually handles upsert
                     } catch (e: Exception) {
                         Log.e("SyncRepository", "Failed to insert task ${taskDto.id} (Collection: ${taskDto.collectionId})", e)
                     }
                 }
             }

             // Handle deleted tasks
             if (pullResponse.changes.tasks.deleted.isNotEmpty()) {
                 pullResponse.changes.tasks.deleted.forEach { id ->
                     try {
                         localRepository.deleteTask(id)
                         localRepository.markTaskSynced(id)
                     } catch (e: Exception) {
                         Log.e("SyncRepository", "Failed to delete task $id", e)
                     }
                 }
             }

            // 3. Pull Target Settings
            fetchTargetSettings()

            // 4. Pull Daily Targets
            val today = java.time.LocalDate.now().toString()
            fetchDailyTargets(today)

            // 5. Activity Log (Streak)
            fetchActivityLog()

             // Update timestamp
             syncPreferences.setLastSyncTimestamp(System.currentTimeMillis())

        } catch (e: Exception) {
            // Handle error
            Log.e("SyncRepository", "Sync failed", e)
            _syncError.value = "Sync failed: ${e.message}"
            if (e is retrofit2.HttpException && e.code() == 401) {
                Log.e("SyncRepository", "CRITICAL: 401 Unauthorized during sync. Token might be expired or invalid.")
            }
        } finally {
            _isSyncing.value = false
        }
    }

    override suspend fun fetchActivityLog() {
        try {
            val logs = api.getActivityLog()
            val json = com.google.gson.Gson().toJson(logs)
            syncPreferences.setActivityLog(json)
            Log.d("SyncRepository", "Activity log fetched: ${logs.size} entries")
        } catch (e: Exception) {
            Log.e("SyncRepository", "Failed to fetch activity log", e)
        }
    }

    override suspend fun fetchDailyTargets(date: String) {
        try {
            Log.d("SyncRepository", "Fetching daily targets for $date...")
            val taskDtos = api.getDailyTargets(date)
            
            if (taskDtos.isNotEmpty()) {
                // 1. Save Tasks (Upsert) using localRepository helper
                // We'll map to entity and use insertTask/insertTasks if available or iterate
                val taskEntities = taskDtos.map { it.toEntity() }
                // We don't have insertTasks exposed on localRepository in the interface shown in earlier logs,
                // but checking OfflineFirstRepositoryImpl might show it.
                // Assuming we can use loop or if bulk insert exists.
                // Let's check OfflineFirstRepository interface.
                // For now, iterate
                taskEntities.forEach { localRepository.createTask(it) }

                // 2. Save DailyTargetEntity
                val targetEntity = com.aashu.privatesuite.data.local.entities.DailyTargetEntity(
                    id = java.util.UUID.randomUUID().toString(), // Helper function handles UUID
                    date = date,
                    taskIds = taskEntities.map { it.id },
                    createdAt = java.util.Date(),
                    isSynced = true,
                    isDirty = false
                )
                localRepository.createDailyTarget(targetEntity)
                Log.d("SyncRepository", "Daily targets saved: ${taskEntities.size} tasks")
            } else {
                // Handle empty? Maybe clear existing for date if any?
                // The requirements say "Server is source of truth".
                // If server returns empty, we should probably reflect that?
                // But typically we just don't overwrite if empty unless we want to clear.
                // Let's just log for now.
                Log.d("SyncRepository", "No daily targets returned from server.")
            }
        } catch (e: Exception) {
            Log.e("SyncRepository", "Failed to fetch daily targets", e)
             // Don't throw, just log
        }
    }

    override suspend fun fetchTargetSettings() {
        try {
            val response = api.getTargetSettings()
            val slots = response.slots.map { it.toDomain() }
            val json = com.google.gson.Gson().toJson(slots)
            syncPreferences.setTargetSlots(json)
            Log.d("SyncRepository", "Target settings fetched: ${slots.size} slots. Saved to prefs.")
        } catch (e: Exception) {
            Log.e("SyncRepository", "Failed to fetch target settings", e)
            // Non-critical, don't throw
        }
    }

    override suspend fun pushTargetSettings(slots: List<com.aashu.privatesuite.domain.model.TargetSlot>) {
        try {
            // Optimistic update locally
            val json = com.google.gson.Gson().toJson(slots)
            syncPreferences.setTargetSlots(json)

            // Push to API
            val dto = com.aashu.privatesuite.data.remote.dto.TargetSettingsDto(
                slots = slots.map { it.toDto() }
            )
            api.updateTargetSettings(dto)
            Log.d("SyncRepository", "Target settings pushed successfully.")
        } catch (e: Exception) {
            Log.e("SyncRepository", "Failed to push target settings", e)
            throw e // Rethrow so UI knows it failed
        }
    }

    override fun getLocalTargetSlots(): List<com.aashu.privatesuite.domain.model.TargetSlot> {
        return try {
            val json = syncPreferences.getTargetSlots()
            val type = object : com.google.gson.reflect.TypeToken<List<com.aashu.privatesuite.domain.model.TargetSlot>>() {}.type
            com.google.gson.Gson().fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

