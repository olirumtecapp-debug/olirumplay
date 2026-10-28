package com.olirum.player.ui.viewmodel

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.olirum.player.data.local.PlaylistEntity
import com.olirum.player.data.repository.CoverCandidate
import com.olirum.player.data.repository.MusicRepository
import com.olirum.player.data.repository.UserPreferencesRepository
import com.olirum.player.model.LyricLine
import com.olirum.player.model.LyricsData
import com.olirum.player.model.Track
import com.olirum.player.service.OlirumPlaybackService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainPlayerViewModel(
    private val context: Context,
    val musicRepository: MusicRepository,
    private val prefsRepository: UserPreferencesRepository
) : ViewModel() {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _lyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val lyrics: StateFlow<List<LyricLine>> = _lyrics.asStateFlow()

    private val _lyricsData = MutableStateFlow<LyricsData>(LyricsData())
    val lyricsData: StateFlow<LyricsData> = _lyricsData.asStateFlow()

    private val _isLyricsLoading = MutableStateFlow(false)
    val isLyricsLoading: StateFlow<Boolean> = _isLyricsLoading.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    val isDarkMode: StateFlow<Boolean> = prefsRepository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val playlists: StateFlow<List<PlaylistEntity>> = musicRepository.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        initMediaController()
        startPositionProgressTracker()
    }

    private fun initMediaController() {
        val sessionToken = SessionToken(context, ComponentName(context, OlirumPlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                mediaController?.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        _isPlaying.value = playing
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        mediaItem?.let { item ->
                            val track = _queue.value.find { it.id == item.mediaId } ?: Track(
                                id = item.mediaId,
                                title = item.mediaMetadata.title?.toString() ?: "Música",
                                artist = item.mediaMetadata.artist?.toString() ?: "Artista",
                                album = item.mediaMetadata.albumTitle?.toString() ?: "Álbum",
                                durationMs = mediaController?.duration?.takeIf { it > 0 } ?: 180000L,
                                mediaUri = item.requestMetadata.mediaUri?.toString() ?: "",
                                artworkUri = item.mediaMetadata.artworkUri?.toString()
                            )
                            _currentTrack.value = track
                            if (track.source == "radio") {
                                _durationMs.value = 0L
                                _currentPositionMs.value = 0L
                            } else {
                                _durationMs.value = mediaController?.duration?.coerceAtLeast(0L) ?: track.durationMs
                                resolveCoverIfNeeded(track)
                                loadLyrics(track)
                            }
                        }
                    }

                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_READY) {
                            _durationMs.value = mediaController?.duration?.coerceAtLeast(0L) ?: 0L
                        }
                    }
                })

                _isPlaying.value = mediaController?.isPlaying ?: false
                _durationMs.value = mediaController?.duration?.coerceAtLeast(0L) ?: 0L
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    private fun startPositionProgressTracker() {
        viewModelScope.launch {
            while (true) {
                mediaController?.let { controller ->
                    if (controller.isPlaying) {
                        if (_currentTrack.value?.source == "radio") {
                            _currentPositionMs.value = 0L
                            _durationMs.value = 0L
                        } else {
                            _currentPositionMs.value = controller.currentPosition.coerceAtLeast(0L)
                            _durationMs.value = controller.duration.coerceAtLeast(0L)
                            _currentTrack.value?.let { trk ->
                                prefsRepository.savePlaybackState(trk.id, controller.currentPosition)
                            }
                        }
                    }
                }
                delay(400)
            }
        }
    }

    fun playRadioStation(station: com.olirum.player.model.RadioStation) {
        val track = station.toTrack()
        playTrack(track, listOf(track))
        viewModelScope.launch {
            com.olirum.player.OlirumApplication.instance.radioRepository.registerStationClick(station.id)
        }
    }

    fun playTrack(track: Track, newQueue: List<Track>? = null) {
        val controller = mediaController ?: return
        val currentQ = newQueue ?: _queue.value.ifEmpty { listOf(track) }
        _queue.value = currentQ
        _currentTrack.value = track

        val mediaItems = currentQ.map { item ->
            val meta = MediaMetadata.Builder()
                .setTitle(item.title)
                .setArtist(item.artist)
                .setAlbumTitle(item.album)
                .setArtworkUri(item.artworkUri?.let { Uri.parse(it) })
                .build()

            MediaItem.Builder()
                .setMediaId(item.id)
                .setUri(item.mediaUri)
                .setMediaMetadata(meta)
                .build()
        }

        val startIndex = currentQ.indexOfFirst { it.id == track.id }.coerceAtLeast(0)

        controller.setMediaItems(mediaItems, startIndex, 0L)
        controller.prepare()
        controller.play()
        resolveCoverIfNeeded(track)
        loadLyrics(track)
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    fun pause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        }
    }

    fun skipNext() {
        mediaController?.seekToNextMediaItem()
    }

    fun skipPrevious() {
        mediaController?.let {
            if (it.currentPosition > 3000) {
                it.seekTo(0)
            } else {
                it.seekToPreviousMediaItem()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        _currentPositionMs.value = positionMs
        mediaController?.seekTo(positionMs)
    }

    fun toggleShuffle() {
        val next = !_isShuffle.value
        _isShuffle.value = next
        mediaController?.shuffleModeEnabled = next
        viewModelScope.launch { prefsRepository.saveShuffle(next) }
    }

    fun toggleRepeat() {
        val next = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        _repeatMode.value = next
        mediaController?.repeatMode = next
        viewModelScope.launch { prefsRepository.saveRepeatMode(next) }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            prefsRepository.saveDarkMode(!isDarkMode.value)
        }
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            musicRepository.toggleFavorite(track)
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            musicRepository.createPlaylist(name)
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            musicRepository.deletePlaylist(playlistId)
        }
    }

    fun addTrackToPlaylist(playlistId: String, track: Track) {
        viewModelScope.launch {
            musicRepository.addTrackToPlaylist(playlistId, track)
        }
    }

    fun loadLyrics(track: Track) {
        viewModelScope.launch {
            _isLyricsLoading.value = true
            val data = musicRepository.getLyrics(track)
            _lyricsData.value = data
            _lyrics.value = data.syncedLines
            _isLyricsLoading.value = false
        }
    }

    fun resolveCoverIfNeeded(track: Track) {
        if (track.artworkUri.isNullOrBlank()) {
            viewModelScope.launch {
                val cover = musicRepository.resolveCover(track)
                if (!cover.isNullOrBlank()) {
                    if (_currentTrack.value?.id == track.id) {
                        _currentTrack.value = _currentTrack.value?.copy(artworkUri = cover)
                    }
                    _queue.value = _queue.value.map {
                        if (it.id == track.id) it.copy(artworkUri = cover) else it
                    }
                }
            }
        }
    }

    suspend fun searchCoverCandidates(query: String): List<CoverCandidate> {
        return musicRepository.searchCoverCandidates(query)
    }

    fun setManualCover(track: Track, coverUrl: String) {
        viewModelScope.launch {
            musicRepository.setManualCover(track, coverUrl)
            if (_currentTrack.value?.id == track.id) {
                _currentTrack.value = _currentTrack.value?.copy(artworkUri = coverUrl)
            }
            _queue.value = _queue.value.map {
                if (it.id == track.id) it.copy(artworkUri = coverUrl) else it
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }

    class Factory(
        private val context: Context,
        private val musicRepository: MusicRepository,
        private val prefsRepository: UserPreferencesRepository
    ) : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainPlayerViewModel::class.java)) {
                return MainPlayerViewModel(context, musicRepository, prefsRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
