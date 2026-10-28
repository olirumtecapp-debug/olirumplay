package com.olirum.player.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Paleta Oficial OlirumPlay Dark Premium
val OlirumBackground = Color(0xFF070B14)
val OlirumBackgroundSecondary = Color(0xFF0D1523)
val OlirumSurface = Color(0xFF111C2D)
val OlirumCard = Color(0xFF111C2D)
val OlirumBorder = Color(0xFF1D2B40)

// Acentos Neon da Marca
val OlirumCyan = Color(0xFF00E5D4)
val OlirumBlue = Color(0xFF008BFF)
val OlirumPurple = Color(0xFF8B5CF6)
val OlirumPink = Color(0xFFFF3DAD)
val OlirumMagenta = Color(0xFFFF3DAD)
val OlirumMint = Color(0xFF00E5D4)

// Tipografia e Contrastes
val OlirumTextPrimary = Color(0xFFF8FAFC)
val OlirumTextSecondary = Color(0xFFA5B2C5)
val OlirumTextDisabled = Color(0xFF475569)

// Gradientes Discretos e Elegantes
val OlirumGradientBrush = Brush.horizontalGradient(
    colors = listOf(OlirumCyan, OlirumBlue, OlirumPurple, OlirumPink)
)

val OlirumAccentBrush = Brush.linearGradient(
    colors = listOf(OlirumCyan, OlirumBlue)
)

val OlirumButtonBrush = Brush.horizontalGradient(
    colors = listOf(OlirumCyan, OlirumBlue)
)

val OlirumCardGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF162338), Color(0xFF0D1523))
)
