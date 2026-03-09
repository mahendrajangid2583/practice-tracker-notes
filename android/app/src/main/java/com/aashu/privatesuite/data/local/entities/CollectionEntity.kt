package com.aashu.privatesuite.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID
import java.util.Date

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey
    val id: String = com.aashu.privatesuite.util.ObjectId.generate(),
    val title: String,
    val type: String, // DSA, PROJECT, LEARNING, NOTES
    val theme: String = "blue",
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val lastOpenedAt: Date = Date(),
    val createdAt: Date = Date(),
    val isSynced: Boolean = false,
    val isDirty: Boolean = true,
    val isDeleted: Boolean = false
)
