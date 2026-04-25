package com.reminder.data.settings

import com.reminder.core.model.ReminderType
import com.reminder.core.model.ScheduleConfig
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val dataStore: SettingsDataStore) {

    fun observeScheduleConfig(type: ReminderType): Flow<ScheduleConfig> =
        dataStore.observeScheduleConfig(type)

    suspend fun saveScheduleConfig(type: ReminderType, config: ScheduleConfig) =
        dataStore.saveScheduleConfig(type, config)

    fun observeAllEnabledConfigs(): Flow<Map<ReminderType, ScheduleConfig>> =
        dataStore.observeAllEnabledConfigs()

    fun observeVibrationEnabled(): Flow<Boolean> =
        dataStore.observeVibrationEnabled()

    suspend fun setVibrationEnabled(enabled: Boolean) =
        dataStore.setVibrationEnabled(enabled)

    fun observeTtsEnabled(): Flow<Boolean> =
        dataStore.observeTtsEnabled()

    suspend fun setTtsEnabled(enabled: Boolean) =
        dataStore.setTtsEnabled(enabled)

    fun observeOnboardingCompleted(): Flow<Boolean> =
        dataStore.observeOnboardingCompleted()

    suspend fun setOnboardingCompleted() =
        dataStore.setOnboardingCompleted()

    suspend fun deleteScheduleConfig(type: ReminderType) =
        dataStore.clearScheduleConfig(type)

    suspend fun addLogEntry(entry: String) =
        dataStore.addLogEntry(entry)

    fun observeTodayLogs(): Flow<List<String>> =
        dataStore.observeTodayLogs()
}
