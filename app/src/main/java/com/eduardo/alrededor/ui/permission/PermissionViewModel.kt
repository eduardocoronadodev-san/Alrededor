package com.eduardo.alrededor.ui.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduardo.alrededor.domain.usecase.MarkLocationPermissionRequestedUseCase
import com.eduardo.alrededor.domain.usecase.ResolveLocationPermissionStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val resolveState: ResolveLocationPermissionStateUseCase,
    private val markRequested: MarkLocationPermissionRequestedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionUiState())
    val uiState: StateFlow<PermissionUiState> = _uiState.asStateFlow()

    fun onAction(action: PermissionAction) {
        when (action) {
            PermissionAction.RequestStarted ->
                viewModelScope.launch { markRequested() }

            is PermissionAction.StatusRefreshed ->
                viewModelScope.launch {
                    val permission = resolveState(action.shouldShowRationale)
                    _uiState.update { it.copy(isLoading = false, permission = permission) }
                }
        }
    }
}
