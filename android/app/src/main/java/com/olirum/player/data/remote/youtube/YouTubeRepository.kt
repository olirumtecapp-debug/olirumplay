package com.olirum.player.data.remote.youtube

import com.olirum.player.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

sealed class YouTubeError(message: String) : Exception(message) {
    object MissingApiKey : YouTubeError("Chave da YouTube Data API v3 não configurada.")
    object QuotaExceeded : YouTubeError("Quota diária da YouTube Data API esgotada no Google Cloud (HTTP 403).")
    object InvalidApiKey : YouTubeError("Chave da API do YouTube inválida ou não autorizada.")
    data class NetworkError(val error: Throwable) : YouTubeError("Falha de conexão com os servidores do YouTube: ${error.localizedMessage}")
    data class GenericError(val error: Throwable) : YouTubeError("Erro ao carregar videoclipes: ${error.localizedMessage}")
}

class YouTubeRepository(
    private val preferencesRepository: UserPreferencesRepository
) {
    private val apiService: YouTubeApiService

    // Cache em memória das buscas para economizar quota da API oficial
    private val memoryCache = ConcurrentHashMap<String, List<VideoClip>>()

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/youtube/v3/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(YouTubeApiService::class.java)
    }

    // As 19 Categorias Oficiais de Descoberta Musical
    val categories: List<VideoCategory> = listOf(
        VideoCategory(
            id = "love_songs",
            name = "Love Songs",
            searchQuery = "Love Songs baladas romanticas music video",
            subtitle = "Baladas românticas e clássicos apaixonantes",
            gradientColors = listOf(0xFFFF3DAD, 0xFF8B5CF6),
            iconName = "Favorite",
            drawableResName = "genre_love_songs"
        ),
        VideoCategory(
            id = "classic_rock",
            name = "Classic Rock",
            searchQuery = "Classic Rock 70s 80s official music video",
            subtitle = "Queen, Led Zeppelin, Pink Floyd & Lendas",
            gradientColors = listOf(0xFF008BFF, 0xFF00E5D4),
            iconName = "Guitar",
            drawableResName = "genre_classic_rock"
        ),
        VideoCategory(
            id = "love_rock",
            name = "Love Rock",
            searchQuery = "Soft Rock Power Ballads 80s 90s music video",
            subtitle = "Power ballads e o lado romântico do rock",
            gradientColors = listOf(0xFF8B5CF6, 0xFFFF3DAD),
            iconName = "Favorite",
            drawableResName = "genre_love_rock"
        ),
        VideoCategory(
            id = "lounge",
            name = "Lounge",
            searchQuery = "Lounge Chillout LoFi Relaxing music video",
            subtitle = "Chillout, Lo-Fi, Bossa & Ambient Beats",
            gradientColors = listOf(0xFF00E5D4, 0xFF8B5CF6),
            iconName = "Headphones",
            drawableResName = "genre_lounge"
        ),
        VideoCategory(
            id = "pop",
            name = "Pop",
            searchQuery = "Pop Hits 2024 official music video",
            subtitle = "Grandes sucessos mundiais das paradas",
            gradientColors = listOf(0xFFFF3DAD, 0xFF008BFF),
            iconName = "Star",
            drawableResName = "genre_pop_hits"
        ),
        VideoCategory(
            id = "greatest_hits",
            name = "Greatest Hits",
            searchQuery = "Greatest Hits All Time official music video",
            subtitle = "As maiores canções de todos os tempos",
            gradientColors = listOf(0xFFF59E0B, 0xFFFF3DAD),
            iconName = "EmojiEvents",
            drawableResName = "genre_greatest_hits"
        ),
        VideoCategory(
            id = "anos_70",
            name = "Anos 70",
            searchQuery = "70s Music Hits Disco Rock official music video",
            subtitle = "Disco, Rock Progressivo, Funk & Soul 70's",
            gradientColors = listOf(0xFFF59E0B, 0xFFEF4444),
            iconName = "History",
            drawableResName = "genre_anos_70"
        ),
        VideoCategory(
            id = "anos_80",
            name = "Anos 80",
            searchQuery = "80s Music Hits Synthpop Rock official video",
            subtitle = "A era de ouro do Synthpop e Pop Rock",
            gradientColors = listOf(0xFFFF3DAD, 0xFF00E5D4),
            iconName = "History",
            drawableResName = "genre_anos_80"
        ),
        VideoCategory(
            id = "anos_90",
            name = "Anos 90",
            searchQuery = "90s Music Hits Eurodance Grunge official music video",
            subtitle = "Eurodance, Grunge, Pop e R&B nostálgico",
            gradientColors = listOf(0xFF8B5CF6, 0xFF008BFF),
            iconName = "History",
            drawableResName = "genre_anos_90"
        ),
        VideoCategory(
            id = "anos_2000",
            name = "Anos 2000",
            searchQuery = "2000s Pop Rock Hits official music video",
            subtitle = "Pop 2000, Nu-Metal, Hip-Hop & Emo",
            gradientColors = listOf(0xFF008BFF, 0xFF8B5CF6),
            iconName = "History",
            drawableResName = "genre_anos_2000"
        ),
        VideoCategory(
            id = "catolica",
            name = "Música Católica",
            searchQuery = "Musica Catolica clipe oficial oracao louvor",
            subtitle = "Louvores, orações e adoração católica",
            gradientColors = listOf(0xFF008BFF, 0xFF00E5D4),
            iconName = "Church",
            drawableResName = "genre_catolica"
        ),
        VideoCategory(
            id = "gospel",
            name = "Gospel",
            searchQuery = "Musica Gospel Louvor clipe oficial",
            subtitle = "Grandes nomes do louvor evangélico",
            gradientColors = listOf(0xFF00E5D4, 0xFF10B981),
            iconName = "Church",
            drawableResName = "genre_gospel"
        ),
        VideoCategory(
            id = "sertanejo",
            name = "Sertanejo",
            searchQuery = "Sertanejo Universitario Acustico clipe oficial",
            subtitle = "Modão, Sertanejo Universitário e Acústicos",
            gradientColors = listOf(0xFFF59E0B, 0xFF00E5D4),
            iconName = "MusicNote",
            drawableResName = "genre_sertanejo"
        ),
        VideoCategory(
            id = "mpb",
            name = "MPB",
            searchQuery = "MPB Musica Popular Brasileira Bossa Nova clipe oficial",
            subtitle = "Caetano, Chico, Gil, Elis, Djavan & Clássicos",
            gradientColors = listOf(0xFF10B981, 0xFFF59E0B),
            iconName = "MusicNote",
            drawableResName = "genre_mpb_bossa"
        ),
        VideoCategory(
            id = "samba_pagode",
            name = "Samba e Pagode",
            searchQuery = "Samba e Pagode ao vivo clipe oficial",
            subtitle = "Samba de raiz, pagode 90 e grandes rodas",
            gradientColors = listOf(0xFFF59E0B, 0xFF8B5CF6),
            iconName = "MusicNote",
            drawableResName = "genre_samba_pagode"
        ),
        VideoCategory(
            id = "eletronica",
            name = "Eletrônica",
            searchQuery = "EDM Electronic Music Festival official video",
            subtitle = "House, Deep House, Techno, Trance & EDM",
            gradientColors = listOf(0xFF00E5D4, 0xFFFF3DAD),
            iconName = "GraphicEq",
            drawableResName = "genre_eletronica"
        ),
        VideoCategory(
            id = "jazz_blues",
            name = "Jazz e Blues",
            searchQuery = "Jazz Blues Smooth Jazz live official music video",
            subtitle = "Smooth Jazz, Chicago Blues, Soul & Bossa",
            gradientColors = listOf(0xFFFF3DAD, 0xFF8B5CF6),
            iconName = "MusicNote",
            drawableResName = "genre_jazz_blues"
        ),
        VideoCategory(
            id = "reggae",
            name = "Reggae",
            searchQuery = "Reggae Roots Dub Bob Marley official music video",
            subtitle = "Roots Reggae, Dub, Jah Music & Ritmos Tropicais",
            gradientColors = listOf(0xFF10B981, 0xFFF59E0B),
            iconName = "MusicNote",
            drawableResName = "genre_reggae"
        ),
        VideoCategory(
            id = "classica",
            name = "Música Clássica",
            searchQuery = "Classical Music Orchestra Symphony Mozart Bach Chopin",
            subtitle = "Bach, Beethoven, Mozart & Orquestras",
            gradientColors = listOf(0xFF8B5CF6, 0xFF008BFF),
            iconName = "MusicNote",
            drawableResName = "genre_classica"
        )
    )

    /**
     * Retorna a chave de API configurada no DataStore do usuário.
     */
    suspend fun getApiKey(): String? {
        return preferencesRepository.youtubeApiKey.firstOrNull()?.trim()?.ifBlank { null }
    }

    /**
     * Salva a nova chave de API no DataStore.
     */
    suspend fun saveApiKey(key: String) {
        preferencesRepository.saveYouTubeApiKey(key)
        memoryCache.clear() // Limpa o cache ao trocar a chave
    }

    /**
     * Pesquisa vídeos musicais oficiais pelo YouTube Data API v3.
     */
    suspend fun searchVideos(query: String, explicitApiKey: String? = null): Result<List<VideoClip>> {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) {
            return Result.success(emptyList())
        }

        // Verifica cache em memória
        val cacheKey = cleanQuery.lowercase()
        memoryCache[cacheKey]?.let { cachedList ->
            if (cachedList.isNotEmpty()) {
                return Result.success(cachedList)
            }
        }

        val apiKey = explicitApiKey ?: getApiKey()
        if (apiKey.isNullOrBlank()) {
            return Result.failure(YouTubeError.MissingApiKey)
        }

        return try {
            val searchResponse = apiService.searchMusicVideos(
                query = cleanQuery,
                apiKey = apiKey
            )

            val rawItems = searchResponse.items ?: emptyList()
            val videoIds = rawItems.mapNotNull { it.id?.videoId }.filter { it.isNotBlank() }

            if (videoIds.isEmpty()) {
                memoryCache[cacheKey] = emptyList()
                return Result.success(emptyList())
            }

            // Busca detalhes reais de duração e canais oficiais
            val detailsResponse = apiService.getVideoDetails(
                videoIds = videoIds.joinToString(","),
                apiKey = apiKey
            )

            val detailsMap = detailsResponse.items?.associateBy { it.id ?: "" } ?: emptyMap()

            val clips = rawItems.mapNotNull { searchItem ->
                val videoId = searchItem.id?.videoId ?: return@mapNotNull null
                val snippet = searchItem.snippet ?: return@mapNotNull null
                val detail = detailsMap[videoId]

                val title = YouTubeDurationParser.cleanHtmlEntities(snippet.title)
                val channelTitle = YouTubeDurationParser.cleanHtmlEntities(snippet.channelTitle)
                val thumbUrl = snippet.thumbnails?.highThumb?.url
                    ?: snippet.thumbnails?.mediumThumb?.url
                    ?: snippet.thumbnails?.defaultThumb?.url
                    ?: "https://img.youtube.com/vi/$videoId/hqdefault.jpg"

                val isoDuration = detail?.contentDetails?.duration
                val durationFormatted = YouTubeDurationParser.parse(isoDuration)
                val durationSeconds = YouTubeDurationParser.parseToSeconds(isoDuration)

                VideoClip(
                    id = videoId,
                    title = title,
                    channelTitle = channelTitle,
                    thumbnailUrl = thumbUrl,
                    durationFormatted = durationFormatted,
                    durationSeconds = durationSeconds,
                    publishedAt = snippet.publishedAt ?: ""
                )
            }

            memoryCache[cacheKey] = clips
            Result.success(clips)

        } catch (e: HttpException) {
            val code = e.code()
            val errorBody = e.response()?.errorBody()?.string() ?: ""

            when {
                code == 403 && (errorBody.contains("quotaExceeded", ignoreCase = true) || errorBody.contains("rateLimitExceeded", ignoreCase = true)) -> {
                    Result.failure(YouTubeError.QuotaExceeded)
                }
                code == 400 || (code == 403 && errorBody.contains("keyInvalid", ignoreCase = true)) -> {
                    Result.failure(YouTubeError.InvalidApiKey)
                }
                else -> {
                    Result.failure(YouTubeError.GenericError(e))
                }
            }
        } catch (e: IOException) {
            Result.failure(YouTubeError.NetworkError(e))
        } catch (e: Throwable) {
            Result.failure(YouTubeError.GenericError(e))
        }
    }
}
