package com.aashu.privatesuite.data.local.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class DailyTargetWithTasks(
    @Embedded val dailyTarget: DailyTargetEntity,
    @Relation(
        parentColumn = "date",
        entityColumn = "id",
        associateBy = Junction(
            value = DailyTargetTaskCrossRef::class,
            parentColumn = "dailyTargetDate",
            entityColumn = "taskId"
        )
    )
    val tasks: List<TaskEntity>
)
