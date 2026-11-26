package com.eduardo.alrededor.data.repository

import android.annotation.SuppressLint
import android.os.Looper
import com.eduardo.alrededor.domain.model.ExploreDefaults
import com.eduardo.alrededor.domain.model.GeoPoint
import com.eduardo.alrededor.domain.repository.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class FusedLocationRepository @Inject constructor(
    private val client: FusedLocationProviderClient
) : LocationRepository {

    // El permiso se garantiza antes de llegar a la pantalla Explorar (pantalla 1).
    @SuppressLint("MissingPermission")
    override fun locationUpdates(): Flow<GeoPoint> = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(GeoPoint(it.latitude, it.longitude)) }
            }
        }

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            ExploreDefaults.LOCATION_INTERVAL_MS
        )
            .setMinUpdateDistanceMeters(ExploreDefaults.LOCATION_MIN_DISTANCE_M)
            .setWaitForAccurateLocation(false)
            .build()

        client.requestLocationUpdates(request, callback, Looper.getMainLooper())

        // Al cancelar la recolección se apaga el GPS.
        awaitClose { client.removeLocationUpdates(callback) }
    }
}
