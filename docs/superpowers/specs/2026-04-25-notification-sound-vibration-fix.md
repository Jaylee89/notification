# 通知声音振动修复

## Bug 描述

通知日志已有提醒记录，但用户听不到提示音，也没有振动。

## 根因分析

在 `NotificationHelper.showNotification()` 中，`vibrationEnabled` 条件的处理逻辑写反了。

### 问题代码段

```kotlin
// NotificationHelper.kt 第 120-123 行
if (!vibrationEnabled) {
    notificationBuilder.setVibrate(longArrayOf(0L))
    notificationBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND)
}
```

问题：
1. **条件反转** — `vibrationEnabled = true` 时（默认设置），跳过整个代码块，既不设置 sound 也不设置 vibrate。
2. **`setVibrate(longArrayOf(0L))` 放错分支** — 这个调用实际上是**关闭振动**的正确方式，但被错误地放在了 `!vibrationEnabled` 分支里。也就是说当用户关闭振动时，反而会生效（逻辑上正确但 unintentional），而用户期望振动时却什么也没发生。

## 修复

```kotlin
if (vibrationEnabled) {
    notificationBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
} else {
    notificationBuilder.setVibrate(longArrayOf(0L))
}
```

### 修复说明

- `vibrationEnabled = true`：通过 `setDefaults` 同时启用系统的默认提示音和振动
- `vibrationEnabled = false`：通过 `setVibrate(longArrayOf(0L))` 显式关闭振动

注意：`NotificationChannel` 使用的是 `IMPORTANCE_HIGH`，在 Android 8.0+ 上默认会播放声音和振动。但如果用户在系统设置中手动修改了通道配置，则需要额外的通道更新逻辑。当前修复覆盖了 `NotificationCompat` 层的默认行为。
