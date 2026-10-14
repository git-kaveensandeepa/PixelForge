package com.example.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class StudioThemeMode(
    val id: String,
    val title: String,
    val description: String,
    val primaryColor: Color,
    val surfaceColor: Color,
    val isDark: Boolean
) {
    MIDNIGHT(
        id = "midnight",
        title = "Midnight Pro",
        description = "Deep studio dark with electric indigo & slate accents",
        primaryColor = Color(0xFF6366F1),
        surfaceColor = Color(0xFF161720),
        isDark = true
    ),
    CYBER_OLED(
        id = "cyber_oled",
        title = "Cyber OLED",
        description = "Pure pitch-black OLED with high-contrast neon cyan & emerald",
        primaryColor = Color(0xFF06B6D4),
        surfaceColor = Color(0xFF000000),
        isDark = true
    ),
    NORDIC_SLATE(
        id = "nordic_slate",
        title = "Nordic Slate",
        description = "Cool deep navy slate with arctic frost blue highlights",
        primaryColor = Color(0xFF38BDF8),
        surfaceColor = Color(0xFF1A222D),
        isDark = true
    ),
    STUDIO_LIGHT(
        id = "studio_light",
        title = "Studio Light",
        description = "Clean modern daylight UI designer workspace with crisp cards",
        primaryColor = Color(0xFF4F46E5),
        surfaceColor = Color(0xFFF1F4F9),
        isDark = false
    ),
    SUNSET_AMBER(
        id = "sunset_amber",
        title = "Sunset Amber",
        description = "Warm espresso charcoal with glowing golden amber & coral",
        primaryColor = Color(0xFFF59E0B),
        surfaceColor = Color(0xFF1D1714),
        isDark = true
    )
}

data class StudioColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val surfaceHover: Color,
    val border: Color,
    val borderLight: Color,
    val primary: Color,
    val primaryVariant: Color,
    val primaryLight: Color,
    val accentCyan: Color,
    val accentPink: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val artboardCanvas: Color,
    val artboardWhite: Color,
    val isDark: Boolean
)

val MidnightColors = StudioColors(
    background = Color(0xFF0F1015),
    surface = Color(0xFF161720),
    surfaceVariant = Color(0xFF1E202B),
    surfaceHover = Color(0xFF262938),
    border = Color(0xFF2A2E3D),
    borderLight = Color(0xFF383E52),
    primary = Color(0xFF6366F1),
    primaryVariant = Color(0xFF4F46E5),
    primaryLight = Color(0xFF818CF8),
    accentCyan = Color(0xFF06B6D4),
    accentPink = Color(0xFFEC4899),
    success = Color(0xFF10B981),
    warning = Color(0xFFF59E0B),
    error = Color(0xFFEF4444),
    textPrimary = Color(0xFFF3F4F6),
    textSecondary = Color(0xFF9CA3AF),
    textMuted = Color(0xFF6B7280),
    artboardCanvas = Color(0xFF0B0C10),
    artboardWhite = Color(0xFFFFFFFF),
    isDark = true
)

val CyberOledColors = StudioColors(
    background = Color(0xFF000000),
    surface = Color(0xFF0A0A0E),
    surfaceVariant = Color(0xFF14141B),
    surfaceHover = Color(0xFF1E1E28),
    border = Color(0xFF1F2430),
    borderLight = Color(0xFF2D3545),
    primary = Color(0xFF06B6D4),
    primaryVariant = Color(0xFF0891B2),
    primaryLight = Color(0xFF67E8F9),
    accentCyan = Color(0xFF10B981),
    accentPink = Color(0xFFF43F5E),
    success = Color(0xFF10B981),
    warning = Color(0xFFFBBF24),
    error = Color(0xFFFF453A),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFA1A1AA),
    textMuted = Color(0xFF71717A),
    artboardCanvas = Color(0xFF000000),
    artboardWhite = Color(0xFFFFFFFF),
    isDark = true
)

val NordicSlateColors = StudioColors(
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFF273549),
    surfaceHover = Color(0xFF334155),
    border = Color(0xFF334155),
    borderLight = Color(0xFF475569),
    primary = Color(0xFF38BDF8),
    primaryVariant = Color(0xFF0284C7),
    primaryLight = Color(0xFF7DD3FC),
    accentCyan = Color(0xFF2DD4BF),
    accentPink = Color(0xFFF472B6),
    success = Color(0xFF34D399),
    warning = Color(0xFFFBBF24),
    error = Color(0xFFF87171),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFF94A3B8),
    textMuted = Color(0xFF64748B),
    artboardCanvas = Color(0xFF0B1120),
    artboardWhite = Color(0xFFFFFFFF),
    isDark = true
)

val StudioLightColors = StudioColors(
    background = Color(0xFFF5F7FB),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEBEFF7),
    surfaceHover = Color(0xFFE1E7F2),
    border = Color(0xFFD6DFEC),
    borderLight = Color(0xFFCBD5E1),
    primary = Color(0xFF4F46E5),
    primaryVariant = Color(0xFF4338CA),
    primaryLight = Color(0xFF6366F1),
    accentCyan = Color(0xFF0284C7),
    accentPink = Color(0xFFDB2777),
    success = Color(0xFF059669),
    warning = Color(0xFFD97706),
    error = Color(0xFFDC2626),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textMuted = Color(0xFF94A3B8),
    artboardCanvas = Color(0xFFE2E8F0),
    artboardWhite = Color(0xFFFFFFFF),
    isDark = false
)

val SunsetAmberColors = StudioColors(
    background = Color(0xFF14100E),
    surface = Color(0xFF1D1714),
    surfaceVariant = Color(0xFF28201C),
    surfaceHover = Color(0xFF362C27),
    border = Color(0xFF3D322B),
    borderLight = Color(0xFF52443C),
    primary = Color(0xFFF59E0B),
    primaryVariant = Color(0xFFD97706),
    primaryLight = Color(0xFFFBBF24),
    accentCyan = Color(0xFFFB923C),
    accentPink = Color(0xFFF43F5E),
    success = Color(0xFF10B981),
    warning = Color(0xFFF59E0B),
    error = Color(0xFFEF4444),
    textPrimary = Color(0xFFFDF8F6),
    textSecondary = Color(0xFFA89F91),
    textMuted = Color(0xFF786F63),
    artboardCanvas = Color(0xFF0D0B0A),
    artboardWhite = Color(0xFFFFFFFF),
    isDark = true
)

fun getStudioColorsForTheme(mode: StudioThemeMode): StudioColors {
    return when (mode) {
        StudioThemeMode.MIDNIGHT -> MidnightColors
        StudioThemeMode.CYBER_OLED -> CyberOledColors
        StudioThemeMode.NORDIC_SLATE -> NordicSlateColors
        StudioThemeMode.STUDIO_LIGHT -> StudioLightColors
        StudioThemeMode.SUNSET_AMBER -> SunsetAmberColors
    }
}

val LocalStudioColors = compositionLocalOf { MidnightColors }

object ThemeManager {
    private const val PREFS_NAME = "pixelforge_theme_prefs"
    private const val KEY_THEME = "selected_theme_mode"

    private val _currentTheme = MutableStateFlow(StudioThemeMode.MIDNIGHT)
    val currentTheme: StateFlow<StudioThemeMode> = _currentTheme.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedId = prefs.getString(KEY_THEME, StudioThemeMode.MIDNIGHT.id)
        val mode = StudioThemeMode.values().find { it.id == savedId } ?: StudioThemeMode.MIDNIGHT
        _currentTheme.value = mode
    }

    fun setTheme(context: Context, mode: StudioThemeMode) {
        _currentTheme.value = mode
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, mode.id).apply()
    }
}
