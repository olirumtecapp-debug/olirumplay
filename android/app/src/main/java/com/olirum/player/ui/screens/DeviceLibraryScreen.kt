package com.olirum.player.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.olirum.player.OlirumApplication
import com.olirum.player.model.Track
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
    val coroutineScope = rememberCoroutineScope()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    var localTracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }

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

    // Auto-scan on launch if empty
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

    val filteredTracks = remember(localTracks, searchQuery) {
        if (searchQuery.isBlank()) {
            localTracks
        } else {
            localTracks.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.artist.contains(searchQuery, ignoreCase = true) ||
                it.album.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            OlirumTopBar(
                title = "Músicas no Dispositivo",
                subtitle = "${localTracks.size} faixas encontradas"
            )
        },
        containerColor = OlirumBackgroundLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Card Principal de Escaneamento
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = OlirumWhite,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Biblioteca do Smartphone",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = OlirumTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isScanning) "Escaneando arquivos de áudio..." else "${localTracks.size} músicas prontas para tocar",
                            fontSize = 12.sp,
                            color = OlirumTextSecondary
                        )
                    }

                    Button(
                        onClick = { requestPermissionAndScan() },
                        enabled = !isScanning,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OlirumBlue)
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Escanear")
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
                placeholder = { Text("Filtrar por nome, artista ou álbum...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = OlirumTextSecondary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpar",
                                tint = OlirumTextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OlirumBlue,
                    unfocusedBorderColor = OlirumBorderLight,
                    focusedContainerColor = OlirumWhite,
                    unfocusedContainerColor = OlirumWhite
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Barra de Ação Rápida: Tocar Todas / Aleatório
            if (filteredTracks.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            if (filteredTracks.isNotEmpty()) {
                                viewModel.playTrack(filteredTracks.first(), filteredTracks)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = OlirumCyan.copy(alpha = 0.15f),
                            contentColor = OlirumBlue
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tocar Todas", fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = {
                            if (filteredTracks.isNotEmpty()) {
                                val shuffled = filteredTracks.shuffled()
                                viewModel.playTrack(shuffled.first(), shuffled)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = OlirumPurple.copy(alpha = 0.15f),
                            contentColor = OlirumPurple
                        )
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Aleatório", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Lista de Músicas
            if (localTracks.isEmpty() && !isScanning) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MusicOff,
                            contentDescription = null,
                            tint = OlirumTextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nenhuma música local carregada",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = OlirumTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Toque em 'Escanear' acima para ler todos os arquivos MP3, FLAC e WAV do seu aparelho.",
                            fontSize = 13.sp,
                            color = OlirumTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    items(filteredTracks, key = { it.id }) { track ->
                        val isThisPlaying = isPlaying && currentTrack?.id == track.id
                        TrackListItem(
                            track = track,
                            isPlaying = isThisPlaying,
                            onTrackClick = { viewModel.playTrack(track, filteredTracks) },
                            onFavoriteClick = { viewModel.toggleFavorite(track) }
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
