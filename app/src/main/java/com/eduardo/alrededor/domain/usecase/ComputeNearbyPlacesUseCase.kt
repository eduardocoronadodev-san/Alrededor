package com.eduardo.alrededor.domain.usecase

import com.eduardo.alrededor.domain.model.ExploreDefaults
import com.eduardo.alrededor.domain.model.GeoPoint
import com.eduardo.alrededor.domain.model.NearbyPlace
import com.eduardo.alrededor.domain.model.Place
import com.eduardo.alrededor.domain.util.GeoUtils
import javax.inject.Inject

/**
 * Calcula distancia y rumbo, ordena y filtra. Es lógica pura: no depende de Android.
 */
class ComputeNearbyPlacesUseCase @Inject constructor() {

    operator fun invoke(places: List<Place>, point: GeoPoint, showAll: Boolean): List<NearbyPlace> {
        val all = places
            .map { place ->
                NearbyPlace(
                    place = place,
                    distanceM = GeoUtils.distance(point.lat, point.lon, place.lat, place.lon),
                    bearingDeg = GeoUtils.bearing(point.lat, point.lon, place.lat, place.lon)
                )
            }
            .sortedBy { it.distanceM }

        if (showAll) return all

        val relevant = all.filter { it.place.isRelevant }

        // Si quedan muy pocos, se relaja el filtro con los no relevantes más cercanos.
        val selected = if (relevant.size >= ExploreDefaults.MIN_RELEVANT_ITEMS) {
            relevant
        } else {
            val missing = ExploreDefaults.MIN_RELEVANT_ITEMS - relevant.size
            relevant + all.filterNot { it.place.isRelevant }.take(missing)
        }

        return selected
            .sortedBy { it.distanceM }
            .take(ExploreDefaults.MAX_RELEVANT_ITEMS)
    }
}
