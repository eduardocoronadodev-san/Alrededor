package com.eduardo.alrededor.data.remote

import com.eduardo.alrededor.data.mapper.toDomain
import com.eduardo.alrededor.domain.model.GeoPoint
import com.eduardo.alrededor.domain.model.Place
import javax.inject.Inject

/** Fuente de datos: Wikipedia. */
class PlacesRemoteDataSource @Inject constructor(
    private val api: WikipediaApi
) {
    suspend fun fetchNearby(point: GeoPoint, radiusM: Int): List<Place> {
        val response = api.nearby(
            coord = "${point.lat}|${point.lon}",
            radius = radiusM.coerceIn(10, 10_000)
        )
        // MediaWiki devuelve los errores con HTTP 200
        response.error?.let {
            throw IllegalStateException(it.info ?: it.code ?: "Error de la API")
        }
        return response.query?.pages?.values.orEmpty().mapNotNull { it.toDomain() }
    }
}
