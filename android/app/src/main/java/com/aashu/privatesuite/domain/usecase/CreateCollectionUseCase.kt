package com.aashu.privatesuite.domain.usecase

import com.aashu.privatesuite.data.local.entities.CollectionEntity
import com.aashu.privatesuite.data.repository.OfflineFirstRepository
import javax.inject.Inject

class CreateCollectionUseCase @Inject constructor(
    private val repository: OfflineFirstRepository,
    private val syncRepository: com.aashu.privatesuite.data.repository.SyncRepository
) {
    suspend operator fun invoke(title: String, type: String, theme: String) {
        val collection = CollectionEntity(
            title = title,
            type = type,
            theme = theme
        )
        repository.createCollection(collection)
        syncRepository.sync()
    }
}
