package com.aashu.privatesuite.domain.usecase

import com.aashu.privatesuite.data.local.entities.TaskEntity
import com.aashu.privatesuite.data.repository.OfflineFirstRepository
import javax.inject.Inject

class UpdateTaskUseCase @Inject constructor(
    private val repository: OfflineFirstRepository
) {
    suspend operator fun invoke(task: TaskEntity) {
        repository.updateTask(task)
    }
}
