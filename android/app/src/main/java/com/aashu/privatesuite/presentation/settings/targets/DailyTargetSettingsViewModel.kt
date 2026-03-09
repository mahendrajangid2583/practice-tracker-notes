package com.aashu.privatesuite.presentation.settings.targets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aashu.privatesuite.data.local.entities.CollectionEntity
import com.aashu.privatesuite.data.repository.OfflineFirstRepository
import com.aashu.privatesuite.data.repository.SyncRepository
import com.aashu.privatesuite.domain.model.TargetSlot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DailyTargetSettingsViewModel @Inject constructor(
    private val syncRepository: SyncRepository,
    private val repository: OfflineFirstRepository
) : ViewModel() {

    private val _slots = MutableStateFlow<List<TargetSlot>>(emptyList())
    val slots: StateFlow<List<TargetSlot>> = _slots

    val collections: StateFlow<List<CollectionEntity>> = repository.getAllCollections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val isSyncing = syncRepository.isSyncing

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // 1. Load local immediately
            _slots.value = syncRepository.getLocalTargetSlots()
            
            // 2. Refresh from server silently
            try {
                syncRepository.fetchTargetSettings()
                // Update local state after fetch
                _slots.value = syncRepository.getLocalTargetSlots()
            } catch (e: Exception) {
                // Ignore fetch error, just stick with local
                android.util.Log.e("DailyTargetSettingsVM", "Failed to fetch settings", e)
            }
        }
    }
    
    // Refresh if needed
    fun refreshSlots() {
         viewModelScope.launch {
             try {
                 syncRepository.fetchTargetSettings()
                 val newSlots = syncRepository.getLocalTargetSlots()
                 _slots.value = newSlots
                 android.util.Log.d("DailyTargetSettingsVM", "Refreshed slots: ${newSlots.size}")
             } catch (e: Exception) {
                 android.util.Log.e("DailyTargetSettingsVM", "Failed to refresh slots", e)
             }
         }
    }

    fun addSlot() {
        val current = _slots.value
        val firstCollection = collections.value.firstOrNull()?.id
        val newSlot = TargetSlot(
             id = java.util.UUID.randomUUID().toString(),
             collectionIds = if (firstCollection != null) listOf(firstCollection) else emptyList(),
             label = "Slot ${current.size + 1}"
        )
        _slots.value = current + newSlot
    }

    fun removeSlot(slotId: String) {
        _slots.value = _slots.value.filter { it.id != slotId }
    }

    fun toggleCollection(slotId: String, collectionId: String) {
        _slots.value = _slots.value.map { slot ->
            if (slot.id == slotId) {
                val currentIds = slot.collectionIds
                val newIds = if (currentIds.contains(collectionId)) {
                    currentIds - collectionId
                } else {
                    currentIds + collectionId
                }
                slot.copy(collectionIds = newIds)
            } else {
                slot
            }
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            // Enforce online only - basic check via repository/catch
            // ideally we check network state, but for now we try-catch the API call
            try {
                // TODO: Add strict isOnline check if feasible, for now relying on SyncRepo to fail if needed or we assume user is online as requested
                
                val validSlots = _slots.value.filter { it.collectionIds.isNotEmpty() }
                // This must call API directly or sync repo must push immediately
                syncRepository.pushTargetSettings(validSlots)
                
                // Show success?
            } catch (e: Exception) {
                // Show error "Must be online to save settings"
                android.util.Log.e("DailyTargetSettingsVM", "Failed to save", e)
            }
        }
    }
}
