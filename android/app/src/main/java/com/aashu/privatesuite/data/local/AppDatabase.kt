package com.aashu.privatesuite.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aashu.privatesuite.data.local.entities.CollectionEntity
import com.aashu.privatesuite.data.local.entities.TaskEntity
import com.aashu.privatesuite.data.local.entities.DailyTargetEntity
import com.aashu.privatesuite.data.local.entities.Checkpoint
import com.aashu.privatesuite.data.local.entities.DeletedItemEntity
import com.aashu.privatesuite.data.local.dao.CollectionDao
import com.aashu.privatesuite.data.local.dao.TaskDao
import com.aashu.privatesuite.data.local.dao.DailyTargetDao
import com.aashu.privatesuite.data.local.dao.SyncStateDao

@Database(
    entities = [
        CollectionEntity::class,
        TaskEntity::class,
        DailyTargetEntity::class,
        Checkpoint::class,
        DeletedItemEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
    abstract fun taskDao(): TaskDao
    abstract fun dailyTargetDao(): DailyTargetDao
    abstract fun syncStateDao(): SyncStateDao
}
