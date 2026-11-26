package com.eduardo.alrededor.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Verde azulado (principal), naranja quemado (acento) y azul (apoyo)
val LightColors = lightColorScheme(
    primary = Color(0xFF0B6E69),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBFF0EA),
    onPrimaryContainer = Color(0xFF00201E),

    secondary = Color(0xFFC2410C),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBCB),
    onSecondaryContainer = Color(0xFF380D00),

    tertiary = Color(0xFF3B5BA9),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD9E2FF),
    onTertiaryContainer = Color(0xFF001A43),

    background = Color(0xFFF4F7F7),
    onBackground = Color(0xFF171D1D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF171D1D),
    surfaceVariant = Color(0xFFDAE5E3),
    onSurfaceVariant = Color(0xFF3F4948),
    outline = Color(0xFF6F7978),
    outlineVariant = Color(0xFFBEC9C7),

    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF)
)

val DarkColors = darkColorScheme(
    primary = Color(0xFF63D9CF),
    onPrimary = Color(0xFF003733),
    primaryContainer = Color(0xFF00504B),
    onPrimaryContainer = Color(0xFFBFF0EA),

    secondary = Color(0xFFFFB693),
    onSecondary = Color(0xFF591D00),
    secondaryContainer = Color(0xFF7D2D00),
    onSecondaryContainer = Color(0xFFFFDBCB),

    tertiary = Color(0xFFB0C6FF),
    onTertiary = Color(0xFF002D6F),
    tertiaryContainer = Color(0xFF214288),
    onTertiaryContainer = Color(0xFFD9E2FF),

    background = Color(0xFF0E1415),
    onBackground = Color(0xFFDDE4E3),
    surface = Color(0xFF161D1E),
    onSurface = Color(0xFFDDE4E3),
    surfaceVariant = Color(0xFF3F4948),
    onSurfaceVariant = Color(0xFFBEC9C7),
    outline = Color(0xFF899392),
    outlineVariant = Color(0xFF3F4948),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)
