package com.olirum.player.model

data class RadioCategory(
    val id: String,
    val name: String,
    val subtitle: String,
    val primaryTag: String,
    val tags: List<String>,
    val gradientColors: List<Long>,
    val iconName: String = "Radio",
    val drawableResName: String? = null,
    val excludedKeywords: List<String> = emptyList()
)

data class RadioStation(
    val id: String,
    val name: String,
    val genre: String, // "Católica", "Rock Clássico", "Música Clássica", "Jazz & Blues", etc.
    val streamUrl: String,
    val artworkUrl: String? = null,
    val location: String = "Brasil",
    val description: String = "Transmissão Digital ao Vivo",
    val bitrate: String = "128 kbps",
    val countryCode: String = "BR",
    val language: String = "Português",
    val votes: Int = 0,
    val codec: String = "MP3",
    val isFavorite: Boolean = false
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
