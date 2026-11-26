package com.eduardo.alrededor.data.local

import com.eduardo.alrededor.data.mapper.toDomain
import com.eduardo.alrededor.data.mapper.toEntity
import com.eduardo.alrededor.domain.model.PlaceSummary
import javax.inject.Inject

/** Fuente de datos: los resúmenes guardados en el teléfono. */
class SummaryLocalDataSource @Inject constructor(
    private val dao: SummaryDao
) {
    suspend fun find(placeId: Long): PlaceSummary? = dao.findByPlaceId(placeId)?.toDomain()

    suspend fun save(summary: PlaceSummary) = dao.upsert(summary.toEntity())
}
