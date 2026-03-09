package com.aashu.privatesuite.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.aashu.privatesuite.domain.repository.StreakWidgetUpdater
import com.aashu.privatesuite.domain.usecase.CalculateStreakUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetStreakManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val calculateStreakUseCase: CalculateStreakUseCase
) : StreakWidgetUpdater {

    companion object {
        private const val TAG = "WidgetStreakManager"
        const val PREFS_NAME = "widget_prefs"
        const val KEY_STREAK = "streak_count"
        const val KEY_STATUS = "streak_status" // "DONE", "PENDING", "BROKEN"
    }

    override suspend fun updateWidget() {
        try {
            Log.d(TAG, "Updating widget streak state...")
            val result = calculateStreakUseCase().firstOrNull() ?: return
            val streak = result.currentStreak
            val completedDates = result.completedDates
            
            val today = java.time.LocalDate.now()
            val isActiveToday = completedDates.contains(today)
            
            // Determine status
            val status = when {
                isActiveToday -> "DONE"
                else -> "PENDING" // Since streak calc handles "broken" logic (resetting to 0), if streak > 0 and not done today, it's pending. If streak == 0, it's effectively "broken" or "pending" (start of streak).
                // Wait, if streak is 0, it doesn't matter much, but let's be precise for UI.
                // If streak > 0 && !isActiveToday -> Pending
                // If streak == 0 -> Broken/Empty
            }
            
            val finalStatus = if (streak == 0) "BROKEN" else status

            Log.d(TAG, "Calculated State: Streak=$streak, Status=$finalStatus")

            // Persist to SharedPrefs for simple access (or use Glance State)
            // Using SharedPreferences directly for simplicity and reliability across processes if needed
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putInt(KEY_STREAK, streak)
                .putString(KEY_STATUS, finalStatus)
                .apply()

            // Force Widget Update
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(StatsWidget::class.java)
            glanceIds.forEach { glanceId ->
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[androidx.datastore.preferences.core.intPreferencesKey(KEY_STREAK)] = streak
                        this[androidx.datastore.preferences.core.stringPreferencesKey(KEY_STATUS)] = finalStatus
                    }
                }
                StatsWidget().update(context, glanceId)
            }
            Log.d(TAG, "Widget update triggered for ${glanceIds.size} widgets")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update widget", e)
        }
    }
}
