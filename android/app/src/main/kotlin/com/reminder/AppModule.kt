package com.reminder

import com.reminder.core.notification.NotificationHelper
import com.reminder.data.settings.SettingsDataStore
import com.reminder.data.settings.SettingsRepository
import com.reminder.feature.log.LogViewModel
import com.reminder.feature.onboarding.OnboardingViewModel
import com.reminder.feature.reminderlist.ReminderListViewModel
import com.reminder.feature.settings.SettingsViewModel
import com.reminder.feature.water.WaterReminderViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Core
    single { NotificationHelper(androidApplication()) }
    single { SettingsDataStore(androidApplication()) }

    // Repository
    single { SettingsRepository(get()) }

    // ViewModels
    viewModel { OnboardingViewModel(get()) }
    viewModel { ReminderListViewModel(get()) }
    viewModel { WaterReminderViewModel(get(), get()) }
    viewModel { LogViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
}
