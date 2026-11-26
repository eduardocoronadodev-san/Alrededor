package com.eduardo.alrededor.domain.usecase

import com.eduardo.alrededor.domain.model.ThemeMode
import com.eduardo.alrededor.domain.repository.SettingsRepository
import javax.inject.Inject

/** Cambia las preferencias. El radio se limita al rango permitido por la API (1 a 10 km). */
class UpdateSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend fun setRadiusKm(km: Int) =
        repository.setRadiusM(km.coerceIn(MIN_RADIUS_KM, MAX_RADIUS_KM) * 1000)

    suspend fun setVoiceEnabled(enabled: Boolean) = repository.setVoiceEnabled(enabled)

    suspend fun setThemeMode(mode: ThemeMode) = repository.setThemeMode(mode)

    private companion object {
        const val MIN_RADIUS_KM = 1
        const val MAX_RADIUS_KM = 10
    }
}
