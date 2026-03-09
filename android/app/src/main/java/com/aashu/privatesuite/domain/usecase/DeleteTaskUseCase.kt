package com.aashu.privatesuite.domain.usecase

import com.aashu.privatesuite.data.repository.OfflineFirstRepository
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(
    private val repository: OfflineFirstRepository,
    private val syncRepository: com.aashu.privatesuite.data.repository.SyncRepository
) {
    suspend operator fun invoke(taskId: String) {
        val task = repository.getTaskById(taskId)
        repository.deleteTask(taskId)
        task?.let {
            repository.updateCollectionCounts(it.collectionId)
        }
        syncRepository.sync()
    }
}
