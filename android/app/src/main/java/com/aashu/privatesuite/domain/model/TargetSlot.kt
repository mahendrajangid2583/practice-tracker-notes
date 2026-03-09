package com.aashu.privatesuite.domain.model

import java.util.UUID

data class TargetSlot(
    val id: String = UUID.randomUUID().toString(),
    val collectionIds: List<String> = emptyList(),
    val label: String = ""
)
