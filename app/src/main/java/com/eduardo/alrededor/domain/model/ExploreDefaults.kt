package com.eduardo.alrededor.domain.model

/** Reglas de negocio con sus valores iniciales (ajustables). */
object ExploreDefaults {
    const val RADIUS_M = 10_000              // máximo que permite la API
    const val REFETCH_DISTANCE_M = 300.0     // distancia para volver a consultar
    const val MAX_RELEVANT_ITEMS = 20        // máximo en la lista sin "Mostrar todo"
    const val MIN_RELEVANT_ITEMS = 5         // por debajo, se relaja el filtro
    const val LOCATION_INTERVAL_MS = 5_000L
    const val LOCATION_MIN_DISTANCE_M = 10f
}
