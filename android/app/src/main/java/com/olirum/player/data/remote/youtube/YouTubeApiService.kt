package com.olirum.player.data.remote.youtube

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface Retrofit oficial para comunicação com a YouTube Data API v3 (Google Cloud).
 * https://developers.google.com/youtube/v3/docs
 */
interface YouTubeApiService {

    /**
     * Pesquisa vídeos de música utilizando a YouTube Data API v3 oficial.
     * Restringe a busca para vídeos incorporáveis da categoria de música (10).
     */
    @GET("search")
    suspend fun searchMusicVideos(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("videoCategoryId") videoCategoryId: String = "10",
        @Query("videoEmbeddable") videoEmbeddable: String = "true",
        @Query("maxResults") maxResults: Int = 25,
        @Query("pageToken") pageToken: String? = null,
        @Query("key") apiKey: String
    ): YouTubeSearchResponse

    /**
     * Obtém os metadados oficiais e duração real (ISO 8601) dos vídeos.
     */
    @GET("videos")
    suspend fun getVideoDetails(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("id") videoIds: String,
        @Query("key") apiKey: String
    ): YouTubeVideoListResponse
}
