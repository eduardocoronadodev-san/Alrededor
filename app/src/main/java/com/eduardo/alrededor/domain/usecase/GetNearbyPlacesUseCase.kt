package com.eduardo.alrededor.domain.usecase

import com.eduardo.alrededor.domain.model.ExploreDefaults
import com.eduardo.alrededor.domain.model.GeoPoint
import com.eduardo.alrededor.domain.model.PlacesResult
import com.eduardo.alrededor.domain.repository.PlacesRepository
import javax.inject.Inject

class GetNearbyPlacesUseCase @Inject constructor(
    private val repository: PlacesRepository
) {
    suspend operator fun invoke(
        point: GeoPoint,
        radiusM: Int = ExploreDefaults.RADIUS_M
    ): PlacesResult = repository.getNearby(point, radiusM)
}
