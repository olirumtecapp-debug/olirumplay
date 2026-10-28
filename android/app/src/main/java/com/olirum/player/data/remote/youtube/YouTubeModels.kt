package com.olirum.player.data.remote.youtube

import com.google.gson.annotations.SerializedName
import java.util.regex.Pattern

// --- Resposta da busca (search.list) ---
data class YouTubeSearchResponse(
    @SerializedName("kind") val kind: String? = null,
    @SerializedName("nextPageToken") val nextPageToken: String? = null,
    @SerializedName("prevPageToken") val prevPageToken: String? = null,
    @SerializedName("items") val items: List<YouTubeSearchResultItem>? = null
)

data class YouTubeSearchResultItem(
    @SerializedName("id") val id: YouTubeResourceId? = null,
    @SerializedName("snippet") val snippet: YouTubeSnippet? = null
)

data class YouTubeResourceId(
    @SerializedName("kind") val kind: String? = null,
    @SerializedName("videoId") val videoId: String? = null
)

data class YouTubeSnippet(
    @SerializedName("publishedAt") val publishedAt: String? = null,
    @SerializedName("channelId") val channelId: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("thumbnails") val thumbnails: YouTubeThumbnails? = null,
    @SerializedName("channelTitle") val channelTitle: String? = null
)

data class YouTubeThumbnails(
    @SerializedName("default") val defaultThumb: YouTubeThumbnailItem? = null,
    @SerializedName("medium") val mediumThumb: YouTubeThumbnailItem? = null,
    @SerializedName("high") val highThumb: YouTubeThumbnailItem? = null,
    @SerializedName("standard") val standardThumb: YouTubeThumbnailItem? = null,
    @SerializedName("maxres") val maxresThumb: YouTubeThumbnailItem? = null
)

data class YouTubeThumbnailItem(
    @SerializedName("url") val url: String? = null,
    @SerializedName("width") val width: Int? = null,
    @SerializedName("height") val height: Int? = null
)

// --- Resposta dos detalhes do vídeo (videos.list) ---
data class YouTubeVideoListResponse(
    @SerializedName("items") val items: List<YouTubeVideoItem>? = null
)

data class YouTubeVideoItem(
    @SerializedName("id") val id: String? = null,
    @SerializedName("snippet") val snippet: YouTubeSnippet? = null,
    @SerializedName("contentDetails") val contentDetails: YouTubeContentDetails? = null
)

data class YouTubeContentDetails(
    @SerializedName("duration") val duration: String? = null
)

// --- Modelo de Domínio para UI ---
data class VideoClip(
    val id: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val durationFormatted: String,
    val durationSeconds: Long,
    val publishedAt: String,
    val youtubeUrl: String = "https://www.youtube.com/watch?v=$id"
)

// --- Categorias de Descoberta ---
data class VideoCategory(
    val id: String,
    val name: String,
    val searchQuery: String,
    val subtitle: String,
    val gradientColors: List<Long>,
    val iconName: String = "PlayCircle",
    val drawableResName: String? = null
)

// --- Utilitários de Conversão de Duração ISO 8601 (ex: PT3M45S -> "03:45") ---
object YouTubeDurationParser {
    private val DURATION_PATTERN: Pattern = Pattern.compile("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?")

    fun parse(isoDuration: String?): String {
        if (isoDuration.isNullOrBlank()) return "--:--"
        val matcher = DURATION_PATTERN.matcher(isoDuration)
        if (!matcher.matches()) return "--:--"

        val hours = matcher.group(1)?.toLongOrNull() ?: 0L
        val minutes = matcher.group(2)?.toLongOrNull() ?: 0L
        val seconds = matcher.group(3)?.toLongOrNull() ?: 0L

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun parseToSeconds(isoDuration: String?): Long {
        if (isoDuration.isNullOrBlank()) return 0L
        val matcher = DURATION_PATTERN.matcher(isoDuration)
        if (!matcher.matches()) return 0L

        val hours = matcher.group(1)?.toLongOrNull() ?: 0L
        val minutes = matcher.group(2)?.toLongOrNull() ?: 0L
        val seconds = matcher.group(3)?.toLongOrNull() ?: 0L

        return (hours * 3600) + (minutes * 60) + seconds
    }

    fun cleanHtmlEntities(text: String?): String {
        if (text.isNullOrBlank()) return ""
        return text
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
    }
}
