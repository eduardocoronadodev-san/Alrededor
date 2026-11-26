package com.eduardo.alrededor.data.mapper

import com.eduardo.alrededor.data.local.PlaceEntity
import com.eduardo.alrededor.data.remote.PageDto
import com.eduardo.alrededor.domain.model.Place
import com.eduardo.alrededor.domain.util.RelevanceFilter
import java.net.URLEncoder

// Devuelve null si el artículo no trae coordenadas.
internal fun PageDto.toDomain(): Place? {
    val coordinate = coordinates?.firstOrNull() ?: return null
    return Place(
        id = pageid,
        title = title,
        lat = coordinate.lat,
        lon = coordinate.lon,
        description = description,
        thumbnailUrl = thumbnail?.source,
        articleUrl = articleUrlFor(title),
        isRelevant = RelevanceFilter.isRelevant(description)
    )
}

internal fun PlaceEntity.toDomain() = Place(
    id = id, title = title, lat = lat, lon = lon,
    description = description, thumbnailUrl = thumbnailUrl,
    articleUrl = articleUrl, isRelevant = isRelevant
)

internal fun Place.toEntity(updatedAt: Long) = PlaceEntity(
    id = id, title = title, lat = lat, lon = lon,
    description = description, thumbnailUrl = thumbnailUrl,
    articleUrl = articleUrl, isRelevant = isRelevant, updatedAt = updatedAt
)

internal fun com.eduardo.alrededor.data.local.SummaryEntity.toDomain() = com.eduardo.alrededor.domain.model.PlaceSummary(
    placeId = placeId, extract = extract, updatedAt = updatedAt
)

internal fun com.eduardo.alrededor.domain.model.PlaceSummary.toEntity() = com.eduardo.alrededor.data.local.SummaryEntity(
    placeId = placeId, extract = extract, updatedAt = updatedAt
)

private fun articleUrlFor(title: String): String =
    "https://es.wikipedia.org/wiki/" + URLEncoder.encode(title.replace(' ', '_'), "UTF-8")
