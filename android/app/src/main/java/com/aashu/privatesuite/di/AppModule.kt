package com.aashu.privatesuite.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideStreakWidgetUpdater(
        manager: com.aashu.privatesuite.widget.WidgetStreakManager
    ): com.aashu.privatesuite.domain.repository.StreakWidgetUpdater {
        return manager
    }
}
