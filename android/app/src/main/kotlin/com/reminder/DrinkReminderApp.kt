package com.reminder

import android.app.Application
import com.reminder.core.notification.NotificationHelper
import com.reminder.core.notification.ReminderService
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DrinkReminderApp : Application() {

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
    }
}
