package com.shashidhara.foundit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val FoundITDarkColorScheme = darkColorScheme(
    primary = MintBase,
    onPrimary = DeepTealDark,
    primaryContainer = DeepTealDark,
    onPrimaryContainer = MintLighter,

    secondary = MintMedium,
    onSecondary = DeepTealDarkest,
    secondaryContainer = DeepTealDarker,
    onSecondaryContainer = MintLight,

    tertiary = MintDark,
    onTertiary = DeepTealDarkest,

    background = DeepTealDarkest,
    onBackground = MintLight,

    surface = DeepTealDarker,
    onSurface = MintLighter,
    surfaceVariant = DeepTealDark,
    onSurfaceVariant = MintLight,

    outline = DeepTealElevated,
    outlineVariant = DeepTealMedium
)

private val FoundITLightColorScheme = lightColorScheme(
    primary = DeepTealDark,
    onPrimary = MintLighter,
    primaryContainer = DeepTealDarker,
    onPrimaryContainer = MintLight,

    secondary = MintDarker,
    onSecondary = ColorPureWhite,
    secondaryContainer = MintLight,
    onSecondaryContainer = DeepTealDarkest,

    tertiary = DeepTealMedium,
    onTertiary = ColorPureWhite,

    background = DeepTealDarkest,
    onBackground = MintLight,

    surface = DeepTealDarker,
    onSurface = MintLighter,
    surfaceVariant = DeepTealDark,
    onSurfaceVariant = MintLight,

    outline = DeepTealElevated,
    outlineVariant = DeepTealMedium
)

@Composable
fun FoundITTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Exclusively built on #06363A & #98FFE1
    val colorScheme = if (darkTheme) FoundITDarkColorScheme else FoundITLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}