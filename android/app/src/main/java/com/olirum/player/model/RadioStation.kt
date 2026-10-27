package com.olirum.player.model

data class RadioStation(
    val id: String,
    val name: String,
    val genre: String, // "Católica", "Rock Clássico", "Música Clássica", "Pop & Variados"
    val streamUrl: String,
    val artworkUrl: String? = null,
    val location: String = "Brasil",
    val description: String = "Transmissão Digital ao Vivo",
    val bitrate: String = "128 kbps"
) {
    fun toTrack(): Track {
        return Track(
            id = id,
            title = name,
            artist = "$genre • $location",
            album = "Rádio Online ao Vivo",
            durationMs = 0L,
            mediaUri = streamUrl,
            artworkUri = artworkUrl,
            source = "radio"
        )
    }
}
