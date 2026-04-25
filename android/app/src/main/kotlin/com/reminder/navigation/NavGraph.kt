package com.reminder.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.reminder.core.model.ReminderType
import com.reminder.core.notification.NotificationHelper
import com.reminder.data.settings.SettingsRepository
import com.reminder.feature.log.LogScreen
import com.reminder.feature.log.LogViewModel
import com.reminder.feature.onboarding.OnboardingScreen
import com.reminder.feature.onboarding.OnboardingViewModel
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
    const val REMINDER_DETAIL = "reminder/{type}?isNew={isNew}"
    const val LOG = "logs"
    const val SETTINGS = "settings"

    fun reminderDetail(type: ReminderType, isNew: Boolean = false) = "reminder/${type.route}?isNew=$isNew"
}

@Composable
fun DrinkReminderNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
    settingsRepository: SettingsRepository = koinInject()
) {
    val onboardingCompleted by settingsRepository.observeOnboardingCompleted()
        .collectAsState(initial = null)

    // Wait for DataStore to load before showing navigation
    val ready = onboardingCompleted != null
    if (!ready) return

    val startDestination = if (onboardingCompleted == true) Routes.REMINDER_LIST else Routes.ONBOARDING
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Routes.ONBOARDING) {
            val onboardingViewModel: OnboardingViewModel = koinViewModel()
            OnboardingScreen(
                onComplete = {
                    onboardingViewModel.completeOnboarding()
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
                onNavigateToDetail = { type, isNew ->
                    navController.navigate(Routes.reminderDetail(type, isNew))
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
                navArgument("type") { type = NavType.StringType },
                navArgument("isNew") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val typeName = backStackEntry.arguments?.getString("type") ?: return@composable
            val isNew = backStackEntry.arguments?.getBoolean("isNew") ?: false
            val type = ReminderType.values().find { it.route == typeName } ?: return@composable

            when (type) {
                ReminderType.WATER -> {
                    val viewModel: WaterReminderViewModel = koinViewModel()
                    WaterReminderScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                ReminderType.MEDICINE, ReminderType.MEAL -> {
                    val repo: SettingsRepository = koinInject()
                    val notifHelper: NotificationHelper = koinInject()
                    val viewModel = viewModel<WaterReminderViewModel>(
                        key = type.name,
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return WaterReminderViewModel(
                                    settingsRepository = repo,
                                    notificationHelper = notifHelper,
                                    reminderType = type
                                ) as T
                            }
                        }
                    )
                    WaterReminderScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
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
