package com.reminder.core.notification

object NotificationConstants {
    const val CHANNEL_ID_REMINDER = "drink_reminder"
    const val CHANNEL_NAME_REMINDER = "提醒"
    const val CHANNEL_DESC_REMINDER = "定时提醒通知"

    const val EXTRA_REMINDER_ID = "extra_reminder_id"
    const val EXTRA_REMINDER_NAME = "extra_reminder_name"
    const val EXTRA_MESSAGE = "extra_message"

    const val ACTION_SNOOZE = "com.reminder.action.SNOOZE"
    const val ACTION_ACKNOWLEDGE = "com.reminder.action.ACKNOWLEDGE"
}
