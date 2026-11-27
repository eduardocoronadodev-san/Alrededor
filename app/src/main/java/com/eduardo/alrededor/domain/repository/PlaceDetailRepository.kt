package com.eduardo.alrededor.domain.repository

import com.eduardo.alrededor.domain.model.Place
import com.eduardo.alrededor.domain.model.PlaceSummary

interface PlaceDetailRepository {
     suspend fun getPlace(placeId: Long): Place?
    suspend fun getSummary(place: Place): PlaceSummary
}
