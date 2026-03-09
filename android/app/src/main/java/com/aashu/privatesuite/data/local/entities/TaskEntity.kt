package com.aashu.privatesuite.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.UUID
import java.util.Date

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("collectionId")]
)
data class TaskEntity(
    @PrimaryKey
    val id: String = com.aashu.privatesuite.util.ObjectId.generate(),
    val collectionId: String,
    val title: String,
    val link: String? = null,
    val status: String = "Pending", // Pending, Done
    val difficulty: String = "Medium", // Easy, Medium, Hard
    val notes: String = "",
    val visualization: String = "",
    val rating: Int? = null,
    val platform: String = "Other",
    val addedAt: Date = Date(),
    val completedAt: Date? = null,
    val isSynced: Boolean = false,
    val isDirty: Boolean = true,
    val isDeleted: Boolean = false
)
