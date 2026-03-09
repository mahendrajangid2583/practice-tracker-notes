package com.aashu.privatesuite.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "daily_targets")
data class DailyTargetEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val date: String, // YYYY-MM-DD
    val taskIds: List<String>, // Stored as JSON string
    val createdAt: Date = Date(),
    val isSynced: Boolean = false,
    val isDirty: Boolean = true
)
