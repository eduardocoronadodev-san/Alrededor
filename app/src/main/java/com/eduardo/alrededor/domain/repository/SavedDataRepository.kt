package com.eduardo.alrededor.domain.repository

/** Los datos que la app guarda en el teléfono (caché de lugares y resúmenes). */
interface SavedDataRepository {
    suspend fun clearAll()
}
