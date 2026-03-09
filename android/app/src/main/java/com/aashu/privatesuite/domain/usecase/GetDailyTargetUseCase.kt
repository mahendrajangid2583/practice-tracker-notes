package com.aashu.privatesuite.domain.usecase

import com.aashu.privatesuite.data.local.entities.DailyTargetEntity
import com.aashu.privatesuite.data.repository.OfflineFirstRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDailyTargetUseCase @Inject constructor(
    private val repository: OfflineFirstRepository
) {
    operator fun invoke(date: String): Flow<DailyTargetEntity?> {
        return repository.getDailyTarget(date)
    }
}
