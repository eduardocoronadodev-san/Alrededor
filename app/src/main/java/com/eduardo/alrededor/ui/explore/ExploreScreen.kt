package com.eduardo.alrededor.ui.explore

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eduardo.alrededor.R
import com.eduardo.alrededor.domain.model.RelativeDirection

private enum class ExploreTab(@StringRes val label: Int, val icon: ImageVector) {
    LIST(R.string.tab_lista, Icons.AutoMirrored.Filled.List),
    MAP(R.string.tab_mapa, Icons.Default.Place)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    state: ExploreUiState,
    onAction: (ExploreAction) -> Unit,
    onPlaceClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // La pestaña elegida es estado de la propia interfaz, no de negocio.
    var selectedTab by rememberSaveable { mutableStateOf(ExploreTab.LIST) }

    KeepScreenOn(enabled = state.explorationMode)

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.explorar_titulo),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = { onAction(ExploreAction.RefreshClicked) }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.explorar_actualizar)
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(R.string.explorar_ajustes)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                ExploreTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = tab == selectedTab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResource(tab.label)) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (state.isRefreshing) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            when (selectedTab) {
                ExploreTab.LIST -> ListTab(state, onAction, onPlaceClick)
                ExploreTab.MAP -> com.eduardo.alrededor.ui.explore.map.MapTab(state, onAction, onPlaceClick)
            }
        }
    }
}

// ---------- Pestaña Lista ----------

@Composable
private fun ListTab(
    state: ExploreUiState,
    onAction: (ExploreAction) -> Unit,
    onPlaceClick: (Long) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        ExploreControls(state, onAction)

        when {
            !state.hasLocation && state.error == null ->
                CenteredMessage(R.string.explorar_buscando_ubicacion, showProgress = true)

            state.isLoading && state.error == null ->
                CenteredMessage(R.string.explorar_cargando, showProgress = true)

            state.places.isEmpty() && state.error != null ->
                ErrorState(state.error, onRetry = { onAction(ExploreAction.RefreshClicked) })

            state.places.isEmpty() ->
                EmptyState(
                    canShowAll = !state.showAll,
                    onShowAll = { onAction(ExploreAction.ShowAllChanged(true)) },
                    onRetry = { onAction(ExploreAction.RefreshClicked) }
                )

            else -> {
                if (state.isFromCache) StatusBanner(R.string.explorar_aviso_cache)
                else if (state.error != null) StatusBanner(R.string.explorar_aviso_error_actualizar)

                PlaceList(state.places, onPlaceClick)
            }
        }
    }
}

@Composable
private fun ExploreControls(state: ExploreUiState, onAction: (ExploreAction) -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ToggleChip(
                text = stringResource(R.string.explorar_modo_exploracion),
                selected = state.explorationMode,
                onClick = {
                    onAction(ExploreAction.ExplorationModeChanged(!state.explorationMode))
                }
            )
            ToggleChip(
                text = stringResource(R.string.explorar_mostrar_todo),
                selected = state.showAll,
                onClick = { onAction(ExploreAction.ShowAllChanged(!state.showAll)) }
            )
        }
        if (state.totalCount > 0) {
            Text(
                text = stringResource(R.string.explorar_contador, state.places.size, state.totalCount),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlaceList(places: List<PlaceUi>, onPlaceClick: (Long) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = places, key = { it.id }) { place ->
            PlaceRow(
                place = place,
                onClick = { onPlaceClick(place.id) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

// ---------- Estados ----------

@Composable
private fun CenteredMessage(@StringRes text: Int, showProgress: Boolean = false) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showProgress) CircularProgressIndicator()
            Text(
                text = stringResource(text),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyState(canShowAll: Boolean, onShowAll: () -> Unit, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.explorar_vacio_titulo),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Text(
                stringResource(R.string.explorar_vacio_mensaje),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            if (canShowAll) {
                Button(onClick = onShowAll) { Text(stringResource(R.string.explorar_mostrar_todo)) }
            }
            TextButton(onClick = onRetry) { Text(stringResource(R.string.explorar_reintentar)) }
        }
    }
}

@Composable
private fun ErrorState(error: ExploreError, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(error.messageRes()),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry) { Text(stringResource(R.string.explorar_reintentar)) }
        }
    }
}

@StringRes
private fun ExploreError.messageRes(): Int = when (this) {
    ExploreError.NO_CONNECTION -> R.string.explorar_error_sin_conexion
    ExploreError.NO_PERMISSION -> R.string.explorar_error_sin_permiso
    ExploreError.UNKNOWN -> R.string.explorar_error_desconocido
}

@Composable
internal fun StatusBanner(@StringRes text: Int) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Warning, contentDescription = null)
            Text(stringResource(text), style = MaterialTheme.typography.bodySmall)
        }
    }
}

// ---------- Pantalla siempre encendida en modo exploración ----------

@Composable
private fun KeepScreenOn(enabled: Boolean) {
    val view = LocalView.current
    DisposableEffect(enabled) {
        view.keepScreenOn = enabled
        onDispose { view.keepScreenOn = false }
    }
}

// ---------- Vistas previas ----------

private val previewPlaces = listOf(
    PlaceUi(1, "Catedral de San Buenaventura", "templo católico", null, 1840.0, 20f, RelativeDirection.FRONT),
    PlaceUi(2, "Facultad de Estudios Superiores Cuautitlán", null, null, 3200.0, 280f, RelativeDirection.LEFT),
    PlaceUi(3, "Estadio Los Pinos", "estadio de fútbol", null, 240.0, 100f, RelativeDirection.RIGHT)
)

@Preview(showBackground = true)
@Composable
private fun PreviewLista() = MaterialTheme {
    ExploreScreen(
        state = ExploreUiState(isLoading = false, hasLocation = true, places = previewPlaces, totalCount = 50),
        onAction = {}, onPlaceClick = {}, onSettingsClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewSinConexion() = MaterialTheme {
    ExploreScreen(
        state = ExploreUiState(
            isLoading = false, hasLocation = true, places = previewPlaces,
            totalCount = 50, isFromCache = true
        ),
        onAction = {}, onPlaceClick = {}, onSettingsClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewBuscandoUbicacion() = MaterialTheme {
    ExploreScreen(
        state = ExploreUiState(),
        onAction = {}, onPlaceClick = {}, onSettingsClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewError() = MaterialTheme {
    ExploreScreen(
        state = ExploreUiState(
            isLoading = false, hasLocation = true, error = ExploreError.NO_CONNECTION
        ),
        onAction = {}, onPlaceClick = {}, onSettingsClick = {}
    )
}
