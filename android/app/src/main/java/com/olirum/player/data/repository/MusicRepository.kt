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
import com.olirum.player.model.LyricsData
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
    val database: AppDatabase,
    private val scanner: MediaStoreScanner
) {
    private val gson = Gson()
    val coverArtRepository = CoverArtRepository(context, database)

    val favoriteTracks: Flow<List<Track>> = database.trackDao().getFavoriteTracks()
        .map { list -> list.map { it.toTrack() } }

    val offlineTracks: Flow<List<Track>> = database.trackDao().getOfflineTracks()
        .map { list -> list.map { it.toTrack() } }

    val playlists: Flow<List<PlaylistEntity>> = database.trackDao().getAllPlaylists()

    suspend fun scanLocalTracks(): List<Track> = withContext(Dispatchers.IO) {
        val tracks = scanner.scanLocalTracks()
        val favorites = try {
            database.trackDao().getFavoriteTracksList().associateBy { it.id }
        } catch (_: Exception) {
            emptyMap()
        }
        if (favorites.isEmpty()) {
            tracks
        } else {
            tracks.map { track ->
                val favEntity = favorites[track.id]
                if (favEntity != null && favEntity.isFavorite) {
                    track.copy(isFavorite = true)
                } else {
                    track
                }
            }
        }
    }

    suspend fun resolveCover(track: Track): String? = coverArtRepository.getCoverForTrack(track)

    suspend fun searchCoverCandidates(query: String) = coverArtRepository.searchCoverCandidates(query)

    suspend fun setManualCover(track: Track, coverUrl: String) = coverArtRepository.setManualCover(track, coverUrl)

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

    suspend fun getLyrics(track: Track): LyricsData = withContext(Dispatchers.IO) {
        try {
            val cleanTitle = track.title
                .replace(Regex("(?i)\\.mp3|\\.flac|\\.m4a|\\.wav|\\.aac"), "")
                .replace(Regex("^\\d{1,3}\\s*[-._]\\s*"), "")
                .replace(Regex("(?i)\\s*[\\(\\[].*?(official|clip|video|remaster|ao vivo|live|lyric|audio|feat|ft).*?[\\)\\]]"), "")
                .trim()

            val cleanArtist = track.artist
                .replace(Regex("(?i)\\s*[\\(\\[].*?(feat|ft).*?[\\)\\]]"), "")
                .trim()

            var dto = try {
                ApiClient.lrcLibApi.getLyrics(cleanTitle, cleanArtist)
            } catch (e: Exception) {
                null
            }

            if (dto?.syncedLyrics == null && dto?.plainLyrics == null) {
                val searchList = ApiClient.lrcLibApi.searchLyrics("$cleanTitle $cleanArtist")
                if (searchList.isNotEmpty()) {
                    dto = searchList.first()
                }
            }

            if (dto == null) {
                return@withContext LyricsData()
            }

            if (dto.instrumental == true) {
                return@withContext LyricsData(isInstrumental = true)
            }

            val synced = dto.syncedLyrics
            if (!synced.isNullOrBlank()) {
                val parsed = parseLrc(synced)
                if (parsed.isNotEmpty()) {
                    return@withContext LyricsData(
                        hasSynced = true,
                        syncedLines = parsed
                    )
                }
            }

            val plain = dto.plainLyrics
            if (!plain.isNullOrBlank()) {
                val plainLines = plain.lines().map { it.trim() }.filter { it.isNotEmpty() }
                return@withContext LyricsData(
                    hasSynced = false,
                    plainLines = plainLines
                )
            }

            LyricsData()
        } catch (e: Exception) {
            LyricsData()
        }
    }

    suspend fun getLyricLines(track: Track): List<LyricLine> {
        val result = getLyrics(track)
        return result.syncedLines
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
