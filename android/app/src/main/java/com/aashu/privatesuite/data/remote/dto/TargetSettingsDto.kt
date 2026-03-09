package com.aashu.privatesuite.data.remote.dto

import com.aashu.privatesuite.domain.model.TargetSlot

data class TargetSettingsDto(
    val slots: List<TargetSlotDto>
)

data class TargetSlotDto(
    val id: String?, // Web might not send ID, but we can generate or use index
    val collectionIds: List<String>,
    val label: String?
)

fun TargetSlotDto.toDomain(): TargetSlot {
    return TargetSlot(
        id = id ?: java.util.UUID.randomUUID().toString(),
        collectionIds = collectionIds,
        label = label ?: ""
    )
}

fun TargetSlot.toDto(): TargetSlotDto {
    return TargetSlotDto(
        id = id,
        collectionIds = collectionIds,
        label = label
    )
}
