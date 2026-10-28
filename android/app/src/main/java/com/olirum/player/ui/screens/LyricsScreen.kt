package com.olirum.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olirum.player.ui.components.OlirumTopBar
import com.olirum.player.ui.theme.*
import com.olirum.player.ui.viewmodel.MainPlayerViewModel

@Composable
fun LyricsScreen(
    viewModel: MainPlayerViewModel
) {
    val colors = LocalOlirumColors.current
    val currentTrack by viewModel.currentTrack.collectAsState()
    val lyricsData by viewModel.lyricsData.collectAsState()
    val isLyricsLoading by viewModel.isLyricsLoading.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    val listState = rememberLazyListState()

    val activeIndex = remember(lyricsData.syncedLines, currentPositionMs) {
        if (lyricsData.syncedLines.isEmpty()) -1
        else {
            val idx = lyricsData.syncedLines.indexOfLast { it.timeMs <= currentPositionMs }
            if (idx >= 0) idx else 0
        }
    }

    LaunchedEffect(activeIndex) {
        if (lyricsData.hasSynced && activeIndex in lyricsData.syncedLines.indices) {
            val scrollTarget = (activeIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(scrollTarget)
        }
    }

    Scaffold(
        topBar = {
            OlirumTopBar(
                title = "Letras em Tempo Real",
                subtitle = currentTrack?.let { "${it.title} • ${it.artist}" } ?: "Nenhuma música tocando",
                onToggleTheme = { viewModel.toggleDarkMode() },
                isDarkTheme = isDarkMode,
                actions = {
                    currentTrack?.let { trk ->
                        IconButton(onClick = { viewModel.loadLyrics(trk) }) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Recarregar Letra",
                                tint = colors.textPrimary
                            )
                        }
                    }
                }
            )
        },
        containerColor = colors.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (currentTrack == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.Mic,
                            contentDescription = null,
                            tint = colors.textSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Inicie uma música para acompanhar a letra",
                            fontSize = 15.sp,
                            color = colors.textSecondary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            } else if (isLyricsLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = OlirumCyan)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Buscando letra no LRCLIB...",
                            color = colors.textSecondary,
                            fontSize = 13.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            } else if (lyricsData.isInstrumental) {
                // Faixa Instrumental
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(OlirumGradientBrush),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Faixa Instrumental",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Esta obra musical não possui letras vocais.",
                            fontSize = 13.sp,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (lyricsData.hasSynced && lyricsData.syncedLines.isNotEmpty()) {
                // Letras Sincronizadas
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    itemsIndexed(lyricsData.syncedLines) { index, line ->
                        val isActive = index == activeIndex

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isActive) OlirumCyan.copy(alpha = 0.15f)
                                    else Color.Transparent
                                )
                                .clickable {
                                    viewModel.seekTo(line.timeMs)
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = line.text,
                                fontSize = if (isActive) 19.sp else 15.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                color = if (isActive) OlirumCyan else colors.textSecondary.copy(alpha = 0.75f),
                                lineHeight = 26.sp
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            } else if (!lyricsData.hasSynced && lyricsData.plainLines.isNotEmpty()) {
                // Letras Completas em Texto (Plain Lyrics)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = colors.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MusicNote,
                                    contentDescription = null,
                                    tint = OlirumCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Letra completa da faixa (Modo Texto)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textSecondary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    itemsIndexed(lyricsData.plainLines) { _, line ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = line,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = colors.textPrimary,
                                lineHeight = 28.sp
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            } else {
                // Nenhuma letra encontrada
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Letra não encontrada para esta faixa",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "O serviço LRCLIB busca sincronizações públicas e gratuitas por título e artista correspondentes.",
                            fontSize = 13.sp,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { currentTrack?.let { viewModel.loadLyrics(it) } },
                            colors = ButtonDefaults.buttonColors(containerColor = OlirumBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tentar Novamente", maxLines = 1, softWrap = false)
                        }
                    }
                }
            }
        }
    }
}
