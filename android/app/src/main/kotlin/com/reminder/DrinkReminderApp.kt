package com.reminder

import android.app.Application
import com.reminder.core.notification.NotificationHelper
import com.reminder.core.notification.ReminderService
import com.reminder.data.settings.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DrinkReminderApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@DrinkReminderApp)
            modules(appModule)
        }

        // Create notification channel on app start
        NotificationHelper(this).createNotificationChannel()

        // Start foreground service to keep reminder process alive
        ReminderService.start(this)

        // Reschedule all enabled reminders — AlarmManager alarms are lost after process death
        appScope.launch {
            val dataStore = SettingsDataStore(this@DrinkReminderApp)
            val helper = NotificationHelper(this@DrinkReminderApp)
            val reminders = dataStore.getAllReminders()
            reminders.forEach { reminder ->
                if (reminder.config.enabled) {
                    helper.scheduleReminders(reminder, reminder.config)
                }
            }
        }
    }
}
