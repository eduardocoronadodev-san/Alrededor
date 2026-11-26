package com.eduardo.alrededor.domain.usecase

import com.eduardo.alrededor.domain.model.ExploreDefaults
import com.eduardo.alrededor.domain.model.GeoPoint
import com.eduardo.alrededor.domain.util.GeoUtils
import javax.inject.Inject

/** Regla: volver a consultar solo si se forzó o si el usuario se alejó lo suficiente. */
class ShouldRefetchPlacesUseCase @Inject constructor() {
    operator fun invoke(lastQueryPoint: GeoPoint?, current: GeoPoint, force: Boolean): Boolean {
        if (force || lastQueryPoint == null) return true
        val moved = GeoUtils.distance(
            lastQueryPoint.lat, lastQueryPoint.lon, current.lat, current.lon
        )
        return moved > ExploreDefaults.REFETCH_DISTANCE_M
    }
}
