package com.aashu.privatesuite.domain.usecase

import com.aashu.privatesuite.data.local.entities.TaskEntity
import com.aashu.privatesuite.data.repository.OfflineFirstRepository
import javax.inject.Inject

class CreateTaskUseCase @Inject constructor(
    private val repository: OfflineFirstRepository,
    private val syncRepository: com.aashu.privatesuite.data.repository.SyncRepository
) {
    suspend operator fun invoke(task: TaskEntity) {
        repository.createTask(task)
        repository.updateCollectionCounts(task.collectionId)
        syncRepository.sync()
    }
}
