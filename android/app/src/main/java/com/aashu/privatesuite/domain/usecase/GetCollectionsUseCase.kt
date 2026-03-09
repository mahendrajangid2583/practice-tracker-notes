package com.aashu.privatesuite.domain.usecase

import com.aashu.privatesuite.data.local.entities.CollectionEntity
import com.aashu.privatesuite.data.repository.OfflineFirstRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCollectionsUseCase @Inject constructor(
    private val repository: OfflineFirstRepository
) {
    operator fun invoke(): Flow<List<com.aashu.privatesuite.data.local.entities.CollectionWithCounts>> {
        return repository.getCollectionsWithCounts()
    }
}
