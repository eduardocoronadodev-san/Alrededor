package com.eduardo.alrededor.domain.model

/** Qué tan lejos y en qué dirección está un lugar respecto al usuario. */
data class Proximity(
    val distanceM: Double,
    /** Grados que debe rotar la flecha (0 = al frente). Null si no hay brújula. */
    val arrowRotationDeg: Float?,
    val direction: RelativeDirection?
)
