package com.eduardo.alrededor

import com.eduardo.alrededor.domain.model.GeoPoint
import com.eduardo.alrededor.domain.usecase.ShouldRefetchPlacesUseCase
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShouldRefetchPlacesUseCaseTest {

    private val useCase = ShouldRefetchPlacesUseCase()
    private val origin = GeoPoint(0.0, 0.0)

    @Test
    fun sin_consulta_previa_siempre_consulta() {
        assertTrue(useCase(lastQueryPoint = null, current = origin, force = false))
    }

    @Test
    fun si_se_fuerza_consulta_aunque_no_se_haya_movido() {
        assertTrue(useCase(lastQueryPoint = origin, current = origin, force = true))
    }

    @Test
    fun a_200_m_no_vuelve_a_consultar() {
        // 0.0018 grados de longitud en el ecuador son unos 200 m
        assertFalse(useCase(origin, GeoPoint(0.0, 0.0018), force = false))
    }

    @Test
    fun a_400_m_si_vuelve_a_consultar() {
        assertTrue(useCase(origin, GeoPoint(0.0, 0.0036), force = false))
    }
}
