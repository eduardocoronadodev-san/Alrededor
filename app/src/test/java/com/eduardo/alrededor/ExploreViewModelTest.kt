package com.eduardo.alrededor

import com.eduardo.alrededor.domain.model.GeoPoint
import com.eduardo.alrededor.domain.model.Place
import com.eduardo.alrededor.domain.model.PlacesResult
import com.eduardo.alrededor.domain.repository.LocationRepository
import com.eduardo.alrededor.domain.repository.OrientationRepository
import com.eduardo.alrededor.domain.repository.PlacesRepository
import com.eduardo.alrededor.domain.usecase.ComputeNearbyPlacesUseCase
import com.eduardo.alrededor.domain.usecase.GetNearbyPlacesUseCase
import com.eduardo.alrededor.domain.usecase.ObserveSettingsUseCase
import com.eduardo.alrededor.domain.usecase.ShouldRefetchPlacesUseCase
import com.eduardo.alrededor.ui.explore.ExploreAction
import com.eduardo.alrededor.ui.explore.ExploreError
import com.eduardo.alrededor.ui.explore.ExploreViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.io.IOException

private class FakeLocationRepository(private val point: GeoPoint) : LocationRepository {
    override fun locationUpdates(): Flow<GeoPoint> = flowOf(point)
}

private class FakeOrientationRepository : OrientationRepository {
    override fun azimuthUpdates(): Flow<Float> = emptyFlow()
}

private class FakePlacesRepository(
    private val places: List<Place> = emptyList(),
    private val failWith: Exception? = null
) : PlacesRepository {
    override suspend fun getNearby(point: GeoPoint, radiusM: Int): PlacesResult {
        failWith?.let { throw it }
        return PlacesResult(places, isFromCache = false)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val zocalo = GeoPoint(19.4326, -99.1332)

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun place(id: Long, lat: Double, lon: Double) = Place(
        id = id, title = "Lugar $id", lat = lat, lon = lon,
        description = null, thumbnailUrl = null, articleUrl = "https://x/$id", isRelevant = true
    )

    private fun viewModel(placesRepository: PlacesRepository) = ExploreViewModel(
        locationRepository = FakeLocationRepository(zocalo),
        orientationRepository = FakeOrientationRepository(),
        getNearbyPlaces = GetNearbyPlacesUseCase(placesRepository),
        shouldRefetch = ShouldRefetchPlacesUseCase(),
        computeNearby = ComputeNearbyPlacesUseCase(),
        observeSettings = ObserveSettingsUseCase(FakeSettingsRepository())
    )

    @Test
    fun al_iniciar_muestra_los_lugares_ordenados_por_distancia() = runTest(dispatcher) {
        val cercano = place(1, 19.4330, -99.1330)
        val lejano = place(2, 19.5000, -99.2000)
        val vm = viewModel(FakePlacesRepository(listOf(lejano, cercano)))
        backgroundScope.launch(dispatcher) { vm.uiState.collect { } }

        vm.onAction(ExploreAction.ScreenStarted)

        val state = vm.uiState.value
        assertEquals(listOf(1L, 2L), state.places.map { it.id })
        assertFalse(state.isLoading)
        assertEquals(null, state.error)
    }

    @Test
    fun si_falla_la_red_y_no_hay_nada_guardado_muestra_error_de_conexion() = runTest(dispatcher) {
        val vm = viewModel(FakePlacesRepository(failWith = IOException("sin red")))
        backgroundScope.launch(dispatcher) { vm.uiState.collect { } }

        vm.onAction(ExploreAction.ScreenStarted)

        val state = vm.uiState.value
        assertEquals(ExploreError.NO_CONNECTION, state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun mostrar_todo_cambia_el_estado() = runTest(dispatcher) {
        val vm = viewModel(FakePlacesRepository())
        backgroundScope.launch(dispatcher) { vm.uiState.collect { } }

        vm.onAction(ExploreAction.ShowAllChanged(true))

        assertEquals(true, vm.uiState.value.showAll)
    }
}
