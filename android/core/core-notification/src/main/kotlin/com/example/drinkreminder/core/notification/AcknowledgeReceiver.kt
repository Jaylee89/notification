package com.example.drinkreminder.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.drinkreminder.core.model.ReminderType
import com.example.drinkreminder.data.settings.SettingsDataStore
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AcknowledgeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val typeName = intent.getStringExtra(NotificationConstants.EXTRA_REMINDER_TYPE) ?: return
        val type = try {
            ReminderType.valueOf(typeName)
        } catch (_: IllegalArgumentException) {
            return
        }

        // Stop TTS
        TTSManager.getInstance(context).stop()

        // Log acknowledgment
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        runBlocking {
            val dataStore = SettingsDataStore(context)
            dataStore.addLogEntry("$timeStr - ${type.displayName}: 已确认")
        }
    }
}
