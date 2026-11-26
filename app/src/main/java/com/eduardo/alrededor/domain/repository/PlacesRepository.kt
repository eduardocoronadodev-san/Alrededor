package com.eduardo.alrededor.domain.repository

import com.eduardo.alrededor.domain.model.GeoPoint
import com.eduardo.alrededor.domain.model.PlacesResult

interface PlacesRepository {
    /**
     * Lugares cerca del punto. Lanza IOException si no hay red y no hay nada guardado.
     */
    suspend fun getNearby(point: GeoPoint, radiusM: Int): PlacesResult
}
