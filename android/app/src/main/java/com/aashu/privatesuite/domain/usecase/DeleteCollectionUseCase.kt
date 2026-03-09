package com.aashu.privatesuite.domain.usecase

import com.aashu.privatesuite.data.repository.OfflineFirstRepository
import javax.inject.Inject

class DeleteCollectionUseCase @Inject constructor(
    private val repository: OfflineFirstRepository,
    private val syncRepository: com.aashu.privatesuite.data.repository.SyncRepository
) {
    suspend operator fun invoke(collectionId: String) {
        repository.deleteCollection(collectionId)
        syncRepository.sync()
    }
}
