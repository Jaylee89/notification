package com.reminder.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.reminder.core.notification.NotificationHelper
import com.reminder.data.settings.SettingsRepository
import com.reminder.feature.log.LogScreen
import com.reminder.feature.log.LogViewModel
import com.reminder.feature.onboarding.OnboardingScreen
import com.reminder.feature.reminderlist.ReminderListScreen
import com.reminder.feature.reminderlist.ReminderListViewModel
import com.reminder.feature.settings.SettingsScreen
import com.reminder.feature.settings.SettingsViewModel
import com.reminder.feature.water.WaterReminderScreen
import com.reminder.feature.water.WaterReminderViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

object Routes {
    const val ONBOARDING = "onboarding"
    const val REMINDER_LIST = "reminders"
    const val REMINDER_DETAIL = "reminder/{reminderId}?isNew={isNew}"
    const val LOG = "logs"
    const val SETTINGS = "settings"

    fun reminderDetail(reminderId: String, isNew: Boolean = false) = "reminder/$reminderId?isNew=$isNew"
}

@Composable
fun DrinkReminderNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.ONBOARDING,
        modifier = modifier
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Routes.REMINDER_LIST) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REMINDER_LIST) {
            val viewModel: ReminderListViewModel = koinViewModel()
            ReminderListScreen(
                viewModel = viewModel,
                onNavigateToDetail = { reminderId ->
                    navController.navigate(Routes.reminderDetail(reminderId))
                },
                onNavigateToLog = {
                    navController.navigate(Routes.LOG)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        composable(
            route = Routes.REMINDER_DETAIL,
            arguments = listOf(
                navArgument("reminderId") { type = NavType.StringType },
                navArgument("isNew") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val reminderId = backStackEntry.arguments?.getString("reminderId") ?: return@composable
            val isNew = backStackEntry.arguments?.getBoolean("isNew") ?: false
            val repo: SettingsRepository = koinInject()
            val notifHelper: NotificationHelper = koinInject()
            val viewModel = viewModel<WaterReminderViewModel>(
                key = reminderId,
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return WaterReminderViewModel(
                            settingsRepository = repo,
                            notificationHelper = notifHelper,
                            reminderId = reminderId,
                            isNewReminder = isNew
                        ) as T
                    }
                }
            )
            WaterReminderScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.LOG) {
            val viewModel: LogViewModel = koinViewModel()
            LogScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            val viewModel: SettingsViewModel = koinViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
