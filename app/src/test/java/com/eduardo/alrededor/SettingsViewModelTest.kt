package com.eduardo.alrededor

import com.eduardo.alrededor.domain.model.ThemeMode
import com.eduardo.alrededor.domain.usecase.ClearSavedDataUseCase
import com.eduardo.alrededor.domain.usecase.ObserveSettingsUseCase
import com.eduardo.alrededor.domain.usecase.UpdateSettingsUseCase
import com.eduardo.alrededor.ui.settings.SettingsAction
import com.eduardo.alrededor.ui.settings.SettingsMessage
import com.eduardo.alrededor.ui.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(
        settings: FakeSettingsRepository = FakeSettingsRepository(),
        savedData: FakeSavedDataRepository = FakeSavedDataRepository()
    ) = SettingsViewModel(
        observeSettings = ObserveSettingsUseCase(settings),
        updateSettings = UpdateSettingsUseCase(settings),
        clearSavedData = ClearSavedDataUseCase(savedData)
    )

    @Test
    fun cambiar_el_radio_actualiza_el_estado() = runTest(dispatcher) {
        val vm = viewModel()
        backgroundScope.launch(dispatcher) { vm.uiState.collect { } }

        vm.onAction(SettingsAction.RadiusChanged(5))

        assertEquals(5, vm.uiState.value.radiusKm)
    }

    @Test
    fun cambiar_la_voz_y_el_tema_actualiza_el_estado() = runTest(dispatcher) {
        val vm = viewModel()
        backgroundScope.launch(dispatcher) { vm.uiState.collect { } }

        vm.onAction(SettingsAction.VoiceEnabledChanged(false))
        vm.onAction(SettingsAction.ThemeChanged(ThemeMode.DARK))

        assertFalse(vm.uiState.value.voiceEnabled)
        assertEquals(ThemeMode.DARK, vm.uiState.value.themeMode)
    }

    @Test
    fun borrar_los_datos_avisa_con_un_mensaje() = runTest(dispatcher) {
        val savedData = FakeSavedDataRepository()
        val vm = viewModel(savedData = savedData)
        backgroundScope.launch(dispatcher) { vm.uiState.collect { } }

        vm.onAction(SettingsAction.ClearDataConfirmed)

        assertTrue(savedData.cleared)
        assertEquals(SettingsMessage.DATA_CLEARED, vm.uiState.value.message)
        assertFalse(vm.uiState.value.isClearing)
    }

    @Test
    fun si_el_borrado_falla_avisa_del_error() = runTest(dispatcher) {
        val vm = viewModel(savedData = FakeSavedDataRepository(fail = true))
        backgroundScope.launch(dispatcher) { vm.uiState.collect { } }

        vm.onAction(SettingsAction.ClearDataConfirmed)

        assertEquals(SettingsMessage.CLEAR_FAILED, vm.uiState.value.message)
    }

    @Test
    fun el_mensaje_se_consume_una_sola_vez() = runTest(dispatcher) {
        val vm = viewModel()
        backgroundScope.launch(dispatcher) { vm.uiState.collect { } }
        vm.onAction(SettingsAction.ClearDataConfirmed)

        vm.onAction(SettingsAction.MessageShown)

        assertNull(vm.uiState.value.message)
    }
}
