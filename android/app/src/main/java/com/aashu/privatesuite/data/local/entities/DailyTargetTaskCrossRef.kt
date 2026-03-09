package com.aashu.privatesuite.data.local.entities

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "daily_target_task_cross_ref",
    primaryKeys = ["dailyTargetDate", "taskId"],
    indices = [Index(value = ["taskId"])]
)
data class DailyTargetTaskCrossRef(
    val dailyTargetDate: String,
    val taskId: String
)
