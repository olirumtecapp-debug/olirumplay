package com.olirum.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olirum.player.OlirumApplication
import com.olirum.player.model.RadioStation
import com.olirum.player.model.Track
import com.olirum.player.ui.components.OlirumTopBar
import com.olirum.player.ui.components.RadioStationCard
import com.olirum.player.ui.components.TrackListItem
import com.olirum.player.ui.theme.*
import com.olirum.player.ui.viewmodel.MainPlayerViewModel

@Composable
fun HomeScreen(
    viewModel: MainPlayerViewModel,
    onNavigateToDevice: () -> Unit,
    onNavigateToExplore: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToWidgets: () -> Unit
) {
    val colors = LocalOlirumColors.current
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    var trendingTracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    var isLoadingTrending by remember { mutableStateOf(false) }

    val featuredRadios = remember {
        OlirumApplication.instance.radioRepository.curatedStations.take(6)
    }

    LaunchedEffect(Unit) {
        if (trendingTracks.isEmpty()) {
            isLoadingTrending = true
            trendingTracks = OlirumApplication.instance.musicRepository.getTrendingOnline()
            isLoadingTrending = false
        }
    }

    Scaffold(
        topBar = {
            OlirumTopBar(
                title = "OlirumPlay",
                subtitle = "Áudio em Alta Fidelidade",
                onToggleTheme = { viewModel.toggleDarkMode() },
                isDarkTheme = isDarkMode,
                actions = {
                    IconButton(onClick = onNavigateToWidgets) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Equalizador",
                            tint = colors.textPrimary
                        )
                    }
                }
            )
        },
        containerColor = colors.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Saudação
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text(
                        text = "Olá, bem-vindo ao OlirumPlay ✨",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Reprodução contínua e sem interrupções",
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
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
                        title = "Músicas & Pastas",
                        subtitle = "Do seu Celular",
                        icon = Icons.Default.FolderOpen,
                        gradient = Brush.linearGradient(listOf(OlirumMint, OlirumCyan)),
                        onClick = onNavigateToDevice,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "Rádios & Músicas",
                        subtitle = "Ao Vivo & Online",
                        icon = Icons.Default.Radio,
                        gradient = Brush.linearGradient(listOf(OlirumBlue, OlirumPurple)),
                        onClick = onNavigateToExplore,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "Biblioteca",
                        subtitle = "Playlists & Favs",
                        icon = Icons.Default.Favorite,
                        gradient = Brush.linearGradient(listOf(OlirumPurple, OlirumMagenta)),
                        onClick = onNavigateToLibrary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Banner Destaque
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(OlirumGradientBrush)
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ÁUDIO INDESTRUTÍVEL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Reprodução Contínua em 2º Plano",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tela apagada, WhatsApp ou outros apps: o áudio não para.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Seção: Rádios ao Vivo em Destaque
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📻 Rádios ao Vivo",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    TextButton(onClick = onNavigateToExplore) {
                        Text(
                            text = "Ver Mais Rádios",
                            color = OlirumBlue,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            // Lista de Rádios em Destaque (Católica, Rock Clássico, Clássica, Sertanejo, etc.)
            items(featuredRadios, key = { it.id }) { station ->
                val isThisPlaying = isPlaying && currentTrack?.id == station.id
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    RadioStationCard(
                        station = station,
                        isPlaying = isThisPlaying,
                        onPlayClick = {
                            val track = station.toTrack()
                            viewModel.playTrack(track, listOf(track))
                        }
                    )
                }
            }

            // Seção Tendências Online
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎵 Faixas do Catálogo Online",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                    TextButton(onClick = onNavigateToExplore) {
                        Text(
                            text = "Ver Catálogo",
                            color = OlirumBlue,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            if (isLoadingTrending) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = OlirumCyan)
                    }
                }
            } else {
                items(trendingTracks.take(6)) { track ->
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
    val colors = LocalOlirumColors.current

    Surface(
        modifier = modifier
            .height(105.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = colors.surface,
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
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(gradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
