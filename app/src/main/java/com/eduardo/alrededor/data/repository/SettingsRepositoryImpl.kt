package com.eduardo.alrededor.data.repository

import com.eduardo.alrededor.data.local.SettingsLocalDataSource
import com.eduardo.alrededor.domain.model.AppSettings
import com.eduardo.alrededor.domain.model.ThemeMode
import com.eduardo.alrededor.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val local: SettingsLocalDataSource
) : SettingsRepository {

    override val settings: Flow<AppSettings> = local.settings

    override suspend fun setRadiusM(radiusM: Int) = local.setRadiusM(radiusM)
    override suspend fun setVoiceEnabled(enabled: Boolean) = local.setVoiceEnabled(enabled)
    override suspend fun setThemeMode(mode: ThemeMode) = local.setThemeMode(mode)
}
