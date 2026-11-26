package com.eduardo.alrededor.domain.model

data class PlacesResult(
    val places: List<Place>,
    /** true si no hubo red y se devolvieron lugares guardados. */
    val isFromCache: Boolean
)
