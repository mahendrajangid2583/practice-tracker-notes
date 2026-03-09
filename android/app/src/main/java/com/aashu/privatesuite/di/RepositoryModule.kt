package com.aashu.privatesuite.di

import com.aashu.privatesuite.data.repository.OfflineFirstRepository
import com.aashu.privatesuite.data.repository.OfflineFirstRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindOfflineFirstRepository(
        offlineFirstRepositoryImpl: OfflineFirstRepositoryImpl
    ): OfflineFirstRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authManager: com.aashu.privatesuite.data.local.AuthManager
    ): com.aashu.privatesuite.domain.repository.AuthRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(
        syncRepositoryImpl: com.aashu.privatesuite.data.repository.SyncRepositoryImpl
    ): com.aashu.privatesuite.data.repository.SyncRepository
}
