package com.example.drinkreminder.core.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.drinkreminder.core.model.ReminderType
import com.example.drinkreminder.core.model.ScheduleConfig
import com.example.drinkreminder.core.model.TriggerTime
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
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun scheduleReminders(type: ReminderType, config: ScheduleConfig) {
        cancelReminders(type)

        val triggers = config.generateTodayTriggers()
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(NotificationConstants.EXTRA_REMINDER_TYPE, type.name)
            putExtra(NotificationConstants.EXTRA_MESSAGE, "${type.displayName}的时间到了！")
        }

        triggers.forEachIndexed { index, trigger ->
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                getRequestCode(type, index),
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

    fun cancelReminders(type: ReminderType) {
        val maxTriggers = 48 // One day with 30-min intervals max
        for (i in 0 until maxTriggers) {
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                getRequestCode(type, i),
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

    fun showNotification(type: ReminderType, message: String, vibrationEnabled: Boolean = true) {
        val notificationId = System.currentTimeMillis().toInt()

        // Acknowledge action
        val ackIntent = Intent(context, AcknowledgeReceiver::class.java).apply {
            putExtra(NotificationConstants.EXTRA_REMINDER_TYPE, type.name)
        }
        val ackPendingIntent = PendingIntent.getBroadcast(
            context, notificationId, ackIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze action
        val snoozeIntent = Intent(context, SnoozeReceiver::class.java).apply {
            putExtra(NotificationConstants.EXTRA_REMINDER_TYPE, type.name)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context, notificationId + 1, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, NotificationConstants.CHANNEL_ID_REMINDER)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("${type.displayName}提醒")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setFullScreenIntent(null, true)
            .setAutoCancel(false)
            .setOngoing(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(android.R.drawable.ic_dialog_dialer, "知道了", ackPendingIntent)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "5分钟后再提醒", snoozePendingIntent)

        if (!vibrationEnabled) {
            notificationBuilder.setVibrate(longArrayOf(0L))
            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND)
        }

        val notification = notificationBuilder.build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS permission not granted
        }
    }

    fun rescheduleAfterBoot() {
        val dataStore = com.example.drinkreminder.data.settings.SettingsDataStore(context)
        runBlocking {
            val configs = dataStore.observeAllEnabledConfigs().first()
            configs.forEach { (type, config) ->
                scheduleReminders(type, config)
            }
        }
    }

    private fun getRequestCode(type: ReminderType, index: Int): Int {
        return type.ordinal * 1000 + index
    }
}
