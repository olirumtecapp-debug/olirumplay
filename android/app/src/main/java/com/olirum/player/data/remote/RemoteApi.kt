package com.olirum.player.data.remote

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// Audius Models
data class AudiusResponse(
    @SerializedName("data") val data: List<AudiusTrackDto>?
)

data class AudiusTrackDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("genre") val genre: String?,
    @SerializedName("duration") val duration: Int?,
    @SerializedName("user") val user: AudiusUserDto?,
    @SerializedName("artwork") val artwork: AudiusArtworkDto?
)

data class AudiusUserDto(
    @SerializedName("name") val name: String,
    @SerializedName("handle") val handle: String
)

data class AudiusArtworkDto(
    @SerializedName("1000x1000") val art1000: String?,
    @SerializedName("480x480") val art480: String?,
    @SerializedName("150x150") val art150: String?
)

interface AudiusApiService {
    @GET("v1/tracks/trending")
    suspend fun getTrending(
        @Query("app_name") appName: String = "OlirumPlay",
        @Query("genre") genre: String? = null,
        @Query("limit") limit: Int = 30
    ): AudiusResponse

    @GET("v1/tracks/search")
    suspend fun searchTracks(
        @Query("query") query: String,
        @Query("app_name") appName: String = "OlirumPlay",
        @Query("limit") limit: Int = 30
    ): AudiusResponse
}

// LRCLIB Models
data class LrcLibDto(
    @SerializedName("id") val id: Long?,
    @SerializedName("trackName") val trackName: String?,
    @SerializedName("artistName") val artistName: String?,
    @SerializedName("plainLyrics") val plainLyrics: String?,
    @SerializedName("syncedLyrics") val syncedLyrics: String?
)

interface LrcLibApiService {
    @GET("api/get")
    suspend fun getLyrics(
        @Query("track_name") trackName: String,
        @Query("artist_name") artistName: String
    ): LrcLibDto

    @GET("api/search")
    suspend fun searchLyrics(
        @Query("q") query: String
    ): List<LrcLibDto>
}

object ApiClient {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    val audiusApi: AudiusApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.audius.co/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AudiusApiService::class.java)
    }

    val lrcLibApi: LrcLibApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://lrclib.net/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LrcLibApiService::class.java)
    }
}
