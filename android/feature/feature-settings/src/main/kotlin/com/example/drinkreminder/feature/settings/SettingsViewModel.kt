package com.example.drinkreminder.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drinkreminder.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _vibrationEnabled = MutableStateFlow(true)
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private val _ttsEnabled = MutableStateFlow(true)
    val ttsEnabled: StateFlow<Boolean> = _ttsEnabled.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.observeVibrationEnabled().collect { _vibrationEnabled.value = it }
        }
        viewModelScope.launch {
            settingsRepository.observeTtsEnabled().collect { _ttsEnabled.value = it }
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setVibrationEnabled(enabled)
        }
    }

    fun setTtsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setTtsEnabled(enabled)
        }
    }
}
