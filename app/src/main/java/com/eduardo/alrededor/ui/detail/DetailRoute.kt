package com.eduardo.alrededor.ui.detail

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eduardo.alrededor.R

@Composable
fun DetailRoute(
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Ubicación, brújula y voz solo funcionan mientras la pantalla está visible.
    LifecycleStartEffect(Unit) {
        viewModel.onAction(DetailAction.ScreenStarted)
        onStopOrDispose { viewModel.onAction(DetailAction.ScreenStopped) }
    }

    DetailScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack = onBack,
        onOpenArticle = { url: String -> context.openUrl(url) },
        onDirections = { place: PlaceDetailUi ->
            if (!context.openWalkingDirections(place.lat, place.lon, place.title)) {
                Toast.makeText(context, R.string.detalle_sin_app_mapas, Toast.LENGTH_SHORT).show()
            }
        }
    )
}

// ---------- Intents (necesitan un Context, por eso viven en la capa de UI) ----------

private fun Context.openUrl(url: String) {
    try {
        startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (e: ActivityNotFoundException) {
        // Sin navegador instalado: no hay nada que hacer
    }
}

/** Intenta Google Maps en modo a pie; si no está, cualquier app que entienda "geo:". */
private fun Context.openWalkingDirections(lat: Double, lon: Double, title: String): Boolean {
    val navigation = Intent(
        Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$lat,$lon&mode=w")
    ).setPackage("com.google.android.apps.maps")

    val geo = Intent(
        Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lon?q=$lat,$lon(${Uri.encode(title)})")
    )

    return listOf(navigation, geo).any { intent ->
        try {
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
