package com.shashidhara.foundit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val FoundITDarkColorScheme = darkColorScheme(
    primary = CyberTealBase,
    onPrimary = AquaFoamLighter,
    primaryContainer = CyberTealDark,
    onPrimaryContainer = AquaFoamLight,

    secondary = AquaFoamBase,
    onSecondary = CyberTealDarkest,
    secondaryContainer = CyberTealDarker,
    onSecondaryContainer = AquaFoamLight,

    tertiary = CyberTealLight,
    onTertiary = AquaFoamLighter,

    background = CyberTealDarkest,
    onBackground = AquaFoamLight,

    surface = CyberTealDarker,
    onSurface = AquaFoamLight,
    surfaceVariant = CyberTealDark,
    onSurfaceVariant = AquaFoamMedium,

    outline = CyberTealElevated,
    outlineVariant = CyberTealMedium
)

private val FoundITLightColorScheme = lightColorScheme(
    primary = CyberTealBase,
    onPrimary = AquaFoamLighter,
    primaryContainer = CyberTealDark,
    onPrimaryContainer = AquaFoamLight,

    secondary = AquaFoamDark,
    onSecondary = AquaFoamLighter,
    secondaryContainer = AquaFoamLight,
    onSecondaryContainer = CyberTealDarkest,

    tertiary = CyberTealMedium,
    onTertiary = AquaFoamLighter,

    background = CyberTealDarkest,
    onBackground = AquaFoamLight,

    surface = CyberTealDarker,
    onSurface = AquaFoamLight,
    surfaceVariant = CyberTealDark,
    onSurfaceVariant = AquaFoamMedium,

    outline = CyberTealElevated,
    outlineVariant = CyberTealMedium
)

@Composable
fun FoundITTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We intentionally adhere exclusively to Cyber Teal (#6A00FA) & Aqua Foam (#FFD6A5) shades
    val colorScheme = if (darkTheme) FoundITDarkColorScheme else FoundITLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}