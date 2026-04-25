package com.reminder.feature.reminderlist

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.reminder.core.designsystem.theme.AlertRed
import com.reminder.core.designsystem.theme.SuccessGreen
import com.reminder.core.model.ReminderState
import com.reminder.core.model.ReminderType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListScreen(
    viewModel: ReminderListViewModel,
    onNavigateToDetail: (ReminderType, Boolean) -> Unit,
    onNavigateToLog: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reminders by viewModel.reminders.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "提醒",
                        style = MaterialTheme.typography.headlineLarge
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToLog) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = "提醒日志",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "设置",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        contentWindowInsets = WindowInsets.systemBars,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToDetail(ReminderType.WATER, true) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加提醒"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val scope = rememberCoroutineScope()
            reminders.forEach { state ->
                if (!state.isActive) {
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
                            Row(modifier = Modifier.fillMaxSize()) {
                                Spacer(modifier = Modifier.weight(2f))
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(1f)
                                        .background(AlertRed, RoundedCornerShape(16.dp))
                                        .clickable {
                                            viewModel.deleteReminder(state.type)
                                            scope.launch { dismissState.reset() }
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
                    ) {
                        ReminderCard(
                            state = state,
                            onClick = { onNavigateToDetail(state.type, false) }
                        )
                    }
                } else {
                    ReminderCard(
                        state = state,
                        onClick = { onNavigateToDetail(state.type, false) }
                    )
                }
            }

            if (reminders.isEmpty()) {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = "点击右下角 + 按钮添加提醒",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReminderCard(
    state: ReminderState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (state.isActive)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (state.type) {
                ReminderType.WATER -> Icons.Default.WaterDrop
                ReminderType.MEDICINE -> Icons.Default.AccessTime
                ReminderType.MEAL -> Icons.Default.AccessTime
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (state.isActive)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.displayName,
                    style = MaterialTheme.typography.titleLarge
                )
                if (state.isActive) {
                    Text(
                        text = "今日已完成 ${state.todayCompletedCount} 次",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SuccessGreen
                    )
                } else {
                    Text(
                        text = "已关闭",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = if (state.isActive) "已开启" else "已关闭",
                style = MaterialTheme.typography.labelLarge,
                color = if (state.isActive)
                    SuccessGreen
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
