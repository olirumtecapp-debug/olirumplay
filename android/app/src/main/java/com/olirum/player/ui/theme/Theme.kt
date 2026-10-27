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
    val surface: Color,
    val card: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val border: Color,
    val isDark: Boolean
)

val LocalOlirumColors = staticCompositionLocalOf {
    OlirumCustomColors(
        background = OlirumBackgroundLight,
        surface = OlirumSurfaceLight,
        card = OlirumCardLight,
        textPrimary = OlirumTextPrimary,
        textSecondary = OlirumTextSecondary,
        border = OlirumBorderLight,
        isDark = false
    )
}

private val LightColorScheme = lightColorScheme(
    primary = OlirumBlue,
    onPrimary = Color.White,
    primaryContainer = OlirumCyan.copy(alpha = 0.15f),
    onPrimaryContainer = OlirumBlue,
    secondary = OlirumCyan,
    onSecondary = Color.White,
    background = OlirumBackgroundLight,
    onBackground = OlirumTextPrimary,
    surface = OlirumWhite,
    onSurface = OlirumTextPrimary,
    surfaceVariant = OlirumSurfaceLight,
    onSurfaceVariant = OlirumTextSecondary
)

private val DarkColorScheme = darkColorScheme(
    primary = OlirumCyan,
    onPrimary = Color.Black,
    primaryContainer = OlirumBlue.copy(alpha = 0.3f),
    onPrimaryContainer = OlirumCyan,
    secondary = OlirumPurple,
    onSecondary = Color.White,
    background = OlirumBackgroundDark,
    onBackground = OlirumTextPrimaryDark,
    surface = OlirumSurfaceDark,
    onSurface = OlirumTextPrimaryDark,
    surfaceVariant = OlirumCardDark,
    onSurfaceVariant = OlirumTextSecondaryDark
)

@Composable
fun OlirumPlayTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val customColors = if (darkTheme) {
        OlirumCustomColors(
            background = OlirumBackgroundDark,
            surface = OlirumSurfaceDark,
            card = OlirumCardDark,
            textPrimary = OlirumTextPrimaryDark,
            textSecondary = OlirumTextSecondaryDark,
            border = OlirumBorderDark,
            isDark = true
        )
    } else {
        OlirumCustomColors(
            background = OlirumBackgroundLight,
            surface = OlirumSurfaceLight,
            card = OlirumCardLight,
            textPrimary = OlirumTextPrimary,
            textSecondary = OlirumTextSecondary,
            border = OlirumBorderLight,
            isDark = false
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val barColor = if (darkTheme) OlirumSurfaceDark.toArgb() else OlirumWhite.toArgb()
            window.statusBarColor = barColor
            window.navigationBarColor = barColor
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalOlirumColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
