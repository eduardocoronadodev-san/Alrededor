package com.eduardo.alrededor.domain.usecase


import com.eduardo.alrededor.domain.repository.LocationPermissionRepository
import javax.inject.Inject

// Registra que la app ya pidió el permiso al usuario.
class MarkLocationPermissionRequestedUseCase @Inject constructor(
    private val repository: LocationPermissionRepository
) {
    suspend operator fun invoke() = repository.markRequested()
}