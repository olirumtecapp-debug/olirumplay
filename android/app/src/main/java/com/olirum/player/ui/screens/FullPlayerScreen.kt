package com.olirum.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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
import com.olirum.player.ui.components.CoverArtSearchDialog
import com.olirum.player.ui.components.formatDuration
import com.olirum.player.ui.theme.*
import com.olirum.player.ui.viewmodel.MainPlayerViewModel

@Composable
fun FullPlayerScreen(
    viewModel: MainPlayerViewModel,
    onDismiss: () -> Unit,
    onNavigateToLyrics: () -> Unit
) {
    val colors = LocalOlirumColors.current
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
    val isRadio = track.source == "radio"

    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var showCoverSearchDialog by remember { mutableStateOf(false) }

    val progress = if (isDraggingSlider) {
        sliderPosition
    } else {
        if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    }

    if (showCoverSearchDialog && !isRadio) {
        CoverArtSearchDialog(
            track = track,
            onDismiss = { showCoverSearchDialog = false },
            onSearch = { q -> viewModel.searchCoverCandidates(q) },
            onSelectCover = { url -> viewModel.setManualCover(track, url) }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 14.dp),
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
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Fechar",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = if (isRadio) "RÁDIO AO VIVO" else "TOCANDO AGORA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OlirumCyan,
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = track.album,
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!isRadio) {
                    IconButton(onClick = onNavigateToLyrics) {
                        Icon(
                            imageVector = Icons.Rounded.Mic,
                            contentDescription = "Ver Letras",
                            tint = OlirumCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }

            // Capa Grande Centralizada
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.card)
                    .clickable(enabled = !isRadio) {
                        showCoverSearchDialog = true
                    },
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
                        imageVector = if (isRadio) Icons.Rounded.Radio else Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = if (isRadio) OlirumCyan else OlirumBlue,
                        modifier = Modifier.size(90.dp)
                    )
                }

                if (!isRadio) {
                    // Badge sutil para trocar capa
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Image,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Alterar Capa",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
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
                        color = colors.textPrimary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = track.artist,
                        fontSize = 14.sp,
                        color = colors.textSecondary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = { viewModel.toggleFavorite(track) }) {
                    Icon(
                        imageVector = if (track.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favoritar",
                        tint = if (track.isFavorite) OlirumMagenta else colors.textSecondary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Seekbar ou Indicador AO VIVO
            if (isRadio) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE53935).copy(alpha = 0.15f))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TRANSMISSÃO AO VIVO • ÁUDIO CONTÍNUO",
                        color = Color(0xFFE53935),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            } else {
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
                            thumbColor = OlirumCyan,
                            activeTrackColor = OlirumCyan,
                            inactiveTrackColor = colors.border
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
                            color = colors.textSecondary,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = formatDuration(durationMs),
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            // Controles de Reprodução Principais
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Aleatório (desativado para rádio)
                IconButton(
                    onClick = { viewModel.toggleShuffle() },
                    enabled = !isRadio
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Aleatório",
                        tint = if (!isRadio && isShuffle) OlirumCyan else colors.textSecondary.copy(alpha = if (isRadio) 0.3f else 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Anterior (desativado para rádio)
                IconButton(
                    onClick = { viewModel.skipPrevious() },
                    enabled = !isRadio
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Anterior",
                        tint = if (isRadio) colors.textSecondary.copy(alpha = 0.3f) else colors.textPrimary,
                        modifier = Modifier.size(34.dp)
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
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Tocar",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Próxima (desativado para rádio)
                IconButton(
                    onClick = { viewModel.skipNext() },
                    enabled = !isRadio
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Próxima",
                        tint = if (isRadio) colors.textSecondary.copy(alpha = 0.3f) else colors.textPrimary,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Repetir (desativado para rádio)
                IconButton(
                    onClick = { viewModel.toggleRepeat() },
                    enabled = !isRadio
                ) {
                    val repeatIcon = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat
                    val repeatTint = if (!isRadio && repeatMode != Player.REPEAT_MODE_OFF) OlirumCyan else colors.textSecondary.copy(alpha = if (isRadio) 0.3f else 0.7f)
                    Icon(
                        imageVector = repeatIcon,
                        contentDescription = "Repetir",
                        tint = repeatTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Rodapé
            if (!isRadio) {
                OutlinedButton(
                    onClick = onNavigateToLyrics,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OlirumCyan),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Icon(Icons.Rounded.Lyrics, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Acompanhar Letra Sincronizada", fontSize = 13.sp, maxLines = 1, softWrap = false)
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
