package com.eduardo.alrededor.domain.model

/** Preferencias del usuario, con sus valores por defecto. */
data class AppSettings(
    val radiusM: Int = ExploreDefaults.RADIUS_M,
    val voiceEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
) {
    val radiusKm: Int get() = radiusM / 1000
}
