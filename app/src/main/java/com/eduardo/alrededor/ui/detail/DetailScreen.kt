package com.eduardo.alrededor.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.eduardo.alrededor.R
import com.eduardo.alrededor.domain.model.RelativeDirection
import com.eduardo.alrededor.domain.model.SpeechState
import com.eduardo.alrededor.ui.explore.formatDistance
import com.eduardo.alrededor.ui.explore.labelRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    state: DetailUiState,
    onAction: (DetailAction) -> Unit,
    onBack: () -> Unit,
    onOpenArticle: (String) -> Unit,
    onDirections: (PlaceDetailUi) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.place?.title.orEmpty(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.detalle_volver)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            val place = state.place
            when {
                state.isLoadingPlace ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                state.notFound || place == null ->
                    Text(
                        text = stringResource(R.string.detalle_no_encontrado),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center).padding(24.dp)
                    )

                else -> DetailContent(state, place, onAction, onOpenArticle, onDirections)
            }
        }
    }
}

@Composable
private fun DetailContent(
    state: DetailUiState,
    place: PlaceDetailUi,
    onAction: (DetailAction) -> Unit,
    onOpenArticle: (String) -> Unit,
    onDirections: (PlaceDetailUi) -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        DetailPhoto(place.thumbnailUrl)

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = place.title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics { heading() }
            )
            place.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ProximityChips(state)

            ActionButtons(
                state = state,
                onSpeakClick = { onAction(DetailAction.SpeakToggled) },
                onDirectionsClick = { onDirections(place) }
            )

            SummarySection(state.summary, onRetry = { onAction(DetailAction.RetrySummary) })

            TextButton(onClick = { onOpenArticle(place.articleUrl) }) {
                Text(stringResource(R.string.detalle_ver_articulo))
            }
            Text(
                text = stringResource(R.string.detalle_atribucion),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DetailPhoto(url: String?) {
    val photoModifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
    if (url != null) {
        AsyncImage(
            model = url,
            contentDescription = null, // decorativa: el título ya se lee en la pantalla
            contentScale = ContentScale.Crop,
            modifier = photoModifier
        )
    } else {
        Box(
            modifier = photoModifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProximityChips(state: DetailUiState) {
    val distance = state.distanceM ?: return
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        InfoChip {
            Text(formatDistance(distance), style = MaterialTheme.typography.labelLarge)
        }
        val rotation = state.arrowRotationDeg
        val direction = state.direction
        if (rotation != null && direction != null) {
            InfoChip {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp).rotate(rotation - 90f)
                )
                Text(
                    stringResource(direction.labelRes()),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

/** Etiqueta informativa (no se toca). */
@Composable
private fun InfoChip(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) { content() }
    }
}

@Composable
private fun ActionButtons(
    state: DetailUiState,
    onSpeakClick: () -> Unit,
    onDirectionsClick: () -> Unit
) {
    val canSpeak = state.summary is SummaryUiState.Loaded &&
        state.speech != SpeechState.UNAVAILABLE
    val speaking = state.speech == SpeechState.SPEAKING

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.voiceEnabled) {
                FilledTonalButton(
                    onClick = onSpeakClick,
                    enabled = canSpeak,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (speaking) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(
                            if (speaking) R.string.detalle_detener else R.string.detalle_escuchar
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            OutlinedButton(onClick = onDirectionsClick, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.detalle_como_llegar),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        if (state.voiceEnabled && state.speech == SpeechState.UNAVAILABLE) {
            Text(
                text = stringResource(R.string.detalle_sin_voz),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun SummarySection(summary: SummaryUiState, onRetry: () -> Unit) {
    when (summary) {
        SummaryUiState.Loading -> Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            Text(
                stringResource(R.string.detalle_cargando_resumen),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        is SummaryUiState.Loaded ->
            Text(summary.text, style = MaterialTheme.typography.bodyLarge)

        SummaryUiState.Empty ->
            Text(
                stringResource(R.string.detalle_sin_resumen),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

        is SummaryUiState.Failed -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(
                    when (summary.error) {
                        SummaryError.NO_CONNECTION -> R.string.detalle_error_resumen_sin_conexion
                        SummaryError.UNKNOWN -> R.string.detalle_error_resumen
                    }
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onRetry) { Text(stringResource(R.string.explorar_reintentar)) }
        }
    }
}

// ---------- Vistas previas ----------

private val previewPlace = PlaceDetailUi(
    id = 1,
    title = "Catedral de San Buenaventura",
    description = "templo católico",
    thumbnailUrl = null,
    articleUrl = "https://es.wikipedia.org/wiki/Catedral_de_San_Buenaventura",
    lat = 19.6689,
    lon = -99.1787
)

@Preview(showBackground = true)
@Composable
private fun PreviewConResumen() = MaterialTheme {
    DetailScreen(
        state = DetailUiState(
            isLoadingPlace = false,
            place = previewPlace,
            summary = SummaryUiState.Loaded(
                "La Catedral de San Buenaventura es un templo católico, parte del conjunto conventual " +
                    "del mismo nombre. Fue designada catedral en 1979."
            ),
            distanceM = 1840.0,
            arrowRotationDeg = 20f,
            direction = RelativeDirection.FRONT
        ),
        onAction = {}, onBack = {}, onOpenArticle = {}, onDirections = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewCargandoResumen() = MaterialTheme {
    DetailScreen(
        state = DetailUiState(isLoadingPlace = false, place = previewPlace),
        onAction = {}, onBack = {}, onOpenArticle = {}, onDirections = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewErrorResumen() = MaterialTheme {
    DetailScreen(
        state = DetailUiState(
            isLoadingPlace = false,
            place = previewPlace,
            summary = SummaryUiState.Failed(SummaryError.NO_CONNECTION)
        ),
        onAction = {}, onBack = {}, onOpenArticle = {}, onDirections = {}
    )
}
