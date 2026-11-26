package com.eduardo.alrededor.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.eduardo.alrededor.domain.model.AppSettings
import com.eduardo.alrededor.domain.model.ExploreDefaults
import com.eduardo.alrededor.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

/** Fuente de datos: las preferencias guardadas en DataStore. */
class SettingsLocalDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val radiusKey = intPreferencesKey("radio_busqueda_m")
    private val voiceKey = booleanPreferencesKey("voz_activada")
    private val themeKey = stringPreferencesKey("tema")

    val settings: Flow<AppSettings> = dataStore.data
        .catch { error ->
            // Si el archivo no se puede leer, se usan los valores por defecto.
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs ->
            AppSettings(
                radiusM = prefs[radiusKey] ?: ExploreDefaults.RADIUS_M,
                voiceEnabled = prefs[voiceKey] ?: true,
                themeMode = prefs[themeKey]
                    ?.let { name -> runCatching { ThemeMode.valueOf(name) }.getOrNull() }
                    ?: ThemeMode.SYSTEM
            )
        }

    suspend fun setRadiusM(radiusM: Int) { dataStore.edit { it[radiusKey] = radiusM } }
    suspend fun setVoiceEnabled(enabled: Boolean) { dataStore.edit { it[voiceKey] = enabled } }
    suspend fun setThemeMode(mode: ThemeMode) { dataStore.edit { it[themeKey] = mode.name } }
}
