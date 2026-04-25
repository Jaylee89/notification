# DrinkReminder Android Project

## Project Overview

- **Name**: DrinkReminder（喝水提醒）
- **App ID**: `com.reminder`
- **Target**: Elderly users — high-contrast warm color scheme, large UI components
- **Language**: Chinese (app name, reminder type display names)
- **Status**: Water reminder fully implemented; Medicine & Meal are TODO stubs

## Build & Install Workflow

**After every `assembleDebug` build, ALWAYS do:**

```bash
# 1. Install APK to connected device
adb install -r android/app/build/outputs/apk/debug/app-debug.apk

# 2. Launch the app
adb shell am start -n com.reminder/.MainActivity
```

This is the standard delivery flow — build, install, launch.

## Project Structure

```
android/
├── build.gradle.kts                   # Root build script (AGP 8.2.2, Kotlin 1.9.22)
├── settings.gradle.kts                # 11 modules registered
├── gradle.properties                  # JDK 17, proxy config
├── local.properties                   # Android SDK path
├── gradlew / gradlew.bat              # Gradle wrapper (Gradle 8.5)
├── gradle/
│   ├── libs.versions.toml             # Version catalog
│   └── wrapper/
├── app/                               # :app — main application module
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── kotlin/com/reminder/
│       │   ├── DrinkReminderApp.kt      # Application class
│       │   ├── MainActivity.kt          # Single activity entry
│       │   ├── AppModule.kt             # Koin DI module
│       │   └── navigation/
│       │       └── NavGraph.kt          # Compose Navigation
│       └── res/                          # Launcher icons, strings, themes
├── core/
│   ├── core-designsystem/              # :core:core-designsystem
│   │   └── theme/ (Color.kt, Theme.kt, Type.kt)
│   │   └── components/ (ElderlyButton, ElderlySwitch, ElderlyTimePicker)
│   ├── core-model/                     # :core:core-model
│   │   └── ReminderType, ReminderContract, ReminderLog, ReminderState, ScheduleConfig
│   └── core-notification/              # :core:core-notification
│       └── NotificationHelper, ReminderReceiver, BootReceiver, TTSManager, etc.
├── data/
│   └── data-settings/                  # :data:data-settings
│       └── SettingsDataStore, SettingsRepository (DataStore Preferences)
└── feature/
    ├── feature-onboarding/             # OnboardingScreen + ViewModel
    ├── feature-reminder-list/          # ReminderListScreen + ViewModel
    ├── feature-water-reminder/         # WaterReminderScreen + ViewModel
    ├── feature-log/                    # LogScreen + ViewModel
    └── feature-settings/               # SettingsScreen + ViewModel
```

## Dependency Graph

```
app ──┬── core-designsystem (compose-only, no project deps)
      ├── core-model (compose-only, no project deps)
      ├── core-notification → core-model
      ├── data-settings → core-model
      ├── feature-onboarding → core-designsystem, data-settings
      ├── feature-reminder-list → core-model, core-designsystem, data-settings
      ├── feature-water-reminder → core-model, core-designsystem, core-notification, data-settings
      ├── feature-log → core-model, data-settings
      └── feature-settings → core-model, core-designsystem, data-settings
```

## Build Configuration

| Item | Value |
|---|---|
| AGP | 8.2.2 |
| Kotlin | 1.9.22 |
| Gradle | 8.5 |
| compileSdk | 36 |
| minSdk | 28 |
| targetSdk | 36 |
| Compose BOM | 2024.02.02 |
| Compose Compiler | 1.5.10 |

## Architecture

- **UI**: Jetpack Compose + Material3
- **Pattern**: MVVM (Screen + ViewModel per feature)
- **DI**: Koin (core + android + compose)
- **Data**: Jetpack DataStore Preferences
- **Notifications**: AlarmManager + BroadcastReceivers + TTS
- **Navigation**: Compose Navigation — 5 routes (onboarding, reminders, reminder/{type}, logs, settings)

## Key Configurations

- **JDK 17** required — set in `gradle.properties`
- **Proxy**: `127.0.0.1:7890` (for Chinese network) — set in `gradle.properties`
- **Android SDK**: `~/Library/Android/sdk` — set in `local.properties`
- **ProGuard**: Enabled for release; keeps Koin + core-model classes

## Build Commands

```bash
./gradlew :app:assembleDebug          # Full debug APK
./gradlew :module:compileDebugKotlin  # Check compilation for a specific module
```

## Known Issues

- **JDK version**: Requires JDK 17. JDK 11 will fail with AGP 8.2.2
- **Network**: Gradle distribution download may timeout in China — proxy configured
- **Launcher icons**: All mipmap densities must be present (hdpi through xxxhdpi)

---

# TODO: Consider adding to this file

- **Coding Conventions**: Kotlin style guide, Compose naming, package conventions
- **Testing Strategy**: Test framework, test directory structure (currently no tests exist)
- **Environment Setup**: Full development environment setup steps
- **Release Process**: Steps for building and publishing a release APK
- **Architecture Decision Records**: Why MVVM + Koin + DataStore were chosen
