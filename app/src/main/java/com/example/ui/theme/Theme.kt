package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BusinessOsDarkColorScheme = darkColorScheme(
    primary = ElectricGreen,
    onPrimary = ObsidianBlack,
    primaryContainer = EmeraldMuted,
    onPrimaryContainer = ElectricGreen,
    secondary = SecondaryEmerald,
    onSecondary = ObsidianBlack,
    secondaryContainer = SlateSurfaceElevated,
    onSecondaryContainer = CrispWhite,
    tertiary = StatusInfo,
    onTertiary = ObsidianBlack,
    background = ObsidianBlack,
    onBackground = CrispWhite,
    surface = SlateSurface,
    onSurface = CrispWhite,
    surfaceVariant = SlateSurfaceElevated,
    onSurfaceVariant = MutedSteelGray,
    outline = SlateBorder,
    error = StatusDanger,
    onError = Color.White
)

private val BusinessOsLightColorScheme = lightColorScheme(
    primary = LightPrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = LightSecondaryGreen,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = LightTextPrimary,
    tertiary = Color(0xFF0284C7),
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = StatusDanger,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) BusinessOsDarkColorScheme else BusinessOsLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
