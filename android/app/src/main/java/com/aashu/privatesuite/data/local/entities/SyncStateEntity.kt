package com.aashu.privatesuite.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_state")
data class SyncStateEntity(
    @PrimaryKey
    val id: Int = 1, // Singleton
    val lastSyncTimestamp: Long = 0
)
