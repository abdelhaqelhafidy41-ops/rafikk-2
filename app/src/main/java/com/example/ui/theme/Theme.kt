package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = IslamicGoldLight,
    onPrimary = Color(0xFF261D02),
    primaryContainer = IslamicGreenLight,
    onPrimaryContainer = Color.White,
    secondary = IslamicGold,
    onSecondary = Color.Black,
    tertiary = IslamicAccentTeal,
    background = SurfaceDark,
    surface = SurfaceCardDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF1E2E28),
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0xFF334A42)
)

private val LightColorScheme = lightColorScheme(
    primary = IslamicGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = IslamicGreenContainer,
    onPrimaryContainer = IslamicGreenOnContainer,
    secondary = IslamicGold,
    onSecondary = Color.White,
    tertiary = IslamicAccentTeal,
    background = SurfaceLight,
    surface = SurfaceCardLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFE5EDE8),
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFCBD8D0)
)

@Composable
fun RafiqAlMuslimTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
