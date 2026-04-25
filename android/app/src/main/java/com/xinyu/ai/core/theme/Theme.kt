package com.xinyu.ai.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.xinyu.ai.domain.model.AppThemeMode

private val LightColors = lightColorScheme(
    primary = Rose500,
    onPrimary = Sand100,
    secondary = Peach300,
    onSecondary = Ink900,
    tertiary = EmeraldSoft,
    background = Sand100,
    onBackground = Ink900,
    surface = Rose50,
    onSurface = Ink900,
    outline = Rose200,
)

private val DarkColors = darkColorScheme(
    primary = Rose200,
    onPrimary = Ink900,
    secondary = Peach300,
    onSecondary = Ink900,
    tertiary = EmeraldSoft,
    background = Ink900,
    onBackground = Sand100,
    surface = Berry700,
    onSurface = Sand100,
    outline = Slate600,
)

@Composable
fun XinyuTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val isDarkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    MaterialTheme(
        colorScheme = if (isDarkTheme) DarkColors else LightColors,
        typography = XinyuTypography,
        content = content,
    )
}
