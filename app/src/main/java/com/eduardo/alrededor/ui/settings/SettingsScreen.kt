package com.eduardo.alrededor.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eduardo.alrededor.BuildConfig
import com.eduardo.alrededor.R
import com.eduardo.alrededor.domain.model.ThemeMode
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    onBack: () -> Unit,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    // Que el diálogo esté abierto es estado de la propia interfaz, no de negocio.
    var showClearDialog by rememberSaveable { mutableStateOf(false) }

    // Muestra el mensaje pendiente una sola vez y avisa al ViewModel que ya lo mostró.
    val clearedText = stringResource(R.string.ajustes_msg_borrado)
    val failedText = stringResource(R.string.ajustes_msg_error_borrado)
    LaunchedEffect(state.message) {
        val text = when (state.message) {
            SettingsMessage.DATA_CLEARED -> clearedText
            SettingsMessage.CLEAR_FAILED -> failedText
            null -> return@LaunchedEffect
        }
        snackbarHostState.showSnackbar(text)
        onAction(SettingsAction.MessageShown)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ajustes_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.ajustes_volver)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ---------- Búsqueda ----------
            SectionHeader(R.string.ajustes_seccion_busqueda)
            RadiusSetting(radiusKm = state.radiusKm) { onAction(SettingsAction.RadiusChanged(it)) }
            SwitchSetting(
                title = R.string.ajustes_voz,
                subtitle = R.string.ajustes_voz_detalle,
                checked = state.voiceEnabled
            ) { onAction(SettingsAction.VoiceEnabledChanged(it)) }
            ThemeSetting(mode = state.themeMode) { onAction(SettingsAction.ThemeChanged(it)) }

            // ---------- Privacidad ----------
            SectionHeader(R.string.ajustes_seccion_privacidad)
            ListItem(
                headlineContent = { Text(stringResource(R.string.ajustes_borrar)) },
                supportingContent = { Text(stringResource(R.string.ajustes_borrar_detalle)) },
                trailingContent = {
                    if (state.isClearing) {
                        CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                },
                modifier = Modifier.clickable(enabled = !state.isClearing) { showClearDialog = true }
            )
            LinkItem(R.string.ajustes_politica, null, stringResource(R.string.url_politica_privacidad), onOpenUrl)

            // ---------- Acerca de ----------
            SectionHeader(R.string.ajustes_seccion_acerca)
            LinkItem(
                R.string.ajustes_wikipedia, R.string.ajustes_wikipedia_detalle,
                stringResource(R.string.url_licencia_wikipedia), onOpenUrl
            )
            LinkItem(
                R.string.ajustes_osm, R.string.ajustes_osm_detalle,
                stringResource(R.string.url_osm), onOpenUrl
            )
            LinkItem(
                R.string.ajustes_geoapify, R.string.ajustes_geoapify_detalle,
                stringResource(R.string.url_geoapify), onOpenUrl
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.ajustes_version)) },
                supportingContent = { Text(BuildConfig.VERSION_NAME) }
            )
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.ajustes_borrar_confirmar_titulo)) },
            text = { Text(stringResource(R.string.ajustes_borrar_confirmar_mensaje)) },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    onAction(SettingsAction.ClearDataConfirmed)
                }) { Text(stringResource(R.string.ajustes_borrar_accion)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.ajustes_cancelar))
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(@StringRes title: Int) {
    Text(
        text = stringResource(title),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 4.dp)
            .semantics { heading() }
    )
}

@Composable
private fun RadiusSetting(radiusKm: Int, onRadiusChanged: (Int) -> Unit) {
    // Mientras se arrastra, el valor es local; se guarda al soltar.
    var sliderValue by remember(radiusKm) { mutableFloatStateOf(radiusKm.toFloat()) }

    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.ajustes_radio))
            Text(
                text = stringResource(R.string.ajustes_radio_valor, sliderValue.roundToInt()),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onRadiusChanged(sliderValue.roundToInt()) },
            valueRange = 1f..10f,
            steps = 8   // pasos de 1 km
        )
    }
}

@Composable
private fun SwitchSetting(
    @StringRes title: Int,
    @StringRes subtitle: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    // Toda la fila es el interruptor (mejor accesibilidad y más fácil de tocar).
    ListItem(
        headlineContent = { Text(stringResource(title)) },
        supportingContent = { Text(stringResource(subtitle)) },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
        modifier = Modifier.toggleable(
            value = checked,
            role = Role.Switch,
            onValueChange = onCheckedChange
        )
    )
}

@Composable
private fun ThemeSetting(mode: ThemeMode, onModeChanged: (ThemeMode) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(stringResource(R.string.ajustes_tema))
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThemeChip(R.string.ajustes_tema_sistema, mode == ThemeMode.SYSTEM) {
                onModeChanged(ThemeMode.SYSTEM)
            }
            ThemeChip(R.string.ajustes_tema_claro, mode == ThemeMode.LIGHT) {
                onModeChanged(ThemeMode.LIGHT)
            }
            ThemeChip(R.string.ajustes_tema_oscuro, mode == ThemeMode.DARK) {
                onModeChanged(ThemeMode.DARK)
            }
        }
    }
}

@Composable
private fun ThemeChip(@StringRes label: Int, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(stringResource(label)) }
    )
}

@Composable
private fun LinkItem(
    @StringRes title: Int,
    @StringRes subtitle: Int?,
    url: String,
    onOpenUrl: (String) -> Unit
) {
    ListItem(
        headlineContent = { Text(stringResource(title)) },
        supportingContent = subtitle?.let { { Text(stringResource(it)) } },
        modifier = Modifier.clickable { onOpenUrl(url) }
    )
}

// ---------- Vista previa ----------

@Preview(showBackground = true)
@Composable
private fun PreviewAjustes() = MaterialTheme {
    SettingsScreen(
        state = SettingsUiState(radiusKm = 5, themeMode = ThemeMode.SYSTEM),
        onAction = {}, onBack = {}, onOpenUrl = {}
    )
}
