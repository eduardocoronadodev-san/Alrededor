package com.eduardo.alrededor.data.repository

import com.eduardo.alrededor.data.local.PlacesLocalDataSource
import com.eduardo.alrededor.domain.repository.SavedDataRepository
import javax.inject.Inject

class SavedDataRepositoryImpl @Inject constructor(
    private val placesLocal: PlacesLocalDataSource
) : SavedDataRepository {

    override suspend fun clearAll() = placesLocal.clearAll()
}
