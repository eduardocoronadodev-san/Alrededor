package com.eduardo.alrededor.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduardo.alrededor.domain.model.GeoPoint
import com.eduardo.alrededor.domain.model.Place
import com.eduardo.alrededor.domain.model.SpeechState
import com.eduardo.alrededor.domain.repository.LocationRepository
import com.eduardo.alrededor.domain.repository.OrientationRepository
import com.eduardo.alrededor.domain.repository.SpeechController
import com.eduardo.alrededor.domain.usecase.CalculateProximityUseCase
import com.eduardo.alrededor.domain.usecase.GetPlaceSummaryUseCase
import com.eduardo.alrededor.domain.usecase.GetPlaceUseCase
import com.eduardo.alrededor.domain.usecase.ObserveSettingsUseCase
import com.eduardo.alrededor.domain.usecase.PrepareSpeechTextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPlace: GetPlaceUseCase,
    private val getSummary: GetPlaceSummaryUseCase,
    private val calculateProximity: CalculateProximityUseCase,
    private val prepareSpeechText: PrepareSpeechTextUseCase,
    private val locationRepository: LocationRepository,
    private val orientationRepository: OrientationRepository,
    private val speech: SpeechController,
    private val observeSettings: ObserveSettingsUseCase
) : ViewModel() {

    /** El argumento de la ruta "detalle/{idPagina}". */
    private val placeId: Long = savedStateHandle.get<Long>("idPagina")
        ?: savedStateHandle.get<String>("idPagina")?.toLongOrNull()
        ?: checkNotNull(savedStateHandle.get<Any>("idPagina")?.toString()?.toLongOrNull())

    private sealed interface PlaceLoad {
        data object Loading : PlaceLoad
        data class Found(val place: Place) : PlaceLoad
        data object NotFound : PlaceLoad
    }

    private val placeLoad = MutableStateFlow<PlaceLoad>(PlaceLoad.Loading)
    private val summary = MutableStateFlow<SummaryUiState>(SummaryUiState.Loading)
    private val location = MutableStateFlow<GeoPoint?>(null)
    private val azimuth = MutableStateFlow<Float?>(null)

    private var locationJob: Job? = null
    private var orientationJob: Job? = null

    private val speechAndVoice = combine(
        speech.state,
        observeSettings().map { it.voiceEnabled }
    ) { state, enabled -> state to enabled }

    val uiState: StateFlow<DetailUiState> =
        combine(placeLoad, summary, location, azimuth, speechAndVoice) { load, sum, loc, az, pair ->
            val sp = pair.first
            val voice = pair.second
            val place = (load as? PlaceLoad.Found)?.place
            val proximity = if (place != null && loc != null) {
                calculateProximity(place, loc, az)
            } else null

            DetailUiState(
                isLoadingPlace = load is PlaceLoad.Loading,
                notFound = load is PlaceLoad.NotFound,
                place = place?.toUi(),
                summary = sum,
                distanceM = proximity?.distanceM,
                arrowRotationDeg = proximity?.arrowRotationDeg,
                direction = proximity?.direction,
                speech = sp,
                voiceEnabled = voice
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUiState())

    init {
        // Primero el lugar (rápido, de la base local) y después el resumen (puede tardar).
        viewModelScope.launch {
            val place = getPlace(placeId)
            if (place == null) {
                placeLoad.value = PlaceLoad.NotFound
            } else {
                placeLoad.value = PlaceLoad.Found(place)
                loadSummary(place)
            }
        }
    }

    fun onAction(action: DetailAction) {
        when (action) {
            DetailAction.ScreenStarted -> {
                startLocationUpdates()
                startOrientationUpdates()
            }
            DetailAction.ScreenStopped -> {
                locationJob?.cancel()
                orientationJob?.cancel()
                speech.stop()
            }
            DetailAction.SpeakToggled -> toggleSpeech()
            DetailAction.RetrySummary -> {
                val place = (placeLoad.value as? PlaceLoad.Found)?.place ?: return
                viewModelScope.launch { loadSummary(place) }
            }
        }
    }

    private suspend fun loadSummary(place: Place) {
        summary.value = SummaryUiState.Loading
        summary.value = try {
            val result = getSummary(place)
            val text = result.extract
            if (text.isNullOrBlank()) SummaryUiState.Empty else SummaryUiState.Loaded(text)
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            SummaryUiState.Failed(SummaryError.NO_CONNECTION)
        } catch (e: Exception) {
            SummaryUiState.Failed(SummaryError.UNKNOWN)
        }
    }

    private fun toggleSpeech() {
        if (speech.state.value == SpeechState.SPEAKING) {
            speech.stop()
            return
        }
        val place = (placeLoad.value as? PlaceLoad.Found)?.place ?: return
        val loaded = summary.value as? SummaryUiState.Loaded ?: return
        speech.speak(prepareSpeechText(place.title, loaded.text))
    }

    private fun startLocationUpdates() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            try {
                locationRepository.locationUpdates().collect { location.value = it }
            } catch (e: SecurityException) {
                // Sin permiso no hay distancia; el resto de la ficha sigue funcionando.
            }
        }
    }

    private fun startOrientationUpdates() {
        orientationJob?.cancel()
        orientationJob = viewModelScope.launch {
            orientationRepository.azimuthUpdates().collect { azimuth.value = it }
        }
    }

    override fun onCleared() {
        speech.release()
    }
}

private fun Place.toUi() = PlaceDetailUi(
    id = id, title = title, description = description,
    thumbnailUrl = thumbnailUrl, articleUrl = articleUrl, lat = lat, lon = lon
)
