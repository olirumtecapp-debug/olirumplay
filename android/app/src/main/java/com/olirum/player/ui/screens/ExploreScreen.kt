package com.olirum.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
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
import com.olirum.player.model.RadioStation
import com.olirum.player.model.Track
import com.olirum.player.ui.components.OlirumTopBar
import com.olirum.player.ui.components.RadioStationCard
import com.olirum.player.ui.components.TrackListItem
import com.olirum.player.ui.theme.*
import com.olirum.player.ui.viewmodel.MainPlayerViewModel
import kotlinx.coroutines.launch

@Composable
fun ExploreScreen(
    viewModel: MainPlayerViewModel
) {
    val colors = LocalOlirumColors.current
    val coroutineScope = rememberCoroutineScope()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    // 0 = Músicas Online, 1 = Rádios ao Vivo
    var selectedSection by remember { mutableIntStateOf(0) }

    // Estado do Catálogo de Músicas
    var musicSearchQuery by remember { mutableStateOf("") }
    var musicResults by remember { mutableStateOf<List<Track>>(emptyList()) }
    var isMusicLoading by remember { mutableStateOf(false) }
    var selectedMusicGenre by remember { mutableStateOf("Em Alta") }
    val musicGenres = listOf("Em Alta", "Electronic", "Rock", "Pop", "Hip-Hop", "Alternative", "Ambient", "Acoustic")

    // Estado das Rádios Online
    var radioSearchQuery by remember { mutableStateOf("") }
    var radioResults by remember { mutableStateOf<List<RadioStation>>(emptyList()) }
    var isRadioLoading by remember { mutableStateOf(false) }
    var selectedRadioGenre by remember { mutableStateOf("Todas") }
    val radioGenres = remember { OlirumApplication.instance.radioRepository.getAvailableGenres() }

    fun doMusicSearch(query: String, genre: String) {
        coroutineScope.launch {
            isMusicLoading = true
            musicResults = if (query.isNotBlank()) {
                OlirumApplication.instance.musicRepository.searchOnlineCatalog(query)
            } else if (genre == "Em Alta") {
                OlirumApplication.instance.musicRepository.getTrendingOnline()
            } else {
                OlirumApplication.instance.musicRepository.getTrendingOnline(genre = genre)
            }
            isMusicLoading = false
        }
    }

    fun doRadioSearch(query: String, genre: String) {
        coroutineScope.launch {
            isRadioLoading = true
            radioResults = OlirumApplication.instance.radioRepository.searchStations(query, genre)
            isRadioLoading = false
        }
    }

    LaunchedEffect(Unit) {
        doMusicSearch("", "Em Alta")
        doRadioSearch("", "Todas")
    }

    Scaffold(
        topBar = {
            OlirumTopBar(
                title = if (selectedSection == 0) "Explorar Músicas" else "Rádios ao Vivo",
                subtitle = if (selectedSection == 0) "Catálogo Musical & Descobertas" else "Transmissões Gratuitas em Alta Fidelidade",
                onToggleTheme = { viewModel.toggleDarkMode() },
                isDarkTheme = isDarkMode
            )
        },
        containerColor = colors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Seletor Superior em Pílula: Músicas vs Rádios
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.card)
                    .padding(4.dp)
            ) {
                // Tab Músicas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedSection == 0) OlirumBlue else Color.Transparent)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        onClick = { selectedSection = 0 },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = if (selectedSection == 0) Color.White else colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Músicas Online",
                                fontWeight = if (selectedSection == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedSection == 0) Color.White else colors.textSecondary,
                                fontSize = 13.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // Tab Rádios
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedSection == 1) OlirumCyan else Color.Transparent)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        onClick = { selectedSection = 1 },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Radio,
                                contentDescription = null,
                                tint = if (selectedSection == 1) Color(0xFF0F172A) else colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Rádios ao Vivo",
                                fontWeight = if (selectedSection == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedSection == 1) Color(0xFF0F172A) else colors.textSecondary,
                                fontSize = 13.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // === SEÇÃO 0: CATÁLOGO DE MÚSICAS ===
            if (selectedSection == 0) {
                // Campo de Pesquisa de Músicas
                OutlinedTextField(
                    value = musicSearchQuery,
                    onValueChange = { musicSearchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    placeholder = {
                        Text(
                            text = "Buscar música ou artista...",
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = colors.textSecondary
                        )
                    },
                    trailingIcon = {
                        if (musicSearchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                musicSearchQuery = ""
                                doMusicSearch("", selectedMusicGenre)
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpar", tint = colors.textSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OlirumBlue,
                        unfocusedBorderColor = colors.border,
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                if (musicSearchQuery.isNotBlank()) {
                    Button(
                        onClick = { doMusicSearch(musicSearchQuery, selectedMusicGenre) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OlirumBlue)
                    ) {
                        Text(
                            text = "Buscar \"$musicSearchQuery\"",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Chips de Gênero Musical
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    musicGenres.forEach { genre ->
                        val isSelected = selectedMusicGenre == genre
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedMusicGenre = genre
                                musicSearchQuery = ""
                                doMusicSearch("", genre)
                            },
                            label = {
                                Text(
                                    text = genre,
                                    maxLines = 1,
                                    softWrap = false,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OlirumCyan.copy(alpha = 0.2f),
                                selectedLabelColor = OlirumBlue,
                                containerColor = colors.surface
                            )
                        )
                    }
                }

                if (isMusicLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = OlirumCyan)
                    }
                } else if (musicResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhuma música encontrada para sua busca.",
                            color = colors.textSecondary,
                            fontSize = 14.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        items(musicResults, key = { it.id }) { track ->
                            val isThisPlaying = isPlaying && currentTrack?.id == track.id
                            TrackListItem(
                                track = track,
                                isPlaying = isThisPlaying,
                                onTrackClick = { viewModel.playTrack(track, musicResults) },
                                onFavoriteClick = { viewModel.toggleFavorite(track) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(90.dp))
                        }
                    }
                }
            } else {
                // === SEÇÃO 1: RÁDIOS ONLINE AO VIVO ===
                // Campo de Pesquisa de Rádios
                OutlinedTextField(
                    value = radioSearchQuery,
                    onValueChange = {
                        radioSearchQuery = it
                        doRadioSearch(it, selectedRadioGenre)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    placeholder = {
                        Text(
                            text = "Buscar rádio pelo nome, cidade ou estilo...",
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = colors.textSecondary
                        )
                    },
                    trailingIcon = {
                        if (radioSearchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                radioSearchQuery = ""
                                doRadioSearch("", selectedRadioGenre)
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpar", tint = colors.textSecondary)
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

                // Chips de Gênero de Rádio (Católica, Rock Clássico, Música Clássica, Sertanejo, MPB, etc.)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    radioGenres.forEach { genre ->
                        val isSelected = selectedRadioGenre == genre
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedRadioGenre = genre
                                doRadioSearch(radioSearchQuery, genre)
                            },
                            label = {
                                Text(
                                    text = genre,
                                    maxLines = 1,
                                    softWrap = false,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OlirumCyan.copy(alpha = 0.25f),
                                selectedLabelColor = OlirumBlue,
                                containerColor = colors.surface
                            )
                        )
                    }
                }

                if (isRadioLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = OlirumCyan)
                    }
                } else if (radioResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhuma rádio encontrada para este filtro.",
                            color = colors.textSecondary,
                            fontSize = 14.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(radioResults, key = { it.id }) { station ->
                            val isThisPlaying = isPlaying && currentTrack?.id == station.id
                            RadioStationCard(
                                station = station,
                                isPlaying = isThisPlaying,
                                onPlayClick = {
                                    val track = station.toTrack()
                                    viewModel.playTrack(track, listOf(track))
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
