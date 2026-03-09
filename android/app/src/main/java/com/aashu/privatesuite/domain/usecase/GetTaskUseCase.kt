package com.aashu.privatesuite.domain.usecase

import com.aashu.privatesuite.data.local.entities.TaskEntity
import com.aashu.privatesuite.data.repository.OfflineFirstRepository
import javax.inject.Inject

class GetTaskUseCase @Inject constructor(
    private val repository: OfflineFirstRepository
) {
    suspend operator fun invoke(taskId: String): TaskEntity? {
        return repository.getTaskById(taskId)
    }
}
