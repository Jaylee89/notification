# App 被 Kill 后重启提醒不工作的问题分析

## 问题描述

App 被系统 kill 或手动划掉后，重新打开 app，虽然某些 task 显示为开启状态，但到时间不会弹出提醒。

## 根因

**AlarmManager 的闹钟在进程被 kill 后会被系统清除，但 app 重启时没有重新注册这些闹钟。**

具体来说：

1. AlarmManager 闹钟与进程生命周期绑定 —— 进程被杀后，所有通过 `setExactAndAllowWhileIdle()` 注册的闹钟都会被系统清除
2. `BootReceiver.kt` 虽然实现了 `rescheduleAfterBoot()`，但它仅监听 `ACTION_BOOT_COMPLETED`（系统开机广播）
3. 用户手动点击图标重启 app 时，走的是 `DrinkReminderApp.onCreate()`，但其中**没有重新调度闹钟**的逻辑

所以流程是：
- 用户设置提醒 → 保存到 DataStore + 注册 AlarmManager ✅
- App 被 kill → AlarmManager 闹钟丢失 ❌
- 用户重新打开 app → DataStore 中还存着 enabled=true，但 AlarmManager 中已没有注册 → 到时间不触发 ❌

## 修复

在 `DrinkReminderApp.onCreate()` 中增加了重新调度逻辑：

```kotlin
appScope.launch {
    val dataStore = SettingsDataStore(this@DrinkReminderApp)
    val helper = NotificationHelper(this@DrinkReminderApp)
    val reminders = dataStore.getAllReminders()
    reminders.forEach { reminder ->
        if (reminder.config.enabled) {
            helper.scheduleReminders(reminder, reminder.config)
        }
    }
}
```

每次 app 启动时，从 DataStore 读取所有已开启的 reminder，重新注册到 AlarmManager。

## 注意事项

- `scheduleReminders()` 内部会先调用 `cancelReminders()` 再注册，不会导致重复
- 使用 `appScope`（非 ViewModel scope），不依赖任何 UI 组件的生命周期
- 对已经超过今天时间窗口的 trigger，`ScheduleConfig.generateTodayTriggers()` 内部已有 `current >= now` 的判断，不会注册过去的时间点
