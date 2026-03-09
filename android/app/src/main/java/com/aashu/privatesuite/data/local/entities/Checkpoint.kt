package com.aashu.privatesuite.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_state")
data class Checkpoint(
    @PrimaryKey
    val id: String = "global_checkpoint",
    val timestamp: Long = 0
)
