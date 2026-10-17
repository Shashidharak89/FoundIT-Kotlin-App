package com.shashidhara.foundit.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Balanced FoundIT Palette:
 * Clean white background with crisp black text, using #06363A (Deep Teal)
 * and #98FFE1 (Mint Aqua) selectively on key cards, buttons, badges, and accents.
 */

// --- Pure Black & White Foundation ---
val ColorPureWhite        = Color(0xFFFFFFFF)
val ColorPureBlack        = Color(0xFF000000)
val AppBackgroundWhite    = Color(0xFFFFFFFF)   // Clean white background
val AppSurfaceWhite       = Color(0xFFFFFFFF)   // Pure white card surfaces
val AppSurfaceSubtle      = Color(0xFFF8FAFC)   // Subtle light grey container
val AppBorderLight        = Color(0xFFE2E8F0)   // Soft border outline
val AppBorderSubtle       = Color(0xFFEDF2F7)   // Ultra-light divider

// --- Typography: Clean Charcoal & Black ---
val TextPrimaryDark       = Color(0xFF0F172A)   // Primary near-black text
val TextSecondarySlate    = Color(0xFF475569)   // Readable secondary slate text
val TextMutedSlate        = Color(0xFF94A3B8)   // Captions & timestamps

// --- User's Selected Colors (Balanced Accent Use) ---
val BrandTealBase         = Color(0xFF06363A)   // #06363A - Hero header, primary CTA, badges
val BrandTealDark         = Color(0xFF032225)   // Gradient deep shade
val BrandTealLight        = Color(0xFF0D5359)   // Lighter teal for interactive hover
val BrandTealMuted        = Color(0x1A06363A)   // 10% translucent teal tint
val BrandTealGhost        = Color(0x0D06363A)   // 5% subtle container tint

val BrandMintBase         = Color(0xFF98FFE1)   // #98FFE1 - Accent badges, borders & glows
val BrandMintLight        = Color(0xFFC7FCEE)   // Light mint highlight
val BrandMintBg           = Color(0xFFE6FAF5)   // Gentle soft mint pill background
val BrandMintGlow         = Color(0x3398FFE1)   // 20% mint glow

// --- Semantic Color Aliases for Universal Screen Compatibility ---
val DeepTealDarkest       = AppBackgroundWhite  // White screen background (#FFFFFF)
val DeepTealDarker        = AppSurfaceWhite     // White card surfaces (#FFFFFF)
val DeepTealDark          = AppSurfaceSubtle    // Light grey text fields & containers (#F8FAFC)
val DeepTealElevated      = AppBorderLight      // Soft card borders & dividers (#E2E8F0)
val DeepTealMedium        = Color(0xFFCBD5E1)   // Border outlines
val DeepTealBase          = BrandTealBase       // Primary brand deep teal (#06363A)
val DeepTealLight         = BrandTealLight      // Interactive teal
val DeepTealSoft          = TextSecondarySlate  // Slate text
val DeepTealMuted         = BrandTealMuted
val DeepTealGhost         = BrandTealGhost

val MintBrightest         = ColorPureWhite
val MintLighter           = TextPrimaryDark     // Primary text: dark charcoal (#0F172A)
val MintLight             = TextSecondarySlate  // Secondary text: slate (#475569)
val MintBase              = BrandMintBase       // #98FFE1
val MintMedium            = Color(0xFF4EEAC0)
val MintDark              = BrandTealBase       // #06363A
val MintDarker            = BrandTealDark
val MintDeepest           = BrandMintBg         // Soft mint background
val MintMuted             = BrandMintBg
val MintGlow              = BrandMintGlow

// Legacy / Component Mapping
val CyberTealDarkest      = AppBackgroundWhite  // Clean white canvas background
val CyberTealDarker       = AppSurfaceWhite     // Clean white cards
val CyberTealDark         = AppSurfaceSubtle    // Light input & subtle container background
val CyberTealElevated     = AppBorderLight      // Subtle grey borders
val CyberTealMedium       = Color(0xFFCBD5E1)   // Inactive toggles
val CyberTealBase         = BrandTealBase       // Primary buttons (#06363A)
val CyberTealLight        = BrandMintBase       // Mint interactive accent (#98FFE1)
val CyberTealSoft         = TextSecondarySlate  // Secondary labels
val CyberTealMuted        = BrandMintBg         // Soft mint container tint
val CyberTealGhost        = Color(0xFFF1F5F9)

val AquaFoamLighter       = TextPrimaryDark     // Crisp dark text (#0F172A)
val AquaFoamLight         = TextSecondarySlate  // Readable secondary text (#475569)
val AquaFoamMuted         = BrandMintBg         // Soft mint pill background
val AquaFoamBase          = BrandTealBase       // Primary accent / icons (#06363A)
val AquaFoamMedium        = TextSecondarySlate  // Subdued metadata
val AquaFoamDark          = TextMutedSlate      // Muted labels & placeholders
val AquaFoamDarker        = BrandTealBase       // Deep teal outlines
val AquaFoamDeepest       = Color(0xFFE2E8F0)