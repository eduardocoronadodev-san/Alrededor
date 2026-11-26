package com.eduardo.alrededor.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduardo.alrededor.domain.model.GeoPoint
import com.eduardo.alrededor.domain.model.NearbyPlace
import com.eduardo.alrededor.domain.model.Place
import com.eduardo.alrededor.domain.model.RelativeDirection
import com.eduardo.alrededor.domain.repository.LocationRepository
import com.eduardo.alrededor.domain.repository.OrientationRepository
import com.eduardo.alrededor.domain.model.ExploreDefaults
import com.eduardo.alrededor.domain.usecase.ComputeNearbyPlacesUseCase
import com.eduardo.alrededor.domain.usecase.GetNearbyPlacesUseCase
import com.eduardo.alrededor.domain.usecase.ObserveSettingsUseCase
import com.eduardo.alrededor.domain.usecase.ShouldRefetchPlacesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import retrofit2.HttpException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val orientationRepository: OrientationRepository,
    private val getNearbyPlaces: GetNearbyPlacesUseCase,
    private val shouldRefetch: ShouldRefetchPlacesUseCase,
    private val computeNearby: ComputeNearbyPlacesUseCase,
    private val observeSettings: ObserveSettingsUseCase
) : ViewModel() {

    /** Banderas internas; el estado que ve la pantalla se arma combinando todo. */
    private data class Core(
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val showAll: Boolean = false,
        val explorationMode: Boolean = false,
        val isFromCache: Boolean = false,
        val error: ExploreError? = null
    )

    private val core = MutableStateFlow(Core())
    private val places = MutableStateFlow<List<Place>>(emptyList())
    private val location = MutableStateFlow<GeoPoint?>(null)
    private val azimuth = MutableStateFlow<Float?>(null)

    private var lastQueryPoint: GeoPoint? = null
    private var radiusM = ExploreDefaults.RADIUS_M
    private var forceFetch = false
    private var isStarted = false
    private var locationJob: Job? = null
    private var orientationJob: Job? = null

    val uiState: StateFlow<ExploreUiState> =
        combine(core, places, location, azimuth) { c, all, loc, az ->
            val nearby = if (loc != null) computeNearby(all, loc, c.showAll) else emptyList()
            ExploreUiState(
                isLoading = c.isLoading,
                isRefreshing = c.isRefreshing,
                hasLocation = loc != null,
                places = nearby.map { it.toUi(az) },
                totalCount = all.size,
                showAll = c.showAll,
                explorationMode = c.explorationMode,
                isFromCache = c.isFromCache,
                error = c.error
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExploreUiState())

    fun onAction(action: ExploreAction) {
        when (action) {
            ExploreAction.ScreenStarted -> {
                isStarted = true
                startLocationUpdates()
                startOrientationUpdates()
            }
            ExploreAction.ScreenStopped -> {
                isStarted = false
                locationJob?.cancel()
                orientationJob?.cancel()
            }
            ExploreAction.RefreshClicked -> {
                forceFetch = true
                if (isStarted) startLocationUpdates()
            }
            is ExploreAction.ExplorationModeChanged -> {
                core.update { it.copy(explorationMode = action.enabled) }
                if (isStarted) startLocationUpdates()
            }
            is ExploreAction.ShowAllChanged ->
                core.update { it.copy(showAll = action.enabled) }
        }
    }

    // ---------- Ubicación y brújula ----------

    /** Modo exploración apagado: una sola posición. Encendido: sigue la posición. */
    private fun startLocationUpdates() {
        locationJob?.cancel()
        val continuous = core.value.explorationMode
        locationJob = viewModelScope.launch {
            try {
                applyCurrentRadius()
                val updates = locationRepository.locationUpdates()
                (if (continuous) updates else updates.take(1)).collect { onNewLocation(it) }
            } catch (e: SecurityException) {
                fail(ExploreError.NO_PERMISSION)
            }
        }
    }

    /** Si el radio de los ajustes cambió desde la última consulta, fuerza una nueva. */
    private suspend fun applyCurrentRadius() {
        val newRadius = observeSettings().first().radiusM
        if (newRadius != radiusM) {
            radiusM = newRadius
            forceFetch = true
        }
    }

    private fun startOrientationUpdates() {
        orientationJob?.cancel()
        orientationJob = viewModelScope.launch {
            orientationRepository.azimuthUpdates().collect { azimuth.value = it }
        }
    }

    private suspend fun onNewLocation(point: GeoPoint) {
        location.value = point
        if (shouldRefetch(lastQueryPoint, point, forceFetch)) {
            forceFetch = false
            fetch(point)
        }
    }


    private suspend fun fetch(point: GeoPoint) {
        // Se marca desde ya para no repetir peticiones en cadena si la consulta falla.
        lastQueryPoint = point
        core.update { it.copy(isRefreshing = true, error = null) }
        try {
            val result = getNearbyPlaces(point, radiusM)
            places.value = result.places
            core.update {
                it.copy(isLoading = false, isRefreshing = false, isFromCache = result.isFromCache)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            fail(ExploreError.NO_CONNECTION)
        } catch (e: HttpException) {
            fail(ExploreError.NO_CONNECTION)
        } catch (e: Exception) {
            fail(ExploreError.UNKNOWN)
        }
    }

    private fun fail(error: ExploreError) {
        core.update { it.copy(isLoading = false, isRefreshing = false, error = error) }
    }
}

/** Convierte un lugar del dominio en uno listo para dibujar, usando hacia dónde mira el teléfono. */
private fun NearbyPlace.toUi(azimuthDeg: Float?): PlaceUi {
    val relativeAngle = azimuthDeg?.let { (bearingDeg - it + 360.0) % 360.0 }
    return PlaceUi(
        id = place.id,
        title = place.title,
        description = place.description,
        thumbnailUrl = place.thumbnailUrl,
        distanceM = distanceM,
        arrowRotationDeg = relativeAngle?.toFloat(),
        direction = relativeAngle?.let { RelativeDirection.fromAngle(it) },
        lat = place.lat,
        lon = place.lon
    )
}
