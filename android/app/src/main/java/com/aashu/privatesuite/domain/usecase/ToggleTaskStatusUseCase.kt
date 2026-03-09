package com.aashu.privatesuite.domain.usecase

import com.aashu.privatesuite.data.repository.OfflineFirstRepository
import javax.inject.Inject

class ToggleTaskStatusUseCase @Inject constructor(
    private val repository: OfflineFirstRepository,
    private val syncRepository: com.aashu.privatesuite.data.repository.SyncRepository
) {
    suspend operator fun invoke(taskId: String, currentStatus: String) {
        repository.toggleTaskStatus(taskId, currentStatus)
        val task = repository.getTaskById(taskId)
        task?.let {
            repository.updateCollectionCounts(it.collectionId)
        }
        syncRepository.sync()
    }
}
