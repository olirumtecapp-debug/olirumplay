package com.olirum.player.model

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val mediaUri: String,
    val artworkUri: String? = null,
    val source: String = "local", // "local", "audius", "offline", "radio"
    val fileSize: Long = 0L,
    val isFavorite: Boolean = false,
    val folderPath: String = "",
    val folderName: String = "Músicas"
)

data class LyricLine(
    val timeMs: Long,
    val text: String
)

data class LyricsData(
    val isInstrumental: Boolean = false,
    val hasSynced: Boolean = false,
    val syncedLines: List<LyricLine> = emptyList(),
    val plainLines: List<String> = emptyList()
)

data class MusicFolder(
    val path: String,
    val name: String,
    val trackCount: Int,
    val tracks: List<Track>
)

