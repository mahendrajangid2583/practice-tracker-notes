package com.aashu.privatesuite.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.aashu.privatesuite.domain.repository.StreakWidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MidnightReceiver : BroadcastReceiver() {

    @Inject
    lateinit var streakWidgetUpdater: StreakWidgetUpdater

    override fun onReceive(context: Context, intent: Intent) {
        // Trigger widget update
        CoroutineScope(Dispatchers.IO).launch {
            streakWidgetUpdater.updateWidget()
        }
        
        // Re-schedule alarm roughly? 
        // Or rely on StatsWidgetReceiver ensuring it's set?
        // Ideally, Alarms should be exact and repeating, or set next one here.
        // For robustness, we can call a helper to set the next alarm.
        StatsWidgetReceiver.scheduleMidnightUpdate(context)
    }
}
