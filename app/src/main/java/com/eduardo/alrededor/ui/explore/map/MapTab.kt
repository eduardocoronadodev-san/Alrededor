package com.eduardo.alrededor.ui.explore.map

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eduardo.alrededor.R
import com.eduardo.alrededor.ui.explore.ExploreAction
import com.eduardo.alrededor.ui.explore.ExploreUiState
import com.eduardo.alrededor.ui.explore.PlaceThumbnail
import com.eduardo.alrededor.ui.explore.PlaceUi
import com.eduardo.alrededor.ui.explore.StatusBanner
import com.eduardo.alrededor.ui.explore.ToggleChip
import com.eduardo.alrededor.ui.explore.formatDistance
import com.eduardo.alrededor.ui.explore.labelRes

@Composable
fun MapTab(
    state: ExploreUiState,
    onAction: (ExploreAction) -> Unit,
    onPlaceClick: (Long) -> Unit
) {
    // Qué pin está seleccionado y cuándo centrar son estado de la interfaz, no de negocio.
    var selectedId by remember { mutableStateOf<Long?>(null) }
    var recenterTick by remember { mutableIntStateOf(0) }

    val mapPlaces = state.places.map { MapPlace(it.id, it.title, it.lat, it.lon) }
    val selected = state.places.firstOrNull { it.id == selectedId }

    Box(Modifier.fillMaxSize()) {

        OsmMap(
            userLocation = state.userLocation,
            places = mapPlaces,
            selectedPlaceId = selectedId,
            recenterTick = recenterTick,
            onPlaceSelected = { selectedId = it },
            onMapTapped = { selectedId = null },
            modifier = Modifier.fillMaxSize()
        )

        // Arriba: chips y avisos
        Column(Modifier.align(Alignment.TopStart).fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ToggleChip(
                    text = stringResource(R.string.explorar_modo_exploracion),
                    selected = state.explorationMode,
                    onClick = { onAction(ExploreAction.ExplorationModeChanged(!state.explorationMode)) }
                )
                ToggleChip(
                    text = stringResource(R.string.explorar_mostrar_todo),
                    selected = state.showAll,
                    onClick = { onAction(ExploreAction.ShowAllChanged(!state.showAll)) }
                )
            }

            when {
                state.isFromCache -> StatusBanner(R.string.explorar_aviso_cache)
                state.error != null -> StatusBanner(R.string.explorar_aviso_error_actualizar)
            }

            when {
                !state.hasLocation && state.error == null ->
                    MapMessage(R.string.mapa_buscando_ubicacion)

                state.hasLocation && !state.isLoading &&
                    state.error == null && state.places.isEmpty() ->
                    MapMessage(R.string.mapa_sin_lugares)
            }
        }

        // Abajo: botón de centrar y tarjeta del lugar seleccionado
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SmallFloatingActionButton(
                onClick = {
                    if (state.userLocation == null) {
                        onAction(ExploreAction.RefreshClicked)
                    }
                    recenterTick++
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = stringResource(R.string.mapa_centrar)
                )
            }
            if (selected != null) {
                SelectedPlaceCard(selected, onOpen = { onPlaceClick(selected.id) })
            }
        }
    }
}

@Composable
private fun SelectedPlaceCard(place: PlaceUi, onOpen: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlaceThumbnail(place.thumbnailUrl)

            Column(Modifier.weight(1f)) {
                Text(
                    text = place.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val directionText = place.direction?.let { stringResource(it.labelRes()) }
                Text(
                    text = listOfNotNull(formatDistance(place.distanceM), directionText)
                        .joinToString(" · "),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Button(onClick = onOpen) { Text(stringResource(R.string.mapa_ver_ficha)) }
        }
    }
}

@Composable
private fun MapMessage(@StringRes text: Int) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = stringResource(text),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}
