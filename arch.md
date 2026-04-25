# 喝水提醒 App — 架构文档

> 为老人提供白天定时喝水提醒，支持 TTS 语音播报，可扩展至吃药/吃饭等提醒类型。

---

## 技术栈

| 项目 | 选型 |
|------|------|
| 语言 | Kotlin |
| 最低 SDK | Android 9 (API 28) |
| 编译 SDK | Android 12 (API 31) |
| UI 框架 | Jetpack Compose + Material3 |
| 架构模式 | MVVM |
| DI 框架 | Koin |
| 导航 | Jetpack Navigation Compose |
| 本地存储 | DataStore |
| 定时调度 | AlarmManager |
| 构建工具 | Gradle Kotlin DSL |

---

## 页面结构

```
引导页 (Onboarding)    ← 首次安装展示
    │
    ▼
提醒类型列表页          ← 主页，列出已启用的提醒
    │
    ├──→ 提醒详情配置页 ← 配置单个提醒的时间段、间隔等
    │
    ├──→ 提醒日志页    ← 当日触发记录
    │
    └──→ 设置页        ← 音量、震动、语言等全局偏好
```

| 页面 | 路由 | 说明 |
|------|------|------|
| 引导页 | `/onboarding` | 首次安装展示，大图+大字介绍功能 |
| 提醒类型列表页 | `/reminders` | 默认主页，展示所有提醒类型 |
| 提醒详情配置页 | `/reminder/{type}` | 配置时间段、间隔、开关 |
| 提醒日志页 | `/logs` | 今日已触发和待触发的提醒 |
| 设置页 | `/settings` | 全局偏好（音量、震动等） |

---

## 模块结构

```
apps/notification/android/
├── app/                          # 壳工程 (Application, Navigation, Koin)
│   └── src/main/java/.../
│
├── core/
│   ├── core-designsystem/        # Material3 主题、大字体排版、通用 Composable
│   ├── core-model/               # 领域模型、ReminderContract 接口
│   └── core-notification/        # 通知渠道、AlarmManager 调度、TTS 语音
│
├── feature/
│   ├── feature-onboarding/       # 首次引导页
│   ├── feature-reminder-list/    # 提醒类型列表页 (主页)
│   ├── feature-water-reminder/   # 喝水提醒详情
│   ├── feature-log/              # 提醒日志页
│   └── feature-settings/         # 全局设置页
│
└── data/
    └── data-settings/            # DataStore 持久化
```

### 模块依赖关系

```
feature-onboarding ──→ core-designsystem
feature-reminder-list ──→ core-model, core-designsystem
feature-water-reminder ──→ core-model, core-notification, core-designsystem, data-settings
feature-log ──→ core-model, data-settings
feature-settings ──→ core-model, data-settings
app ──→ 所有 feature 模块
core-notification ──→ core-model
core-designsystem ──→ 无
core-model ──→ 无
data-settings ──→ core-model
```

---

## 核心接口设计

```kotlin
// ReminderType.kt — core-model
enum class ReminderType(val displayName: String, val route: String) {
    WATER("喝水", "water"),
    MEDICINE("吃药", "medicine"),
    MEAL("吃饭", "meal")
}

// ReminderContract.kt — core-model
interface ReminderContract {
    val type: ReminderType
    val description: String
    val defaultConfig: ScheduleConfig
    @Composable
    fun DetailScreen(onBack: () -> Unit)
}

// ScheduleConfig.kt — core-model
data class ScheduleConfig(
    val enabled: Boolean = false,
    val startHour: Int = 8,
    val startMinute: Int = 0,
    val endHour: Int = 20,
    val endMinute: Int = 0,
    val intervalMinutes: Int = 60
)
```

---

## 调度流程

```
用户保存配置
    → data-settings 写入 DataStore
    → core-notification 计算当天所有触发时间
    → 对每个时间点 AlarmManager.setExactAndAllowWhileIdle()
    → 闹钟触发 → BroadcastReceiver.onReceive()
    → NotificationManager 弹出通知 + TTS 语音播报
    → 记录日志
    → 所有触发完成后 → 等待次日重新注册

开机自启
    → BOOT_COMPLETED 广播
    → 读取 DataStore 配置
    → 重新注册当天所有闹钟
```

---

## 后台保活

| 策略 | 实现 |
|------|------|
| Doze 模式 | `AlarmManager.setExactAndAllowWhileIdle()` Android 9+ 支持 |
| 开机自启 | `BOOT_COMPLETED` BroadcastReceiver |
| 厂商白名单 | 引导页指引用户设置（华为/小米/OPPO/vivo） |

---

## UI 设计原则

- **大字体**: 标题 24sp、正文 18sp、按钮 20sp
- **高对比度**: 深色文字 + 浅色背景
- **大触摸目标**: 按钮最小 56dp
- **暖色调**: 温暖舒适的视觉风格
- **减少层级**: 核心操作一屏可见，不超过二级导航

---

## 扩展新提醒类型

1. 新增模块 `feature-xxx-reminder`
2. 实现 `ReminderContract` 接口
3. 在 `feature-reminder-list` 中注册类型
4. 在 `app` 中注册导航路由
5. 已有模块无需修改
