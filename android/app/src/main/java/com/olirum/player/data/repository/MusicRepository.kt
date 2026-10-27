package com.olirum.player.data.repository

import android.content.Context
import com.olirum.player.data.local.AppDatabase
import com.olirum.player.data.local.MediaStoreScanner
import com.olirum.player.data.local.TrackEntity
import com.olirum.player.data.remote.ApiClient
import com.olirum.player.model.LyricLine
import com.olirum.player.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

class MusicRepository(
    private val context: Context,
    private val database: AppDatabase,
    private val scanner: MediaStoreScanner
) {
    val favoriteTracks: Flow<List<Track>> = database.trackDao().getFavoriteTracks()
        .map { list -> list.map { it.toTrack() } }

    val offlineTracks: Flow<List<Track>> = database.trackDao().getOfflineTracks()
        .map { list -> list.map { it.toTrack() } }

    suspend fun scanLocalTracks(): List<Track> = scanner.scanLocalTracks()

    suspend fun toggleFavorite(track: Track) {
        val newFav = !track.isFavorite
        database.trackDao().insertTrack(TrackEntity.fromTrack(track.copy(isFavorite = newFav)))
    }

    suspend fun searchOnlineCatalog(query: String): List<Track> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.audiusApi.searchTracks(query)
            response.data?.map { dto ->
                Track(
                    id = "audius-${dto.id}",
                    title = dto.title,
                    artist = dto.user?.name ?: "Artista Audius",
                    album = dto.user?.handle?.let { "@$it" } ?: "Audius Online",
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

    suspend fun getTrendingOnline(): List<Track> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.audiusApi.getTrending()
            response.data?.map { dto ->
                Track(
                    id = "audius-${dto.id}",
                    title = dto.title,
                    artist = dto.user?.name ?: "Artista Audius",
                    album = dto.user?.handle?.let { "@$it" } ?: "Audius Trending",
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
