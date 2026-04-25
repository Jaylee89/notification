package com.example.drinkreminder.core.model

enum class ReminderType(val displayName: String, val route: String) {
    WATER("喝水", "water"),
    MEDICINE("吃药", "medicine"),
    MEAL("吃饭", "meal")
}
