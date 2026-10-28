package com.olirum.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olirum.player.OlirumApplication
import com.olirum.player.model.RadioCategory
import com.olirum.player.model.RadioStation
import com.olirum.player.ui.components.OlirumTopBar
import com.olirum.player.ui.components.RadioStationCard
import com.olirum.player.ui.theme.*
import com.olirum.player.ui.viewmodel.MainPlayerViewModel
import kotlinx.coroutines.launch

@Composable
fun CategoryStationsScreen(
    category: RadioCategory,
    viewModel: MainPlayerViewModel,
    onBackClick: () -> Unit
) {
    val colors = LocalOlirumColors.current
    val coroutineScope = rememberCoroutineScope()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    var stations by remember { mutableStateOf<List<RadioStation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todas") }

    fun loadStations() {
        coroutineScope.launch {
            isLoading = true
            stations = OlirumApplication.instance.radioRepository.getStationsForCategory(category)
            isLoading = false
        }
    }

    LaunchedEffect(category.id) {
        loadStations()
    }

    val filteredStations = remember(stations, searchQuery, selectedFilter) {
        var list = stations
        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.location.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
            }
        }
        when (selectedFilter) {
            "Brasil" -> list.filter { it.countryCode.equals("BR", ignoreCase = true) || it.location.contains("Brasil", ignoreCase = true) }
            "Mais Votadas" -> list.sortedByDescending { it.votes }
            else -> list
        }
    }

    Scaffold(
        topBar = {
            OlirumTopBar(
                title = category.name,
                subtitle = "${stations.size} estações encontradas via Radio Browser",
                onToggleTheme = { viewModel.toggleDarkMode() },
                isDarkTheme = isDarkMode,
                actions = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Voltar",
                            tint = colors.textPrimary
                        )
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
            // Banner do Gênero com degradê estilizado
            val gradientBrush = remember(category.gradientColors) {
                val c1 = Color(category.gradientColors.getOrNull(0) ?: 0xFF00E5D4)
                val c2 = Color(category.gradientColors.getOrNull(1) ?: 0xFF008BFF)
                Brush.linearGradient(listOf(c1, c2))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(gradientBrush)
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Radio,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GÊNERO SELECIONADO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = category.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = category.subtitle,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Barra de Busca
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = {
                    Text(
                        text = "Filtrar estações por nome ou cidade...",
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

            // Filtros rápidos
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Todas", "Brasil", "Mais Votadas").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = filter,
                                fontSize = 12.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OlirumCyan,
                            selectedLabelColor = Color(0xFF070B14),
                            containerColor = colors.surface,
                            labelColor = colors.textSecondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) OlirumCyan else colors.border
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Lista de Rádios
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = OlirumCyan)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Consultando diretório Radio Browser...",
                            color = colors.textSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else if (filteredStations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.Radio,
                            contentDescription = null,
                            tint = colors.textSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Nenhuma estação encontrada",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tente alterar o filtro de busca ou verificar sua conexão de internet.",
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredStations, key = { it.id }) { station ->
                        val isThisPlaying = isPlaying && currentTrack?.id == station.id
                        RadioStationCard(
                            station = station,
                            isPlaying = isThisPlaying,
                            onPlayClick = {
                                viewModel.playRadioStation(station)
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}
