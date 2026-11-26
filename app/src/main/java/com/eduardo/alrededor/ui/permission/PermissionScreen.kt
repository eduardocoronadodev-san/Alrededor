package com.eduardo.alrededor.ui.permission

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eduardo.alrededor.R
import com.eduardo.alrededor.domain.model.LocationPermissionState

/** Qué texto y qué acción corresponden a cada estado. */
private data class PermissionContent(
    @StringRes val title: Int,
    @StringRes val message: Int,
    @StringRes val buttonText: Int,
    val onButtonClick: () -> Unit
)

@Composable
fun PermissionScreen(
    state: PermissionUiState,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                PermissionBody(
                    content = state.permission.toContent(
                        onRequestPermission = onRequestPermission,
                        onOpenSettings = onOpenSettings,
                        onContinue = onContinue
                    )
                )
            }
        }
    }
}

@Composable
private fun PermissionBody(content: PermissionContent) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null, // decorativo
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(content.title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = stringResource(content.message),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.permiso_nota_privacidad),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Button(onClick = content.onButtonClick) {
            Text(stringResource(content.buttonText))
        }
    }
}

/** Agregar un estado nuevo solo obliga a agregar una rama aquí (el compilador avisa). */
private fun LocationPermissionState.toContent(
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onContinue: () -> Unit
): PermissionContent = when (this) {
    LocationPermissionState.NotRequested -> PermissionContent(
        R.string.permiso_titulo_inicial, R.string.permiso_mensaje_inicial,
        R.string.permiso_boton_permitir, onRequestPermission
    )
    LocationPermissionState.Denied -> PermissionContent(
        R.string.permiso_titulo_negado, R.string.permiso_mensaje_negado,
        R.string.permiso_boton_reintentar, onRequestPermission
    )
    LocationPermissionState.PermanentlyDenied -> PermissionContent(
        R.string.permiso_titulo_permanente, R.string.permiso_mensaje_permanente,
        R.string.permiso_boton_ajustes, onOpenSettings
    )
    LocationPermissionState.GrantedApproximate -> PermissionContent(
        R.string.permiso_titulo_aproximado, R.string.permiso_mensaje_aproximado,
        R.string.permiso_boton_continuar, onContinue
    )
    LocationPermissionState.GrantedPrecise -> PermissionContent(
        // Normalmente no se ve: la ruta navega sola al tener el permiso preciso.
        R.string.permiso_titulo_inicial, R.string.permiso_mensaje_inicial,
        R.string.permiso_boton_continuar, onContinue
    )
}

// ---------- Vistas previas (una por estado) ----------

@Preview(showBackground = true)
@Composable
private fun PreviewInicial() = MaterialTheme {
    PermissionScreen(
        PermissionUiState(isLoading = false, permission = LocationPermissionState.NotRequested),
        {}, {}, {}
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewNegado() = MaterialTheme {
    PermissionScreen(
        PermissionUiState(isLoading = false, permission = LocationPermissionState.Denied),
        {}, {}, {}
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewNegadoPermanente() = MaterialTheme {
    PermissionScreen(
        PermissionUiState(isLoading = false, permission = LocationPermissionState.PermanentlyDenied),
        {}, {}, {}
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewAproximado() = MaterialTheme {
    PermissionScreen(
        PermissionUiState(isLoading = false, permission = LocationPermissionState.GrantedApproximate),
        {}, {}, {}
    )
}
