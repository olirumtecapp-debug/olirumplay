package com.olirum.player.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.olirum.player.model.Track

@Entity(tableName = "saved_tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val mediaUri: String,
    val artworkUri: String?,
    val source: String,
    val isFavorite: Boolean = false,
    val savedAt: Long = System.currentTimeMillis()
) {
    fun toTrack(): Track = Track(
        id = id,
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        mediaUri = mediaUri,
        artworkUri = artworkUri,
        source = source,
        isFavorite = isFavorite
    )

    companion object {
        fun fromTrack(track: Track): TrackEntity = TrackEntity(
            id = track.id,
            title = track.title,
            artist = track.artist,
            album = track.album,
            durationMs = track.durationMs,
            mediaUri = track.mediaUri,
            artworkUri = track.artworkUri,
            source = track.source,
            isFavorite = track.isFavorite
        )
    }
}

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val trackIdsJson: String, // Lista serializada de IDs
    val createdAt: Long = System.currentTimeMillis()
)
