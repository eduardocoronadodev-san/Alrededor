package com.eduardo.alrededor.domain.repository

import com.eduardo.alrededor.domain.model.GeoPoint
import com.eduardo.alrededor.domain.model.PlacesResult

interface PlacesRepository {

    suspend fun getNearby(point: GeoPoint, radiusM: Int): PlacesResult
}
