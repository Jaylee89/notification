package com.reminder.data.settings

import com.reminder.core.model.ReminderData
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val dataStore: SettingsDataStore) {

    fun observeAllReminders(): Flow<List<ReminderData>> =
        dataStore.observeAllReminders()

    fun observeReminder(id: String): Flow<ReminderData?> =
        dataStore.observeReminder(id)

    suspend fun saveReminder(reminder: ReminderData) =
        dataStore.saveReminder(reminder)

    suspend fun deleteReminder(id: String) =
        dataStore.deleteReminder(id)

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

    suspend fun addLogEntry(entry: String) =
        dataStore.addLogEntry(entry)

    suspend fun migrateNow() =
        dataStore.migrateNow()

    fun observeTodayLogs(): Flow<List<String>> =
        dataStore.observeTodayLogs()
}
