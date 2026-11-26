package com.eduardo.alrededor

import com.eduardo.alrededor.domain.model.GeoPoint
import com.eduardo.alrededor.domain.model.Place
import com.eduardo.alrededor.domain.usecase.ComputeNearbyPlacesUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ComputeNearbyPlacesUseCaseTest {

    private val useCase = ComputeNearbyPlacesUseCase()
    private val origin = GeoPoint(0.0, 0.0)

    /** Un lugar sobre el ecuador: más "lonOffset" = más lejos. */
    private fun place(id: Long, lonOffset: Double, relevant: Boolean = true) = Place(
        id = id, title = "Lugar $id", lat = 0.0, lon = lonOffset,
        description = null, thumbnailUrl = null, articleUrl = "https://x/$id",
        isRelevant = relevant
    )

    @Test
    fun ordena_por_distancia() {
        val places = listOf(place(1, 0.010), place(2, 0.001, relevant = false), place(3, 0.020))
        val result = useCase(places, origin, showAll = true)
        assertEquals(listOf(2L, 1L, 3L), result.map { it.place.id })
    }

    @Test
    fun sin_mostrar_todo_descarta_los_no_relevantes() {
        val relevant = (1..6).map { place(it.toLong(), 0.01 * it) }
        val irrelevant = place(99, 0.0001, relevant = false)
        val result = useCase(relevant + irrelevant, origin, showAll = false)
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L, 6L), result.map { it.place.id })
        assertFalse(result.any { it.place.id == 99L })
    }

    @Test
    fun si_hay_muy_pocos_relevantes_se_relaja_el_filtro() {
        val relevant = listOf(place(1, 0.010), place(2, 0.020))
        val irrelevant = (10..14).map { place(it.toLong(), 0.001 * (it - 9), relevant = false) }
        val result = useCase(relevant + irrelevant, origin, showAll = false)
        // 2 relevantes + los 3 no relevantes más cercanos = 5, ordenados por distancia
        assertEquals(listOf(10L, 11L, 12L, 1L, 2L), result.map { it.place.id })
    }

    @Test
    fun limita_la_lista_a_20() {
        val places = (1..25).map { place(it.toLong(), 0.001 * it) }
        assertEquals(20, useCase(places, origin, showAll = false).size)
    }

    @Test
    fun el_rumbo_hacia_un_lugar_al_este_es_90() {
        val result = useCase(listOf(place(1, 0.01)), origin, showAll = true)
        assertEquals(90.0, result.first().bearingDeg, 0.5)
    }
}
