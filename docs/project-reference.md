# Project Reference

## Build Configuration

| Item | Value |
|------|-------|
| AGP | 8.2.2 |
| Kotlin | 1.9.22 |
| Gradle | 8.5 |
| compileSdk | 36 |
| minSdk | 28 |
| targetSdk | 36 |
| Compose BOM | 2024.02.02 |
| Compose Compiler | 1.5.10 |

## Key Configurations

- **JDK 17** required — set in `gradle.properties`
- **Proxy**: `127.0.0.1:7890` (for Chinese network) — set in `gradle.properties`
- **Android SDK**: `~/Library/Android/sdk` — set in `local.properties`
- **ProGuard**: Enabled for release; keeps Koin + core-model classes

## Known Issues

- **JDK version**: Requires JDK 17. JDK 11 will fail with AGP 8.2.2
- **Network**: Gradle distribution download may timeout in China — proxy configured
- **Launcher icons**: All mipmap densities must be present (hdpi through xxxhdpi)

## TODO

- Coding conventions: Kotlin style guide, Compose naming, package conventions
- Testing strategy: Test framework, test directory structure
- Environment setup: Full development environment setup steps
- Release process: Steps for building and publishing a release APK
- Architecture Decision Records: Why MVVM + Koin + DataStore were chosen
