package com.eduardo.alrededor.ui.explore

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.eduardo.alrededor.R
import com.eduardo.alrededor.domain.model.RelativeDirection
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun PlaceRow(
    place: PlaceUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, colors.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlaceThumbnail(place.thumbnailUrl)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = place.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = place.description ?: stringResource(R.string.explorar_sin_descripcion),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                DistancePill(formatDistance(place.distanceM))
            }

            DirectionBadge(place.arrowRotationDeg, place.direction)
        }
    }
}

/** Distancia en una "pastilla" de color, para que sea lo primero que se lee. */
@Composable
private fun DistancePill(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Composable
internal fun PlaceThumbnail(url: String?) {
    val shape = RoundedCornerShape(16.dp)
    val colors = MaterialTheme.colorScheme
    if (url != null) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .crossfade(true)
                .build(),
            contentDescription = null, // decorativa: el título ya se lee en la tarjeta
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(colors.surfaceVariant),
            error = ColorPainter(colors.errorContainer),
            modifier = Modifier.size(72.dp).clip(shape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(shape)
                .background(colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = colors.onSurfaceVariant
            )
        }
    }
}

/** Flecha dentro de un círculo de color de acento, con el texto de dirección debajo. */
@Composable
private fun DirectionBadge(rotationDeg: Float?, direction: RelativeDirection?) {
    if (rotationDeg == null || direction == null) return
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                // El icono apunta a la derecha; se resta 90° para que 0° sea "al frente" (arriba).
                modifier = Modifier.size(24.dp).rotate(rotationDeg - 90f)
            )
        }
        Text(
            text = stringResource(direction.labelRes()),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@StringRes
fun RelativeDirection.labelRes(): Int = when (this) {
    RelativeDirection.FRONT -> R.string.direccion_frente
    RelativeDirection.RIGHT -> R.string.direccion_derecha
    RelativeDirection.BEHIND -> R.string.direccion_atras
    RelativeDirection.LEFT -> R.string.direccion_izquierda
}

fun formatDistance(meters: Double): String =
    if (meters < 1000) "${meters.roundToInt()} m"
    else String.format(Locale.getDefault(), "%.1f km", meters / 1000)
