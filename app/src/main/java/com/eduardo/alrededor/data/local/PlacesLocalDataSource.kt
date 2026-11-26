package com.eduardo.alrededor.data.local

import com.eduardo.alrededor.data.mapper.toDomain
import com.eduardo.alrededor.data.mapper.toEntity
import com.eduardo.alrededor.domain.model.GeoPoint
import com.eduardo.alrededor.domain.model.Place
import javax.inject.Inject
import kotlin.math.cos

// Fuente de datos: los lugares guardados en el teléfono.
class PlacesLocalDataSource @Inject constructor(
    private val dao: PlaceDao
) {
    suspend fun save(places: List<Place>) {
        val now = System.currentTimeMillis()
        dao.upsertAll(places.map { it.toEntity(updatedAt = now) })
    }

    /** Lugares guardados dentro de un cuadro alrededor del punto (aprox. el radio indicado). */
    suspend fun findNear(point: GeoPoint, radiusM: Int): List<Place> {
        val deltaLat = radiusM / METERS_PER_DEGREE
        val deltaLon = radiusM / (METERS_PER_DEGREE * cos(Math.toRadians(point.lat)))
        return dao.findInBounds(
            minLat = point.lat - deltaLat,
            maxLat = point.lat + deltaLat,
            minLon = point.lon - deltaLon,
            maxLon = point.lon + deltaLon
        ).map { it.toDomain() }
    }

    suspend fun findById(id: Long): Place? = dao.findById(id)?.toDomain()

    suspend fun clearAll() = dao.deleteAll()

    private companion object {
        const val METERS_PER_DEGREE = 111_320.0
    }
}
