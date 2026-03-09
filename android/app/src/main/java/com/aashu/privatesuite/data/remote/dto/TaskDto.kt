package com.aashu.privatesuite.data.remote.dto

import com.aashu.privatesuite.data.local.entities.TaskEntity
import com.google.gson.annotations.SerializedName
import java.util.Date
import java.time.Instant

data class TaskDto(
    @SerializedName("_id") val id: String,
    val collectionId: String,
    val title: String,
    val link: String?,
    val status: String,
    val difficulty: String,
    val completedAt: String?, // Changed to String to match ISO 8601 from backend
    val rating: Int?,
    val platform: String,
    val addedAt: String, // Changed to String
    @SerializedName("notes") val noteContent: String?,
    val visualization: String?,
    @SerializedName("updatedAt") val lastModified: String? // Changed to String
)

fun TaskDto.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        collectionId = collectionId,
        title = title,
        link = link,
        status = status,
        difficulty = difficulty,
        notes = noteContent ?: "",
        visualization = visualization ?: "",
        rating = rating,
        platform = platform,
        addedAt = parseDate(addedAt) ?: Date(),
        completedAt = parseDate(completedAt),
        isSynced = true,
        isDirty = false
    )
}

fun TaskEntity.toDto(): TaskDto {
    return TaskDto(
        id = id,
        collectionId = collectionId,
        title = title,
        link = link,
        status = status,
        difficulty = difficulty,
        completedAt = completedAt?.toInstant()?.toString(),
        rating = rating,
        platform = platform,
        addedAt = addedAt.toInstant().toString(),
        noteContent = notes,
        visualization = visualization,
        lastModified = Instant.now().toString()
    )
}

private fun parseDate(dateString: String?): Date? {
    if (dateString == null) return null
    return try {
        Date.from(Instant.parse(dateString))
    } catch (e: Exception) {
        null
    }
}


