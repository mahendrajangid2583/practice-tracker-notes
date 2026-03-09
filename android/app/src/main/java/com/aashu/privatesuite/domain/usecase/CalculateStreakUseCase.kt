package com.aashu.privatesuite.domain.usecase

import com.aashu.privatesuite.data.local.SyncPreferences
import com.aashu.privatesuite.data.repository.OfflineFirstRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class StreakResult(
    val currentStreak: Int,
    val completedDates: List<LocalDate>
)

class CalculateStreakUseCase @Inject constructor(
    private val repository: OfflineFirstRepository,
    private val syncPreferences: SyncPreferences
) {
    // Hardcoded start date as per requirements
    private val START_DATE = LocalDate.of(2026, 2, 1)

    operator fun invoke(): Flow<StreakResult> {
        // We combine local tasks flow to trigger updates when tasks change locally
        return repository.getAllCompletedTasks().map { localTasks ->
            // 1. Get Local Dates
            val localDates = localTasks.mapNotNull { task ->
                task.completedAt?.let { date ->
                    try {
                        Instant.ofEpochMilli(date.time)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    } catch (e: Exception) {
                        null
                    }
                }
            }.toSet()

            // 2. Get Remote Dates from Preferences
            val remoteJson = syncPreferences.getActivityLog()
            val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
            val remoteStrings: List<String> = try {
                com.google.gson.Gson().fromJson(remoteJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
            
            val remoteDates = remoteStrings.mapNotNull { str ->
                try {
                     // Handle ISO string or simple date string
                     // Backend sends: res.json(timestamps) -> timestamps are Date objects from Mongoose
                     // Axio/Retrofit/Gson might serialize as ISO string "2026-02-01T..."
                     Instant.parse(str).atZone(ZoneId.systemDefault()).toLocalDate()
                } catch (e: Exception) {
                    null
                }
            }.toSet()

            // 3. Merge
            val allDates = (localDates + remoteDates).toList().sorted()
            val completedDatesSet = allDates.toSet()

            // 4. Calculate Streak (Same logic as useStreak.js)
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)

            val isActiveToday = completedDatesSet.contains(today)
            
            var currentStreak = 0

            // "If not active today and not active yesterday, streak is 0."
            if (!isActiveToday && !completedDatesSet.contains(yesterday)) {
                currentStreak = 0
            } else {
                // "Start checking from today if active, otherwise start from yesterday"
                var checkDate = if (isActiveToday) today else yesterday
                
                while (completedDatesSet.contains(checkDate)) {
                    currentStreak++
                    checkDate = checkDate.minusDays(1)
                }
            }

            StreakResult(
                currentStreak = currentStreak,
                completedDates = allDates
            )
        }
    }
}
