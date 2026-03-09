package com.aashu.privatesuite.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "sync_prefs",
        Context.MODE_PRIVATE
    )

    fun getLastSyncTimestamp(): Long {
        return sharedPreferences.getLong("last_sync_timestamp", 0L)
    }

    fun setLastSyncTimestamp(timestamp: Long) {
        sharedPreferences.edit().putLong("last_sync_timestamp", timestamp).apply()
    }

    fun getTargetSlots(): String {
        // Return JSON string of List<TargetSlot>
        return sharedPreferences.getString("target_slots_config", "[]") ?: "[]"
    }

    fun setTargetSlots(json: String) {
        sharedPreferences.edit().putString("target_slots_config", json).apply()
    }

    fun getActivityLog(): String {
        return sharedPreferences.getString("activity_log", "[]") ?: "[]"
    }

    fun setActivityLog(json: String) {
        sharedPreferences.edit().putString("activity_log", json).apply()
    }
}
