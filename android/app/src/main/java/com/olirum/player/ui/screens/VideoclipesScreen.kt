package com.olirum.player.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.olirum.player.data.remote.youtube.VideoCategory
import com.olirum.player.data.remote.youtube.VideoClip
import com.olirum.player.ui.components.OlirumTopBar
import com.olirum.player.ui.theme.*
import com.olirum.player.ui.viewmodel.MainPlayerViewModel
import com.olirum.player.ui.viewmodel.VideosUiState
import com.olirum.player.ui.viewmodel.VideosViewModel

@Composable
fun VideoclipesScreen(
    mainPlayerViewModel: MainPlayerViewModel,
    modifier: Modifier = Modifier,
    videosViewModel: VideosViewModel = viewModel()
) {
    val colors = LocalOlirumColors.current
    val focusManager = LocalFocusManager.current

    val uiState by videosViewModel.uiState.collectAsState()
    val searchQuery by videosViewModel.searchQuery.collectAsState()
    val selectedCategory by videosViewModel.selectedCategory.collectAsState()
    val activeVideo by videosViewModel.activeVideo.collectAsState()

    var showApiKeyDialog by remember { mutableStateOf(false) }
    var inputKey by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Barra Superior Padrão OlirumPlay
            OlirumTopBar(
                title = "Videoclipes",
                subtitle = "YouTube Oficial",
                onSearchClick = {},
                actions = {
                    IconButton(onClick = { showApiKeyDialog = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Key,
                            contentDescription = "Configurar Chave da API",
                            tint = OlirumCyan
                        )
                    }
                }
            )

            // Campo de Pesquisa com Borda Neon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { videosViewModel.onSearchQueryChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Buscar clipe, artista, banda ou álbum...",
                            color = colors.textSecondary,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Buscar",
                            tint = OlirumCyan
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { videosViewModel.onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Limpar busca",
                                    tint = colors.textSecondary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedBorderColor = OlirumCyan,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            if (searchQuery.isNotBlank()) {
                                videosViewModel.performSearch(searchQuery)
                            }
                        }
                    )
                )
            }

            // Carrossel das 19 Categorias de Descoberta Musical
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                videosViewModel.categories.forEach { category ->
                    val isSelected = selectedCategory?.id == category.id && searchQuery.isEmpty()
                    CategoryChip(
                        category = category,
                        isSelected = isSelected,
                        onClick = {
                            focusManager.clearFocus()
                            videosViewModel.selectCategory(category)
                        }
                    )
                }
            }

            // Conteúdo Principal da Tela
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                when (val state = uiState) {
                    is VideosUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = OlirumCyan,
                                    modifier = Modifier.size(42.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Buscando videoclipes oficiais...",
                                    color = colors.textSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    is VideosUiState.MissingApiKey -> {
                        ApiKeySetupCard(
                            onConfigureClick = { showApiKeyDialog = true }
                        )
                    }

                    is VideosUiState.QuotaExceeded -> {
                        ErrorStateCard(
                            title = "Quota Diária da API Esgotada",
                            message = "O limite diário de requisições gratuitas (10.000 unidades) do Google Cloud foi atingido. Você pode inserir outra chave de API ou aguardar o reset diário.",
                            actionLabel = "Alterar Chave de API",
                            onActionClick = { showApiKeyDialog = true }
                        )
                    }

                    is VideosUiState.InvalidApiKey -> {
                        ErrorStateCard(
                            title = "Chave da API Inválida",
                            message = "A chave do YouTube fornecida não é válida ou não possui permissão para a YouTube Data API v3 no Google Cloud Console.",
                            actionLabel = "Verificar / Alterar Chave",
                            onActionClick = { showApiKeyDialog = true }
                        )
                    }

                    is VideosUiState.Empty -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SearchOff,
                                    contentDescription = "Nada encontrado",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Nenhum clipe oficial encontrado",
                                    color = colors.textPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Não encontramos vídeos incorporáveis para \"${state.query}\". Tente buscar por outros termos ou selecione uma categoria acima.",
                                    color = colors.textSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    is VideosUiState.Error -> {
                        ErrorStateCard(
                            title = "Falha ao Carregar Vídeos",
                            message = state.message,
                            actionLabel = "Tentar Novamente",
                            onActionClick = { videosViewModel.retry() }
                        )
                    }

                    is VideosUiState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.SmartDisplay,
                                        contentDescription = null,
                                        tint = OlirumCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = state.categoryName ?: "Resultados da Pesquisa",
                                        color = colors.textPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = "${state.videos.size} clipes",
                                        color = colors.textSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            items(state.videos, key = { it.id }) { video ->
                                VideoClipCard(
                                    video = video,
                                    onClick = {
                                        videosViewModel.playVideo(video)
                                    }
                                )
                            }
                        }
                    }

                    is VideosUiState.Idle -> {
                        Box(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }

        // Diálogo para Configurar a Chave da YouTube Data API v3
        if (showApiKeyDialog) {
            AlertDialog(
                onDismissRequest = { showApiKeyDialog = false },
                containerColor = colors.surface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Key,
                            contentDescription = null,
                            tint = OlirumCyan
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Chave da YouTube API v3",
                            color = colors.textPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Cole sua chave gratuita da YouTube Data API v3 gerada no Google Cloud Console:",
                            color = colors.textSecondary,
                            fontSize = 13.sp,
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = inputKey,
                            onValueChange = { inputKey = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "AIzaSy...",
                                    color = colors.textSecondary,
                                    fontSize = 13.sp
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OlirumCyan,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "A chave fica salva localmente no seu aparelho de forma privada e segura.",
                            color = OlirumCyan.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (inputKey.isNotBlank()) {
                                videosViewModel.saveApiKey(inputKey)
                            }
                            showApiKeyDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OlirumCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Salvar Chave", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showApiKeyDialog = false }) {
                        Text("Cancelar", color = colors.textSecondary)
                    }
                }
            )
        }

        // Tela de Reprodução Oficial (Player IFrame com Controles e Anúncios YouTube Oficiais)
        activeVideo?.let { currentVideo ->
            VideoPlayerScreen(
                video = currentVideo,
                mainPlayerViewModel = mainPlayerViewModel,
                onDismiss = { videosViewModel.closePlayer() }
            )
        }
    }
}

@Composable
private fun CategoryChip(
    category: VideoCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalOlirumColors.current
    val borderModifier = if (isSelected) {
        Modifier.border(1.5.dp, OlirumCyan, RoundedCornerShape(20.dp))
    } else {
        Modifier.border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
    }

    val backgroundBrush = if (isSelected) {
        Brush.horizontalGradient(
            colors = listOf(
                Color(category.gradientColors[0]).copy(alpha = 0.35f),
                Color(category.gradientColors[1]).copy(alpha = 0.35f)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(colors.surface, colors.surface)
        )
    }

    Box(
        modifier = Modifier
            .then(borderModifier)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundBrush)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = category.name,
                color = if (isSelected) OlirumCyan else colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun VideoClipCard(
    video: VideoClip,
    onClick: () -> Unit
) {
    val colors = LocalOlirumColors.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Miniatura 16:9 Oficial
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = video.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Duração Oficial no canto inferior direito
                if (video.durationFormatted != "--:--") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.8f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = video.durationFormatted,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Ícone de Play Neon no centro
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .border(1.dp, OlirumCyan.copy(alpha = 0.7f), RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Reproduzir",
                        tint = OlirumCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Metadados Oficiais
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = video.title,
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = video.channelTitle,
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Canal Oficial",
                        tint = OlirumCyan.copy(alpha = 0.8f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ApiKeySetupCard(
    onConfigureClick: () -> Unit
) {
    val colors = LocalOlirumColors.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, OlirumCyan.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(OlirumCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VpnKey,
                        contentDescription = null,
                        tint = OlirumCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Ativação da Seção Videoclipes",
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "YouTube Data API v3 Oficial",
                        color = OlirumCyan,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Para pesquisar e listar videoclipes diretamente dos servidores oficiais do YouTube com dados reais (sem scraping ou anúncios invasivos), configure uma chave gratuita da API no Google Cloud Console.",
                color = colors.textSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.background.copy(alpha = 0.6f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Como obter sua chave em 3 passos:",
                    color = colors.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "1. Acesse console.cloud.google.com (gratuito)",
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = "2. Ative a \"YouTube Data API v3\" na biblioteca",
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = "3. Vá em Credenciais, crie uma Chave de API e cole aqui",
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onConfigureClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OlirumCyan),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Key,
                    contentDescription = null,
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Inserir Chave da API",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ErrorStateCard(
    title: String,
    message: String,
    actionLabel: String,
    onActionClick: () -> Unit
) {
    val colors = LocalOlirumColors.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(46.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = title,
                    color = colors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = message,
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(containerColor = OlirumCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = actionLabel, color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
