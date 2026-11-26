package com.eduardo.alrededor.data.repository

import com.eduardo.alrededor.data.local.PlacesLocalDataSource
import com.eduardo.alrededor.data.remote.PlacesRemoteDataSource
import com.eduardo.alrededor.domain.model.GeoPoint
import com.eduardo.alrededor.domain.model.PlacesResult
import com.eduardo.alrededor.domain.repository.PlacesRepository
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class PlacesRepositoryImpl @Inject constructor(
    private val remote: PlacesRemoteDataSource,
    private val local: PlacesLocalDataSource
) : PlacesRepository {

    override suspend fun getNearby(point: GeoPoint, radiusM: Int): PlacesResult =
        try {
            val places = remote.fetchNearby(point, radiusM)
            try {
                local.save(places)
            } catch (_: Exception) {
                // Si la BD local falla, se entregan los datos remotos igualmente.
            }
            PlacesResult(places, isFromCache = false)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            fallbackToCache(point, radiusM, e)
        }

    private suspend fun fallbackToCache(
        point: GeoPoint,
        radiusM: Int,
        cause: Exception
    ): PlacesResult {
        val cached = local.findNear(point, radiusM)
        if (cached.isEmpty()) throw cause
        return PlacesResult(cached, isFromCache = true)
    }
}
