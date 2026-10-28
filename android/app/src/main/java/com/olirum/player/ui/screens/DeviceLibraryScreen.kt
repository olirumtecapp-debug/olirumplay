package com.olirum.player.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.olirum.player.OlirumApplication
import com.olirum.player.model.MusicFolder
import com.olirum.player.model.Track
import com.olirum.player.ui.components.CoverArtSearchDialog
import com.olirum.player.ui.components.OlirumTopBar
import com.olirum.player.ui.components.TrackListItem
import com.olirum.player.ui.theme.*
import com.olirum.player.ui.viewmodel.MainPlayerViewModel
import kotlinx.coroutines.launch

@Composable
fun DeviceLibraryScreen(
    viewModel: MainPlayerViewModel
) {
    val context = LocalContext.current
    val colors = LocalOlirumColors.current
    val coroutineScope = rememberCoroutineScope()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val playlists by viewModel.playlists.collectAsState()

    var localTracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Todas, 1: Pastas
    var selectedFolder by remember { mutableStateOf<MusicFolder?>(null) }
    var trackToAddToPlaylist by remember { mutableStateOf<Track?>(null) }
    var trackToChangeCover by remember { mutableStateOf<Track?>(null) }

    fun doScan() {
        coroutineScope.launch {
            isScanning = true
            localTracks = OlirumApplication.instance.musicRepository.scanLocalTracks()
            isScanning = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            doScan()
        }
    }

    fun requestPermissionAndScan() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            doScan()
        } else {
            permissionLauncher.launch(permission)
        }
    }

    LaunchedEffect(Unit) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            doScan()
        }
    }

    val folders = remember(localTracks) {
        OlirumApplication.instance.musicRepository.groupTracksByFolder(localTracks)
    }

    val filteredTracks = remember(localTracks, searchQuery) {
        if (searchQuery.isBlank()) {
            localTracks
        } else {
            localTracks.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.artist.contains(searchQuery, ignoreCase = true) ||
                it.album.contains(searchQuery, ignoreCase = true) ||
                it.folderName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    if (trackToChangeCover != null) {
        val trk = trackToChangeCover!!
        CoverArtSearchDialog(
            track = trk,
            onDismiss = { trackToChangeCover = null },
            onSearch = { q -> viewModel.searchCoverCandidates(q) },
            onSelectCover = { coverUrl ->
                viewModel.setManualCover(trk, coverUrl)
                localTracks = localTracks.map {
                    if (it.id == trk.id) it.copy(artworkUri = coverUrl) else it
                }
            }
        )
    }

    Scaffold(
        topBar = {
            OlirumTopBar(
                title = if (selectedFolder != null) selectedFolder!!.name else "Músicas Locais",
                subtitle = if (selectedFolder != null) "${selectedFolder!!.trackCount} músicas na pasta" else "${localTracks.size} faixas encontradas",
                onToggleTheme = { viewModel.toggleDarkMode() },
                isDarkTheme = isDarkMode,
                actions = {
                    if (selectedFolder != null) {
                        IconButton(onClick = { selectedFolder = null }) {
                            Icon(Icons.Rounded.ArrowBack, contentDescription = "Voltar", tint = colors.textPrimary)
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
            // Se estiver dentro de uma pasta específica
            if (selectedFolder != null) {
                val folder = selectedFolder!!
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    color = colors.surface,
                    shadowElevation = 2.dp
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = com.olirum.player.R.drawable.bg_folder_header),
                            contentDescription = null,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop,
                            alpha = 0.35f
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pasta: ${folder.name}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = folder.path,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        if (folder.tracks.isNotEmpty()) {
                                            viewModel.playTrack(folder.tracks.first(), folder.tracks)
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = OlirumCyan),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color(0xFF070B14), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Tocar", color = Color(0xFF070B14), fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                }

                                OutlinedButton(
                                    onClick = {
                                        if (folder.tracks.isNotEmpty()) {
                                            val shuffled = folder.tracks.shuffled()
                                            viewModel.playTrack(shuffled.first(), shuffled)
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, OlirumPurple),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OlirumPurple),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Rounded.Shuffle, contentDescription = null, tint = OlirumPurple, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Aleatório", color = OlirumPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                }
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    items(folder.tracks, key = { it.id }) { track ->
                        val isThisPlaying = isPlaying && currentTrack?.id == track.id
                        TrackListItem(
                            track = track,
                            isPlaying = isThisPlaying,
                            onTrackClick = { viewModel.playTrack(track, folder.tracks) },
                            onFavoriteClick = {
                                viewModel.toggleFavorite(track)
                                localTracks = localTracks.map { if (it.id == track.id) it.copy(isFavorite = !track.isFavorite) else it }
                            },
                            onAddToPlaylistClick = { trackToAddToPlaylist = track },
                            onChangeCoverClick = { trackToChangeCover = track }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(90.dp))
                    }
                }
            } else {
                // Card de Escanear
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = colors.surface,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Biblioteca do Smartphone",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isScanning) "Escaneando arquivos de áudio..." else "${localTracks.size} músicas prontas para tocar",
                                fontSize = 12.sp,
                                color = colors.textSecondary,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = { requestPermissionAndScan() },
                            enabled = !isScanning,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OlirumCyan)
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color(0xFF070B14),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Rounded.Refresh, contentDescription = null, tint = Color(0xFF070B14), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Escanear", color = Color(0xFF070B14), fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                            }
                        }
                    }
                }

                // Campo de Busca
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = {
                        Text(
                            text = "Buscar música ou pasta...",
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Rounded.Search, contentDescription = null, tint = colors.textSecondary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Limpar", tint = colors.textSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OlirumCyan,
                        unfocusedBorderColor = colors.border,
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                // Abas de Visualização: Músicas vs Pastas
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
                                text = "Músicas (${localTracks.size})",
                                maxLines = 1,
                                softWrap = false,
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
                                text = "Pastas (${folders.size})",
                                maxLines = 1,
                                softWrap = false,
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
                }

                // Conteúdo da Aba 0: Todas as Músicas
                if (selectedTab == 0) {
                    if (filteredTracks.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (filteredTracks.isNotEmpty()) {
                                        viewModel.playTrack(filteredTracks.first(), filteredTracks)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OlirumCyan)
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color(0xFF070B14), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tocar Todas", color = Color(0xFF070B14), fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                            }

                            OutlinedButton(
                                onClick = {
                                    if (filteredTracks.isNotEmpty()) {
                                        val shuffled = filteredTracks.shuffled()
                                        viewModel.playTrack(shuffled.first(), shuffled)
                                    }
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

                    if (localTracks.isEmpty() && !isScanning) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Rounded.MusicOff,
                                    contentDescription = null,
                                    tint = colors.textSecondary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Nenhuma música escaneada ainda",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Toque no botão 'Escanear' acima para carregar suas faixas.",
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
                            items(filteredTracks, key = { it.id }) { track ->
                                val isThisPlaying = isPlaying && currentTrack?.id == track.id
                                TrackListItem(
                                    track = track,
                                    isPlaying = isThisPlaying,
                                    onTrackClick = { viewModel.playTrack(track, filteredTracks) },
                                    onFavoriteClick = {
                                viewModel.toggleFavorite(track)
                                localTracks = localTracks.map { if (it.id == track.id) it.copy(isFavorite = !track.isFavorite) else it }
                            },
                                    onAddToPlaylistClick = { trackToAddToPlaylist = track },
                                    onChangeCoverClick = { trackToChangeCover = track }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(90.dp))
                            }
                        }
                    }
                } else {
                    // Conteúdo da Aba 1: Pastas do Celular
                    if (folders.isEmpty() && !isScanning) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhuma pasta com música encontrada.",
                                color = colors.textSecondary,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            items(folders, key = { it.path }) { folder ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedFolder = folder },
                                    color = colors.surface,
                                    shadowElevation = 1.dp
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(OlirumCyan.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Folder,
                                                contentDescription = null,
                                                tint = OlirumCyan,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = folder.name,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${folder.trackCount} músicas • ${folder.path}",
                                                fontSize = 12.sp,
                                                color = colors.textSecondary,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                if (folder.tracks.isNotEmpty()) {
                                                    val shuffled = folder.tracks.shuffled()
                                                    viewModel.playTrack(shuffled.first(), shuffled)
                                                }
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Shuffle,
                                                contentDescription = "Tocar Pasta Aleatório",
                                                tint = OlirumPurple,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                if (folder.tracks.isNotEmpty()) {
                                                    viewModel.playTrack(folder.tracks.first(), folder.tracks)
                                                }
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.PlayArrow,
                                                contentDescription = "Tocar Pasta",
                                                tint = OlirumCyan,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Rounded.ChevronRight,
                                            contentDescription = null,
                                            tint = colors.textSecondary
                                        )
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

        // Diálogo para Adicionar à Playlist
        if (trackToAddToPlaylist != null) {
            val targetTrack = trackToAddToPlaylist!!
            var newPlaylistName by remember { mutableStateOf("") }
            var showCreateInput by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { trackToAddToPlaylist = null },
                title = {
                    Text(
                        text = "Adicionar à Playlist",
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = targetTrack.title,
                            fontSize = 13.sp,
                            color = colors.textSecondary,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (showCreateInput) {
                            OutlinedTextField(
                                value = newPlaylistName,
                                onValueChange = { newPlaylistName = it },
                                placeholder = { Text("Nome da playlist...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OlirumCyan,
                                    unfocusedBorderColor = colors.border
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (newPlaylistName.isNotBlank()) {
                                        coroutineScope.launch {
                                            val created = OlirumApplication.instance.musicRepository.createPlaylist(newPlaylistName)
                                            viewModel.addTrackToPlaylist(created.id, targetTrack)
                                            trackToAddToPlaylist = null
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = OlirumCyan)
                            ) {
                                Text("Criar e Adicionar", color = Color(0xFF070B14), fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                            }
                        } else {
                            if (playlists.isEmpty()) {
                                Text(
                                    text = "Nenhuma playlist criada ainda.",
                                    fontSize = 13.sp,
                                    color = colors.textSecondary
                                )
                            } else {
                                playlists.forEach { pl ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                viewModel.addTrackToPlaylist(pl.id, targetTrack)
                                                trackToAddToPlaylist = null
                                            }
                                            .padding(vertical = 10.dp, horizontal = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Rounded.QueueMusic, contentDescription = null, tint = OlirumCyan)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = pl.name,
                                            fontWeight = FontWeight.Medium,
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { showCreateInput = true }) {
                                Icon(Icons.Rounded.Add, contentDescription = null, tint = OlirumCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Criar Nova Playlist", color = OlirumCyan, maxLines = 1, softWrap = false)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { trackToAddToPlaylist = null }) {
                        Text("Fechar", color = colors.textSecondary)
                    }
                },
                containerColor = colors.surface
            )
        }
    }
}
