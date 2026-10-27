package com.olirum.player.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.olirum.player.data.local.AppDatabase
import com.olirum.player.data.local.MediaStoreScanner
import com.olirum.player.data.local.PlaylistEntity
import com.olirum.player.data.local.TrackEntity
import com.olirum.player.data.remote.ApiClient
import com.olirum.player.model.LyricLine
import com.olirum.player.model.MusicFolder
import com.olirum.player.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.regex.Pattern

class MusicRepository(
    private val context: Context,
    private val database: AppDatabase,
    private val scanner: MediaStoreScanner
) {
    private val gson = Gson()

    val favoriteTracks: Flow<List<Track>> = database.trackDao().getFavoriteTracks()
        .map { list -> list.map { it.toTrack() } }

    val offlineTracks: Flow<List<Track>> = database.trackDao().getOfflineTracks()
        .map { list -> list.map { it.toTrack() } }

    val playlists: Flow<List<PlaylistEntity>> = database.trackDao().getAllPlaylists()

    suspend fun scanLocalTracks(): List<Track> = scanner.scanLocalTracks()

    fun groupTracksByFolder(tracks: List<Track>): List<MusicFolder> = scanner.groupTracksByFolder(tracks)

    suspend fun toggleFavorite(track: Track) {
        val newFav = !track.isFavorite
        database.trackDao().insertTrack(TrackEntity.fromTrack(track.copy(isFavorite = newFav)))
    }

    suspend fun createPlaylist(name: String): PlaylistEntity = withContext(Dispatchers.IO) {
        val playlist = PlaylistEntity(
            id = UUID.randomUUID().toString(),
            name = name.trim().ifEmpty { "Minha Playlist" },
            trackIdsJson = "[]",
            createdAt = System.currentTimeMillis()
        )
        database.trackDao().insertPlaylist(playlist)
        playlist
    }

    suspend fun deletePlaylist(playlistId: String) = withContext(Dispatchers.IO) {
        database.trackDao().deletePlaylist(playlistId)
    }

    suspend fun addTrackToPlaylist(playlistId: String, track: Track) = withContext(Dispatchers.IO) {
        // Save track to Room first so it can be resolved later
        database.trackDao().insertTrack(TrackEntity.fromTrack(track))
        val playlist = database.trackDao().getPlaylistById(playlistId) ?: return@withContext
        val listType = object : TypeToken<MutableList<String>>() {}.type
        val currentIds: MutableList<String> = try {
            gson.fromJson(playlist.trackIdsJson, listType) ?: mutableListOf()
        } catch (_: Exception) {
            mutableListOf()
        }
        if (!currentIds.contains(track.id)) {
            currentIds.add(track.id)
            val updated = playlist.copy(trackIdsJson = gson.toJson(currentIds))
            database.trackDao().insertPlaylist(updated)
        }
    }

    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String) = withContext(Dispatchers.IO) {
        val playlist = database.trackDao().getPlaylistById(playlistId) ?: return@withContext
        val listType = object : TypeToken<MutableList<String>>() {}.type
        val currentIds: MutableList<String> = try {
            gson.fromJson(playlist.trackIdsJson, listType) ?: mutableListOf()
        } catch (_: Exception) {
            mutableListOf()
        }
        if (currentIds.remove(trackId)) {
            val updated = playlist.copy(trackIdsJson = gson.toJson(currentIds))
            database.trackDao().insertPlaylist(updated)
        }
    }

    fun getTrackIdsFromPlaylist(playlist: PlaylistEntity): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return try {
            gson.fromJson(playlist.trackIdsJson, listType) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun searchOnlineCatalog(query: String, genre: String? = null): List<Track> = withContext(Dispatchers.IO) {
        try {
            val response = if (genre != null && query.isBlank()) {
                ApiClient.audiusApi.getTrending(genre = genre)
            } else {
                ApiClient.audiusApi.searchTracks(query)
            }
            response.data?.map { dto ->
                Track(
                    id = "audius-${dto.id}",
                    title = dto.title,
                    artist = dto.user?.name ?: "Artista Audius",
                    album = dto.genre ?: dto.user?.handle?.let { "@$it" } ?: "Audius Original",
                    durationMs = (dto.duration ?: 180) * 1000L,
                    mediaUri = "https://discoveryprovider.audius.co/v1/tracks/${dto.id}/stream?app_name=OlirumPlay",
                    artworkUri = dto.artwork?.art1000 ?: dto.artwork?.art480 ?: dto.artwork?.art150,
                    source = "audius"
                )
            } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getTrendingOnline(genre: String? = null): List<Track> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.audiusApi.getTrending(genre = genre)
            response.data?.map { dto ->
                Track(
                    id = "audius-${dto.id}",
                    title = dto.title,
                    artist = dto.user?.name ?: "Artista Audius",
                    album = dto.genre ?: dto.user?.handle?.let { "@$it" } ?: "Audius Original",
                    durationMs = (dto.duration ?: 180) * 1000L,
                    mediaUri = "https://discoveryprovider.audius.co/v1/tracks/${dto.id}/stream?app_name=OlirumPlay",
                    artworkUri = dto.artwork?.art1000 ?: dto.artwork?.art480 ?: dto.artwork?.art150,
                    source = "audius"
                )
            } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getLyrics(track: Track): List<LyricLine> = withContext(Dispatchers.IO) {
        try {
            val cleanTitle = track.title.replace(Regex("\\s*[\\(\\[].*?[\\)\\]]"), "").trim()
            val cleanArtist = track.artist.replace(Regex("\\s*[\\(\\[].*?[\\)\\]]"), "").trim()

            var dto = try {
                ApiClient.lrcLibApi.getLyrics(cleanTitle, cleanArtist)
            } catch (e: Exception) {
                null
            }

            if (dto?.syncedLyrics == null) {
                val searchList = ApiClient.lrcLibApi.searchLyrics("$cleanTitle $cleanArtist")
                if (searchList.isNotEmpty()) {
                    dto = searchList.first()
                }
            }

            dto?.syncedLyrics?.let { parseLrc(it) } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseLrc(lrcContent: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        val pattern = Pattern.compile("\\[(\\d{2}):(\\d{2}(?:\\.\\d{1,3})?)\\](.*)")
        for (rawLine in lrcContent.lines()) {
            val matcher = pattern.matcher(rawLine.trim())
            if (matcher.matches()) {
                val min = matcher.group(1)?.toLongOrNull() ?: 0L
                val secFloat = matcher.group(2)?.toFloatOrNull() ?: 0f
                val text = matcher.group(3)?.trim() ?: ""
                val totalMs = (min * 60 * 1000) + (secFloat * 1000).toLong()
                if (text.isNotEmpty()) {
                    lines.add(LyricLine(totalMs, text))
                }
            }
        }
        return lines.sortedBy { it.timeMs }
    }
}
