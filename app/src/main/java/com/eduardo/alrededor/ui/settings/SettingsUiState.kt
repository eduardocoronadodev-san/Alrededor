package com.eduardo.alrededor.ui.settings

import com.eduardo.alrededor.domain.model.ThemeMode

/** Mensajes de un solo uso que la pantalla muestra y luego avisa que ya mostró. */
enum class SettingsMessage { DATA_CLEARED, CLEAR_FAILED }

/** Estado único de la pantalla. */
data class SettingsUiState(
    val radiusKm: Int = 10,
    val voiceEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isClearing: Boolean = false,
    val message: SettingsMessage? = null
)

/** Todo lo que la pantalla le puede comunicar al ViewModel. */
sealed interface SettingsAction {
    data class RadiusChanged(val km: Int) : SettingsAction
    data class VoiceEnabledChanged(val enabled: Boolean) : SettingsAction
    data class ThemeChanged(val mode: ThemeMode) : SettingsAction
    data object ClearDataConfirmed : SettingsAction
    data object MessageShown : SettingsAction
}
