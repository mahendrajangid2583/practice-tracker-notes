package com.aashu.privatesuite.domain.usecase

import com.aashu.privatesuite.data.local.entities.TaskEntity
import com.aashu.privatesuite.data.repository.OfflineFirstRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTasksUseCase @Inject constructor(
    private val repository: OfflineFirstRepository
) {
    operator fun invoke(collectionId: String): Flow<List<TaskEntity>> {
        return repository.getTasksByCollection(collectionId)
    }
}
