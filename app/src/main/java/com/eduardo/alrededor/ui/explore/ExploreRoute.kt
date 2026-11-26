package com.eduardo.alrededor.ui.explore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ExploreRoute(
    onPlaceClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Ubicación y brújula solo funcionan mientras la pantalla está visible.
    LifecycleStartEffect(Unit) {
        viewModel.onAction(ExploreAction.ScreenStarted)
        onStopOrDispose { viewModel.onAction(ExploreAction.ScreenStopped) }
    }

    ExploreScreen(
        state = state,
        onAction = viewModel::onAction,
        onPlaceClick = onPlaceClick,
        onSettingsClick = onSettingsClick
    )
}
