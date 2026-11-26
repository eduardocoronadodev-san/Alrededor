package com.eduardo.alrededor.domain.repository

import com.eduardo.alrededor.domain.model.AppSettings
import com.eduardo.alrededor.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    /** Emite los ajustes actuales y cada cambio posterior. */
    val settings: Flow<AppSettings>

    suspend fun setRadiusM(radiusM: Int)
    suspend fun setVoiceEnabled(enabled: Boolean)
    suspend fun setThemeMode(mode: ThemeMode)
}
