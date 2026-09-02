package com.wopro.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand palette — warm neutral + corporate blue, EMS-style but original.
val Blue600 = Color(0xFF1565C0)
val Blue500 = Color(0xFF1E88E5)
val Amber400 = Color(0xFFFFCA28)
val SurfaceWarm = Color(0xFFFBF7F0)
val SurfaceWarmDark = Color(0xFF1C1B1A)

val Red500 = Color(0xFFF44336)
val Green600 = Color(0xFF43A047)
val Gray400 = Color(0xFF9E9E9E)

private val LightColors = lightColorScheme(
    primary = Blue600,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Amber400,
    onSecondary = Color(0xFF3E2F00),
    surface = SurfaceWarm,
    onSurface = Color(0xFF1E1B16),
    background = SurfaceWarm,
    onBackground = Color(0xFF1E1B16),
    error = Red500
)

private val DarkColors = darkColorScheme(
    primary = Blue500,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0D47A1),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = Amber400,
    onSecondary = Color(0xFF3E2F00),
    surface = SurfaceWarmDark,
    onSurface = Color(0xFFE8E4DC),
    background = SurfaceWarmDark,
    onBackground = Color(0xFFE8E4DC),
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
