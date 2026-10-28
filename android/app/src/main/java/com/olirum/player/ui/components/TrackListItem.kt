package com.olirum.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.olirum.player.model.Track
import com.olirum.player.ui.theme.*

@Composable
fun TrackListItem(
    track: Track,
    isPlaying: Boolean,
    onTrackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onAddToPlaylistClick: (() -> Unit)? = null,
    onChangeCoverClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalOlirumColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isPlaying) OlirumCyan.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(onClick = onTrackClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Capa do Álbum
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.card)
                .clickable(enabled = onChangeCoverClick != null) {
                    onChangeCoverClick?.invoke()
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
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = OlirumCyan,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.40f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Equalizer,
                        contentDescription = "Tocando",
                        tint = OlirumCyan,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Título e Artista (Zero quebra de linha)
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = track.title,
                fontSize = 15.sp,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Medium,
                color = if (isPlaying) OlirumCyan else colors.textPrimary,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${track.artist} • ${formatDuration(track.durationMs)}",
                fontSize = 12.sp,
                color = colors.textSecondary,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Alterar Capa (se disponível)
        if (onChangeCoverClick != null) {
            IconButton(
                onClick = onChangeCoverClick,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Image,
                    contentDescription = "Alterar Capa",
                    tint = colors.textSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        // Adicionar à Playlist (se disponível)
        if (onAddToPlaylistClick != null) {
            IconButton(
                onClick = onAddToPlaylistClick,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlaylistAdd,
                    contentDescription = "Adicionar à Playlist",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Botão Favorito
        IconButton(
            onClick = onFavoriteClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (track.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = "Favoritar",
                tint = if (track.isFavorite) OlirumMagenta else colors.textSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
