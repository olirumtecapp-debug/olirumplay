package com.olirum.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.olirum.player.model.Track
import com.olirum.player.ui.components.formatDuration
import com.olirum.player.ui.theme.*
import com.olirum.player.ui.viewmodel.MainPlayerViewModel

@Composable
fun FullPlayerScreen(
    viewModel: MainPlayerViewModel,
    onDismiss: () -> Unit,
    onNavigateToLyrics: () -> Unit
) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val isShuffle by viewModel.isShuffle.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()

    if (currentTrack == null) {
        onDismiss()
        return
    }

    val track = currentTrack!!
    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }

    val progress = if (isDraggingSlider) {
        sliderPosition
    } else {
        if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = OlirumWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Cabeçalho Superior (Fechar + Título)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Fechar",
                        tint = OlirumTextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TOCANDO AGORA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OlirumBlue,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = track.album,
                        fontSize = 13.sp,
                        color = OlirumTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onNavigateToLyrics) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Ver Letras",
                        tint = OlirumBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Capa Grande Centralizada
            Box(
                modifier = Modifier
                    .size(290.dp)
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(OlirumSurfaceLight),
                contentAlignment = Alignment.Center
            ) {
                if (!track.artworkUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(track.artworkUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = track.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = OlirumBlue,
                        modifier = Modifier.size(96.dp)
                    )
                }
            }

            // Título, Artista e Botão Favorito
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = OlirumTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = track.artist,
                        fontSize = 15.sp,
                        color = OlirumTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = { viewModel.toggleFavorite(track) }) {
                    Icon(
                        imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favoritar",
                        tint = if (track.isFavorite) OlirumMagenta else OlirumTextSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Seekbar com tempo decorrido e restante
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = progress,
                    onValueChange = {
                        isDraggingSlider = true
                        sliderPosition = it
                    },
                    onValueChangeFinished = {
                        val seekTarget = (sliderPosition * durationMs).toLong()
                        viewModel.seekTo(seekTarget)
                        isDraggingSlider = false
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = OlirumBlue,
                        activeTrackColor = OlirumCyan,
                        inactiveTrackColor = OlirumBorderLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val currentDisplayMs = if (isDraggingSlider) (sliderPosition * durationMs).toLong() else currentPositionMs
                    Text(
                        text = formatDuration(currentDisplayMs),
                        fontSize = 12.sp,
                        color = OlirumTextSecondary
                    )
                    Text(
                        text = formatDuration(durationMs),
                        fontSize = 12.sp,
                        color = OlirumTextSecondary
                    )
                }
            }

            // Controles de Reprodução Principais
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Aleatório
                IconButton(onClick = { viewModel.toggleShuffle() }) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Aleatório",
                        tint = if (isShuffle) OlirumBlue else OlirumTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Anterior
                IconButton(onClick = { viewModel.skipPrevious() }) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Anterior",
                        tint = OlirumTextPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Play / Pause (Botão Grande com Degradê Olirum)
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(OlirumGradientBrush)
                        .clickable { viewModel.togglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Tocar",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Próxima
                IconButton(onClick = { viewModel.skipNext() }) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Próxima",
                        tint = OlirumTextPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Repetir
                IconButton(onClick = { viewModel.toggleRepeat() }) {
                    val repeatIcon = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat
                    val repeatTint = if (repeatMode != Player.REPEAT_MODE_OFF) OlirumBlue else OlirumTextSecondary
                    Icon(
                        imageVector = repeatIcon,
                        contentDescription = "Repetir",
                        tint = repeatTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Rodapé com atalho rápido de letra
            OutlinedButton(
                onClick = onNavigateToLyrics,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OlirumBlue),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(Icons.Default.Lyrics, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Acompanhar Letra Sincronizada", fontSize = 13.sp)
            }
        }
    }
}
