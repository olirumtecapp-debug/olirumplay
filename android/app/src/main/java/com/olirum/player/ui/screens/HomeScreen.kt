package com.olirum.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

@Composable
fun HomeScreen(
    viewModel: MainPlayerViewModel,
    onNavigateToDevice: () -> Unit,
    onNavigateToExplore: () -> Unit,
    onNavigateToRadios: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToWidgets: () -> Unit,
    onCategoryClick: (RadioCategory) -> Unit
) {
    val colors = LocalOlirumColors.current
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    val categories = remember { OlirumApplication.instance.radioRepository.categories }
    val curatedStations = remember { OlirumApplication.instance.radioRepository.curatedStations }

    // Destaques selecionados com garantia absoluta de Católica, Clássica, Jazz & Blues e Rock
    val featuredStations = remember(curatedStations) {
        curatedStations.filter {
            it.id in listOf(
                "curated_evangelizar_pr",
                "curated_swiss_classic",
                "curated_swiss_jazz",
                "curated_89_rock",
                "curated_aparecida_fm",
                "curated_1fm_blues",
                "curated_mec_fm_classica"
            )
        }
    }

    Scaffold(
        topBar = {
            OlirumTopBar(
                title = "OlirumPlay",
                subtitle = "Rádios ao Vivo & Músicas Locais",
                onToggleTheme = { viewModel.toggleDarkMode() },
                isDarkTheme = isDarkMode,
                actions = {
                    IconButton(onClick = onNavigateToWidgets) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = "Equalizador",
                            tint = colors.textPrimary
                        )
                    }
                }
            )
        },
        containerColor = colors.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Saudação
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Olá, bem-vindo ao OlirumPlay ✨",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Rádios gratuitas e seus arquivos locais sem limites",
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Ações Rápidas (3 Cards modernos)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeQuickActionCard(
                        title = "Músicas do Celular",
                        subtitle = "Arquivos & Pastas",
                        icon = Icons.Rounded.FolderOpen,
                        gradient = Brush.linearGradient(listOf(OlirumMint, OlirumCyan)),
                        onClick = onNavigateToDevice,
                        modifier = Modifier.weight(1f)
                    )
                    HomeQuickActionCard(
                        title = "Rádios ao Vivo",
                        subtitle = "Radio Browser",
                        icon = Icons.Rounded.Radio,
                        gradient = Brush.linearGradient(listOf(OlirumBlue, OlirumPurple)),
                        onClick = onNavigateToRadios,
                        modifier = Modifier.weight(1f)
                    )
                    HomeQuickActionCard(
                        title = "Explorar Estilos",
                        subtitle = "Todos os Gêneros",
                        icon = Icons.Rounded.Explore,
                        gradient = Brush.linearGradient(listOf(OlirumPurple, OlirumMagenta)),
                        onClick = onNavigateToExplore,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Banner Destaque: Áudio Indestrutível
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(OlirumGradientBrush)
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ÁUDIO INDESTRUTÍVEL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Reprodução Contínua em 2º Plano",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tela apagada, redes sociais ou outros apps: o áudio não para.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Seção: Navegar por Estilo (Carrossel Horizontal de Categorias Prioritárias)
            item {
                Spacer(modifier = Modifier.height(22.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎧 Estilos em Destaque",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                    TextButton(onClick = onNavigateToExplore) {
                        Text(
                            text = "Ver Todos",
                            color = OlirumCyan,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    categories.take(10).forEach { cat ->
                        CategoryPillCard(
                            category = cat,
                            onClick = { onCategoryClick(cat) }
                        )
                    }
                }
            }

            // Seção: Rádios em Alta Recomendadas (Católica, Clássica, Jazz & Blues, Classic Rock)
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📻 Rádios Recomendadas",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                    TextButton(onClick = onNavigateToRadios) {
                        Text(
                            text = "Mais Rádios",
                            color = OlirumCyan,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            // Lista de Estações em Destaque
            items(featuredStations, key = { it.id }) { station ->
                val isThisPlaying = isPlaying && currentTrack?.id == station.id
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    RadioStationCard(
                        station = station,
                        isPlaying = isThisPlaying,
                        onPlayClick = {
                            viewModel.playRadioStation(station)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryPillCard(
    category: RadioCategory,
    onClick: () -> Unit
) {
    val gradientBrush = remember(category.gradientColors) {
        val c1 = Color(category.gradientColors.getOrNull(0) ?: 0xFF00E5D4)
        val c2 = Color(category.gradientColors.getOrNull(1) ?: 0xFF008BFF)
        Brush.linearGradient(listOf(c1, c2))
    }

    Surface(
        modifier = Modifier
            .width(135.dp)
            .height(74.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(10.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column {
                Text(
                    text = category.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = category.subtitle,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun HomeQuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalOlirumColors.current

    Surface(
        modifier = modifier
            .height(105.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = colors.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(gradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
