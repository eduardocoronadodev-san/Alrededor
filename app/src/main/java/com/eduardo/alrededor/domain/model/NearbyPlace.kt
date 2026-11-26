package com.eduardo.alrededor.domain.model

/** Un lugar visto desde la posición actual del usuario. */
data class NearbyPlace(
    val place: Place,
    val distanceM: Double,
    // Rumbo hacia el lugar, de 0 a 360 grados (0 = norte, 90 = este).
    val bearingDeg: Double
)
