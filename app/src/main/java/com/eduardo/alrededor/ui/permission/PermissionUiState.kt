package com.eduardo.alrededor.ui.permission

import com.eduardo.alrededor.domain.model.LocationPermissionState

/** Estado único de la pantalla (fuente de verdad). */
data class PermissionUiState(
    val isLoading: Boolean = true,
    val permission: LocationPermissionState = LocationPermissionState.NotRequested
)

/** Todo lo que la pantalla le puede comunicar al ViewModel. */
sealed interface PermissionAction {
    /** El usuario pulsó "Permitir" o "Reintentar": se anota que ya se pidió. */
    data object RequestStarted : PermissionAction

    /** Volvió el resultado del diálogo del sistema o la pantalla se reanudó. */
    data class StatusRefreshed(val shouldShowRationale: Boolean) : PermissionAction
}
