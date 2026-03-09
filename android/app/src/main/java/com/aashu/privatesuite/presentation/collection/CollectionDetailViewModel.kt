package com.aashu.privatesuite.presentation.collection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aashu.privatesuite.data.local.entities.TaskEntity
import com.aashu.privatesuite.domain.usecase.GetTasksUseCase
import com.aashu.privatesuite.domain.usecase.ToggleTaskStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import com.aashu.privatesuite.domain.usecase.GetCollectionUseCase
import com.aashu.privatesuite.domain.usecase.DeleteCollectionUseCase
import com.aashu.privatesuite.domain.usecase.UpdateCollectionUseCase
import com.aashu.privatesuite.domain.usecase.DeleteTaskUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CollectionDetailViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val getTaskUseCase: com.aashu.privatesuite.domain.usecase.GetTaskUseCase,
    private val toggleTaskStatusUseCase: ToggleTaskStatusUseCase,
    private val deleteCollectionUseCase: DeleteCollectionUseCase,
    private val getCollectionUseCase: GetCollectionUseCase,
    private val updateCollectionUseCase: UpdateCollectionUseCase,
    private val updateCollectionLastOpenedUseCase: com.aashu.privatesuite.domain.usecase.UpdateCollectionLastOpenedUseCase,
    private val createTaskUseCase: com.aashu.privatesuite.domain.usecase.CreateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val syncRepository: com.aashu.privatesuite.data.repository.SyncRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val collectionId: String = checkNotNull(savedStateHandle["collectionId"])

    private val _uiState = MutableStateFlow<CollectionUiState>(CollectionUiState.Loading)
    val uiState: StateFlow<CollectionUiState> = _uiState.asStateFlow()

    val syncError = syncRepository.syncError

    init {
        loadCollection(collectionId)
    }

    fun loadCollection(collectionId: String) {
        viewModelScope.launch {
            try {
                // Update Last Opened (MRU) locally immediately
                launch {
                     updateCollectionLastOpenedUseCase(collectionId)
                }

                // Trigger refresh from server to ensure fresh data
                launch {
                    try {
                        syncRepository.refreshCollection(collectionId)
                    } catch (e: Exception) {
                         android.util.Log.e("CollectionDetailVM", "Failed to refresh collection", e)
                    }
                }

                val collection = getCollectionUseCase(collectionId)
                if (collection == null) {
                    _uiState.value = CollectionUiState.Error("Collection not found")
                    return@launch
                }
                
                getTasksUseCase(collectionId)
                    .catch { e ->
                        _uiState.value = CollectionUiState.Error("Failed to load tasks: ${e.message}")
                    }
                    .collect { tasks ->
                        // Log.d("CollectionDetailVM", "Loaded ${tasks.size} tasks")
                        _uiState.value = CollectionUiState.Success(collection, tasks)
                    }
            } catch (e: Exception) {
                _uiState.value = CollectionUiState.Error("An error occurred: ${e.message}")
            }
        }
    }

    fun updateCollectionTitle(newTitle: String) {
        val currentState = _uiState.value
        if (currentState is CollectionUiState.Success) {
            viewModelScope.launch {
                val updatedCollection = currentState.collection.copy(title = newTitle, isDirty = true, isSynced = false)
                updateCollectionUseCase(updatedCollection)
                // UI update via flow automatically
            }
        }
    }

    fun addTask(title: String, link: String? = null, difficulty: String = "Medium") {
        viewModelScope.launch {
            val newTask = TaskEntity(
                collectionId = collectionId,
                title = title,
                link = link,
                difficulty = difficulty,
                platform = "Other" // Default for quick add
            )
            createTaskUseCase(newTask)
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

    fun deleteCollection(onSuccess: () -> Unit) {
        viewModelScope.launch {
            deleteCollectionUseCase(collectionId)
            onSuccess()
        }
    }

    suspend fun getTask(taskId: String): TaskEntity? {
        return getTaskUseCase(taskId)
    }
}

sealed interface CollectionUiState {
    data object Loading : CollectionUiState
    data class Success(
        val collection: com.aashu.privatesuite.data.local.entities.CollectionEntity,
        val tasks: List<TaskEntity>
    ) : CollectionUiState
    data class Error(val message: String) : CollectionUiState
}
