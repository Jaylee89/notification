package com.reminder.core.notification

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.reminder.core.model.ReminderData
import com.reminder.core.model.ScheduleConfig
import com.reminder.core.model.TriggerTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun createNotificationChannel() {
        val channel = NotificationChannel(
            NotificationConstants.CHANNEL_ID_REMINDER,
            NotificationConstants.CHANNEL_NAME_REMINDER,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = NotificationConstants.CHANNEL_DESC_REMINDER
            setShowBadge(true)
            enableVibration(true)
            // 显式设置通知音 — 对 Android 8.0+ 必需，仅靠 setDefaults() 不生效
            setSound(
                Settings.System.DEFAULT_NOTIFICATION_URI,
                Notification.AUDIO_ATTRIBUTES_DEFAULT
            )
            vibrationPattern = longArrayOf(300, 400, 200, 400)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun scheduleReminders(reminder: ReminderData, config: ScheduleConfig) {
        cancelReminders(reminder.id)

        val triggers = config.generateTodayTriggers()
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(NotificationConstants.EXTRA_REMINDER_ID, reminder.id)
            putExtra(NotificationConstants.EXTRA_REMINDER_NAME, reminder.name)
            putExtra(NotificationConstants.EXTRA_MESSAGE, "${reminder.name}的时间到了！")
        }

        triggers.forEachIndexed { index, trigger ->
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                getRequestCode(reminder.id, index),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerMillis = trigger.toCalendar().timeInMillis
            if (triggerMillis > System.currentTimeMillis()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        }
    }

    fun cancelReminders(reminderId: String) {
        val maxTriggers = 48
        for (i in 0 until maxTriggers) {
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                getRequestCode(reminderId, i),
                Intent(context, ReminderReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE or
                        PendingIntent.FLAG_NO_CREATE
            )
            pendingIntent?.cancel()
            pendingIntent?.let {
                alarmManager.cancel(it)
            }
        }
    }

    fun showNotification(reminderName: String, message: String, vibrationEnabled: Boolean = true) {
        val notificationId = System.currentTimeMillis().toInt()

        // Acknowledge action
        val ackIntent = Intent(context, AcknowledgeReceiver::class.java).apply {
            putExtra(NotificationConstants.EXTRA_REMINDER_NAME, reminderName)
        }
        val ackPendingIntent = PendingIntent.getBroadcast(
            context, notificationId, ackIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze action
        val snoozeIntent = Intent(context, SnoozeReceiver::class.java).apply {
            putExtra(NotificationConstants.EXTRA_REMINDER_NAME, reminderName)
            putExtra(NotificationConstants.EXTRA_MESSAGE, message)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context, notificationId + 1, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, NotificationConstants.CHANNEL_ID_REMINDER)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("${reminderName}提醒")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setFullScreenIntent(null, true)
            .setAutoCancel(false)
            .setOngoing(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(android.R.drawable.ic_dialog_dialer, "知道了", ackPendingIntent)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "5分钟后再提醒", snoozePendingIntent)

        if (vibrationEnabled) {
            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
        } else {
            notificationBuilder.setVibrate(longArrayOf(0L))
        }

        val notification = notificationBuilder.build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS permission not granted
        }
    }

    fun rescheduleAfterBoot() {
        val dataStore = com.reminder.data.settings.SettingsDataStore(context)
        runBlocking {
            val reminders = dataStore.getAllReminders()
            reminders.forEach { reminder ->
                if (reminder.config.enabled) {
                    scheduleReminders(reminder, reminder.config)
                }
            }
        }
    }

    private fun getRequestCode(reminderId: String, index: Int): Int {
        return reminderId.hashCode() and 0x7FFFFFFF * 1000 + index
    }
}
