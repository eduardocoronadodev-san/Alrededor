package com.eduardo.alrededor.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduardo.alrededor.domain.usecase.ClearSavedDataUseCase
import com.eduardo.alrededor.domain.usecase.ObserveSettingsUseCase
import com.eduardo.alrededor.domain.usecase.UpdateSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeSettings: ObserveSettingsUseCase,
    private val updateSettings: UpdateSettingsUseCase,
    private val clearSavedData: ClearSavedDataUseCase
) : ViewModel() {

    /** Lo que no viene de los ajustes guardados: el borrado en curso y el mensaje pendiente. */
    private data class Transient(
        val isClearing: Boolean = false,
        val message: SettingsMessage? = null
    )

    private val transient = MutableStateFlow(Transient())

    val uiState: StateFlow<SettingsUiState> =
        combine(observeSettings(), transient) { settings, t ->
            SettingsUiState(
                radiusKm = settings.radiusKm,
                voiceEnabled = settings.voiceEnabled,
                themeMode = settings.themeMode,
                isClearing = t.isClearing,
                message = t.message
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.RadiusChanged ->
                viewModelScope.launch { updateSettings.setRadiusKm(action.km) }

            is SettingsAction.VoiceEnabledChanged ->
                viewModelScope.launch { updateSettings.setVoiceEnabled(action.enabled) }

            is SettingsAction.ThemeChanged ->
                viewModelScope.launch { updateSettings.setThemeMode(action.mode) }

            SettingsAction.ClearDataConfirmed -> clearData()

            SettingsAction.MessageShown -> transient.update { it.copy(message = null) }
        }
    }

    private fun clearData() {
        viewModelScope.launch {
            transient.update { it.copy(isClearing = true) }
            try {
                clearSavedData()
                transient.update {
                    it.copy(isClearing = false, message = SettingsMessage.DATA_CLEARED)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                transient.update {
                    it.copy(isClearing = false, message = SettingsMessage.CLEAR_FAILED)
                }
            }
        }
    }
}
