package com.eduardo.alrededor.domain.repository

import com.eduardo.alrededor.domain.model.Place
import com.eduardo.alrededor.domain.model.PlaceSummary

interface PlaceDetailRepository {
    /** El lugar guardado en el teléfono, o null si no existe. */
    suspend fun getPlace(placeId: Long): Place?

    /**
     * Resumen del lugar: guardado si es reciente; si no, de Wikipedia.
     * Sin red devuelve el guardado aunque sea viejo. Lanza IOException si no hay nada.
     */
    suspend fun getSummary(place: Place): PlaceSummary
}
