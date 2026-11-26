package com.eduardo.alrededor.domain.usecase

import com.eduardo.alrededor.domain.model.LocationGrant
import com.eduardo.alrededor.domain.model.LocationPermissionState
import com.eduardo.alrededor.domain.repository.LocationPermissionRepository
import javax.inject.Inject

//Decide en qué estado está el permiso combinando lo que dice el sistema y lo que recuerda la app.
class ResolveLocationPermissionStateUseCase @Inject constructor(
    private val repository: LocationPermissionRepository
) {
    suspend operator fun invoke(shouldShowRationale: Boolean): LocationPermissionState =
        decide(
            grant = repository.currentGrant(),
            requestedBefore = repository.hasRequestedBefore(),
            shouldShowRationale = shouldShowRationale
        )

    companion object {
        /** Regla pura, sin dependencias: es lo más fácil de probar. */
        fun decide(
            grant: LocationGrant,
            requestedBefore: Boolean,
            shouldShowRationale: Boolean
        ): LocationPermissionState = when {
            grant == LocationGrant.PRECISE -> LocationPermissionState.GrantedPrecise
            grant == LocationGrant.APPROXIMATE -> LocationPermissionState.GrantedApproximate
            !requestedBefore -> LocationPermissionState.NotRequested
            shouldShowRationale -> LocationPermissionState.Denied
            else -> LocationPermissionState.PermanentlyDenied
        }
    }
}