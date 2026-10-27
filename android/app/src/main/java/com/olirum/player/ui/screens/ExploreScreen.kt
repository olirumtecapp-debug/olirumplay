package com.olirum.player.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
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
import kotlinx.coroutines.launch

@Composable
fun ExploreScreen(
    viewModel: MainPlayerViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Track>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedGenre by remember { mutableStateOf("Em Alta") }

    val genres = listOf("Em Alta", "Eletrônica", "Lo-Fi", "Hip-Hop", "Rock", "Pop", "Chillout", "Acoustic")

    fun doSearch(query: String) {
        coroutineScope.launch {
            isLoading = true
            results = if (query.isBlank() || query == "Em Alta") {
                OlirumApplication.instance.musicRepository.getTrendingOnline()
            } else {
                OlirumApplication.instance.musicRepository.searchOnlineCatalog(query)
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        doSearch("Em Alta")
    }

    Scaffold(
        topBar = {
            OlirumTopBar(
                title = "Explorar Músicas",
                subtitle = "Catálogo Online Audius"
            )
        },
        containerColor = OlirumBackgroundLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Campo de Pesquisa
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Pesquisar músicas, artistas ou bandas...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = OlirumTextSecondary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            doSearch(selectedGenre)
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpar", tint = OlirumTextSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OlirumBlue,
                    unfocusedBorderColor = OlirumBorderLight,
                    focusedContainerColor = OlirumWhite,
                    unfocusedContainerColor = OlirumWhite
                )
            )

            // Botão Buscar se tiver texto digitado
            if (searchQuery.isNotBlank()) {
                Button(
                    onClick = { doSearch(searchQuery) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OlirumBlue)
                ) {
                    Text("Buscar \"$searchQuery\"", fontWeight = FontWeight.Bold)
                }
            }

            // Chips de Gênero
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                genres.forEach { genre ->
                    val isSelected = selectedGenre == genre
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedGenre = genre
                            searchQuery = ""
                            doSearch(genre)
                        },
                        label = { Text(genre, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OlirumCyan.copy(alpha = 0.2f),
                            selectedLabelColor = OlirumBlue
                        )
                    )
                }
            }

            // Lista de Resultados
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OlirumCyan)
                }
            } else if (results.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma música encontrada para sua busca.",
                        color = OlirumTextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    items(results, key = { it.id }) { track ->
                        val isThisPlaying = isPlaying && currentTrack?.id == track.id
                        TrackListItem(
                            track = track,
                            isPlaying = isThisPlaying,
                            onTrackClick = { viewModel.playTrack(track, results) },
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
