package com.aashu.privatesuite.domain.usecase

import com.aashu.privatesuite.data.local.entities.CollectionEntity
import com.aashu.privatesuite.data.repository.OfflineFirstRepository
import javax.inject.Inject

class UpdateCollectionUseCase @Inject constructor(
    private val repository: OfflineFirstRepository
) {
    suspend operator fun invoke(collection: CollectionEntity) {
        repository.updateCollection(collection)
    }
}
