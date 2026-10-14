package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

private fun createColorScheme(colors: StudioColors): ColorScheme {
    return if (colors.isDark) {
        darkColorScheme(
            primary = colors.primary,
            onPrimary = Color.White,
            primaryContainer = colors.primaryVariant,
            onPrimaryContainer = Color.White,
            secondary = colors.accentCyan,
            onSecondary = Color.Black,
            secondaryContainer = colors.surfaceVariant,
            onSecondaryContainer = colors.textPrimary,
            tertiary = colors.accentPink,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.surfaceVariant,
            onSurfaceVariant = colors.textSecondary,
            outline = colors.border,
            outlineVariant = colors.borderLight,
            error = colors.error,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = colors.primary,
            onPrimary = Color.White,
            primaryContainer = colors.primaryLight.copy(alpha = 0.2f),
            onPrimaryContainer = colors.primaryVariant,
            secondary = colors.accentCyan,
            onSecondary = Color.White,
            secondaryContainer = colors.surfaceVariant,
            onSecondaryContainer = colors.textPrimary,
            tertiary = colors.accentPink,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.surfaceVariant,
            onSurfaceVariant = colors.textSecondary,
            outline = colors.border,
            outlineVariant = colors.borderLight,
            error = colors.error,
            onError = Color.White
        )
    }
}

@Composable
fun PixelForgeTheme(
    themeMode: StudioThemeMode? = null,
    content: @Composable () -> Unit
) {
    val activeMode by ThemeManager.currentTheme.collectAsState()
    val effectiveMode = themeMode ?: activeMode
    val studioColors = getStudioColorsForTheme(effectiveMode)
    val m3ColorScheme = createColorScheme(studioColors)

    CompositionLocalProvider(LocalStudioColors provides studioColors) {
        MaterialTheme(
            colorScheme = m3ColorScheme,
            typography = Typography,
            content = content
        )
    }
}
