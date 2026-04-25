package com.reminder.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.reminder.core.model.ReminderType
import com.reminder.core.model.ScheduleConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "drink_reminder_settings")

class SettingsDataStore(private val context: Context) {

    fun observeScheduleConfig(type: ReminderType): Flow<ScheduleConfig> {
        return context.dataStore.data.map { prefs ->
            ScheduleConfig(
                enabled = prefs[getEnabledKey(type)] ?: false,
                startHour = prefs[getStartHourKey(type)] ?: 8,
                startMinute = prefs[getStartMinuteKey(type)] ?: 0,
                endHour = prefs[getEndHourKey(type)] ?: 20,
                endMinute = prefs[getEndMinuteKey(type)] ?: 0,
                intervalMinutes = prefs[getIntervalKey(type)] ?: 60,
                customName = prefs[getCustomNameKey(type)]
        }
    }

    suspend fun saveScheduleConfig(type: ReminderType, config: ScheduleConfig) {
        context.dataStore.edit { prefs ->
            prefs[getEnabledKey(type)] = config.enabled
            prefs[getStartHourKey(type)] = config.startHour
            prefs[getStartMinuteKey(type)] = config.startMinute
            prefs[getEndHourKey(type)] = config.endHour
            prefs[getEndMinuteKey(type)] = config.endMinute
            prefs[getIntervalKey(type)] = config.intervalMinutes
            if (config.customName != null) {
                prefs[getCustomNameKey(type)] = config.customName!!
            } else {
                prefs.remove(getCustomNameKey(type))
            }
        }
    }

    fun observeAllEnabledConfigs(): Flow<Map<ReminderType, ScheduleConfig>> {
        return context.dataStore.data.map { prefs ->
            ReminderType.values().mapNotNull { type ->
                val enabled = prefs[getEnabledKey(type)] ?: false
                if (enabled) {
                    type to ScheduleConfig(
                        enabled = true,
                        startHour = prefs[getStartHourKey(type)] ?: 8,
                        startMinute = prefs[getStartMinuteKey(type)] ?: 0,
                        endHour = prefs[getEndHourKey(type)] ?: 20,
                        endMinute = prefs[getEndMinuteKey(type)] ?: 0,
                        intervalMinutes = prefs[getIntervalKey(type)] ?: 60,
                        customName = prefs[getCustomNameKey(type)]
                    )
                } else null
            }.toMap()
        }
    }

    // Global settings
    fun observeVibrationEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[VIBRATION_ENABLED] ?: true
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[VIBRATION_ENABLED] = enabled
        }
    }

    fun observeTtsEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[TTS_ENABLED] ?: true
        }
    }

    suspend fun setTtsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[TTS_ENABLED] = enabled
        }
    }

    fun observeOnboardingCompleted(): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[ONBOARDING_COMPLETED] ?: false
        }
    }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED] = true
        }
    }

    // Log storage — date-keyed newline-separated entries
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun addLogEntry(entry: String) {
        val key = stringPreferencesKey("logs_${dateFormat.format(Date())}")
        context.dataStore.edit { prefs ->
            val existing = prefs[key] ?: ""
            val updated = if (existing.isEmpty()) entry else "$existing\n$entry"
            prefs[key] = updated
        }
    }

    fun observeTodayLogs(): Flow<List<String>> {
        val key = stringPreferencesKey("logs_${dateFormat.format(Date())}")
        return context.dataStore.data.map { prefs ->
            val raw = prefs[key] ?: ""
            if (raw.isEmpty()) emptyList() else raw.split("\n")
        }
    }

    suspend fun clearScheduleConfig(type: ReminderType) {
        context.dataStore.edit { prefs ->
            prefs.remove(getEnabledKey(type))
            prefs.remove(getStartHourKey(type))
            prefs.remove(getStartMinuteKey(type))
            prefs.remove(getEndHourKey(type))
            prefs.remove(getEndMinuteKey(type))
            prefs.remove(getIntervalKey(type))
            prefs.remove(getCustomNameKey(type))
        }
    }

    private fun getEnabledKey(type: ReminderType) =
        booleanPreferencesKey("${type.name}_enabled")
    private fun getStartHourKey(type: ReminderType) =
        intPreferencesKey("${type.name}_start_hour")
    private fun getStartMinuteKey(type: ReminderType) =
        intPreferencesKey("${type.name}_start_minute")
    private fun getEndHourKey(type: ReminderType) =
        intPreferencesKey("${type.name}_end_hour")
    private fun getEndMinuteKey(type: ReminderType) =
        intPreferencesKey("${type.name}_end_minute")
    private fun getIntervalKey(type: ReminderType) =
        intPreferencesKey("${type.name}_interval_min")
    private fun getCustomNameKey(type: ReminderType) =
        stringPreferencesKey("${type.name}_custom_name")

    companion object {
        private val VIBRATION_ENABLED = booleanPreferencesKey("global_vibration")
        private val TTS_ENABLED = booleanPreferencesKey("global_tts")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("global_onboarding_done")
    }
}
