package com.eduardo.alrededor.data.system

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.eduardo.alrededor.domain.model.LocationGrant
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

// Fuente de datos: lo que el sistema Android dice sobre el permiso
class SystemLocationPermissionDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun currentGrant(): LocationGrant {
        val fine = isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        return when {
            fine -> LocationGrant.PRECISE
            coarse -> LocationGrant.APPROXIMATE
            else -> LocationGrant.NONE
        }
    }

    private fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}