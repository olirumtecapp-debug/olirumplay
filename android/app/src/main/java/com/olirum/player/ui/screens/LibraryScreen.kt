package com.olirum.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.olirum.player.OlirumApplication
import com.olirum.player.data.local.PlaylistEntity
import com.olirum.player.model.RadioStation
import com.olirum.player.model.Track
import com.olirum.player.ui.components.OlirumTopBar
import com.olirum.player.ui.components.RadioStationCard
import com.olirum.player.ui.components.TrackListItem
import com.olirum.player.ui.theme.*
import com.olirum.player.ui.viewmodel.MainPlayerViewModel
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(
    viewModel: MainPlayerViewModel,
    onNavigateToDevice: () -> Unit
) {
    val colors = LocalOlirumColors.current
    val coroutineScope = rememberCoroutineScope()
    val allFavorites by OlirumApplication.instance.musicRepository.favoriteTracks.collectAsState(initial = emptyList())
    val playlists by viewModel.playlists.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    // Separação de músicas locais e rádios favoritas conforme o prompt
    val localFavorites = remember(allFavorites) { allFavorites.filter { it.source != "radio" } }
    val radioFavorites = remember(allFavorites) { allFavorites.filter { it.source == "radio" } }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Músicas Favoritas, 1: Playlists, 2: Rádios Favoritas
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var openedPlaylist by remember { mutableStateOf<PlaylistEntity?>(null) }
    var playlistTracks by remember { mutableStateOf<List<Track>>(emptyList()) }

    LaunchedEffect(openedPlaylist, allFavorites) {
        if (openedPlaylist != null) {
            val trackIds = OlirumApplication.instance.musicRepository.getTrackIdsFromPlaylist(openedPlaylist!!)
            val localTracks = OlirumApplication.instance.musicRepository.scanLocalTracks()
            val allPool = (allFavorites + localTracks).distinctBy { it.id }
            playlistTracks = trackIds.mapNotNull { id -> allPool.find { it.id == id } }
        }
    }

    Scaffold(
        topBar = {
            OlirumTopBar(
                title = if (openedPlaylist != null) openedPlaylist!!.name else "Sua Biblioteca",
                subtitle = if (openedPlaylist != null) "${playlistTracks.size} músicas na playlist" else "${localFavorites.size} músicas • ${radioFavorites.size} rádios",
                onToggleTheme = { viewModel.toggleDarkMode() },
                isDarkTheme = isDarkMode,
                actions = {
                    if (openedPlaylist != null) {
                        IconButton(onClick = { openedPlaylist = null }) {
                            Icon(Icons.Rounded.ArrowBack, contentDescription = "Voltar", tint = colors.textPrimary)
                        }
                    } else if (selectedTab == 1) {
                        IconButton(onClick = { showCreateDialog = true }) {
                            Icon(Icons.Rounded.Add, contentDescription = "Nova Playlist", tint = OlirumCyan)
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
                                colors = ButtonDefaults.buttonColors(containerColor = OlirumCyan)
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color(0xFF070B14), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tocar", color = Color(0xFF070B14), fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
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
                            text = "Esta playlist ainda está vazia.\nToque no ícone '+' em qualquer música local para adicioná-la aqui.",
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
                // Card de Atalho para Músicas Locais / Pastas
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onNavigateToDevice),
                    color = colors.card,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(OlirumCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FolderOpen,
                                contentDescription = null,
                                tint = OlirumCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Músicas & Pastas do Celular",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = "Navegar pelos arquivos locais e pastas do aparelho",
                                fontSize = 12.sp,
                                color = colors.textSecondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = colors.textSecondary
                        )
                    }
                }

                // Abas Principais: Favoritas vs Playlists vs Rádios
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = {
                            Text(
                                text = "Músicas (${localFavorites.size})",
                                maxLines = 1,
                                softWrap = false,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OlirumCyan,
                            selectedLabelColor = Color(0xFF070B14),
                            containerColor = colors.surface,
                            labelColor = colors.textSecondary
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
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OlirumCyan,
                            selectedLabelColor = Color(0xFF070B14),
                            containerColor = colors.surface,
                            labelColor = colors.textSecondary
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        label = {
                            Text(
                                text = "Rádios (${radioFavorites.size})",
                                maxLines = 1,
                                softWrap = false,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OlirumCyan,
                            selectedLabelColor = Color(0xFF070B14),
                            containerColor = colors.surface,
                            labelColor = colors.textSecondary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                when (selectedTab) {
                    0 -> {
                        // Aba Músicas Favoritas
                        if (localFavorites.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.playTrack(localFavorites.first(), localFavorites)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = OlirumCyan)
                                ) {
                                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color(0xFF070B14), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tocar Favoritas", color = Color(0xFF070B14), fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val shuffled = localFavorites.shuffled()
                                        viewModel.playTrack(shuffled.first(), shuffled)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OlirumCyan)
                                ) {
                                    Icon(Icons.Rounded.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Aleatório", fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                }
                            }
                        }

                        if (localFavorites.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Rounded.FavoriteBorder,
                                        contentDescription = null,
                                        tint = colors.textSecondary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Nenhuma música favorita ainda",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Toque no coração de qualquer música para adicioná-la aqui.",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                items(localFavorites, key = { it.id }) { track ->
                                    val isThisPlaying = isPlaying && currentTrack?.id == track.id
                                    TrackListItem(
                                        track = track,
                                        isPlaying = isThisPlaying,
                                        onTrackClick = { viewModel.playTrack(track, localFavorites) },
                                        onFavoriteClick = { viewModel.toggleFavorite(track) }
                                    )
                                }
                                item {
                                    Spacer(modifier = Modifier.height(90.dp))
                                }
                            }
                        }
                    }

                    1 -> {
                        // Aba Playlists
                        if (playlists.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Rounded.QueueMusic,
                                        contentDescription = null,
                                        tint = colors.textSecondary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Você ainda não tem playlists",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Crie sua primeira playlist para organizar suas faixas.",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { showCreateDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = OlirumCyan),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Rounded.Add, contentDescription = null, tint = Color(0xFF070B14), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Criar Playlist", color = Color(0xFF070B14), fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(playlists, key = { it.id }) { pl ->
                                    val count = OlirumApplication.instance.musicRepository.getTrackIdsFromPlaylist(pl).size
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { openedPlaylist = pl },
                                        color = colors.surface,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(OlirumGradientBrush),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.QueueMusic,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(14.dp))

                                            Column(modifier = Modifier.weight(1f)) {
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

                                            IconButton(onClick = { viewModel.deletePlaylist(pl.id) }) {
                                                Icon(
                                                    imageVector = Icons.Rounded.DeleteOutline,
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

                    2 -> {
                        // Aba Rádios Favoritas (Separadas claramente)
                        if (radioFavorites.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Rounded.Radio,
                                        contentDescription = null,
                                        tint = colors.textSecondary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Nenhuma rádio favoritada",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Toque no coração de qualquer rádio online para salvá-la aqui.",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(radioFavorites, key = { it.id }) { trk ->
                                    val isThisPlaying = isPlaying && currentTrack?.id == trk.id
                                    val station = RadioStation(
                                        id = trk.id,
                                        name = trk.title,
                                        genre = trk.artist.split("•").firstOrNull()?.trim() ?: "Rádio",
                                        streamUrl = trk.mediaUri,
                                        artworkUrl = trk.artworkUri,
                                        location = trk.artist.split("•").getOrNull(1)?.trim() ?: "Online",
                                        isFavorite = true
                                    )
                                    RadioStationCard(
                                        station = station,
                                        isPlaying = isThisPlaying,
                                        isFavorite = true,
                                        onPlayClick = {
                                            viewModel.playTrack(trk, listOf(trk))
                                        },
                                        onFavoriteClick = {
                                            viewModel.toggleFavorite(trk)
                                        }
                                    )
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

        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Nova Playlist", fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        placeholder = { Text("Nome da playlist...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OlirumCyan,
                            unfocusedBorderColor = colors.border
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPlaylistName.isNotBlank()) {
                                viewModel.createPlaylist(newPlaylistName.trim())
                                newPlaylistName = ""
                                showCreateDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OlirumCyan)
                    ) {
                        Text("Criar", color = Color(0xFF070B14), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancelar", color = colors.textSecondary)
                    }
                },
                containerColor = colors.surface
            )
        }
    }
}
