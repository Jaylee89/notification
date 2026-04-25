# DrinkReminder ProGuard Rules

# Keep Koin
-keep class org.koin.** { *; }

# Keep model classes (used in serialization/preferences)
-keep class com.reminder.core.model.** { *; }
