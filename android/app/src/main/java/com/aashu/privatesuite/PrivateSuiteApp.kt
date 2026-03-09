package com.aashu.privatesuite

import android.app.Application
import androidx.work.Configuration
import com.aashu.privatesuite.data.manager.SyncManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.aashu.privatesuite.data.repository.SyncRepository

@HiltAndroidApp
class PrivateSuiteApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: androidx.hilt.work.HiltWorkerFactory

    @Inject
    lateinit var syncManager: SyncManager
    
    @Inject
    lateinit var syncRepository: SyncRepository

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        syncManager.schedulePeriodicSync()
        syncManager.scheduleDailyBriefing()
        
        // Immediate fetch for "Show Collections" feature
        CoroutineScope(Dispatchers.IO).launch {
            syncRepository.refreshCollections()
        }
    }
}
