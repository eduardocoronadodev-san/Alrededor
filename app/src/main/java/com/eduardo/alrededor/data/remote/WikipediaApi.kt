package com.eduardo.alrededor.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WikipediaApi {
    @GET("w/api.php?action=query&generator=geosearch&prop=coordinates|description|pageimages&piprop=thumbnail&pithumbsize=250&format=json")
    suspend fun nearby(
        @Query("ggscoord") coord: String,
        @Query("ggsradius") radius: Int,
        @Query("ggslimit") limit: Int = 50
    ): WikipediaResponseDto

    @GET("api/rest_v1/page/summary/{title}")
    suspend fun summary(
        @Path("title") title: String
    ): SummaryDto
}

@Serializable
data class SummaryDto(
    @SerialName("extract") val extract: String? = null
)

@Serializable
data class WikipediaResponseDto(
    @SerialName("query") val query: QueryDto? = null,
    @SerialName("error") val error: ApiErrorDto? = null
)

@Serializable
data class QueryDto(
    @SerialName("pages") val pages: Map<String, PageDto>? = null
)

@Serializable
data class ApiErrorDto(
    @SerialName("code") val code: String? = null,
    @SerialName("info") val info: String? = null
)

@Serializable
data class PageDto(
    @SerialName("pageid") val pageid: Long,
    @SerialName("title") val title: String,
    @SerialName("coordinates") val coordinates: List<CoordinateDto>? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("thumbnail") val thumbnail: ThumbnailDto? = null
)

@Serializable
data class CoordinateDto(
    @SerialName("lat") val lat: Double,
    @SerialName("lon") val lon: Double
)

@Serializable
data class ThumbnailDto(
    @SerialName("source") val source: String
)
