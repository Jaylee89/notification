package com.reminder.feature.reminderlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminder.core.model.ReminderState
import com.reminder.core.model.ReminderType
import com.reminder.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ReminderListViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _reminders = MutableStateFlow<List<ReminderState>>(emptyList())
    val reminders: StateFlow<List<ReminderState>> = _reminders.asStateFlow()

    fun deleteReminder(type: ReminderType) {
        viewModelScope.launch {
            settingsRepository.deleteScheduleConfig(type)
        }
    }

    init {
        viewModelScope.launch {
            val flows = ReminderType.values().map { type ->
                settingsRepository.observeScheduleConfig(type)
            }
            combine(flows) { configs ->
                ReminderType.values().mapIndexed { index, type ->
                    ReminderState(type = type, config = configs[index])
                }
            }.collect { states ->
                _reminders.value = states
            }
        }
    }
}
