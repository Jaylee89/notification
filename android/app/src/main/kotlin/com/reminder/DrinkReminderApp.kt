package com.reminder

import android.app.Application
import com.reminder.core.notification.NotificationHelper
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
    }
}
