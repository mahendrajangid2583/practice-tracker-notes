package com.aashu.privatesuite.domain.usecase

import com.aashu.privatesuite.data.local.entities.CollectionEntity
import com.aashu.privatesuite.data.repository.OfflineFirstRepository
import javax.inject.Inject

class GetCollectionUseCase @Inject constructor(
    private val repository: OfflineFirstRepository
) {
    suspend operator fun invoke(id: String): CollectionEntity? {
        return repository.getCollectionById(id)
    }
}
