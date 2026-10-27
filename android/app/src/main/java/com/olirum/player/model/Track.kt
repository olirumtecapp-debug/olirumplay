package com.olirum.player.model

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val mediaUri: String,
    val artworkUri: String? = null,
    val source: String = "local", // "local", "audius", "offline"
    val fileSize: Long = 0L,
    val isFavorite: Boolean = false
)

data class LyricLine(
    val timeMs: Long,
    val text: String
)
