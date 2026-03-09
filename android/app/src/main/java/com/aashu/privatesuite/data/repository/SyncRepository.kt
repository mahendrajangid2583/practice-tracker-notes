package com.aashu.privatesuite.data.repository

import kotlinx.coroutines.flow.StateFlow

interface SyncRepository {
    val isSyncing: StateFlow<Boolean>
    val syncError: StateFlow<String?>

    suspend fun refreshCollections()
    suspend fun refreshCollection(id: String)
    suspend fun sync()
    
    suspend fun fetchDailyTargets(date: String)
    suspend fun fetchActivityLog()

    // Target Settings
    suspend fun fetchTargetSettings()
    suspend fun pushTargetSettings(slots: List<com.aashu.privatesuite.domain.model.TargetSlot>)
    fun getLocalTargetSlots(): List<com.aashu.privatesuite.domain.model.TargetSlot>
}
