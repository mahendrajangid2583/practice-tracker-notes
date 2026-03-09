package com.aashu.privatesuite.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SyncPullRequest(
    val lastSyncTimestamp: Long
)

data class SyncPullResponse(
    val timestamp: Long,
    val changes: SyncChanges
)

data class SyncChanges(
    val collections: EntityChanges<CollectionDto>,
    val tasks: EntityChanges<TaskDto>
)

data class EntityChanges<T>(
    val updated: List<T>,
    val deleted: List<String> // List of IDs
)

data class SyncPushRequest(
    val changes: SyncChanges
)

data class SyncPushResponse(
    val success: Boolean,
    val results: SyncPushResults?
)

data class SyncPushResults(
    val applied: Int,
    val errors: List<SyncItemError>?
)

data class SyncItemError(
    val type: String,
    val id: String,
    val error: String
)
