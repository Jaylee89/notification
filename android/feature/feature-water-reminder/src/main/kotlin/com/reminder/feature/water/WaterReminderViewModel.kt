package com.reminder.feature.water

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminder.core.model.ReminderData
import com.reminder.core.model.ScheduleConfig
import com.reminder.core.notification.NotificationHelper
import com.reminder.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WaterReminderViewModel(
    private val settingsRepository: SettingsRepository,
    private val notificationHelper: NotificationHelper,
    val reminderId: String,
    val isNewReminder: Boolean
) : ViewModel() {

    private val _config = MutableStateFlow(ScheduleConfig())
    val config: StateFlow<ScheduleConfig> = _config.asStateFlow()

    private val _savedConfig = MutableStateFlow(ScheduleConfig())

    private val _reminderName = MutableStateFlow("")
    val reminderName: StateFlow<String> = _reminderName.asStateFlow()

    private val _hasPendingChanges = MutableStateFlow(false)
    val hasPendingChanges: StateFlow<Boolean> = _hasPendingChanges.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.observeReminder(reminderId).collect { reminderData ->
                if (reminderData != null) {
                    _config.value = reminderData.config
                    _savedConfig.value = reminderData.config
                    _reminderName.value = reminderData.name
                }
                _hasPendingChanges.value = false
            }
        }
    }

    fun toggleEnabled(enabled: Boolean) {
        _config.value = _config.value.copy(enabled = enabled)
        checkPendingChanges()
    }

    fun setStartHour(hour: Int) {
        _config.value = _config.value.copy(startHour = hour)
        checkPendingChanges()
    }

    fun setStartMinute(minute: Int) {
        _config.value = _config.value.copy(startMinute = minute)
        checkPendingChanges()
    }

    fun setEndHour(hour: Int) {
        _config.value = _config.value.copy(endHour = hour)
        checkPendingChanges()
    }

    fun setEndMinute(minute: Int) {
        _config.value = _config.value.copy(endMinute = minute)
        checkPendingChanges()
    }

    fun setInterval(minutes: Int) {
        _config.value = _config.value.copy(intervalMinutes = minutes)
        checkPendingChanges()
    }

    fun setName(name: String) {
        _reminderName.value = name
        checkPendingChanges()
    }

    fun save(onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            val config = _config.value
            val name = _reminderName.value.ifBlank { "提醒" }
            val reminder = ReminderData(
                id = reminderId,
                name = name,
                config = config
            )
            settingsRepository.saveReminder(reminder)

            // Cancel old alarms and schedule new ones
            notificationHelper.cancelReminders(reminderId)
            if (config.enabled) {
                notificationHelper.scheduleReminders(reminder, config)
            }

            _savedConfig.value = config
            _hasPendingChanges.value = false
            onSaved()
        }
    }

    private fun checkPendingChanges() {
        _hasPendingChanges.value = _config.value != _savedConfig.value
    }
}
