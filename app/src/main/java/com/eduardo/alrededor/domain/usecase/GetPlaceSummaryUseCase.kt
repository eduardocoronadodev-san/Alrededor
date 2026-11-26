package com.eduardo.alrededor.domain.usecase

import com.eduardo.alrededor.domain.model.Place
import com.eduardo.alrededor.domain.model.PlaceSummary
import com.eduardo.alrededor.domain.repository.PlaceDetailRepository
import javax.inject.Inject

class GetPlaceSummaryUseCase @Inject constructor(
    private val repository: PlaceDetailRepository
) {
    suspend operator fun invoke(place: Place): PlaceSummary = repository.getSummary(place)
}
