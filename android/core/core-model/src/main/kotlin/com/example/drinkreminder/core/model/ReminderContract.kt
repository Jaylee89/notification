package com.example.drinkreminder.core.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

interface ReminderContract {
    val type: ReminderType
    val displayName: String
    val description: String
    val icon: ImageVector
    val defaultConfig: ScheduleConfig

    @Composable
    fun DetailScreen(onBack: () -> Unit)
}
