package com.reminder.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.reminder.data.settings.SettingsDataStore
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AcknowledgeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderName = intent.getStringExtra(NotificationConstants.EXTRA_REMINDER_NAME) ?: return

        // Stop TTS
        TTSManager.getInstance(context).stop()

        // Log acknowledgment
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        runBlocking {
            val dataStore = SettingsDataStore(context)
            dataStore.addLogEntry("$timeStr - $reminderName: 已确认")
        }
    }
}
