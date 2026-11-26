package com.eduardo.alrededor.domain.model

/** Estado del permiso tal como lo entiende la app (lo que la pantalla necesita saber). */
sealed interface LocationPermissionState {
    data object NotRequested : LocationPermissionState
    data object Denied : LocationPermissionState
    data object PermanentlyDenied : LocationPermissionState
    data object GrantedApproximate : LocationPermissionState
    data object GrantedPrecise : LocationPermissionState
}