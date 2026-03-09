package com.aashu.privatesuite.presentation.note

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aashu.privatesuite.data.local.entities.TaskEntity
import com.aashu.privatesuite.data.repository.OfflineFirstRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadNoteViewModel @Inject constructor(
    private val repository: OfflineFirstRepository
) : ViewModel() {

    private val _task = mutableStateOf<TaskEntity?>(null)
    val task: State<TaskEntity?> = _task

    fun loadTask(taskId: String) {
        viewModelScope.launch {
            _task.value = repository.getTaskById(taskId)
        }
    }

    suspend fun getTask(taskId: String): TaskEntity? {
        return repository.getTaskById(taskId)
    }
}
