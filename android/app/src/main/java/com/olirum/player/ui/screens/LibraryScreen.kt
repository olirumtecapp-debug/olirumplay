package com.olirum.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olirum.player.OlirumApplication
import com.olirum.player.model.Track
import com.olirum.player.ui.components.OlirumTopBar
import com.olirum.player.ui.components.TrackListItem
import com.olirum.player.ui.theme.*
import com.olirum.player.ui.viewmodel.MainPlayerViewModel

@Composable
fun LibraryScreen(
    viewModel: MainPlayerViewModel
) {
    val favorites by OlirumApplication.instance.musicRepository.favoriteTracks.collectAsState(initial = emptyList())
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    Scaffold(
        topBar = {
            OlirumTopBar(
                title = "Sua Biblioteca",
                subtitle = "${favorites.size} músicas favoritadas"
            )
        },
        containerColor = OlirumBackgroundLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (favorites.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
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
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tocar Favoritas", fontWeight = FontWeight.Bold)
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
                        Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Aleatório", fontWeight = FontWeight.Bold)
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
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nenhuma música favorita ainda",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = OlirumTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Toque no ícone de coração em qualquer música para salvá-la aqui e ouvir sempre que quiser.",
                            fontSize = 13.sp,
                            color = OlirumTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
        }
    }
}
