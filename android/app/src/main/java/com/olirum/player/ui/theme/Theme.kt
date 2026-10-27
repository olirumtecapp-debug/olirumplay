package com.olirum.player.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = OlirumPurple,
    onPrimary = OlirumWhite,
    primaryContainer = OlirumSurfaceLight,
    onPrimaryContainer = OlirumTextPrimary,
    secondary = OlirumCyan,
    onSecondary = OlirumTextPrimary,
    background = OlirumWhite,
    onBackground = OlirumTextPrimary,
    surface = OlirumSurfaceLight,
    onSurface = OlirumTextPrimary,
    surfaceVariant = OlirumCardLight,
    onSurfaceVariant = OlirumTextSecondary
)

@Composable
fun OlirumPlayTheme(
    darkTheme: Boolean = false, // Padrão CLARO Premium conforme especificação
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = OlirumWhite.toArgb()
            window.navigationBarColor = OlirumWhite.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
