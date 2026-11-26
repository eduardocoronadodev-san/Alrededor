package com.eduardo.alrededor

import com.eduardo.alrededor.domain.model.ThemeMode
import com.eduardo.alrededor.domain.usecase.UpdateSettingsUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateSettingsUseCaseTest {

    private val repository = FakeSettingsRepository()
    private val useCase = UpdateSettingsUseCase(repository)

    @Test
    fun el_radio_se_guarda_en_metros() = runTest {
        useCase.setRadiusKm(5)
        assertEquals(5_000, repository.current.radiusM)
    }

    @Test
    fun el_radio_no_baja_de_1_km() = runTest {
        useCase.setRadiusKm(0)
        assertEquals(1_000, repository.current.radiusM)
    }

    @Test
    fun el_radio_no_pasa_de_10_km() = runTest {
        useCase.setRadiusKm(50)
        assertEquals(10_000, repository.current.radiusM)
    }

    @Test
    fun guarda_la_voz_y_el_tema() = runTest {
        useCase.setVoiceEnabled(false)
        useCase.setThemeMode(ThemeMode.DARK)
        assertEquals(false, repository.current.voiceEnabled)
        assertEquals(ThemeMode.DARK, repository.current.themeMode)
    }
}
