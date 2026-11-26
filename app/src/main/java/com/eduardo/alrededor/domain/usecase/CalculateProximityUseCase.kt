package com.eduardo.alrededor.domain.usecase

import com.eduardo.alrededor.domain.model.GeoPoint
import com.eduardo.alrededor.domain.model.Place
import com.eduardo.alrededor.domain.model.Proximity
import com.eduardo.alrededor.domain.model.RelativeDirection
import com.eduardo.alrededor.domain.util.GeoUtils
import javax.inject.Inject

/** Lógica pura: distancia y dirección de un lugar respecto al usuario. */
class CalculateProximityUseCase @Inject constructor() {

    operator fun invoke(place: Place, point: GeoPoint, azimuthDeg: Float?): Proximity {
        val distance = GeoUtils.distance(point.lat, point.lon, place.lat, place.lon)
        val relativeAngle = azimuthDeg?.let {
            val bearing = GeoUtils.bearing(point.lat, point.lon, place.lat, place.lon)
            (bearing - it + 360.0) % 360.0
        }
        return Proximity(
            distanceM = distance,
            arrowRotationDeg = relativeAngle?.toFloat(),
            direction = relativeAngle?.let { RelativeDirection.fromAngle(it) }
        )
    }
}
