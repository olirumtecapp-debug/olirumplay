package com.olirum.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olirum.player.ui.components.OlirumTopBar
import com.olirum.player.ui.theme.*

@Composable
fun WidgetsScreen() {
    var bassBoost by remember { mutableFloatStateOf(0.7f) }
    var midRange by remember { mutableFloatStateOf(0.5f) }
    var treble by remember { mutableFloatStateOf(0.6f) }
    var selectedPreset by remember { mutableStateOf("Vibrante") }

    Scaffold(
        topBar = {
            OlirumTopBar(
                title = "Widgets & Áudio",
                subtitle = "Tela Inicial e Equalização"
            )
        },
        containerColor = OlirumBackgroundLight
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
                    color = OlirumWhite,
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
                                    imageVector = Icons.Default.Widgets,
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
                                    color = OlirumTextPrimary
                                )
                                Text(
                                    text = "Controle suas músicas direto da tela do Android",
                                    fontSize = 12.sp,
                                    color = OlirumTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Demonstração visual do Widget
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = OlirumSurfaceLight,
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
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(OlirumBlue),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("OlirumPlay 4x1", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Play • Pause • Capa", fontSize = 11.sp, color = OlirumTextSecondary)
                                    }
                                }

                                Row {
                                    Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = OlirumTextPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = OlirumBlue)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Default.SkipNext, contentDescription = null, tint = OlirumTextPrimary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Como Adicionar:\n1. Vá para a tela inicial do seu celular e pressione e segure um espaço vazio.\n2. Selecione 'Widgets' no menu do Android.\n3. Procure por 'OlirumPlay' e arraste o widget para onde preferir.",
                            fontSize = 12.sp,
                            color = OlirumTextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Seção 2: Equalizador Profissional
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = OlirumWhite,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = OlirumBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Equalizador de Áudio",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = OlirumTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Sliders de Graves, Médios e Agudos
                        Text(text = "Graves Profundos (Bass)", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Slider(
                            value = bassBoost,
                            onValueChange = { bassBoost = it },
                            colors = SliderDefaults.colors(thumbColor = OlirumCyan, activeTrackColor = OlirumCyan)
                        )

                        Text(text = "Médios / Vozes (Mid)", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Slider(
                            value = midRange,
                            onValueChange = { midRange = it },
                            colors = SliderDefaults.colors(thumbColor = OlirumBlue, activeTrackColor = OlirumBlue)
                        )

                        Text(text = "Agudos Cristalinos (Treble)", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Slider(
                            value = treble,
                            onValueChange = { treble = it },
                            colors = SliderDefaults.colors(thumbColor = OlirumPurple, activeTrackColor = OlirumPurple)
                        )
                    }
                }
            }

            // Seção 3: Status de Reprodução Persistente
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = OlirumWhite,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = OlirumMint,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Motor de Áudio em Segundo Plano",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = OlirumTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "• MediaSessionService ativo\n• WakeLock & Network Lock habilitados\n• Interrupção de chamadas e desconexão de fones tratados automaticamente",
                            fontSize = 12.sp,
                            color = OlirumTextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}
