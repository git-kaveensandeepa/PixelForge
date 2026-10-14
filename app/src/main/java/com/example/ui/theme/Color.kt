package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Dynamic studio theme colors bound to LocalStudioColors
val ForgeBackground: Color
    @Composable @ReadOnlyComposable get() = LocalStudioColors.current.background

val ForgeSurface: Color
    @Composable @ReadOnlyComposable get() = LocalStudioColors.current.surface

val ForgeSurfaceVariant: Color
    @Composable @ReadOnlyComposable get() = LocalStudioColors.current.surfaceVariant

val ForgeSurfaceHover: Color
    @Composable @ReadOnlyComposable get() = LocalStudioColors.current.surfaceHover

val ForgeBorder: Color
    @Composable @ReadOnlyComposable get() = LocalStudioColors.current.border

val ForgeBorderLight: Color
    @Composable @ReadOnlyComposable get() = LocalStudioColors.current.borderLight

val ForgePrimary: Color
    @Composable @ReadOnlyComposable get() = LocalStudioColors.current.primary

val ForgePrimaryVariant: Color
    @Composable @ReadOnlyComposable get() = LocalStudioColors.current.primaryVariant

val ForgePrimaryLight: Color
    @Composable @ReadOnlyComposable get() = LocalStudioColors.current.primaryLight

val ForgeAccentCyan: Color
    @Composable @ReadOnlyComposable get() = LocalStudioColors.current.accentCyan

val ForgeAccentPink: Color
    @Composable @ReadOnlyComposable get() = LocalStudioColors.current.accentPink

val ForgeSuccess: Color
    @Composable @ReadOnlyComposable get() = LocalStudioColors.current.success

val ForgeWarning: Color
    @Composable @ReadOnlyComposable get() = LocalStudioColors.current.warning

val ForgeError: Color
    @Composable @ReadOnlyComposable get() = LocalStudioColors.current.error

val ForgeTextPrimary: Color
    @Composable @ReadOnlyComposable get() = LocalStudioColors.current.textPrimary

val ForgeTextSecondary: Color
    @Composable @ReadOnlyComposable get() = LocalStudioColors.current.textSecondary

val ForgeTextMuted: Color
    @Composable @ReadOnlyComposable get() = LocalStudioColors.current.textMuted

val ForgeArtboardCanvas: Color
    @Composable @ReadOnlyComposable get() = LocalStudioColors.current.artboardCanvas

val ForgeArtboardWhite: Color
    @Composable @ReadOnlyComposable get() = LocalStudioColors.current.artboardWhite

// Palette presets for UI elements
val ElementPresetColors = listOf(
    Color(0xFF6366F1), // Indigo
    Color(0xFF3B82F6), // Blue
    Color(0xFF06B6D4), // Cyan
    Color(0xFF10B981), // Emerald
    Color(0xFFEAB308), // Yellow
    Color(0xFFF97316), // Orange
    Color(0xFFEF4444), // Red
    Color(0xFFEC4899), // Pink
    Color(0xFF8B5CF6), // Purple
    Color(0xFF111827), // Near Black
    Color(0xFF4B5563), // Gray
    Color(0xFFFFFFFF)  // White
)
