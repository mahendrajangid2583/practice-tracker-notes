package com.aashu.privatesuite.data.remote.dto

data class CollectionsResponseDto(
    val collections: List<CollectionDto>,
    // we can ignore globalStats for now or add it if needed
)
