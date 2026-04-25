package com.reminder.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ElderlyColorScheme = lightColorScheme(
    primary = WarmOrange,
    onPrimary = TextOnPrimary,
    primaryContainer = WarmOrangeLight,
    secondary = BlueAccent,
    onSecondary = TextOnPrimary,
    secondaryContainer = BlueAccentLight,
    background = WarmBackground,
    surface = WarmSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = AlertRed,
    onError = TextOnPrimary,
    outline = DividerColor,
    surfaceVariant = CardBackground,
    onSurfaceVariant = TextSecondary
)

@Composable
fun DrinkReminderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ElderlyColorScheme,
        typography = ElderlyTypography,
        content = content
    )
}
