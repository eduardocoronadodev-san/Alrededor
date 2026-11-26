package com.eduardo.alrededor.domain.model

data class PlaceSummary(
    val placeId: Long,
    /** Null si Wikipedia no tiene resumen para este artículo. */
    val extract: String?,
    val updatedAt: Long
)
