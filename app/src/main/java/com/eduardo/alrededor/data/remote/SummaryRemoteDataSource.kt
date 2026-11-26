package com.eduardo.alrededor.data.remote

import com.eduardo.alrededor.domain.model.Place
import com.eduardo.alrededor.domain.model.PlaceSummary
import javax.inject.Inject

/** Fuente de datos: el endpoint de resumen de Wikipedia. */
class SummaryRemoteDataSource @Inject constructor(
    private val api: WikipediaApi
) {
    suspend fun fetch(place: Place): PlaceSummary {
        val dto = api.summary(place.title.replace(' ', '_'))
        return PlaceSummary(
            placeId = place.id,
            extract = dto.extract,
            updatedAt = System.currentTimeMillis()
        )
    }
}
