package com.aashu.privatesuite.presentation.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aashu.privatesuite.data.repository.OfflineFirstRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val getTaskUseCase: com.aashu.privatesuite.domain.usecase.GetTaskUseCase,
    private val updateTaskUseCase: com.aashu.privatesuite.domain.usecase.UpdateTaskUseCase,
    private val searchUseCase: com.aashu.privatesuite.domain.usecase.SearchUseCase,
    savedStateHandle: androidx.lifecycle.SavedStateHandle
) : ViewModel() {

    private val taskId: String = checkNotNull(savedStateHandle["taskId"])

    private val _task = kotlinx.coroutines.flow.MutableStateFlow<com.aashu.privatesuite.data.local.entities.TaskEntity?>(null)
    val task: kotlinx.coroutines.flow.StateFlow<com.aashu.privatesuite.data.local.entities.TaskEntity?> = _task.asStateFlow()

    init {
        loadTask()
    }

    private fun loadTask() {
        viewModelScope.launch {
            _task.value = getTaskUseCase(taskId)
        }
    }

    fun updateTitle(newTitle: String) {
        val currentTask = _task.value ?: return
        val updatedTask = currentTask.copy(title = newTitle)
        _task.value = updatedTask
        // Auto-save removed to prevent sync spam. User must save explicitly or on exit.
    }

    fun updateStatus(newStatus: String) {
        val currentTask = _task.value ?: return
        val updatedTask = currentTask.copy(status = newStatus)
        _task.value = updatedTask
        // Status toggle usually implies immediate action, but for editor consistency we can wait.
        // However, if user toggles and forcing save is expected, we can leave it.
        // Let's defer to saveTask for consistency in Editor.
    }

    fun updateNotes(htmlContent: String) {
        val currentTask = _task.value ?: return

        // Ultimate Robust Mention Processing:
        // Match ANY <a> tag, capture href and content.
        // Decode href to handle HtmlCompat escaping (%2F, %3F etc).
        // Check if it matches mention pattern.
        
        // Regex matches: <a ... href="..." ...>content</a>
        // Handles single/double quotes.
        val anchorRegex = Regex("""<a\s+[^>]*href=(["'])(.*?)\1[^>]*>(.*?)</a>""", RegexOption.IGNORE_CASE)
        
        val processedHtml = anchorRegex.replace(htmlContent) { matchResult ->
            val rawUrl = matchResult.groupValues[2]
            val content = matchResult.groupValues[3]
            
            try {
                // Decode URL to handle escaping (e.g. %2Fcollection, %3Ffocus)
                // This is CRITICAL because HtmlCompat often escapes '?' as '%3F', breaking standard parsing.
                val decodedUrl = java.net.URLDecoder.decode(rawUrl, "UTF-8")
                
                if (decodedUrl.contains("/collection/") && decodedUrl.contains("focus=")) {
                     // Check if it really is a mention link
                     val fullUrlStr = if (decodedUrl.startsWith("http")) decodedUrl else "https://dummy.com$decodedUrl"
                     val uri = android.net.Uri.parse(fullUrlStr)
                     
                     val taskId = uri.getQueryParameter("focus")
                     
                     if (taskId != null) {
                         val label = content.removePrefix("@")
                         // Return fully attributed TipTap mention HTML
                         // We use the ORIGINAL rawUrl for href to match original encoding usage, 
                         // but inject the clean taskId and label into data attributes.
                         // This ensures the TipTap Mention extension recognizes the node.
                         
                         """<a href="$rawUrl" data-id="$taskId" data-label="$label" data-type="mention" class="mention text-amber-400 font-bold bg-amber-900/20 px-1 rounded decoration-clone cursor-pointer hover:underline" target="_blank" rel="noopener noreferrer">$content</a>"""
                     } else {
                         matchResult.value
                     }
                } else {
                    matchResult.value
                }
            } catch (e: Exception) {
                matchResult.value
            }
        }

        if (currentTask.notes == processedHtml) return

        val updatedTask = currentTask.copy(notes = processedHtml)
        _task.value = updatedTask
    }

    fun updateDifficulty(difficulty: String) {
        val currentTask = _task.value ?: return
        val updatedTask = currentTask.copy(difficulty = difficulty)
        _task.value = updatedTask
    }

    fun updateLink(link: String) {
        val currentTask = _task.value ?: return
        val updatedTask = currentTask.copy(link = link)
        _task.value = updatedTask
    }

    fun updateRating(rating: Int?) {
        val currentTask = _task.value ?: return
        val updatedTask = currentTask.copy(rating = rating)
        _task.value = updatedTask
    }

    fun saveTask() {
        val currentTask = _task.value ?: return
        android.util.Log.d("EditorViewModel", "saveTask called. Title: ${currentTask.title}, Notes Length: ${currentTask.notes.length}")
        updateTask(currentTask)
    }

    private fun updateTask(task: com.aashu.privatesuite.data.local.entities.TaskEntity) {
        viewModelScope.launch {
            android.util.Log.d("EditorViewModel", "updateTask launching coroutine for task: ${task.id}")
            updateTaskUseCase(task)
            android.util.Log.d("EditorViewModel", "updateTaskUseCase executed")
        }
    }

    suspend fun searchTasks(query: String): List<com.aashu.privatesuite.data.local.entities.TaskEntity> {
        val results = searchUseCase(query)
        return results.tasks
    }
}
