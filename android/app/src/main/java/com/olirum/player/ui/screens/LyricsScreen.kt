package com.olirum.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olirum.player.model.LyricLine
import com.olirum.player.ui.components.OlirumTopBar
import com.olirum.player.ui.theme.*
import com.olirum.player.ui.viewmodel.MainPlayerViewModel

@Composable
fun LyricsScreen(
    viewModel: MainPlayerViewModel
) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val lyrics by viewModel.lyrics.collectAsState()
    val isLyricsLoading by viewModel.isLyricsLoading.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()

    val listState = rememberLazyListState()

    // Encontra o índice da linha de letra ativa com base no tempo atual
    val activeIndex = remember(lyrics, currentPositionMs) {
        if (lyrics.isEmpty()) -1
        else {
            val idx = lyrics.indexOfLast { it.timeMs <= currentPositionMs }
            if (idx >= 0) idx else 0
        }
    }

    // Auto-scroll suave para manter a linha ativa centralizada
    LaunchedEffect(activeIndex) {
        if (activeIndex in lyrics.indices) {
            val scrollTarget = (activeIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(scrollTarget)
        }
    }

    Scaffold(
        topBar = {
            OlirumTopBar(
                title = "Letras em Tempo Real",
                subtitle = currentTrack?.let { "${it.title} • ${it.artist}" } ?: "Nenhuma música tocando",
                actions = {
                    currentTrack?.let { trk ->
                        IconButton(onClick = { viewModel.loadLyrics(trk) }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Recarregar Letra",
                                tint = OlirumTextPrimary
                            )
                        }
                    }
                }
            )
        },
        containerColor = OlirumBackgroundLight
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
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = OlirumTextSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Inicie uma música para acompanhar a letra",
                            fontSize = 15.sp,
                            color = OlirumTextSecondary
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
                        Text("Buscando letra sincronizada...", color = OlirumTextSecondary, fontSize = 13.sp)
                    }
                }
            } else if (lyrics.isEmpty()) {
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
                            color = OlirumTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "O serviço LRCLIB busca sincronizações automáticas para títulos e artistas correspondentes.",
                            fontSize = 13.sp,
                            color = OlirumTextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { currentTrack?.let { viewModel.loadLyrics(it) } },
                            colors = ButtonDefaults.buttonColors(containerColor = OlirumBlue)
                        ) {
                            Text("Tentar Novamente")
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp)
                ) {
                    itemsIndexed(lyrics) { index, line ->
                        val isActive = index == activeIndex

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isActive) OlirumCyan.copy(alpha = 0.12f)
                                    else androidx.compose.ui.graphics.Color.Transparent
                                )
                                .clickable {
                                    viewModel.seekTo(line.timeMs)
                                }
                                .padding(vertical = 12.dp, horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = line.text,
                                fontSize = if (isActive) 20.sp else 16.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                color = if (isActive) OlirumBlue else OlirumTextSecondary.copy(alpha = 0.7f),
                                lineHeight = 28.sp
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}
