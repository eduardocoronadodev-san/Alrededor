package com.eduardo.alrededor.domain.repository

import com.eduardo.alrededor.domain.model.GeoPoint
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    /** Posiciones del usuario mientras el flujo se recolecta. Al cancelar, se detiene el GPS. */
    fun locationUpdates(): Flow<GeoPoint>
}
