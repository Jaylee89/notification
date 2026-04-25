# 喝水提醒 (DrinkReminder)

> 为老年人设计的白天定时喝水提醒 Android 应用，支持 TTS 语音播报，可扩展至吃药、吃饭等提醒类型。

## 项目概述

- **包名**: `com.reminder`
- **最低支持**: Android 9 (API 28)
- **目标用户**: 老年人 — 高对比度暖色配色、大字体、大触摸目标
- **界面语言**: 中文

### 功能特性

- 定时喝水提醒，支持自定义时间段和间隔
- TTS 语音播报提醒内容
- 支持震动开关
- 当日提醒日志查看
- 开机自启，自动重新注册闹钟
- 模块化架构，方便扩展吃药/吃饭等提醒类型

### 技术栈

| 项目 | 选型 |
|------|------|
| 语言 | Kotlin |
| UI 框架 | Jetpack Compose + Material3 |
| 架构模式 | MVVM |
| DI 框架 | Koin |
| 导航 | Jetpack Navigation Compose |
| 本地存储 | Jetpack DataStore Preferences |
| 定时调度 | AlarmManager |
| 构建工具 | Gradle Kotlin DSL (Gradle 8.5) |
| AGP | 8.2.2 |
| JDK | 17 |

### 项目结构

```
android/
├── app/                          # 壳工程 — Application、Navigation、Koin DI
├── core/
│   ├── core-designsystem/        # Material3 主题、通用 Composable 组件
│   ├── core-model/               # 领域模型 (ReminderType, ScheduleConfig 等)
│   └── core-notification/        # 通知渠道、AlarmManager 调度、TTS 语音播报
├── data/
│   └── data-settings/            # DataStore 偏好设置持久化
├── feature/
│   ├── feature-onboarding/       # 首次使用引导页
│   ├── feature-reminder-list/    # 提醒类型列表（主页）
│   ├── feature-water-reminder/   # 喝水提醒详情配置
│   ├── feature-log/              # 提醒日志
│   └── feature-settings/         # 全局设置（音量、震动等）
├── build.gradle.kts              # 根构建脚本
├── settings.gradle.kts           # 11 个模块注册
├── gradle.properties             # JDK 17、代理配置
└── local.properties              # Android SDK 路径
```

### 页面路由

| 页面 | 路由 | 说明 |
|------|------|------|
| 引导页 | `/onboarding` | 首次安装展示功能介绍 |
| 提醒列表 | `/reminders` | 默认主页，列出所有提醒类型 |
| 提醒详情 | `/reminder/{type}` | 配置时间段、间隔、开关 |
| 提醒日志 | `/logs` | 当日已触发和待触发的提醒 |
| 设置 | `/settings` | 全局偏好（TTS、震动等） |

## 构建与运行

### 环境要求

- JDK 17
- Android SDK（API 28-36）
- 连接 Android 设备或启动模拟器（Android 9+）

### 构建命令

```bash
# 构建完整 Debug APK
cd android
./gradlew :app:assembleDebug
```

### 安装到设备

```bash
# 安装 APK
adb install -r android/app/build/outputs/apk/debug/app-debug.apk

# 启动应用
adb shell am start -n com.reminder/.MainActivity
```

### 单模块编译检查

```bash
./gradlew :module:compileDebugKotlin
```

### 网络代理

如果 Gradle 下载依赖超时，`gradle.properties` 中已配置代理 `127.0.0.1:7890`。

## 架构说明

详见 [arch.md](arch.md) — 包含模块依赖图、核心接口设计、调度流程、后台保活策略及扩展新提醒类型的步骤。
