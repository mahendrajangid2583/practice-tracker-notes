package com.aashu.privatesuite.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aashu.privatesuite.data.local.dao.DailyTargetDao
import com.aashu.privatesuite.presentation.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate

@HiltWorker
class DailyBriefingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val dailyTargetDao: DailyTargetDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now().toString()
        val dailyTarget = dailyTargetDao.getDailyTarget(today).firstOrNull()
        
        val taskCount = dailyTarget?.taskIds?.size ?: 0
        
        if (taskCount > 0) {
            val notificationHelper = NotificationHelper(applicationContext)
            notificationHelper.showNotification(
                title = "Daily Briefing",
                content = "You have $taskCount targets focused for today. Let's get them done!"
            )
        }

        return Result.success()
    }
}
