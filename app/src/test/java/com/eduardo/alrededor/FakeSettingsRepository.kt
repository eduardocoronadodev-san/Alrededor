package com.eduardo.alrededor

import com.eduardo.alrededor.domain.model.AppSettings
import com.eduardo.alrededor.domain.model.ThemeMode
import com.eduardo.alrededor.domain.repository.SavedDataRepository
import com.eduardo.alrededor.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeSettingsRepository(initial: AppSettings = AppSettings()) : SettingsRepository {
    private val state = MutableStateFlow(initial)
    override val settings: Flow<AppSettings> = state

    val current: AppSettings get() = state.value

    override suspend fun setRadiusM(radiusM: Int) { state.update { it.copy(radiusM = radiusM) } }
    override suspend fun setVoiceEnabled(enabled: Boolean) { state.update { it.copy(voiceEnabled = enabled) } }
    override suspend fun setThemeMode(mode: ThemeMode) { state.update { it.copy(themeMode = mode) } }
}

class FakeSavedDataRepository(private val fail: Boolean = false) : SavedDataRepository {
    var cleared = false
        private set

    override suspend fun clearAll() {
        if (fail) throw IllegalStateException("fallo simulado")
        cleared = true
    }
}
