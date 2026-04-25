package com.example.drinkreminder

import com.example.drinkreminder.core.notification.NotificationHelper
import com.example.drinkreminder.data.settings.SettingsDataStore
import com.example.drinkreminder.data.settings.SettingsRepository
import com.example.drinkreminder.feature.log.LogViewModel
import com.example.drinkreminder.feature.onboarding.OnboardingViewModel
import com.example.drinkreminder.feature.reminderlist.ReminderListViewModel
import com.example.drinkreminder.feature.settings.SettingsViewModel
import com.example.drinkreminder.feature.water.WaterReminderViewModel
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
