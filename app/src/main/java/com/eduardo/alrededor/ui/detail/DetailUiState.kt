package com.eduardo.alrededor.ui.detail

import com.eduardo.alrededor.domain.model.RelativeDirection
import com.eduardo.alrededor.domain.model.SpeechState

/** El lugar listo para dibujar. */
data class PlaceDetailUi(
    val id: Long,
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    val articleUrl: String,
    val lat: Double,
    val lon: Double
)

enum class SummaryError { NO_CONNECTION, UNKNOWN }

sealed interface SummaryUiState {
    data object Loading : SummaryUiState
    data class Loaded(val text: String) : SummaryUiState
    data object Empty : SummaryUiState
    data class Failed(val error: SummaryError) : SummaryUiState
}

/** Estado único de la pantalla. */
data class DetailUiState(
    val isLoadingPlace: Boolean = true,
    val notFound: Boolean = false,
    val place: PlaceDetailUi? = null,
    val summary: SummaryUiState = SummaryUiState.Loading,
    val distanceM: Double? = null,
    val arrowRotationDeg: Float? = null,
    val direction: RelativeDirection? = null,
    val speech: SpeechState = SpeechState.IDLE,
    val voiceEnabled: Boolean = true
)

/** Todo lo que la pantalla le puede comunicar al ViewModel. */
sealed interface DetailAction {
    data object ScreenStarted : DetailAction
    data object ScreenStopped : DetailAction
    data object SpeakToggled : DetailAction
    data object RetrySummary : DetailAction
}
