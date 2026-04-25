package com.example.drinkreminder.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.drinkreminder.core.model.ReminderType

class SnoozeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val typeName = intent.getStringExtra(NotificationConstants.EXTRA_REMINDER_TYPE) ?: return
        val message = intent.getStringExtra(NotificationConstants.EXTRA_MESSAGE) ?: "时间到了！"
        val type = try {
            ReminderType.valueOf(typeName)
        } catch (_: IllegalArgumentException) {
            return
        }

        // Stop current TTS
        TTSManager.getInstance(context).stop()

        // Schedule snooze for 5 minutes
        val snoozeIntent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(NotificationConstants.EXTRA_REMINDER_TYPE, type.name)
            putExtra(NotificationConstants.EXTRA_MESSAGE, message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            type.ordinal * 1000 + 999,
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
