package com.eduardo.alrededor.domain.model

/** Un lugar de interés, independiente de dónde venga el dato (API o base local). */
data class Place(
    val id: Long,
    val title: String,
    val lat: Double,
    val lon: Double,
    val description: String?,
    val thumbnailUrl: String?,
    val articleUrl: String,
    val isRelevant: Boolean
)
