package com.aashu.privatesuite.di

import android.content.Context
import androidx.room.Room
import com.aashu.privatesuite.data.local.AppDatabase
import com.aashu.privatesuite.data.local.dao.CollectionDao
import com.aashu.privatesuite.data.local.dao.DailyTargetDao
import com.aashu.privatesuite.data.local.dao.SyncStateDao
import com.aashu.privatesuite.data.local.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "private_suite_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideTaskDao(database: AppDatabase): TaskDao = database.taskDao()

    @Provides
    fun provideCollectionDao(database: AppDatabase): CollectionDao = database.collectionDao()

    @Provides
    fun provideDailyTargetDao(database: AppDatabase): DailyTargetDao = database.dailyTargetDao()

    @Provides
    fun provideSyncStateDao(database: AppDatabase): SyncStateDao = database.syncStateDao()
}
