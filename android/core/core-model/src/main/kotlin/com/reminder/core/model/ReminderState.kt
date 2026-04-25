package com.reminder.core.model

data class ReminderState(
    val data: ReminderData,
    val todayLogs: List<ReminderLog> = emptyList()
) {
    val id: String get() = data.id
    val isActive: Boolean get() = data.config.enabled
    val displayName: String get() = data.name
    val todayCompletedCount: Int get() = todayLogs.count { it.wasAcknowledged }
    val todayPendingCount: Int get() = data.config.generateTodayTriggers().size - todayCompletedCount
}
