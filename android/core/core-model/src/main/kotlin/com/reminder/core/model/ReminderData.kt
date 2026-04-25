package com.reminder.core.model

import java.util.UUID

data class ReminderData(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val config: ScheduleConfig
)
