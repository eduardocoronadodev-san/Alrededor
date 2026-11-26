package com.eduardo.alrededor.domain.usecase

import com.eduardo.alrededor.domain.repository.SavedDataRepository
import javax.inject.Inject

class ClearSavedDataUseCase @Inject constructor(
    private val repository: SavedDataRepository
) {
    suspend operator fun invoke() = repository.clearAll()
}
