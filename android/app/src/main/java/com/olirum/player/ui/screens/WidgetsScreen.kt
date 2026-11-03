package com.olirum.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olirum.player.ui.components.OlirumTopBar
import com.olirum.player.ui.components.SleepTimerDialog
import com.olirum.player.ui.theme.*
import com.olirum.player.ui.viewmodel.MainPlayerViewModel

@Composable
fun WidgetsScreen(
    viewModel: MainPlayerViewModel
) {
    val colors = LocalOlirumColors.current
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    val bassBoost by viewModel.bassBoost.collectAsState()
    val midRange by viewModel.midRange.collectAsState()
    val treble by viewModel.treble.collectAsState()
    val selectedPreset by viewModel.selectedPreset.collectAsState()
    val sleepTimerRemainingSeconds by viewModel.sleepTimerRemainingSeconds.collectAsState()
    var showSleepTimerDialog by remember { mutableStateOf(false) }

    
    if (showSleepTimerDialog) {
        SleepTimerDialog(
            remainingSeconds = sleepTimerRemainingSeconds,
            onSetTimer = { mins -> viewModel.setSleepTimer(mins) },
            onCancelTimer = { viewModel.cancelSleepTimer() },
            onDismiss = { showSleepTimerDialog = false }
        )
    }

    Scaffold(
        topBar = {
            OlirumTopBar(
                title = "Widgets & Áudio",
                subtitle = "Tela Inicial e Equalização",
                onToggleTheme = { viewModel.toggleDarkMode() },
                isDarkTheme = isDarkMode
            )
        },
        containerColor = colors.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
        ) {
            // Seção 1: Widget da Tela Inicial do Android
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surface,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(OlirumGradientBrush),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Widgets,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Widget de Tela Inicial",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Controle suas músicas direto da tela do Android",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Demonstração visual do Widget
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = colors.card,
                            tonalElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(OlirumBlue),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = Color.White)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "OlirumPlay 4x1",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        Text(
                                            text = "Play • Pause • Capa",
                                            fontSize = 11.sp,
                                            color = colors.textSecondary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }

                                Row {
                                    Icon(Icons.Rounded.SkipPrevious, contentDescription = null, tint = colors.textPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = OlirumCyan)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Rounded.SkipNext, contentDescription = null, tint = colors.textPrimary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Como Adicionar:\n1. Vá para a tela inicial do seu celular e pressione e segure um espaço vazio.\n2. Selecione 'Widgets' no menu do Android.\n3. Procure por 'OlirumPlay' e arraste o widget para onde preferir.",
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Seção 2: Equalizador Profissional com Presets de 1 Toque
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surface,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.GraphicEq,
                                    contentDescription = null,
                                    tint = OlirumCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Equalizador & Mega Bass",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = OlirumCyan.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = selectedPreset,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OlirumCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Presets Rápidos
                        val presets = listOf("Mega Bass", "Voz Clara", "Modo Estrada", "Rock Potente", "Equilibrado")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(presets) { preset ->
                                val isSelected = selectedPreset == preset
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) OlirumCyan else colors.card,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) OlirumCyan else colors.border),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { viewModel.applySoundPreset(preset) }
                                ) {
                                    Text(
                                        text = preset,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.Black else colors.textPrimary,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Graves Profundos (Bass)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = "${(bassBoost * 100).toInt()}%",
                                fontSize = 12.sp,
                                color = OlirumCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = bassBoost,
                            onValueChange = { viewModel.setEqualizerBands(it, midRange, treble) },
                            colors = SliderDefaults.colors(thumbColor = OlirumCyan, activeTrackColor = OlirumCyan)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Médios / Vozes (Mid)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = "${(midRange * 100).toInt()}%",
                                fontSize = 12.sp,
                                color = OlirumBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = midRange,
                            onValueChange = { viewModel.setEqualizerBands(bassBoost, it, treble) },
                            colors = SliderDefaults.colors(thumbColor = OlirumBlue, activeTrackColor = OlirumBlue)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Agudos Cristalinos (Treble)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = "${(treble * 100).toInt()}%",
                                fontSize = 12.sp,
                                color = OlirumPurple,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = treble,
                            onValueChange = { viewModel.setEqualizerBands(bassBoost, midRange, it) },
                            colors = SliderDefaults.colors(thumbColor = OlirumPurple, activeTrackColor = OlirumPurple)
                        )
                    }
                }
            }

            // Seção Extra: Timer para Dormir (Sleep Timer)
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showSleepTimerDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surface,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(OlirumPurple.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Bedtime,
                                    contentDescription = null,
                                    tint = OlirumPurple,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Timer para Dormir",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text(
                                    text = if (sleepTimerRemainingSeconds != null) {
                                        val m = sleepTimerRemainingSeconds!! / 60
                                        val s = sleepTimerRemainingSeconds!! % 60
                                        "Ativo: %02d:%02d restantes (fade-out)".format(m, s)
                                    } else {
                                        "Desligamento automático com fade suave"
                                    },
                                    fontSize = 12.sp,
                                    color = if (sleepTimerRemainingSeconds != null) OlirumCyan else colors.textSecondary,
                                    fontWeight = if (sleepTimerRemainingSeconds != null) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = colors.textSecondary
                        )
                    }
                }
            }

            // Seção 3: Modo Escuro / Claro
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surface,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                                contentDescription = null,
                                tint = if (isDarkMode) OlirumCyan else OlirumBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isDarkMode) "Modo Escuro Ativo" else "Modo Claro Ativo",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text(
                                    text = "Toque para alternar o tema do app",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { viewModel.toggleDarkMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = OlirumCyan,
                                checkedTrackColor = OlirumBlue.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }
    }
}
