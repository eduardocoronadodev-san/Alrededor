package com.eduardo.alrededor.ui.explore.map

import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.doOnLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.eduardo.alrededor.BuildConfig
import com.eduardo.alrededor.R
import com.eduardo.alrededor.domain.model.GeoPoint
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.util.GeoPoint as OsmGeoPoint

/** Lo mínimo que el mapa necesita saber de un lugar. Al ser data class, se compara por valor. */
data class MapPlace(
    val id: Long,
    val title: String,
    val lat: Double,
    val lon: Double
)

@Composable
fun OsmMap(
    userLocation: GeoPoint?,
    places: List<MapPlace>,
    selectedPlaceId: Long?,
    recenterTick: Int,
    onPlaceSelected: (Long) -> Unit,
    onMapTapped: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Los callbacks se leen siempre con su valor más reciente, sin recrear el mapa.
    val currentOnPlaceSelected by rememberUpdatedState(onPlaceSelected)
    val currentOnMapTapped by rememberUpdatedState(onMapTapped)

    val mapView = remember {
        MapView(context).apply {
            setTileSource(createTileSource())
            setMultiTouchControls(true)
            isTilesScaledToDpi = true
            minZoomLevel = 4.0
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(3.0)

            // Capa que detecta toques sobre el mapa vacío (para cerrar la tarjeta)
            overlays.add(
                MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: OsmGeoPoint?): Boolean {
                        currentOnMapTapped()
                        return true
                    }
                    override fun longPressHelper(p: OsmGeoPoint?): Boolean = false
                })
            )
            // Atribución: toma el texto de la fuente de mosaicos (Geoapify y OpenStreetMap)
            overlays.add(CopyrightOverlay(context))
        }
    }

    val userMarker = remember { createUserMarker(context, mapView) }

    // El MapView necesita enterarse del ciclo de vida para pausar y reanudar la carga de mosaicos.
    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onDetach()
        }
    }

    // Pines: solo se reconstruyen si cambian los lugares o el seleccionado.
    LaunchedEffect(places, selectedPlaceId) {
        mapView.syncPlaces(places, selectedPlaceId, userMarker) { id -> currentOnPlaceSelected(id) }
    }

    // Vista inicial mientras no hay ubicación: México.
    LaunchedEffect(Unit) {
        if (userLocation == null) {
            mapView.doOnLayout {
                mapView.controller.setZoom(5.0)
                mapView.controller.setCenter(OsmGeoPoint(23.6345, -102.5528))
            }
        }
    }

    // Tu posición: se mueve el punto azul; la primera vez, se centra el mapa.
    LaunchedEffect(userLocation) {
        val location = userLocation ?: return@LaunchedEffect
        val point = OsmGeoPoint(location.lat, location.lon)
        userMarker.position = point
        userMarker.isEnabled = true
        // El mapa solo acepta el centro y el zoom cuando ya tiene tamaño en pantalla.
        // doOnLayout espera a eso (o se ejecuta de inmediato si ya lo tiene).
        mapView.doOnLayout {
            mapView.controller.setZoom(15.0)
            mapView.controller.setCenter(point)
        }
        mapView.invalidate()
    }

    // Botón de centrar
    LaunchedEffect(recenterTick) {
        if (recenterTick > 0) {
            userLocation?.let {
                val point = OsmGeoPoint(it.lat, it.lon)
                mapView.post {
                    mapView.controller.setZoom(15.0)
                    mapView.controller.animateTo(point)
                }
            }
        }
    }

    AndroidView(factory = { mapView }, modifier = modifier)
}

/**
 * Geoapify siempre. Solo en builds de depuración y sin clave configurada (por ejemplo, un
 * compañero que aún no creó su local.properties) se usa OpenStreetMap para no dejar el mapa vacío.
 * En la versión publicada nunca se usan los mosaicos públicos de OpenStreetMap.
 */
private fun createTileSource(): ITileSource =
    if (BuildConfig.GEOAPIFY_API_KEY.isBlank() && BuildConfig.DEBUG) {
        TileSourceFactory.MAPNIK
    } else {
        GeoapifyTileSource(BuildConfig.GEOAPIFY_API_KEY)
    }

/** Punto azul con borde blanco; oculto hasta conocer la ubicación. */
private fun createUserMarker(context: Context, map: MapView): Marker =
    Marker(map).apply {
        icon = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(0xFF1A73E8.toInt())
            setStroke(5, 0xFFFFFFFF.toInt())
            setSize(44, 44)
        }
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        title = context.getString(R.string.mapa_mi_ubicacion)
        setOnMarkerClickListener { _, _ -> true }
        isEnabled = false
    }

/** Reemplaza todos los pines por los de la lista actual; tu posición queda encima. */
private fun MapView.syncPlaces(
    places: List<MapPlace>,
    selectedId: Long?,
    userMarker: Marker,
    onSelected: (Long) -> Unit
) {
    overlays.removeAll { it is Marker }
    places.forEach { place ->
        overlays.add(
            Marker(this).apply {
                position = OsmGeoPoint(place.lat, place.lon)
                title = place.title
                setAlpha(if (place.id == selectedId) 1f else 0.75f)
                setOnMarkerClickListener { _, _ ->
                    onSelected(place.id)
                    true // el toque se consume: no se abre la burbuja de información
                }
            }
        )
    }
    overlays.add(userMarker)
    invalidate()
}
