package com.reminder.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.reminder.core.model.ReminderType
import com.reminder.data.settings.SettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val typeName = intent.getStringExtra(NotificationConstants.EXTRA_REMINDER_TYPE) ?: return
        val message = intent.getStringExtra(NotificationConstants.EXTRA_MESSAGE) ?: "时间到了！"
        val type = try {
            ReminderType.valueOf(typeName)
        } catch (_: IllegalArgumentException) {
            return
        }

        // Read settings
        val dataStore = SettingsDataStore(context)
        val (ttsEnabled, vibrationEnabled) = runBlocking {
            val tts = dataStore.observeTtsEnabled().first()
            val vib = dataStore.observeVibrationEnabled().first()
            tts to vib
        }

        // Show notification
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showNotification(type, message, vibrationEnabled = vibrationEnabled)

        // Check TTS setting before speaking
        if (ttsEnabled) {
            TTSManager.getInstance(context).speak(message)
        }

        // Log reminder trigger
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        runBlocking {
            dataStore.addLogEntry("$timeStr - ${type.displayName}: $message")
        }
    }
}
