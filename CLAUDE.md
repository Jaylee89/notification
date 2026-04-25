# CLAUDE.md

Behavioral guidelines and project quick reference.

## Guidelines

- **Be explicit**: State assumptions, surface tradeoffs, ask when uncertain.
- **Minimum code**: Solve the problem. No speculative features or abstractions.
- **Surgical changes**: Touch only what's needed. Clean up only unused code from YOUR changes.
- **Goal-driven**: Define success criteria, verify, loop until done.

## Project: DrinkReminder

Android water reminder app (Compose + Material3 + MVVM, elderly-friendly UI).

```bash
# Build & install
./gradlew :app:assembleDebug
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.reminder/.MainActivity

# Check compilation
./gradlew :module:compileDebugKotlin
```

## Quick Links

| Info | File |
|------|------|
| Architecture & design | `arch.md` |
| Project details | `docs/project-reference.md` |

## Rules

- Write .md files in Chinese under `docs/superpowers`
