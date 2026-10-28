package com.olirum.player.data.remote

import com.google.gson.annotations.SerializedName
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// LRCLIB Models
data class LrcLibDto(
    @SerializedName("id") val id: Long?,
    @SerializedName("trackName") val trackName: String?,
    @SerializedName("artistName") val artistName: String?,
    @SerializedName("albumName") val albumName: String?,
    @SerializedName("duration") val duration: Double?,
    @SerializedName("instrumental") val instrumental: Boolean?,
    @SerializedName("plainLyrics") val plainLyrics: String?,
    @SerializedName("syncedLyrics") val syncedLyrics: String?
)

interface LrcLibApiService {
    @GET("api/get")
    suspend fun getLyrics(
        @Query("track_name") trackName: String,
        @Query("artist_name") artistName: String,
        @Query("album_name") albumName: String? = null,
        @Query("duration") duration: Int? = null
    ): LrcLibDto

    @GET("api/search")
    suspend fun searchLyrics(
        @Query("q") query: String
    ): List<LrcLibDto>
}

object ApiClient {
    // User-Agent oficial descritivo conforme políticas da Radio Browser, MusicBrainz e LRCLIB
    const val USER_AGENT = "OlirumPlay/1.5.0 (Android; https://github.com/olirumtecapp-debug/olirumplay; contact@olirumplay.com)"

    val userAgentInterceptor = Interceptor { chain ->
        val original = chain.request()
        val request = original.newBuilder()
            .header("User-Agent", USER_AGENT)
            .build()
        chain.proceed(request)
    }

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(userAgentInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val lrcLibApi: LrcLibApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://lrclib.net/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LrcLibApiService::class.java)
    }

    val musicBrainzApi: MusicBrainzApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://musicbrainz.org/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MusicBrainzApiService::class.java)
    }
}
