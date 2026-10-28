package com.olirum.player.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olirum.player.OlirumApplication
import com.olirum.player.data.remote.youtube.VideoCategory
import com.olirum.player.data.remote.youtube.VideoClip
import com.olirum.player.data.remote.youtube.YouTubeError
import com.olirum.player.data.remote.youtube.YouTubeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class VideosUiState {
    object Idle : VideosUiState()
    object Loading : VideosUiState()
    object MissingApiKey : VideosUiState()
    object QuotaExceeded : VideosUiState()
    object InvalidApiKey : VideosUiState()
    data class Success(val videos: List<VideoClip>, val query: String, val categoryName: String? = null) : VideosUiState()
    data class Empty(val query: String) : VideosUiState()
    data class Error(val message: String) : VideosUiState()
}

class VideosViewModel(
    private val repository: YouTubeRepository = OlirumApplication.instance.youtubeRepository
) : ViewModel() {

    val categories: List<VideoCategory> = repository.categories

    private val _uiState = MutableStateFlow<VideosUiState>(VideosUiState.Idle)
    val uiState: StateFlow<VideosUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<VideoCategory?>(null)
    val selectedCategory: StateFlow<VideoCategory?> = _selectedCategory.asStateFlow()

    private val _apiKey = MutableStateFlow<String?>(null)
    val apiKey: StateFlow<String?> = _apiKey.asStateFlow()

    // Vídeo em reprodução no player oficial (IFrame)
    private val _activeVideo = MutableStateFlow<VideoClip?>(null)
    val activeVideo: StateFlow<VideoClip?> = _activeVideo.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadApiKeyAndInitialCategory()
    }

    fun loadApiKeyAndInitialCategory() {
        viewModelScope.launch {
            val key = repository.getApiKey()
            _apiKey.value = key
            if (key.isNullOrBlank()) {
                _uiState.value = VideosUiState.MissingApiKey
            } else {
                // Seleciona a primeira categoria por padrão (ex: Pop Hits ou Classic Rock)
                val defaultCat = categories.firstOrNull { it.id == "pop" } ?: categories.first()
                selectCategory(defaultCat)
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        searchJob?.cancel()
        if (newQuery.isBlank()) {
            val cat = _selectedCategory.value
            if (cat != null) {
                selectCategory(cat)
            }
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // Debounce para digitação
            performSearch(newQuery, categoryName = null)
        }
    }

    fun selectCategory(category: VideoCategory) {
        _selectedCategory.value = category
        _searchQuery.value = ""
        performSearch(category.searchQuery, categoryName = category.name)
    }

    fun performSearch(query: String, categoryName: String? = null) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val currentKey = _apiKey.value
            if (currentKey.isNullOrBlank()) {
                _uiState.value = VideosUiState.MissingApiKey
                return@launch
            }

            _uiState.value = VideosUiState.Loading

            val result = repository.searchVideos(query, currentKey)
            result.fold(
                onSuccess = { list ->
                    if (list.isEmpty()) {
                        _uiState.value = VideosUiState.Empty(query)
                    } else {
                        _uiState.value = VideosUiState.Success(list, query, categoryName)
                    }
                },
                onFailure = { error ->
                    when (error) {
                        is YouTubeError.MissingApiKey -> _uiState.value = VideosUiState.MissingApiKey
                        is YouTubeError.QuotaExceeded -> _uiState.value = VideosUiState.QuotaExceeded
                        is YouTubeError.InvalidApiKey -> _uiState.value = VideosUiState.InvalidApiKey
                        else -> _uiState.value = VideosUiState.Error(error.localizedMessage ?: "Erro desconhecido")
                    }
                }
            )
        }
    }

    fun saveApiKey(newKey: String) {
        viewModelScope.launch {
            repository.saveApiKey(newKey)
            _apiKey.value = newKey.trim()
            if (newKey.isNotBlank()) {
                val cat = _selectedCategory.value ?: categories.first()
                selectCategory(cat)
            } else {
                _uiState.value = VideosUiState.MissingApiKey
            }
        }
    }

    fun playVideo(video: VideoClip) {
        _activeVideo.value = video
    }

    fun closePlayer() {
        _activeVideo.value = null
    }

    fun retry() {
        val cat = _selectedCategory.value
        val query = _searchQuery.value
        when {
            query.isNotBlank() -> performSearch(query, null)
            cat != null -> selectCategory(cat)
            else -> loadApiKeyAndInitialCategory()
        }
    }
}
