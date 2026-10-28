package com.olirum.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Search
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
import com.olirum.player.model.RadioCategory
import com.olirum.player.model.RadioStation
import com.olirum.player.ui.components.OlirumTopBar
import com.olirum.player.ui.components.RadioStationCard
import com.olirum.player.ui.theme.*
import com.olirum.player.ui.viewmodel.MainPlayerViewModel
import kotlinx.coroutines.launch

@Composable
fun RadiosScreen(
    viewModel: MainPlayerViewModel,
    onCategoryClick: (RadioCategory) -> Unit
) {
    val colors = LocalOlirumColors.current
    val coroutineScope = rememberCoroutineScope()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    val categories = remember { OlirumApplication.instance.radioRepository.categories }

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<RadioStation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf<RadioCategory?>(null) }
    var selectedCountryFilter by remember { mutableStateOf("Todos") }

    fun doSearch(query: String, cat: RadioCategory?, country: String) {
        coroutineScope.launch {
            isLoading = true
            val countryCode = if (country == "Brasil") "BR" else null
            searchResults = if (cat != null && query.isBlank()) {
                OlirumApplication.instance.radioRepository.getStationsForCategory(cat)
            } else {
                OlirumApplication.instance.radioRepository.searchStations(
                    query = query,
                    categoryName = cat?.name,
                    countryCode = countryCode
                )
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        doSearch("", null, "Todos")
    }

    Scaffold(
        topBar = {
            OlirumTopBar(
                title = "Rádios ao Vivo",
                subtitle = "Transmissões Gratuitas via Radio Browser",
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
            // Barra de Busca
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    if (it.length >= 3 || it.isEmpty()) {
                        doSearch(it, selectedCategoryFilter, selectedCountryFilter)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = {
                    Text(
                        text = "Buscar rádio por nome ou estilo...",
                        fontSize = 13.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
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
                        IconButton(onClick = {
                            searchQuery = ""
                            doSearch("", selectedCategoryFilter, selectedCountryFilter)
                        }) {
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

            // Filtros Rápidos de Categorias em Carrossel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Chip "Todas"
                val isAllSelected = selectedCategoryFilter == null
                FilterChip(
                    selected = isAllSelected,
                    onClick = {
                        selectedCategoryFilter = null
                        doSearch(searchQuery, null, selectedCountryFilter)
                    },
                    label = { Text("Todas as Rádios", fontSize = 12.sp, maxLines = 1, softWrap = false) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OlirumCyan,
                        selectedLabelColor = Color(0xFF070B14),
                        containerColor = colors.surface,
                        labelColor = colors.textSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isAllSelected) OlirumCyan else colors.border
                    )
                )

                // Chips das Categorias Prioritárias
                categories.forEach { cat ->
                    val isSelected = selectedCategoryFilter?.id == cat.id
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategoryFilter = if (isSelected) null else cat
                            doSearch(searchQuery, selectedCategoryFilter, selectedCountryFilter)
                        },
                        label = { Text(cat.name, fontSize = 12.sp, maxLines = 1, softWrap = false) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OlirumCyan,
                            selectedLabelColor = Color(0xFF070B14),
                            containerColor = colors.surface,
                            labelColor = colors.textSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) OlirumCyan else colors.border
                        )
                    )
                }
            }

            // Filtro País
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Todos os Países", "Brasil").forEach { country ->
                    val isSelected = (country == "Todos os Países" && selectedCountryFilter == "Todos") || (country == "Brasil" && selectedCountryFilter == "Brasil")
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCountryFilter = if (country == "Brasil") "Brasil" else "Todos"
                            doSearch(searchQuery, selectedCategoryFilter, selectedCountryFilter)
                        },
                        label = { Text(country, fontSize = 11.sp, maxLines = 1, softWrap = false) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OlirumBlue,
                            selectedLabelColor = Color.White,
                            containerColor = colors.surface,
                            labelColor = colors.textSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) OlirumBlue else colors.border
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Lista de Resultados
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
                            text = "Carregando rádios ao vivo...",
                            color = colors.textSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else if (searchResults.isEmpty()) {
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
                            text = "Nenhuma rádio encontrada",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tente buscar por outro termo ou selecionar outra categoria.",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults, key = { it.id }) { station ->
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
