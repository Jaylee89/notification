package com.reminder.core.model

data class ReminderState(
    val type: ReminderType,
    val config: ScheduleConfig,
    val todayLogs: List<ReminderLog> = emptyList()
) {
    val isActive: Boolean get() = config.enabled
    val todayCompletedCount: Int get() = todayLogs.count { it.wasAcknowledged }
    val todayPendingCount: Int get() = config.generateTodayTriggers().size - todayCompletedCount
    val displayName: String get() = config.customName ?: type.displayName
}
