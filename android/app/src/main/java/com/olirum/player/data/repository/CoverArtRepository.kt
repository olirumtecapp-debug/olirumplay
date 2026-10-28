package com.olirum.player.data.repository

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.olirum.player.data.local.AppDatabase
import com.olirum.player.data.local.TrackEntity
import com.olirum.player.data.remote.ApiClient
import com.olirum.player.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap

data class CoverCandidate(
    val title: String,
    val artist: String,
    val year: String,
    val thumbnailUrl: String,
    val fullUrl: String
)

class CoverArtRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    // Cache em memória de capas já resolvidas para rolagem ultra-rápida
    private val memoryCache = ConcurrentHashMap<String, String>()

    suspend fun getCoverForTrack(track: Track): String? = withContext(Dispatchers.IO) {
        // 1. Verifica cache em memória
        memoryCache[track.id]?.let { return@withContext it }

        // 2. Se a faixa já tiver artworkUri válida que não seja o albumart legado quebrado
        if (!track.artworkUri.isNullOrBlank() && !track.artworkUri.contains("content://media/external/audio/albumart")) {
            memoryCache[track.id] = track.artworkUri
            return@withContext track.artworkUri
        }

        // 3. Tenta extrair a arte embutida diretamente do arquivo local (ID3 / APIC)
        if (track.source == "local") {
            val embedded = extractEmbeddedArtwork(track)
            if (embedded != null) {
                memoryCache[track.id] = embedded
                updateTrackCoverInDb(track.id, embedded)
                return@withContext embedded
            }
        }

        // 4. Busca automática na MusicBrainz + Cover Art Archive
        val cleanTitle = cleanMetadataString(track.title)
        val cleanArtist = cleanMetadataString(track.artist)
        if (cleanTitle.isNotBlank() && cleanArtist != "Artista Desconhecido") {
            val onlineCover = findCoverInMusicBrainz(cleanTitle, cleanArtist, track.album)
            if (onlineCover != null) {
                memoryCache[track.id] = onlineCover
                updateTrackCoverInDb(track.id, onlineCover)
                return@withContext onlineCover
            }
        }

        null
    }

    private fun extractEmbeddedArtwork(track: Track): String? {
        var retriever: MediaMetadataRetriever? = null
        try {
            retriever = MediaMetadataRetriever()
            val uri = Uri.parse(track.mediaUri)
            if (track.mediaUri.startsWith("content://")) {
                retriever.setDataSource(context, uri)
            } else {
                retriever.setDataSource(track.mediaUri)
            }
            val artBytes = retriever.embeddedPicture
            if (artBytes != null && artBytes.isNotEmpty()) {
                val cacheDir = File(context.cacheDir, "covers")
                if (!cacheDir.exists()) cacheDir.mkdirs()
                val coverFile = File(cacheDir, "art_${track.id.hashCode()}.jpg")
                if (!coverFile.exists() || coverFile.length() == 0L) {
                    FileOutputStream(coverFile).use { it.write(artBytes) }
                }
                return Uri.fromFile(coverFile).toString()
            }
        } catch (_: Exception) {
        } finally {
            try {
                retriever?.release()
            } catch (_: Exception) {}
        }
        return null
    }

    private suspend fun findCoverInMusicBrainz(title: String, artist: String, album: String?): String? {
        try {
            // Tenta primeiro por Release Group (álbum + artista)
            val cleanAlbum = if (!album.isNullOrBlank() && album != "Álbum do Dispositivo" && album != "Desconhecido") {
                cleanMetadataString(album)
            } else null

            if (cleanAlbum != null && cleanAlbum.isNotBlank()) {
                val rgQuery = "releasegroup:\"$cleanAlbum\" AND artist:\"$artist\""
                val rgResp = ApiClient.musicBrainzApi.searchReleaseGroup(rgQuery, limit = 4)
                val firstRg = rgResp.releaseGroups?.firstOrNull()
                if (firstRg != null) {
                    return "https://coverartarchive.org/release-group/${firstRg.id}/front-250"
                }
            }

            // Fallback: Busca pela gravação (título + artista)
            val recQuery = "recording:\"$title\" AND artist:\"$artist\""
            val recResp = ApiClient.musicBrainzApi.searchRecording(recQuery, limit = 4)
            val releases = recResp.recordings?.firstOrNull()?.releases
            val releaseGroupRef = releases?.firstOrNull()?.releaseGroup
            if (releaseGroupRef != null) {
                return "https://coverartarchive.org/release-group/${releaseGroupRef.id}/front-250"
            }
            val firstRelease = releases?.firstOrNull()
            if (firstRelease != null) {
                return "https://coverartarchive.org/release/${firstRelease.id}/front-250"
            }
        } catch (_: Exception) {
        }
        return null
    }

    suspend fun searchCoverCandidates(query: String): List<CoverCandidate> = withContext(Dispatchers.IO) {
        val list = mutableListOf<CoverCandidate>()
        try {
            val q = query.trim()
            if (q.isBlank()) return@withContext emptyList()

            val rgResp = ApiClient.musicBrainzApi.searchReleaseGroup(q, limit = 8)
            rgResp.releaseGroups?.forEach { rg ->
                val artist = rg.artistCredit?.firstOrNull()?.name ?: "Artista"
                val year = rg.firstReleaseDate?.take(4) ?: ""
                list.add(
                    CoverCandidate(
                        title = rg.title,
                        artist = artist,
                        year = year,
                        thumbnailUrl = "https://coverartarchive.org/release-group/${rg.id}/front-250",
                        fullUrl = "https://coverartarchive.org/release-group/${rg.id}/front-500"
                    )
                )
            }
        } catch (_: Exception) {
        }
        list
    }

    suspend fun setManualCover(track: Track, coverUrl: String) = withContext(Dispatchers.IO) {
        memoryCache[track.id] = coverUrl
        updateTrackCoverInDb(track.id, coverUrl)
    }

    private suspend fun updateTrackCoverInDb(trackId: String, artworkUri: String) {
        try {
            val existing = database.trackDao().getTrackById(trackId)
            if (existing != null) {
                database.trackDao().insertTrack(existing.copy(artworkUri = artworkUri))
            }
        } catch (_: Exception) {
        }
    }

    private fun cleanMetadataString(raw: String): String {
        return raw
            .replace(Regex("(?i)\\.mp3|\\.flac|\\.m4a|\\.wav|\\.aac"), "")
            .replace(Regex("^\\d{1,3}\\s*[-._]\\s*"), "") // remove 01 - , 02.
            .replace(Regex("(?i)\\s*[\\(\\[].*?(official|clip|video|remaster|ao vivo|live|lyric|audio|feat|ft).*?[\\)\\]]"), "")
            .replace(Regex("[_]+"), " ")
            .trim()
    }
}
