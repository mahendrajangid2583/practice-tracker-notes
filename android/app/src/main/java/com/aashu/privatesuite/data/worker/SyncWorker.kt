package com.aashu.privatesuite.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aashu.privatesuite.data.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import android.util.Log

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: com.aashu.privatesuite.data.repository.SyncRepository,
    private val streakWidgetUpdater: com.aashu.privatesuite.domain.repository.StreakWidgetUpdater
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("SyncWorker", "Starting sync work...")
            syncRepository.sync()
            Log.d("SyncWorker", "Sync work completed successfully.")
            
            // Update widget after successful sync
            try {
                streakWidgetUpdater.updateWidget()
            } catch (e: Exception) {
                Log.e("SyncWorker", "Failed to update widget after sync", e)
            }
            
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync work failed", e)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
