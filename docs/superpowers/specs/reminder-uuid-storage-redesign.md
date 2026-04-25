# 提醒系统改造：基于UUID的独立提醒存储

## 问题

点击右下角的 "+" 按钮添加新提醒时（如 "喝水"），会覆盖已有的同名提醒按钮。根本原因是 `ReminderType` 枚举（WATER、MEDICINE、MEAL）被用作 DataStore 的存储键，每个枚举值只能对应一组配置。

## 解决方案

引入基于 UUID 的独立提醒存储，将提醒的身份标识从 `ReminderType` 解耦。

### 核心改动

1. **新增 `ReminderData` 数据模型** — 包含 `id`（UUID）、`name`（自由文本）、`config`（ScheduleConfig）
2. **SettingsDataStore 存储层改造** — 从基于 `ReminderType` 的 7 个独立偏好键改为统一的 JSON 字符串存储
3. **全链路适配** — Navigation、ViewModel、NotificationHelper 均改为使用 `reminderId` 字符串

### 数据格式

```
<id>|<name>|<enabled>|<startHour>|<startMinute>|<endHour>|<endMinute>|<intervalMinutes>|<customName>
```

所有提醒以换行符分隔存储在 `reminders_json` 偏好键下。

### 迁移逻辑

应用首次启动时自动检测旧格式数据（`WATER_enabled` 等键），将其转换为新格式并清除旧键。迁移幂等，通过 `migration_v2_done` 标志控制。

### 涉及文件

| 文件 | 改动 |
|------|------|
| `core/core-model/.../ReminderData.kt` | 新增 |
| `core/core-model/.../ReminderState.kt` | 适配新模型 |
| `data/data-settings/.../SettingsDataStore.kt` | 重写存储层 |
| `data/data-settings/.../SettingsRepository.kt` | 更新方法签名 |
| `feature/feature-reminder-list/.../ReminderListScreen.kt` | "+" 生成 UUID |
| `feature/feature-reminder-list/.../ReminderListViewModel.kt` | 新存储 API |
| `feature/feature-water-reminder/.../WaterReminderViewModel.kt` | UUID 参数 |
| `feature/feature-water-reminder/.../WaterReminderScreen.kt` | 名称输入 |
| `app/.../navigation/NavGraph.kt` | 路由改动 |
| `app/.../AppModule.kt` | 移除 VM 注册 |
| `core/core-notification/.../*.kt` | 通知适配 UUID |
| `core/core-notification/.../NotificationConstants.kt` | Extra 键名更新 |
