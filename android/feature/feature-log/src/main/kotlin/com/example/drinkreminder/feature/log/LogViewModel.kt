package com.example.drinkreminder.feature.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drinkreminder.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LogViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _logs = MutableStateFlow(emptyList<String>())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.observeTodayLogs().collect { logList ->
                _logs.value = logList
            }
        }
    }
}
