# Bug 修复第二轮 — Drink Reminder Android App

## Bug 1: 移除引导页 "开始使用" 按钮

### 现状
`OnboardingScreen` 已有 `LaunchedEffect` 在 3 秒后自动执行 `onComplete()`，底部仍有冗余的 "开始使用" `ElderlyButton`。

### 修改
- 删除底部包含 `ElderlyButton` 的 `Column(weight=0.4f)` 区块
- 清理未使用的 import（`ElderlyButton`, `RoundedCornerShape`）

### 涉及文件
- `feature/feature-onboarding/src/main/kotlin/com/reminder/feature/onboarding/OnboardingScreen.kt`

---

## Bug 2: 滑动删除修复

### 现状
`ReminderListScreen` 仅对非活跃卡片（`!state.isActive`）启用 `SwipeToDismissBox`。`backgroundContent` 占满整张卡片（`fillMaxSize()`），且不可点击。反向滑动被禁用（`enableDismissFromStartToEnd = false`）。

### 修改

1. **所有卡片支持滑动** — 移除 `if (!state.isActive)` 分支，所有卡片都包裹 `SwipeToDismissBox`
2. **删除按钮只占右侧 1/3** — `backgroundContent` 改用 `fillMaxWidth(0.33f)` + `align(Alignment.CenterEnd)`，保持红色背景和删除图标
3. **点击删除触发删除** — 给背景 `Box` 添加 `clickable` 修饰符，点击后调用 `viewModel.deleteReminder(state.type)` 并复位 `dismissState`
4. **反向滑动隐藏删除按钮** — 删除 `enableDismissFromStartToEnd = false`，`SwipeToDismissBox` 在滑动不足阈值或反向滑动时自然回弹

### 涉及文件
- `feature/feature-reminder-list/src/main/kotlin/com/reminder/feature/reminderlist/ReminderListScreen.kt`

---

## Bug 3: FAB (+) 按钮 — 添加提醒页面改造

### 现状
FAB 导航到 `WaterReminderScreen`，标题为 "{type}提醒"，无名输入框，无底部摘要文本。名称使用硬编码的 `ReminderType.displayName`。

### 修改

1. **标题改为 "添加"** — `WaterReminderViewModel` 暴露 `isNewReminder` 标志（config 为默认值时判定为新建）。`WaterReminderScreen` 据此显示 "添加" 或原类型名称
2. **添加名称输入框** — `ScheduleConfig` 增加 `customName: String? = null` 字段。界面顶部添加 `OutlinedTextField`，默认预填 `reminderType.displayName`。ViewModel 提供 `setCustomName(name: String)`
3. **底部固定摘要** — 页面底部常显文字：`"每 {intervalMinutes} 分钟提醒 {name}"`（如 "每 60 分钟提醒 喝水"）

### 数据模型变更
`ScheduleConfig` 增加可选字段 `customName: String? = null`。当该字段非空时，`ReminderState` 在提醒列表卡片中使用此名称而非 `type.displayName`。

### 涉及文件
- `core/core-model/src/main/kotlin/com/reminder/core/model/ScheduleConfig.kt` — 新增 `customName` 字段
- `feature/feature-water-reminder/.../WaterReminderScreen.kt` — 标题逻辑、文本输入框、摘要
- `feature/feature-water-reminder/.../WaterReminderViewModel.kt` — `isNewReminder`、`setCustomName`
- `feature/feature-reminder-list/.../ReminderListScreen.kt` — `ReminderCard` 使用 `customName`
- `core/core-model/src/main/kotlin/com/reminder/core/model/ReminderState.kt` — 新增 `displayName` getter
