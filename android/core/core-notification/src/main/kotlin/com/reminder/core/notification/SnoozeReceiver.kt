package com.reminder.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SnoozeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderName = intent.getStringExtra(NotificationConstants.EXTRA_REMINDER_NAME) ?: return
        val message = intent.getStringExtra(NotificationConstants.EXTRA_MESSAGE) ?: "时间到了！"

        // Stop current TTS
        TTSManager.getInstance(context).stop()

        // Schedule snooze for 5 minutes
        val snoozeIntent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(NotificationConstants.EXTRA_REMINDER_NAME, reminderName)
            putExtra(NotificationConstants.EXTRA_MESSAGE, message)
        }

        val requestCode = (reminderName.hashCode() and 0x7FFFFFFF) * 1000 + 999
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 5 * 60_000L,
            pendingIntent
        )
    }
}
