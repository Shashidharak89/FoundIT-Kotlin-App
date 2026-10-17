package com.shashidhara.foundit.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * FoundIT Brand Color Palette
 * Built strictly on #06363A (Deep Pine Teal) and #98FFE1 (Vibrant Mint Green),
 * their harmonious tints and shades, plus pure black and white.
 */

// --- Base Theme Constants ---
val ColorTealDarkBase = Color(0xFF06363A)   // Primary Dark: #06363A
val ColorMintAquaBase = Color(0xFF98FFE1)   // Primary Light Accent: #98FFE1

// --- Black & White Constants ---
val ColorPureBlack    = Color(0xFF000000)
val ColorPureWhite    = Color(0xFFFFFFFF)
val ColorMidnightTeal = Color(0xFF021618)   // Near-black dark canvas background

// --- Deep Teal (#06363A) Spectrum ---
val DeepTealDarkest   = Color(0xFF021618)   // Main screen scaffold background
val DeepTealDarker    = Color(0xFF032225)   // Drawer, cards, elevated modal sheets
val DeepTealDark      = Color(0xFF06363A)   // Core #06363A card & container surface
val DeepTealElevated  = Color(0xFF0E4A50)   // Card borders, textfield outlines, dividers
val DeepTealMedium    = Color(0xFF145D64)   // Secondary button surfaces, inactive pills
val DeepTealBase      = Color(0xFF06363A)   // Core Deep Teal (#06363A)
val DeepTealLight     = Color(0xFF1F7B84)   // Active highlight / teal border accent
val DeepTealSoft      = Color(0xFF2FA2AD)   // Secondary accent icons & labels
val DeepTealMuted     = Color(0x3306363A)   // 20% alpha container tint
val DeepTealGhost     = Color(0x1406363A)   // 8% alpha subtle background

// --- Vibrant Mint (#98FFE1) Spectrum ---
val MintBrightest     = Color(0xFFFFFFFF)   // Pure white text / high contrast
val MintLighter       = Color(0xFFE8FFF8)   // Primary headline & bright icon highlight
val MintLight         = Color(0xFFC4FCEE)   // Secondary body text & high-contrast labels
val MintBase          = Color(0xFF98FFE1)   // Core Vibrant Mint (#98FFE1)
val MintMedium        = Color(0xFF6DF7D2)   // Interactive button & active state
val MintDark          = Color(0xFF38D9B0)   // Subdued accent / badge outline
val MintDarker        = Color(0xFF1BAE88)   // Deep mint border
val MintDeepest       = Color(0xFF0D5243)   // Deep badge background container
val MintMuted         = Color(0x2698FFE1)   // 15% mint container background tint
val MintGlow          = Color(0x4098FFE1)   // 25% mint border glow

// --- Semantic Color Aliases for Seamless App Compatibility ---
val CyberTealDarkest  = DeepTealDarkest
val CyberTealDarker   = DeepTealDarker
val CyberTealDark     = DeepTealDark
val CyberTealElevated = DeepTealElevated
val CyberTealMedium   = DeepTealMedium
val CyberTealBase     = MintBase            // Primary action color (#98FFE1)
val CyberTealLight    = MintMedium          // Vibrant interactive accent
val CyberTealSoft     = MintDark            // Soft accent text & icons
val CyberTealMuted    = MintMuted           // Translucent pill container
val CyberTealGhost    = Color(0x1498FFE1)   // Subtle background glow

val AquaFoamLighter   = MintLighter         // #E8FFF8
val AquaFoamLight     = MintLight           // #C4FCEE
val AquaFoamMuted     = MintMuted           // 15% #98FFE1
val AquaFoamBase      = MintBase            // #98FFE1
val AquaFoamMedium    = MintMedium          // #6DF7D2
val AquaFoamDark      = MintDark            // #38D9B0
val AquaFoamDarker    = MintDarker          // #1BAE88
val AquaFoamDeepest   = MintDeepest         // #0D5243