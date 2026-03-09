package com.aashu.privatesuite.domain.usecase

import com.aashu.privatesuite.data.repository.OfflineFirstRepository
import javax.inject.Inject

class UpdateCollectionLastOpenedUseCase @Inject constructor(
    private val repository: OfflineFirstRepository
) {
    suspend operator fun invoke(collectionId: String) {
        repository.updateCollectionLastOpened(collectionId)
    }
}
