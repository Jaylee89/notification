package com.reminder.core.model

data class ReminderLog(
    val type: ReminderType,
    val triggeredAtMillis: Long,
    val message: String,
    val wasAcknowledged: Boolean = false
)
