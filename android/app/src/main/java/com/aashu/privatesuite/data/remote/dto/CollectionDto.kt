package com.aashu.privatesuite.data.remote.dto

import com.aashu.privatesuite.data.local.entities.CollectionEntity
import com.google.gson.annotations.SerializedName
import java.util.Date

data class CollectionDto(
    @SerializedName("_id") val id: String,
    val title: String,
    val type: String,
    val theme: String?,
    val totalTasks: Int,
    val completedTasks: Int,
    val createdAt: String,
    val lastOpenedAt: String?,
    val tasks: List<TaskDto>? = null
)

fun CollectionDto.toEntity(): CollectionEntity {
    return CollectionEntity(
        id = id,
        title = title,
        type = type,
        theme = theme ?: "blue",
        totalTasks = totalTasks,
        completedTasks = completedTasks,
        // Helper to parse dates? backend sends ISO strings. 
        // Gson handles basic ISO dates usually, but we are mapping manually here.
        // Let's assume standard ISO format.
        // Actually, we can use a helper or just rely on simple parsing.
        // For MVP speed, we'll try to parse, fallback to now.
        createdAt = parseDate(createdAt),
        lastOpenedAt = parseDate(lastOpenedAt),
        isSynced = true,
        isDirty = false
    )
}

// Simple ISO parser for now, or just use Instant if API level allows (minSdk 26).
private fun parseDate(dateString: String?): Date {
    if (dateString == null) return Date()
    return try {
        // Instant.parse is available since API 26 (our minSdk is 26)
        Date.from(java.time.Instant.parse(dateString))
    } catch (e: Exception) {
        Date()
    }
}

fun CollectionEntity.toDto(): CollectionDto {
    return CollectionDto(
        id = id,
        title = title,
        type = type,
        theme = theme,
        totalTasks = totalTasks,
        completedTasks = completedTasks,
        createdAt = createdAt.toInstant().toString(),
        lastOpenedAt = lastOpenedAt.toInstant().toString()
    )
}
