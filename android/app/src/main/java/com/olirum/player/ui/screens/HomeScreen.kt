package com.olirum.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olirum.player.model.Track
import com.olirum.player.ui.components.OlirumTopBar
import com.olirum.player.ui.components.TrackListItem
import com.olirum.player.ui.theme.*
import com.olirum.player.ui.viewmodel.MainPlayerViewModel

@Composable
fun HomeScreen(
    viewModel: MainPlayerViewModel,
    onNavigateToDevice: () -> Unit,
    onNavigateToExplore: () -> Unit,
    onNavigateToLibrary: () -> Unit
) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val queue by viewModel.queue.collectAsState()

    var trendingTracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    var isLoadingTrending by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (trendingTracks.isEmpty()) {
            isLoadingTrending = true
            trendingTracks = com.olirum.player.OlirumApplication.instance.musicRepository.getTrendingOnline()
            isLoadingTrending = false
        }
    }

    Scaffold(
        topBar = {
            OlirumTopBar(
                title = "OlirumPlay",
                subtitle = "Música em Alta Fidelidade"
            )
        },
        containerColor = OlirumBackgroundLight
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Saudação
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Olá, apaixonado por música ✨",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = OlirumTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Escolha sua vibe e ouça sem interrupções",
                        fontSize = 14.sp,
                        color = OlirumTextSecondary
                    )
                }
            }

            // Ações Rápidas (3 Cards modernos)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = "Músicas Locais",
                        subtitle = "Do seu Celular",
                        icon = Icons.Default.FolderOpen,
                        gradient = Brush.linearGradient(listOf(OlirumMint, OlirumCyan)),
                        onClick = onNavigateToDevice,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "Tendências",
                        subtitle = "Audius Online",
                        icon = Icons.Default.TrendingUp,
                        gradient = Brush.linearGradient(listOf(OlirumBlue, OlirumPurple)),
                        onClick = onNavigateToExplore,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "Favoritas",
                        subtitle = "Sua Coleção",
                        icon = Icons.Default.Favorite,
                        gradient = Brush.linearGradient(listOf(OlirumPurple, OlirumMagenta)),
                        onClick = onNavigateToLibrary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Banner Destaque
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(OlirumGradientBrush)
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ÁUDIO INDESTRUTÍVEL",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Reprodução Contínua em 2º Plano",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bloqueie a tela, abra o WhatsApp ou qualquer app: sua música não para nunca.",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Seção Tendências Online
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tendências Mundiais (Audius)",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = OlirumTextPrimary
                    )
                    TextButton(onClick = onNavigateToExplore) {
                        Text("Ver Tudo", color = OlirumBlue, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (isLoadingTrending) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = OlirumCyan)
                    }
                }
            } else {
                items(trendingTracks.take(8)) { track ->
                    val isThisPlaying = isPlaying && currentTrack?.id == track.id
                    TrackListItem(
                        track = track,
                        isPlaying = isThisPlaying,
                        onTrackClick = { viewModel.playTrack(track, trendingTracks) },
                        onFavoriteClick = { viewModel.toggleFavorite(track) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(115.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = OlirumWhite,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(gradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = OlirumTextPrimary,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = OlirumTextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}
