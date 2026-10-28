package com.olirum.player.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

data class OlirumCustomColors(
    val background: Color,
    val backgroundSecondary: Color,
    val surface: Color,
    val card: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    val border: Color,
    val cyan: Color,
    val blue: Color,
    val purple: Color,
    val pink: Color,
    val isDark: Boolean = true
)

val LocalOlirumColors = staticCompositionLocalOf {
    OlirumCustomColors(
        background = OlirumBackground,
        backgroundSecondary = OlirumBackgroundSecondary,
        surface = OlirumSurface,
        card = OlirumCard,
        textPrimary = OlirumTextPrimary,
        textSecondary = OlirumTextSecondary,
        textDisabled = OlirumTextDisabled,
        border = OlirumBorder,
        cyan = OlirumCyan,
        blue = OlirumBlue,
        purple = OlirumPurple,
        pink = OlirumPink,
        isDark = true
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = OlirumCyan,
    onPrimary = Color.Black,
    primaryContainer = OlirumBlue.copy(alpha = 0.25f),
    onPrimaryContainer = OlirumCyan,
    secondary = OlirumPurple,
    onSecondary = Color.White,
    background = OlirumBackground,
    onBackground = OlirumTextPrimary,
    surface = OlirumSurface,
    onSurface = OlirumTextPrimary,
    surfaceVariant = OlirumCard,
    onSurfaceVariant = OlirumTextSecondary,
    outline = OlirumBorder
)

@Composable
fun OlirumPlayTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val customColors = OlirumCustomColors(
        background = OlirumBackground,
        backgroundSecondary = OlirumBackgroundSecondary,
        surface = OlirumSurface,
        card = OlirumCard,
        textPrimary = OlirumTextPrimary,
        textSecondary = OlirumTextSecondary,
        textDisabled = OlirumTextDisabled,
        border = OlirumBorder,
        cyan = OlirumCyan,
        blue = OlirumBlue,
        purple = OlirumPurple,
        pink = OlirumPink,
        isDark = true
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val barColor = OlirumBackground.toArgb()
            window.statusBarColor = barColor
            window.navigationBarColor = barColor
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    CompositionLocalProvider(LocalOlirumColors provides customColors) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            content = content
        )
    }
}
