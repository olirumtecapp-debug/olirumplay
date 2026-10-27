package com.olirum.player.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olirum.player.OlirumApplication
import com.olirum.player.data.local.PlaylistEntity
import com.olirum.player.model.Track
import com.olirum.player.ui.components.OlirumTopBar
import com.olirum.player.ui.components.TrackListItem
import com.olirum.player.ui.theme.*
import com.olirum.player.ui.viewmodel.MainPlayerViewModel
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(
    viewModel: MainPlayerViewModel
) {
    val colors = LocalOlirumColors.current
    val coroutineScope = rememberCoroutineScope()
    val favorites by OlirumApplication.instance.musicRepository.favoriteTracks.collectAsState(initial = emptyList())
    val playlists by viewModel.playlists.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Favoritas, 1: Playlists
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var openedPlaylist by remember { mutableStateOf<PlaylistEntity?>(null) }
    var playlistTracks by remember { mutableStateOf<List<Track>>(emptyList()) }

    LaunchedEffect(openedPlaylist, favorites) {
        if (openedPlaylist != null) {
            val trackIds = OlirumApplication.instance.musicRepository.getTrackIdsFromPlaylist(openedPlaylist!!)
            val localTracks = OlirumApplication.instance.musicRepository.scanLocalTracks()
            val allPool = (favorites + localTracks).distinctBy { it.id }
            playlistTracks = trackIds.mapNotNull { id -> allPool.find { it.id == id } }
        }
    }

    Scaffold(
        topBar = {
            OlirumTopBar(
                title = if (openedPlaylist != null) openedPlaylist!!.name else "Sua Biblioteca",
                subtitle = if (openedPlaylist != null) "${playlistTracks.size} músicas na playlist" else "${favorites.size} favoritas • ${playlists.size} playlists",
                onToggleTheme = { viewModel.toggleDarkMode() },
                isDarkTheme = isDarkMode,
                actions = {
                    if (openedPlaylist != null) {
                        IconButton(onClick = { openedPlaylist = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = colors.textPrimary)
                        }
                    } else if (selectedTab == 1) {
                        IconButton(onClick = { showCreateDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Nova Playlist", tint = OlirumBlue)
                        }
                    }
                }
            )
        },
        containerColor = colors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (openedPlaylist != null) {
                // Visualização de Playlist Aberta
                val pl = openedPlaylist!!
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surface,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pl.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${playlistTracks.size} faixas na playlist",
                                fontSize = 12.sp,
                                color = colors.textSecondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        if (playlistTracks.isNotEmpty()) {
                            Button(
                                onClick = {
                                    viewModel.playTrack(playlistTracks.first(), playlistTracks)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OlirumBlue)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tocar Playlist", maxLines = 1, softWrap = false)
                            }
                        }
                    }
                }

                if (playlistTracks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Esta playlist ainda está vazia.\nToque no ícone '+' em qualquer música para adicioná-la aqui.",
                            color = colors.textSecondary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        items(playlistTracks, key = { it.id }) { track ->
                            val isThisPlaying = isPlaying && currentTrack?.id == track.id
                            TrackListItem(
                                track = track,
                                isPlaying = isThisPlaying,
                                onTrackClick = { viewModel.playTrack(track, playlistTracks) },
                                onFavoriteClick = { viewModel.toggleFavorite(track) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(90.dp))
                        }
                    }
                }
            } else {
                // Abas Principais: Favoritas vs Playlists
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = {
                            Text(
                                text = "Favoritas (${favorites.size})",
                                maxLines = 1,
                                softWrap = false,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OlirumCyan.copy(alpha = 0.2f),
                            selectedLabelColor = OlirumBlue,
                            containerColor = colors.surface
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = {
                            Text(
                                text = "Playlists (${playlists.size})",
                                maxLines = 1,
                                softWrap = false,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OlirumCyan.copy(alpha = 0.2f),
                            selectedLabelColor = OlirumBlue,
                            containerColor = colors.surface
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (selectedTab == 0) {
                    // Aba Favoritas
                    if (favorites.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    viewModel.playTrack(favorites.first(), favorites)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = OlirumCyan.copy(alpha = 0.15f),
                                    contentColor = OlirumBlue
                                )
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tocar Favoritas", fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                            }

                            FilledTonalButton(
                                onClick = {
                                    val shuffled = favorites.shuffled()
                                    viewModel.playTrack(shuffled.first(), shuffled)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = OlirumMagenta.copy(alpha = 0.15f),
                                    contentColor = OlirumMagenta
                                )
                            ) {
                                Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Aleatório", fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                            }
                        }
                    }

                    if (favorites.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = OlirumMagenta.copy(alpha = 0.4f),
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Nenhuma música favoritada",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Toque no coração de qualquer música para salvá-la aqui.",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            items(favorites, key = { it.id }) { track ->
                                val isThisPlaying = isPlaying && currentTrack?.id == track.id
                                TrackListItem(
                                    track = track,
                                    isPlaying = isThisPlaying,
                                    onTrackClick = { viewModel.playTrack(track, favorites) },
                                    onFavoriteClick = { viewModel.toggleFavorite(track) }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(90.dp))
                            }
                        }
                    }
                } else {
                    // Aba Playlists
                    Column(modifier = Modifier.fillMaxSize()) {
                        Button(
                            onClick = { showCreateDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OlirumBlue)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Criar Nova Playlist", fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                        }

                        if (playlists.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.QueueMusic,
                                        contentDescription = null,
                                        tint = colors.textSecondary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "Nenhuma playlist criada ainda",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Toque em 'Criar Nova Playlist' acima para organizar suas músicas.",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                items(playlists, key = { it.id }) { pl ->
                                    val count = OlirumApplication.instance.musicRepository.getTrackIdsFromPlaylist(pl).size
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { openedPlaylist = pl },
                                        color = colors.surface,
                                        shadowElevation = 1.dp
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.QueueMusic,
                                                    contentDescription = null,
                                                    tint = OlirumBlue,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = pl.name,
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = colors.textPrimary,
                                                        maxLines = 1,
                                                        softWrap = false,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = "$count músicas",
                                                        fontSize = 12.sp,
                                                        color = colors.textSecondary,
                                                        maxLines = 1,
                                                        softWrap = false
                                                    )
                                                }
                                            }

                                            IconButton(onClick = { viewModel.deletePlaylist(pl.id) }) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "Excluir",
                                                    tint = colors.textSecondary.copy(alpha = 0.6f)
                                                )
                                            }
                                        }
                                    }
                                }
                                item {
                                    Spacer(modifier = Modifier.height(90.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Diálogo para Criar Playlist
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = {
                    showCreateDialog = false
                    newPlaylistName = ""
                },
                title = {
                    Text(
                        text = "Nova Playlist",
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Digite um nome para a playlist:",
                            fontSize = 13.sp,
                            color = colors.textSecondary,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPlaylistName,
                            onValueChange = { newPlaylistName = it },
                            placeholder = { Text("Ex: Viagem, Treino, Favoritas...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPlaylistName.isNotBlank()) {
                                viewModel.createPlaylist(newPlaylistName)
                                showCreateDialog = false
                                newPlaylistName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OlirumBlue)
                    ) {
                        Text("Criar", maxLines = 1, softWrap = false)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showCreateDialog = false
                        newPlaylistName = ""
                    }) {
                        Text("Cancelar", maxLines = 1, softWrap = false)
                    }
                },
                containerColor = colors.surface
            )
        }
    }
}
