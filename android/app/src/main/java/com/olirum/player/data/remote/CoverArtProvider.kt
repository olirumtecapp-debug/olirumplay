package com.olirum.player.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

// MusicBrainz Release Group Models
data class MbReleaseGroupSearchResponse(
    @SerializedName("release-groups") val releaseGroups: List<MbReleaseGroupDto>?
)

data class MbReleaseGroupDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("primary-type") val primaryType: String?,
    @SerializedName("first-release-date") val firstReleaseDate: String?,
    @SerializedName("artist-credit") val artistCredit: List<MbArtistCreditDto>?
)

data class MbArtistCreditDto(
    @SerializedName("name") val name: String?
)

// MusicBrainz Recording Models
data class MbRecordingSearchResponse(
    @SerializedName("recordings") val recordings: List<MbRecordingDto>?
)

data class MbRecordingDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("releases") val releases: List<MbReleaseDto>?
)

data class MbReleaseDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("date") val date: String?,
    @SerializedName("release-group") val releaseGroup: MbReleaseGroupRefDto?
)

data class MbReleaseGroupRefDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String?
)

interface MusicBrainzApiService {
    @GET("ws/2/release-group")
    suspend fun searchReleaseGroup(
        @Query("query") query: String,
        @Query("fmt") format: String = "json",
        @Query("limit") limit: Int = 6
    ): MbReleaseGroupSearchResponse

    @GET("ws/2/recording")
    suspend fun searchRecording(
        @Query("query") query: String,
        @Query("fmt") format: String = "json",
        @Query("limit") limit: Int = 6
    ): MbRecordingSearchResponse
}
