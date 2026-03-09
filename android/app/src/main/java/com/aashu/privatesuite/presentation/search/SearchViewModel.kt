package com.aashu.privatesuite.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aashu.privatesuite.domain.usecase.SearchResults
import com.aashu.privatesuite.domain.usecase.SearchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchUseCase: SearchUseCase,
    private val toggleTaskStatusUseCase: com.aashu.privatesuite.domain.usecase.ToggleTaskStatusUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow(SearchResults())
    val results: StateFlow<SearchResults> = _results.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // Debounce
            _results.value = searchUseCase(newQuery)
        }
    }

    fun toggleTask(taskId: String, currentStatus: String) {
        viewModelScope.launch {
            toggleTaskStatusUseCase(taskId, currentStatus)
            // Ideally we refresh results or the flow updates itself if searchUseCase returns a Flow (it returns List currently).
            // Since SearchUseCase returns a snapshot List, we need to re-run search or manually update the list in _results.
            // For simplicity/robustness, let's re-run search silently or update local state.
            // But search is debounced.
            // Better: update the local list immediately for UI responsiveness.
            
            val currentResults = _results.value
            val updatedTasks = currentResults.tasks.map { task ->
                if (task.id == taskId) {
                    val newStatus = if (currentStatus == "Done") "Pending" else "Done"
                    task.copy(status = newStatus, isDirty = true, isSynced = false)
                } else {
                    task
                }
            }
            _results.value = currentResults.copy(tasks = updatedTasks)
        }
    }
}
