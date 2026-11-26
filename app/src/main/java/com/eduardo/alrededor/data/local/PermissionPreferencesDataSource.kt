package com.eduardo.alrededor.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// fuente de datos: recuerda si la app ya pidió el permiso alguna vez
class PermissionPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val requestedKey = booleanPreferencesKey("ubicacion_ya_solicitada")

    suspend fun hasRequestedBefore(): Boolean =
        dataStore.data.map { it[requestedKey] ?: false }.first()

    suspend fun markRequested() {
        dataStore.edit { it[requestedKey] = true }
    }
}