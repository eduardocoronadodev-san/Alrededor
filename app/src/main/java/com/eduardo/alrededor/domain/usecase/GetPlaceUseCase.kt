package com.eduardo.alrededor.domain.usecase

import com.eduardo.alrededor.domain.model.Place
import com.eduardo.alrededor.domain.repository.PlaceDetailRepository
import javax.inject.Inject

class GetPlaceUseCase @Inject constructor(
    private val repository: PlaceDetailRepository
) {
    suspend operator fun invoke(placeId: Long): Place? = repository.getPlace(placeId)
}
