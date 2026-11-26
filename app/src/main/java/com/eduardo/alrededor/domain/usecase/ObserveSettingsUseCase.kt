package com.eduardo.alrededor.domain.usecase

import com.eduardo.alrededor.domain.model.AppSettings
import com.eduardo.alrededor.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<AppSettings> = repository.settings
}
