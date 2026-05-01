package com.reminder.feature.reminderlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminder.core.model.ReminderState
import com.reminder.core.notification.NotificationHelper
import com.reminder.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReminderListViewModel(
    private val settingsRepository: SettingsRepository,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _reminders = MutableStateFlow<List<ReminderState>>(emptyList())
    val reminders: StateFlow<List<ReminderState>> = _reminders.asStateFlow()

    fun deleteReminder(id: String) {
        notificationHelper.cancelReminders(id)
        viewModelScope.launch {
            settingsRepository.deleteReminder(id)
        }
    }

    init {
        viewModelScope.launch {
            // Migrate old data to new format on first launch
            settingsRepository.migrateNow()

            settingsRepository.observeAllReminders().collect { reminderDataList ->
                _reminders.value = reminderDataList.map { ReminderState(data = it) }
            }
        }
    }
}
