package com.olirum.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.olirum.player.ui.components.OlirumTopBar
import com.olirum.player.ui.theme.*
import com.olirum.player.ui.viewmodel.MainPlayerViewModel

@Composable
fun ExploreScreen(
    viewModel: MainPlayerViewModel,
    onCategoryClick: (RadioCategory) -> Unit
) {
    val colors = LocalOlirumColors.current
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    val allCategories = remember { OlirumApplication.instance.radioRepository.categories }
    var searchQuery by remember { mutableStateOf("") }

    val filteredCategories = remember(allCategories, searchQuery) {
        if (searchQuery.isBlank()) allCategories
        else allCategories.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.subtitle.contains(searchQuery, ignoreCase = true) ||
            it.tags.any { tag -> tag.contains(searchQuery, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            OlirumTopBar(
                title = "Explorar Gêneros",
                subtitle = "Descubra rádios online por estilo musical",
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
            // Campo de Busca de Estilos
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = {
                    Text(
                        text = "Buscar gênero ou estilo musical...",
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

            // Grade de Cards dos Gêneros
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredCategories, key = { it.id }) { cat ->
                    CategoryGridCard(
                        category = cat,
                        onClick = { onCategoryClick(cat) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryGridCard(
    category: RadioCategory,
    onClick: () -> Unit
) {
    val gradientBrush = remember(category.gradientColors) {
        val c1 = Color(category.gradientColors.getOrNull(0) ?: 0xFF00E5D4)
        val c2 = Color(category.gradientColors.getOrNull(1) ?: 0xFF008BFF)
        Brush.linearGradient(listOf(c1, c2))
    }

    val iconVector: ImageVector = when (category.iconName) {
        "Church" -> Icons.Rounded.Favorite
        "Piano" -> Icons.Rounded.GraphicEq
        "Saxophone" -> Icons.Rounded.MusicNote
        "Guitar" -> Icons.Rounded.Radio
        "Heart" -> Icons.Rounded.Favorite
        "Spa" -> Icons.Rounded.Spa
        "Star" -> Icons.Rounded.Star
        "Trophy" -> Icons.Rounded.EmojiEvents
        "Album" -> Icons.Rounded.Album
        "Equalizer" -> Icons.Rounded.Equalizer
        else -> Icons.Rounded.Radio
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 3.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(14.dp)
        ) {
            // Ícone de fundo sutil
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.22f),
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 10.dp, y = 10.dp)
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = category.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = category.subtitle,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
