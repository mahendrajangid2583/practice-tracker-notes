package com.aashu.privatesuite.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.aashu.privatesuite.domain.repository.StreakWidgetUpdater
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class StatsWidgetReceiver : GlanceAppWidgetReceiver() {

    @Inject
    lateinit var streakWidgetUpdater: StreakWidgetUpdater

    override val glanceAppWidget: GlanceAppWidget = StatsWidget()
    
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleMidnightUpdate(context)
        // Initial update
        CoroutineScope(Dispatchers.IO).launch {
            streakWidgetUpdater.updateWidget()
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        CoroutineScope(Dispatchers.IO).launch {
            streakWidgetUpdater.updateWidget()
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // Ensure update on any receive if needed, but onUpdate covers standard broadcasts.
        // If we have custom actions, handle here.
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == "com.aashu.privatesuite.action.UPDATE_STREAK_WIDGET") {
             if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                 scheduleMidnightUpdate(context)
             }
             CoroutineScope(Dispatchers.IO).launch {
                streakWidgetUpdater.updateWidget()
            }
        }
    }

    companion object {
        fun scheduleMidnightUpdate(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, MidnightReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Calculate time for next midnight
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // Set exact alarm
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                // Handle permission if needed (rare for exact alarm unless strict)
                // Fallback to setExact or set
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
    }
}


