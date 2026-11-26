package com.eduardo.alrededor.ui.explore

import com.eduardo.alrededor.domain.model.GeoPoint
import com.eduardo.alrededor.domain.model.RelativeDirection

data class PlaceUi(
    val id: Long,
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    val distanceM: Double,

    val arrowRotationDeg: Float?,
    val direction: RelativeDirection?,
    val lat: Double = 0.0,
    val lon: Double = 0.0
)

enum class ExploreError { NO_CONNECTION, NO_PERMISSION, UNKNOWN }

data class ExploreUiState(

    val isLoading: Boolean = true,

    val isRefreshing: Boolean = false,
    val hasLocation: Boolean = false,
    val userLocation: GeoPoint? = null,
    val places: List<PlaceUi> = emptyList(),
    val totalCount: Int = 0,
    val showAll: Boolean = false,
    val explorationMode: Boolean = false,
    val isFromCache: Boolean = false,
    val error: ExploreError? = null
)

sealed interface ExploreAction {
    data object ScreenStarted : ExploreAction
    data object ScreenStopped : ExploreAction
    data object RefreshClicked : ExploreAction
    data class ExplorationModeChanged(val enabled: Boolean) : ExploreAction
    data class ShowAllChanged(val enabled: Boolean) : ExploreAction
}
