# Bug 修复第二轮 — 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**目标:** 修复 Android Drink Reminder 应用的 3 组 UI/交互 bug
**架构:** MVVM + Jetpack Compose + DataStore。模型层修改 `ScheduleConfig` 增加 `customName` 字段并持久化；UI 层调整 OnboardingScreen、ReminderListScreen、WaterReminderScreen
**技术栈:** Kotlin, Jetpack Compose, Material3, DataStore Preferences

---

### Task 1: 数据模型 — ScheduleConfig 增加 customName 字段

**文件:**
- 修改: `android/core/core-model/src/main/kotlin/com/reminder/core/model/ScheduleConfig.kt`
- 修改: `android/data/data-settings/src/main/kotlin/com/reminder/data/settings/SettingsDataStore.kt`

- [ ] **Step 1: ScheduleConfig 添加 customName 字段**

```kotlin
data class ScheduleConfig(
    val enabled: Boolean = false,
    val startHour: Int = 8,
    val startMinute: Int = 0,
    val endHour: Int = 20,
    val endMinute: Int = 0,
    val intervalMinutes: Int = 60,
    val customName: String? = null
)
```

- [ ] **Step 2: SettingsDataStore — observeScheduleConfig 读取 customName**

在 `observeScheduleConfig` 中，于 `intervalMinutes = ...` 行后增加：
```kotlin
customName = prefs[getCustomNameKey(type)]
```

- [ ] **Step 3: SettingsDataStore — saveScheduleConfig 写入 customName**

在 `saveScheduleConfig` 中，于 `prefs[getIntervalKey(type)] = config.intervalMinutes` 行后增加：
```kotlin
if (config.customName != null) {
    prefs[getCustomNameKey(type)] = config.customName!!
} else {
    prefs.remove(getCustomNameKey(type))
}
```

- [ ] **Step 4: SettingsDataStore — clearScheduleConfig 清理 customName**

在 `clearScheduleConfig` 中，于 `prefs.remove(getIntervalKey(type))` 行后增加：
```kotlin
prefs.remove(getCustomNameKey(type))
```

- [ ] **Step 5: SettingsDataStore — 添加 key 函数**

在 `getIntervalKey` 函数后增加：
```kotlin
private fun getCustomNameKey(type: ReminderType) =
    stringPreferencesKey("${type.name}_custom_name")
```

---

### Task 2: 数据模型 — ReminderState 添加 displayName getter

**文件:**
- 修改: `android/core/core-model/src/main/kotlin/com/reminder/core/model/ReminderState.kt`

- [ ] **Step 1: 添加 displayName 计算属性**

在 `todayPendingCount` 行后增加：
```kotlin
val displayName: String get() = config.customName ?: type.displayName
```

---

### Task 3: 移除引导页 "开始使用" 按钮

**文件:**
- 修改: `android/feature/feature-onboarding/src/main/kotlin/com/reminder/feature/onboarding/OnboardingScreen.kt`

- [ ] **Step 1: 删除按钮区块和清理 import**

删除第 112-130 行的底部 `Column(weight=0.4f)` 区块（含 `ElderlyButton`），同时删除未使用的 import：
- `import com.reminder.core.designsystem.components.ElderlyButton`
- `import androidx.compose.ui.draw.clip`
- `import androidx.compose.foundation.shape.RoundedCornerShape`

删除后，`fileMaxWidth` import 也可能不再需要（检查是否还在其他地方使用）。

---

### Task 4: 修复滑动删除行为

**文件:**
- 修改: `android/feature/feature-reminder-list/src/main/kotlin/com/reminder/feature/reminderlist/ReminderListScreen.kt`

- [ ] **Step 1: 所有卡片支持滑动删除**

将：
```kotlin
reminders.forEach { state ->
    if (!state.isActive) {
        val dismissState = ...
        SwipeToDismissBox(...) { ReminderCard(...) }
    } else {
        ReminderCard(...)
    }
}
```

改为：
```kotlin
reminders.forEach { state ->
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                viewModel.deleteReminder(state.type)
                true
            } else {
                false
            }
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.33f)
                    .fillMaxHeight()
                    .background(AlertRed, RoundedCornerShape(16.dp))
                    .clickable {
                        viewModel.deleteReminder(state.type)
                        dismissState.reset()
                    }
                    .align(Alignment.CenterEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) {
        ReminderCard(
            state = state,
            onClick = { onNavigateToDetail(state.type) }
        )
    }
}
```

注意：上面的 `fillMaxHeight()` 在 Compose 中需要检查可用性。更稳妥的做法是在 `Box` 中用 `Modifier.fillMaxSize(0.33f)` 或 `Modifier.fillMaxWidth(0.33f)` + 父 Box 用 `Row` 布局对齐。

实际代码：
```kotlin
// SwipeToDismissBox 的 backgroundContent 包在一个 Box 中
backgroundContent = {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 66.6f.coerceAtMost(...))  // 不精确
    )
}
```

更准确的做法：`SwipeToDismissBox` 的 `backgroundContent` 默认铺满整个容器。要只显示右侧 1/3，可以使用 `Row` + `Spacer`：
```kotlin
backgroundContent = {
    Row(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.weight(2f)) // 左侧 2/3 空白
        Box(                                   // 右侧 1/3 红色删除区
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .background(AlertRed, RoundedCornerShape(16.dp))
                .clickable {
                    viewModel.deleteReminder(state.type)
                    dismissState.reset()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "删除",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
```

- [ ] **Step 2: 添加缺失的 import**

```kotlin
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
```

---

### Task 5: NavGraph — 添加 isNew 路由参数

**文件:**
- 修改: `android/app/src/main/kotlin/com/reminder/navigation/NavGraph.kt`

- [ ] **Step 1: NavGraph 路由增加 isNew 参数**

修改 `REMINDER_DETAIL` 路由：
```kotlin
const val REMINDER_DETAIL = "reminder/{type}?isNew={isNew}"
```

更新 `reminderDetail` 函数：
```kotlin
fun reminderDetail(type: ReminderType, isNew: Boolean = false) =
    "reminder/${type.route}?isNew=$isNew"
```

- [ ] **Step 2: composable 参数列表增加 isNew**

```kotlin
composable(
    route = Routes.REMINDER_DETAIL,
    arguments = listOf(
        navArgument("type") { type = NavType.StringType },
        navArgument("isNew") { type = NavType.BoolType; defaultValue = false }
    )
) { backStackEntry ->
    val typeName = backStackEntry.arguments?.getString("type") ?: return@composable
    val isNew = backStackEntry.arguments?.getBoolean("isNew") ?: false
    val type = ReminderType.values().find { it.route == typeName } ?: return@composable
    ...
}
```

- [ ] **Step 3: FAB 传 isNew=true**

在 ReminderListScreen 中，FAB 的 `onNavigateToDetail` 调用处改为传 `isNew = true`。但 FAB 调用的是外部传入的 lambda，需要在 NavGraph 中处理。在 NavGraph 中：
```kotlin
onNavigateToDetail = { type ->
    navController.navigate(Routes.reminderDetail(type, isNew = true))
},
```

水、吃药、吃饭卡片点击传 `isNew = false`（默认），无需改动（Route.reminderDetail(type) 默认 isNew=false）。

---

### Task 6: WaterReminderViewModel — 添加新建标志和名称管理

**文件:**
- 修改: `android/feature/feature-water-reminder/src/main/kotlin/com/reminder/feature/water/WaterReminderViewModel.kt`

- [ ] **Step 1: 添加 isNewReminder 标志和 setCustomName**

```kotlin
class WaterReminderViewModel(
    private val settingsRepository: SettingsRepository,
    private val notificationHelper: NotificationHelper,
    val reminderType: ReminderType = ReminderType.WATER,
    val isNewReminder: Boolean = false
) : ViewModel() {
    // ... 现有字段 ...

    fun setCustomName(name: String) {
        _config.value = _config.value.copy(customName = name.ifBlank { null })
        checkPendingChanges()
    }
}
```

`isNewReminder` 参数在 NavGraph 中构造 ViewModel 时传入。

---

### Task 7: WaterReminderScreen — 标题、输入框、底部摘要

**文件:**
- 修改: `android/feature/feature-water-reminder/src/main/kotlin/com/reminder/feature/water/WaterReminderScreen.kt`

- [ ] **Step 1: 标题改为条件显示**

```kotlin
title = {
    Text(
        text = if (viewModel.isNewReminder) "添加" else "${viewModel.reminderType.displayName}提醒",
        style = MaterialTheme.typography.headlineLarge
    )
}
```

- [ ] **Step 2: 顶部添加名称输入框**

在关于"开启提醒"的 Card 之前（或之后），添加：
```kotlin
// Name input
Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
) {
    Column(modifier = Modifier.padding(20.dp)) {
        Text(
            text = "提醒名称",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = config.customName ?: viewModel.reminderType.displayName,
            onValueChange = { viewModel.setCustomName(it) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("输入提醒名称") }
        )
    }
}
```

- [ ] **Step 3: 底部添加摘要文本**

在 Column 末尾、Spacer 之前添加：
```kotlin
// Summary
Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer
    )
) {
    Row(
        modifier = Modifier.padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.WaterDrop,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "每 ${config.intervalMinutes} 分钟提醒 ${config.customName ?: viewModel.reminderType.displayName}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
```

- [ ] **Step 4: 添加缺失的 import**

```kotlin
import androidx.compose.material3.OutlinedTextField
```

---

### Task 8: ReminderListScreen 卡片显示自定义名称

**文件:**
- 修改: `android/feature/feature-reminder-list/src/main/kotlin/com/reminder/feature/reminderlist/ReminderListScreen.kt`

- [ ] **Step 1: ReminderCard 使用 state.displayName**

将 `ReminderCard` 中的：
```kotlin
text = state.type.displayName,
```

改为：
```kotlin
text = state.displayName,
```

---

### Task 9: NavGraph — 构建 ViewModel 时传入 isNewReminder

**文件:**
- 修改: `android/app/src/main/kotlin/com/reminder/navigation/NavGraph.kt`

- [ ] **Step 1: WATER 类型 ViewModel 传入 isNew**

从 NavGraph composable 中获取 `isNew` 参数，在构造 ViewModel 时传入：
```kotlin
ReminderType.WATER -> {
    // 注意：koinViewModel 不支持构造参数注入，需要回退到 ViewModelProvider.Factory
    val repo: SettingsRepository = koinInject()
    val notifHelper: NotificationHelper = koinInject()
    val viewModel = viewModel<WaterReminderViewModel>(
        key = "${type.name}_$isNew",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return WaterReminderViewModel(
                    settingsRepository = repo,
                    notificationHelper = notifHelper,
                    reminderType = type,
                    isNewReminder = isNew
                ) as T
            }
        }
    )
    WaterReminderScreen(...)
}
```

MEDICINE/MEAL 同理，把 `isNewReminder = isNew` 传入。

由于 WATER 原来用 `koinViewModel()`，现在需要统一改为 `ViewModelProvider.Factory` 方式（和 MEDICINE/MEAL 一致），以支持传 `isNewReminder` 参数。

---

### 验证步骤

- [ ] **编译检查**

```bash
cd /Users/jayleeli/work/AI/apps/notification/android
./gradlew :app:assembleDebug 2>&1 | tail -20
```

- [ ] **设备安装启动**

```bash
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.reminder/.MainActivity
```

- [ ] **手动测试清单**
  - 启动 app → 引导页 3 秒后自动消失，无 "开始使用" 按钮
  - 提醒列表 → 任意卡片从右向左滑动 → 右侧 1/3 显示红色删除按钮
  - 点击删除按钮 → 该卡片被删除
  - 滑动露出删除按钮后，从左向右滑 → 删除按钮平滑隐藏
  - 点击 + 按钮 → 导航栏标题显示 "添加"
  - 添加页面有文本输入框可输入名称
  - 底部显示 "每 X 分钟提醒 Y" 摘要
  - 保存后列表卡片显示自定义名称
