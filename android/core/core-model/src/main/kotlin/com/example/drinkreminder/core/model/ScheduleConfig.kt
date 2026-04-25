package com.example.drinkreminder.core.model

import java.util.Calendar

data class ScheduleConfig(
    val enabled: Boolean = false,
    val startHour: Int = 8,
    val startMinute: Int = 0,
    val endHour: Int = 20,
    val endMinute: Int = 0,
    val intervalMinutes: Int = 60
) {
    fun generateTodayTriggers(): List<TriggerTime> {
        if (!enabled) return emptyList()

        val triggers = mutableListOf<TriggerTime>()
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis

        val startMillis = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endMillis = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, endHour)
            set(Calendar.MINUTE, endMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        var current = startMillis
        while (current <= endMillis) {
            if (current >= now) {
                val trigCal = Calendar.getInstance().apply { timeInMillis = current }
                triggers.add(
                    TriggerTime(
                        hour = trigCal.get(Calendar.HOUR_OF_DAY),
                        minute = trigCal.get(Calendar.MINUTE)
                    )
                )
            }
            current += intervalMinutes * 60_000L
        }

        return triggers
    }
}

data class TriggerTime(
    val hour: Int,
    val minute: Int
) {
    fun toCalendar(): Calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}
