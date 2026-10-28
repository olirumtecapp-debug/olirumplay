package com.olirum.player.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.olirum.player.model.RadioStation
import com.olirum.player.ui.theme.*

@Composable
fun RadioStationCard(
    station: RadioStation,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onFavoriteClick: (() -> Unit)? = null,
    isFavorite: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = LocalOlirumColors.current

    // Animação de pulso para a badge AO VIVO
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val liveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liveAlpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onPlayClick),
        color = if (isPlaying) OlirumCyan.copy(alpha = 0.12f) else colors.card,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = if (isPlaying) 6.dp else 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagem / Ícone da Rádio
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface),
                contentAlignment = Alignment.Center
            ) {
                if (!station.artworkUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(station.artworkUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = station.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Radio,
                        contentDescription = null,
                        tint = if (isPlaying) OlirumCyan else OlirumBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }

                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
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

            // Informações da Rádio (Zero quebra de linha)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Badge AO VIVO
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE53935).copy(alpha = if (isPlaying) 1f else 0.85f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = if (isPlaying) liveAlpha else 1f))
                            )
                            Text(
                                text = "AO VIVO",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Gênero
                    Text(
                        text = station.genre,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OlirumCyan,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Nome da Estação
                Text(
                    text = station.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPlaying) OlirumCyan else colors.textPrimary,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Localização & Bitrate
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = station.location,
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = station.bitrate,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = OlirumMint,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Botão Favoritar
            if (onFavoriteClick != null) {
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favoritar",
                        tint = if (isFavorite) OlirumMagenta else colors.textSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Botão de Play / Equalizer
            IconButton(
                onClick = onPlayClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (isPlaying) OlirumCyan else OlirumBlue.copy(alpha = 0.12f)
                ),
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Equalizer else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPlaying) "Ouvindo" else "Ouvir",
                    tint = if (isPlaying) Color(0xFF0F172A) else OlirumBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
