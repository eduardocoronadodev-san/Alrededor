package com.eduardo.alrededor

import androidx.lifecycle.SavedStateHandle
import com.eduardo.alrededor.domain.model.GeoPoint
import com.eduardo.alrededor.domain.model.Place
import com.eduardo.alrededor.domain.model.PlaceSummary
import com.eduardo.alrededor.domain.model.SpeechState
import com.eduardo.alrededor.domain.repository.LocationRepository
import com.eduardo.alrededor.domain.repository.OrientationRepository
import com.eduardo.alrededor.domain.repository.PlaceDetailRepository
import com.eduardo.alrededor.domain.repository.SpeechController
import com.eduardo.alrededor.domain.usecase.CalculateProximityUseCase
import com.eduardo.alrededor.domain.usecase.GetPlaceSummaryUseCase
import com.eduardo.alrededor.domain.usecase.GetPlaceUseCase
import com.eduardo.alrededor.domain.usecase.ObserveSettingsUseCase
import com.eduardo.alrededor.domain.usecase.PrepareSpeechTextUseCase
import com.eduardo.alrededor.ui.detail.DetailAction
import com.eduardo.alrededor.ui.detail.DetailViewModel
import com.eduardo.alrededor.ui.detail.SummaryError
import com.eduardo.alrededor.ui.detail.SummaryUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

private class StubLocationRepository : LocationRepository {
    override fun locationUpdates(): Flow<GeoPoint> = emptyFlow()
}

private class StubOrientationRepository : OrientationRepository {
    override fun azimuthUpdates(): Flow<Float> = emptyFlow()
}

private class FakeDetailRepository(
    private val place: Place?,
    private val summary: PlaceSummary? = null,
    private val summaryError: Exception? = null
) : PlaceDetailRepository {
    override suspend fun getPlace(placeId: Long) = place
    override suspend fun getSummary(place: Place): PlaceSummary {
        summaryError?.let { throw it }
        return summary ?: PlaceSummary(place.id, null, 0)
    }
}

private class FakeSpeechController : SpeechController {
    private val _state = MutableStateFlow(SpeechState.IDLE)
    override val state: StateFlow<SpeechState> = _state
    var spoken: String? = null

    override fun speak(text: String) {
        spoken = text
        _state.value = SpeechState.SPEAKING
    }
    override fun stop() { _state.value = SpeechState.IDLE }
    override fun release() {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private val place = Place(
        id = 1, title = "Lugar 1", lat = 19.43, lon = -99.13,
        description = "desc", thumbnailUrl = null, articleUrl = "https://x/1", isRelevant = true
    )

    private fun viewModel(
        repository: PlaceDetailRepository,
        speech: SpeechController = FakeSpeechController()
    ) = DetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf("idPagina" to 1L)),
        getPlace = GetPlaceUseCase(repository),
        getSummary = GetPlaceSummaryUseCase(repository),
        calculateProximity = CalculateProximityUseCase(),
        prepareSpeechText = PrepareSpeechTextUseCase(),
        locationRepository = StubLocationRepository(),
        orientationRepository = StubOrientationRepository(),
        speech = speech,
        observeSettings = ObserveSettingsUseCase(FakeSettingsRepository())
    )

    @Test
    fun carga_el_lugar_y_su_resumen() = runTest(dispatcher) {
        val vm = viewModel(
            FakeDetailRepository(place, PlaceSummary(1, "Texto uno. Texto dos.", 0))
        )
        backgroundScope.launch(dispatcher) { vm.uiState.collect { } }

        val state = vm.uiState.value
        assertEquals("Lugar 1", state.place?.title)
        assertEquals(SummaryUiState.Loaded("Texto uno. Texto dos."), state.summary)
    }

    @Test
    fun si_el_lugar_no_existe_marca_no_encontrado() = runTest(dispatcher) {
        val vm = viewModel(FakeDetailRepository(place = null))
        backgroundScope.launch(dispatcher) { vm.uiState.collect { } }

        assertTrue(vm.uiState.value.notFound)
    }

    @Test
    fun si_falla_la_red_muestra_error_de_conexion_en_el_resumen() = runTest(dispatcher) {
        val vm = viewModel(FakeDetailRepository(place, summaryError = IOException("sin red")))
        backgroundScope.launch(dispatcher) { vm.uiState.collect { } }

        assertEquals(
            SummaryUiState.Failed(SummaryError.NO_CONNECTION),
            vm.uiState.value.summary
        )
    }

    @Test
    fun escuchar_lee_el_titulo_y_las_primeras_oraciones() = runTest(dispatcher) {
        val speech = FakeSpeechController()
        val vm = viewModel(
            FakeDetailRepository(place, PlaceSummary(1, "Texto uno. Texto dos.", 0)),
            speech
        )
        backgroundScope.launch(dispatcher) { vm.uiState.collect { } }

        vm.onAction(DetailAction.SpeakToggled)

        assertEquals("Lugar 1. Texto uno. Texto dos.", speech.spoken)
        assertEquals(SpeechState.SPEAKING, vm.uiState.value.speech)
    }

    @Test
    fun pulsar_escuchar_dos_veces_detiene_la_voz() = runTest(dispatcher) {
        val vm = viewModel(
            FakeDetailRepository(place, PlaceSummary(1, "Texto uno.", 0))
        )
        backgroundScope.launch(dispatcher) { vm.uiState.collect { } }

        vm.onAction(DetailAction.SpeakToggled)
        vm.onAction(DetailAction.SpeakToggled)

        assertEquals(SpeechState.IDLE, vm.uiState.value.speech)
    }
}
