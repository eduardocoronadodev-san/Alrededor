package com.eduardo.alrededor

import com.eduardo.alrededor.domain.usecase.PrepareSpeechTextUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PrepareSpeechTextUseCaseTest {

    private val useCase = PrepareSpeechTextUseCase()

    @Test
    fun antepone_el_titulo_y_toma_solo_tres_oraciones() {
        val extract = "Uno. Dos. Tres. Cuatro. Cinco."
        assertEquals("Lugar. Uno. Dos. Tres.", useCase("Lugar", extract))
    }

    @Test
    fun maneja_saltos_de_linea_entre_oraciones() {
        assertEquals("Lugar. Uno. Dos.", useCase("Lugar", "Uno.\nDos."))
    }

    @Test
    fun recorta_los_textos_muy_largos() {
        val largo = "a".repeat(2000)
        assertTrue(useCase("Lugar", largo).length <= 600)
    }
}
