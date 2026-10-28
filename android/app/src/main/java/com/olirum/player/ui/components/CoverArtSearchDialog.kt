package com.olirum.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.olirum.player.data.repository.CoverCandidate
import com.olirum.player.model.Track
import com.olirum.player.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CoverArtSearchDialog(
    track: Track,
    onDismiss: () -> Unit,
    onSearch: suspend (String) -> List<CoverCandidate>,
    onSelectCover: (String) -> Unit
) {
    val colors = LocalOlirumColors.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember {
        val initial = if (track.album.isNotBlank() && track.album != "Álbum do Dispositivo" && track.album != "Desconhecido") {
            "${track.album} ${track.artist}"
        } else {
            "${track.title} ${track.artist}"
        }
        mutableStateOf(initial.trim())
    }

    var candidates by remember { mutableStateOf<List<CoverCandidate>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }

    fun executeSearch() {
        if (searchQuery.isBlank()) return
        coroutineScope.launch {
            isLoading = true
            hasSearched = true
            candidates = onSearch(searchQuery)
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        executeSearch()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = colors.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Cabeçalho
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Buscar Capa Oficial",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = "${track.title} • ${track.artist}",
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Fechar",
                            tint = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Campo de Busca
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Álbum ou artista...",
                            fontSize = 13.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = colors.textSecondary
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { executeSearch() }) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Buscar",
                                tint = OlirumCyan
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OlirumCyan,
                        unfocusedBorderColor = colors.border,
                        focusedContainerColor = colors.card,
                        unfocusedContainerColor = colors.card,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Área de Resultados
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = OlirumCyan)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Buscando no Cover Art Archive...",
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    } else if (candidates.isEmpty() && hasSearched) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Image,
                                contentDescription = null,
                                tint = colors.textSecondary.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Nenhuma capa encontrada",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tente alterar o nome do álbum ou buscar somente pelo nome do artista.",
                                fontSize = 12.sp,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(candidates) { item ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            onSelectCover(item.fullUrl)
                                            onDismiss()
                                        },
                                    color = colors.card,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(colors.surface),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(item.thumbnailUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = item.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = item.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Text(
                                            text = "${item.artist}${if (item.year.isNotBlank()) " • ${item.year}" else ""}",
                                            fontSize = 11.sp,
                                            color = colors.textSecondary,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "Toque para aplicar",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = OlirumCyan,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
