package com.example.drinkreminder.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drinkreminder.data.settings.SettingsRepository
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted()
        }
    }
}
