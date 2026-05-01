package com.reminder.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.reminder.data.settings.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderName = intent.getStringExtra(NotificationConstants.EXTRA_REMINDER_NAME) ?: return
        val message = intent.getStringExtra(NotificationConstants.EXTRA_MESSAGE) ?: "时间到了！"

        // Ensure foreground service is running so process survives
        ReminderService.start(context)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Read settings
                val dataStore = SettingsDataStore(context)
                val (ttsEnabled, vibrationEnabled) = run {
                    val tts = dataStore.observeTtsEnabled().first()
                    val vib = dataStore.observeVibrationEnabled().first()
                    tts to vib
                }

                // Show notification
                val notificationHelper = NotificationHelper(context)
                notificationHelper.showNotification(
                    reminderName, message,
                    vibrationEnabled = vibrationEnabled
                )

                // Check TTS setting before speaking
                if (ttsEnabled) {
                    TTSManager.getInstance(context).speak(message)
                }

                // Log reminder trigger
                val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).format(Date())
                dataStore.addLogEntry("$timeStr - $reminderName: $message")
            } finally {
                pendingResult.finish()
            }
        }
    }
}
