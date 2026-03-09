package com.aashu.privatesuite.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import com.aashu.privatesuite.domain.usecase.CreateCollectionUseCase
import com.aashu.privatesuite.domain.usecase.CalculateStreakUseCase
import com.aashu.privatesuite.domain.usecase.GetCollectionsUseCase
import com.aashu.privatesuite.domain.usecase.ToggleTaskStatusUseCase
import com.aashu.privatesuite.domain.usecase.DeleteTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: com.aashu.privatesuite.data.repository.OfflineFirstRepository,
    private val dailyTargetDao: com.aashu.privatesuite.data.local.dao.DailyTargetDao,
    private val taskDao: com.aashu.privatesuite.data.local.dao.TaskDao,
    private val createCollectionUseCase: CreateCollectionUseCase,
    private val calculateStreakUseCase: CalculateStreakUseCase,
    private val toggleTaskStatusUseCase: ToggleTaskStatusUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    val syncRepository: com.aashu.privatesuite.data.repository.SyncRepository
) : ViewModel() {

    val isSyncing = syncRepository.isSyncing
    val syncError = syncRepository.syncError // Expose error flow

    private val _filter = kotlinx.coroutines.flow.MutableStateFlow("ALL")
    val filter = _filter.asStateFlow()

    // In-memory aggregation for 100% accuracy
    val collections: StateFlow<List<com.aashu.privatesuite.data.local.entities.CollectionWithCounts>> = 
        kotlinx.coroutines.flow.combine(
            repository.getAllCollections(),
            repository.getAllTasks(),
            _filter
        ) { collections, tasks, filterType ->
            // Filter collections first
            val filteredCollections = if (filterType == "ALL") collections else collections.filter { it.type.equals(filterType, ignoreCase = true) }
            
            // Map to CollectionWithCounts by counting tasks in memory
            filteredCollections.map { collection ->
                // Use robust matching: trim + ignore case
                val collectionTasks = tasks.filter { 
                    it.collectionId.trim().equals(collection.id.trim(), ignoreCase = true) 
                }
                
                val total = collectionTasks.size
                val completed = collectionTasks.count { it.status == "Done" }
                
                // Debug log (can be removed later)
                if (total > 0 && collection.title == "System Design") {
                     android.util.Log.d("DashboardVM", "Collection: ${collection.title} (${collection.id}) has $total tasks. IDs: ${collectionTasks.take(5).map { it.id }}")
                } else if (collection.title == "System Design") {
                     android.util.Log.d("DashboardVM", "Collection: ${collection.title} (${collection.id}) has 0 tasks. Total tasks in DB: ${tasks.size}. Sample task col ID: ${tasks.firstOrNull()?.collectionId}")
                }
                
                com.aashu.privatesuite.data.local.entities.CollectionWithCounts(
                    collection = collection,
                    actualTotal = total,
                    actualCompleted = completed
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    // Daily Targets Logic
    private val today = LocalDate.now().toString()

    init {
        viewModelScope.launch {
            // Fetch latest targets from server on init
            syncRepository.fetchDailyTargets(today)
            // Also trigger full sync to ensure everything is up to date
            syncRepository.sync()
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val dailyTargets: StateFlow<List<com.aashu.privatesuite.data.local.entities.TaskEntity>> = dailyTargetDao.getDailyTarget(today)
        .flatMapLatest { target: com.aashu.privatesuite.data.local.entities.DailyTargetEntity? ->
            if (target == null || target.taskIds.isEmpty()) {
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                taskDao.getTasksByIds(target.taskIds)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val streakFlow: StateFlow<Int> = calculateStreakUseCase()
        .map { it.currentStreak }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0
        )

    fun setFilter(newFilter: String) {
        _filter.value = newFilter
    }

    fun triggerSync() {
        viewModelScope.launch {
            syncRepository.sync()
        }
    }

    fun createCollection(title: String, type: String, theme: String) {
        viewModelScope.launch {
            createCollectionUseCase(title, type, theme)
        }
    }

    fun toggleTask(taskId: String, currentStatus: String) {
        viewModelScope.launch {
            toggleTaskStatusUseCase(taskId, currentStatus)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            deleteTaskUseCase(taskId)
        }
    }
}

