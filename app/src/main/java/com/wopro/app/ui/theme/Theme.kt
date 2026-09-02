package com.wopro.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand palette — teal corporate, EMS-style professional.
val TealPrimary = Color(0xFF00796B)      // deep teal (bottom nav)
val Teal500 = Color(0xFF009688)
val TealAccent = Color(0xFF26A69A)
val Amber400 = Color(0xFFFFC107)
val Amber600 = Color(0xFFFFB300)
val SurfaceLight = Color(0xFFF5F7F7)
val SurfaceDark = Color(0xFF121615)

val Red500 = Color(0xFFE53935)
val Green600 = Color(0xFF2E9E5B)
val Gray500 = Color(0xFF8A9295)
val Gray700 = Color(0xFF4A5457)

private val LightColors = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2DFDB),
    onPrimaryContainer = Color(0xFF004D45),
    secondary = TealAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCF1EC),
    onSecondaryContainer = Color(0xFF00554D),
    surface = Color.White,
    onSurface = Color(0xFF1A1D1D),
    surfaceVariant = Color(0xFFE7ECEC),
    onSurfaceVariant = Gray700,
    background = SurfaceLight,
    onBackground = Color(0xFF1A1D1D),
    error = Red500,
    outline = Color(0xFF8B9396),
    outlineVariant = Color(0xFFD5DCDD)
)

private val DarkColors = darkColorScheme(
    primary = TealAccent,
    onPrimary = Color(0xFF00312C),
    primaryContainer = Color(0xFF00554D),
    onPrimaryContainer = Color(0xFFB2DFDB),
    secondary = Teal500,
    onSecondary = Color.White,
    surface = SurfaceDark,
    onSurface = Color(0xFFE0E5E5),
    surfaceVariant = Color(0xFF232A2B),
    onSurfaceVariant = Color(0xFFA9B4B5),
    background = Color(0xFF0D1010),
    onBackground = Color(0xFFE0E5E5),
    error = Color(0xFFEF5350)
)

@Composable
fun WOProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
