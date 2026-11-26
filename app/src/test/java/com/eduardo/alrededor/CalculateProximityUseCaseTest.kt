package com.eduardo.alrededor

import com.eduardo.alrededor.domain.model.GeoPoint
import com.eduardo.alrededor.domain.model.Place
import com.eduardo.alrededor.domain.model.RelativeDirection
import com.eduardo.alrededor.domain.usecase.CalculateProximityUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculateProximityUseCaseTest {

    private val useCase = CalculateProximityUseCase()
    private val origin = GeoPoint(0.0, 0.0)

    // Un lugar a unos 1.1 km al este del origen
    private val east = Place(
        id = 1, title = "Este", lat = 0.0, lon = 0.01,
        description = null, thumbnailUrl = null, articleUrl = "https://x", isRelevant = true
    )

    @Test
    fun lugar_al_este_mirando_al_norte_queda_a_la_derecha() {
        val result = useCase(east, origin, azimuthDeg = 0f)
        assertEquals(RelativeDirection.RIGHT, result.direction)
        assertEquals(1112.0, result.distanceM, 5.0)
    }

    @Test
    fun lugar_al_este_mirando_al_este_queda_al_frente() {
        assertEquals(RelativeDirection.FRONT, useCase(east, origin, azimuthDeg = 90f).direction)
    }

    @Test
    fun sin_brujula_hay_distancia_pero_no_direccion() {
        val result = useCase(east, origin, azimuthDeg = null)
        assertNull(result.direction)
        assertNull(result.arrowRotationDeg)
    }
}
