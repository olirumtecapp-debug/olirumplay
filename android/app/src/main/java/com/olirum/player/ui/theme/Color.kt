package com.olirum.player.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Paleta Oficial Premium Light OlirumPlay
val OlirumWhite = Color(0xFFFFFFFF)
val OlirumSurfaceLight = Color(0xFFF6F8FC)
val OlirumBackgroundLight = Color(0xFFF6F8FC)
val OlirumCardLight = Color(0xFFFFFFFF)
val OlirumTextPrimary = Color(0xFF19212E)
val OlirumTextSecondary = Color(0xFF697586)
val OlirumDivider = Color(0xFFE5E9F2)
val OlirumBorderLight = Color(0xFFE5E9F2)

// Paleta Oficial Premium Dark OlirumPlay
val OlirumBackgroundDark = Color(0xFF0B101B)
val OlirumSurfaceDark = Color(0xFF121826)
val OlirumCardDark = Color(0xFF182032)
val OlirumTextPrimaryDark = Color(0xFFF8FAFC)
val OlirumTextSecondaryDark = Color(0xFF94A3B8)
val OlirumBorderDark = Color(0xFF1E293B)

// Degradê da Marca OlirumPlay (Equalizador Musical)
val OlirumMint = Color(0xFF10DDB5)
val OlirumCyan = Color(0xFF08CFF0)
val OlirumBlue = Color(0xFF1677FF)
val OlirumPurple = Color(0xFF713DFF)
val OlirumMagenta = Color(0xFFE82EEA)

val OlirumGradientBrush = Brush.horizontalGradient(
    colors = listOf(OlirumMint, OlirumCyan, OlirumBlue, OlirumPurple, OlirumMagenta)
)

val OlirumPlayButtonBrush = Brush.horizontalGradient(
    colors = listOf(OlirumCyan, OlirumBlue, OlirumPurple)
)
