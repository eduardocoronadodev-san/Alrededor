package com.eduardo.alrededor.ui.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eduardo.alrededor.domain.model.LocationPermissionState

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

@Composable
fun PermissionRoute(
    onContinue: () -> Unit,
    viewModel: PermissionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Lanza el diálogo del sistema. Al volver, se recalcula el estado.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.onAction(
            PermissionAction.StatusRefreshed(context.shouldShowLocationRationale())
        )
    }

    // También se recalcula al volver a la pantalla (por ejemplo, desde los ajustes del sistema).
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onAction(
            PermissionAction.StatusRefreshed(context.shouldShowLocationRationale())
        )
    }

    // Con el permiso preciso concedido, esta pantalla ya no hace falta.
    LaunchedEffect(state.isLoading, state.permission) {
        if (!state.isLoading && state.permission == LocationPermissionState.GrantedPrecise) {
            onContinue()
        }
    }

    PermissionScreen(
        state = state,
        onRequestPermission = {
            viewModel.onAction(PermissionAction.RequestStarted)
            permissionLauncher.launch(LOCATION_PERMISSIONS)
        },
        onOpenSettings = { context.openAppSettings() },
        onContinue = onContinue
    )
}

// ---------- Ayudas que necesitan el framework de Android ----------

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

private fun Context.shouldShowLocationRationale(): Boolean {
    val activity = findActivity() ?: return false
    return LOCATION_PERMISSIONS.any {
        ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
    }
}

private fun Context.openAppSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}
