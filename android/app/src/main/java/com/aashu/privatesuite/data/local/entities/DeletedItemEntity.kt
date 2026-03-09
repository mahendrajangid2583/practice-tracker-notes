package com.aashu.privatesuite.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "deleted_items")
data class DeletedItemEntity(
    @PrimaryKey
    val id: String, // ID of the deleted item
    val entityType: String, // "collection", "task"
    val isSynced: Boolean = false
)
