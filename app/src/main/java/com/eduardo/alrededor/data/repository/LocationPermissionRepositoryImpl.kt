package com.eduardo.alrededor.data.repository

import com.eduardo.alrededor.data.local.PermissionPreferencesDataSource
import com.eduardo.alrededor.data.system.SystemLocationPermissionDataSource
import com.eduardo.alrededor.domain.model.LocationGrant
import com.eduardo.alrededor.domain.repository.LocationPermissionRepository
import javax.inject.Inject

/** Repositorio: une dos fuentes de datos y las oculta detrás de una sola interfaz. */
class LocationPermissionRepositoryImpl @Inject constructor(
    private val system: SystemLocationPermissionDataSource,
    private val preferences: PermissionPreferencesDataSource
) : LocationPermissionRepository {

    override fun currentGrant(): LocationGrant = system.currentGrant()

    override suspend fun hasRequestedBefore(): Boolean = preferences.hasRequestedBefore()

    override suspend fun markRequested() = preferences.markRequested()
}