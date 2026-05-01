package com.reminder.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.reminder.core.model.ReminderData
import com.reminder.core.model.ReminderType
import com.reminder.core.model.ScheduleConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "drink_reminder_settings")

class SettingsDataStore(private val context: Context) {

    private val REMINDERS_JSON = stringPreferencesKey("reminders_json")
    private val MIGRATION_DONE = booleanPreferencesKey("migration_v2_done")

    // ── New ReminderData-based API ──────────────────────────────────────────

    suspend fun saveReminder(reminder: ReminderData) {
        context.dataStore.edit { prefs ->
            prefs.migrateOldData()
            val reminders = parseReminders(prefs[REMINDERS_JSON]).toMutableMap()
            reminders[reminder.id] = reminder
            prefs[REMINDERS_JSON] = encodeReminders(reminders)
        }
    }

    fun observeAllReminders(): Flow<List<ReminderData>> {
        return context.dataStore.data.map { prefs ->
            parseReminders(prefs[REMINDERS_JSON]).values.toList()
        }
    }

    /** Trigger migration from old format immediately. Safe to call multiple times. */
    suspend fun migrateNow() {
        context.dataStore.edit { prefs ->
            prefs.migrateOldData()
        }
    }

    suspend fun getAllReminders(): List<ReminderData> {
        return context.dataStore.data.map { prefs ->
            parseReminders(prefs[REMINDERS_JSON]).values.toList()
        }.first()
    }

    fun observeReminder(id: String): Flow<ReminderData?> {
        return context.dataStore.data.map { prefs ->
            parseReminders(prefs[REMINDERS_JSON])[id]
        }
    }

    suspend fun deleteReminder(id: String) {
        context.dataStore.edit { prefs ->
            prefs.migrateOldData()
            val reminders = parseReminders(prefs[REMINDERS_JSON]).toMutableMap()
            reminders.remove(id)
            prefs[REMINDERS_JSON] = encodeReminders(reminders)
        }
    }

    // ── JSON encoding / decoding ────────────────────────────────────────────

    private fun encodeReminders(reminders: Map<String, ReminderData>): String {
        return reminders.values.joinToString("\n") { r ->
            val c = r.config
            listOf(
                r.id, r.name,
                c.enabled.toString(),
                c.startHour.toString(), c.startMinute.toString(),
                c.endHour.toString(), c.endMinute.toString(),
                c.intervalMinutes.toString(),
                c.customName ?: ""
            ).joinToString("|")
        }
    }

    private fun parseReminders(raw: String?): Map<String, ReminderData> {
        if (raw == null) return emptyMap()
        return raw.lines().filter { it.isNotBlank() }.mapNotNull { line ->
            val parts = line.split("|", limit = 9)
            if (parts.size < 8) return@mapNotNull null
            val customName = parts.getOrNull(8)?.ifBlank { null }
            ReminderData(
                id = parts[0],
                name = parts[1],
                config = ScheduleConfig(
                    enabled = parts[2].toBoolean(),
                    startHour = parts[3].toIntOrNull() ?: 8,
                    startMinute = parts[4].toIntOrNull() ?: 0,
                    endHour = parts[5].toIntOrNull() ?: 20,
                    endMinute = parts[6].toIntOrNull() ?: 0,
                    intervalMinutes = parts[7].toIntOrNull() ?: 60,
                    customName = customName
                )
            )
        }.associateBy { it.id }
    }

    // ── Migration from old ReminderType-keyed format ────────────────────────

    private fun MutablePreferences.migrateOldData() {
        // This is called from edit {} where prefs is actually MutablePreferences
        if (this[MIGRATION_DONE] == true) return
        if (this[REMINDERS_JSON] != null) {
            this[MIGRATION_DONE] = true
            return
        }

        val oldReminders = ReminderType.values().mapNotNull { type ->
            val enabled = this[booleanPreferencesKey("${type.name}_enabled")] ?: false
            if (!enabled) return@mapNotNull null
            ReminderData(
                name = this[stringPreferencesKey("${type.name}_custom_name")] ?: type.displayName,
                config = ScheduleConfig(
                    enabled = true,
                    startHour = this[intPreferencesKey("${type.name}_start_hour")] ?: 8,
                    startMinute = this[intPreferencesKey("${type.name}_start_minute")] ?: 0,
                    endHour = this[intPreferencesKey("${type.name}_end_hour")] ?: 20,
                    endMinute = this[intPreferencesKey("${type.name}_end_minute")] ?: 0,
                    intervalMinutes = this[intPreferencesKey("${type.name}_interval_min")] ?: 60,
                    customName = this[stringPreferencesKey("${type.name}_custom_name")]
                )
            )
        }

        if (oldReminders.isNotEmpty()) {
            this[REMINDERS_JSON] = encodeReminders(oldReminders.associateBy { it.id })
        }

        for (type in ReminderType.values()) {
            remove(booleanPreferencesKey("${type.name}_enabled"))
            remove(intPreferencesKey("${type.name}_start_hour"))
            remove(intPreferencesKey("${type.name}_start_minute"))
            remove(intPreferencesKey("${type.name}_end_hour"))
            remove(intPreferencesKey("${type.name}_end_minute"))
            remove(intPreferencesKey("${type.name}_interval_min"))
            remove(stringPreferencesKey("${type.name}_custom_name"))
        }

        this[MIGRATION_DONE] = true
    }

    // ── Global settings ─────────────────────────────────────────────────────

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

    // ── Log storage ─────────────────────────────────────────────────────────

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
            if (raw.isEmpty()) emptyList() else raw.split("\n").reversed()
        }
    }

    companion object {
        private val VIBRATION_ENABLED = booleanPreferencesKey("global_vibration")
        private val TTS_ENABLED = booleanPreferencesKey("global_tts")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("global_onboarding_done")
    }
}
