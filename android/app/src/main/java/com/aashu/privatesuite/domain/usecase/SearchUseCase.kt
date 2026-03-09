package com.aashu.privatesuite.domain.usecase

import com.aashu.privatesuite.data.local.entities.CollectionEntity
import com.aashu.privatesuite.data.local.entities.TaskEntity
import com.aashu.privatesuite.data.repository.OfflineFirstRepository
import javax.inject.Inject

data class SearchResults(
    val collections: List<CollectionEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList()
)

class SearchUseCase @Inject constructor(
    private val repository: OfflineFirstRepository
) {
    suspend operator fun invoke(query: String): SearchResults {
        if (query.isBlank()) return SearchResults()
        
        val collections = repository.searchCollections(query)
        val tasks = repository.searchTasks(query)
        
        return SearchResults(collections, tasks)
    }
}
