package com.aashu.privatesuite.data.local.entities

import androidx.room.Embedded

data class CollectionWithCounts(
    @Embedded val collection: CollectionEntity,
    val actualTotal: Int,
    val actualCompleted: Int
)
