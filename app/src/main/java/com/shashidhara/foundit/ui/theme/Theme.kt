package com.shashidhara.foundit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val FoundITLightColorScheme = lightColorScheme(
    primary = BrandTealBase,
    onPrimary = ColorPureWhite,
    primaryContainer = BrandMintBg,
    onPrimaryContainer = BrandTealBase,

    secondary = BrandMintBase,
    onSecondary = BrandTealBase,
    secondaryContainer = AppSurfaceSubtle,
    onSecondaryContainer = TextPrimaryDark,

    tertiary = BrandTealLight,
    onTertiary = ColorPureWhite,

    background = AppBackgroundWhite,
    onBackground = TextPrimaryDark,

    surface = AppSurfaceWhite,
    onSurface = TextPrimaryDark,
    surfaceVariant = AppSurfaceSubtle,
    onSurfaceVariant = TextSecondarySlate,

    outline = AppBorderLight,
    outlineVariant = AppBorderSubtle
)

private val FoundITDarkColorScheme = FoundITLightColorScheme

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