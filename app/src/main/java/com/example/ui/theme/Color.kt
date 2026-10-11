package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Bright & Shining Green Palette
val ContractorGreenDeep = Color(0xFF065F46)     // Rich Forest Emerald
val ContractorGreen = Color(0xFF047857)         // Radiant Emerald Green (Primary)
val ContractorGreenVibrant = Color(0xFF059669)  // Vibrant Luminous Green
val ContractorGreenBright = Color(0xFF10B981)   // Bright Emerald Highlight
val ContractorGreenLight = Color(0xFF34D399)    // Neon/Mint Glow Accent
val ContractorMintContainer = Color(0xFFDCFCE7) // Luminous Mint
val ContractorMintSoft = Color(0xFFECFDF5)      // Sparkling Ultra-light Mint
val ContractorDarkText = Color(0xFF064E3B)

// Ultra-Bright & Crystal Backgrounds
val ContractorBackground = Color(0xFFF8FCF9)    // Ultra-bright crystal clean background
val ContractorSurface = Color(0xFFFFFFFF)       // Pure Sparkling White
val ContractorSurfaceVariant = Color(0xFFF0FDF4)// Subtle Mint-white surface
val ContractorTextPrimary = Color(0xFF0F172A)   // Crisp High-contrast Slate 900
val ContractorTextSecondary = Color(0xFF334155) // Slate 700
val ContractorTextMuted = Color(0xFF64748B)     // Slate 500
val ContractorBorder = Color(0xFFD1FAE5)        // Glowing mint-slate border

// Financial Indicators (Vibrant & Shining)
val IncomeGreen = Color(0xFF059669)             // Radiant Green
val IncomeGreenBg = Color(0xFFD1FAE5)
val ExpenseRed = Color(0xFFE11D48)              // Vibrant Rose/Red
val ExpenseRedBg = Color(0xFFFFE4E6)
val SalaryAmber = Color(0xFFD97706)             // Golden Amber
val SalaryAmberBg = Color(0xFFFEF3C7)
val BalanceBlue = Color(0xFF0284C7)
val BalanceBlueBg = Color(0xFFE0F2FE)

// Electric Gold Highlights (Shine)
val ElectricGold = Color(0xFFF59E0B)
val ElectricGoldLight = Color(0xFFFDE047)

// Shiny Gradient Brushes for Luminous Aesthetics
val ShinyHeaderGradient = Brush.linearGradient(
    listOf(
        Color(0xFF047857),
        Color(0xFF059669),
        Color(0xFF10B981)
    )
)

val ShinyCardGradient = Brush.linearGradient(
    listOf(
        Color(0xFFFFFFFF),
        Color(0xFFF0FDF4),
        Color(0xFFE6F9EE)
    )
)

val ShinyBalanceGradient = Brush.linearGradient(
    listOf(
        Color(0xFF047857),
        Color(0xFF059669),
        Color(0xFF0D9488)
    )
)

val ShinyGoldBadgeGradient = Brush.linearGradient(
    listOf(
        Color(0xFFD97706),
        Color(0xFFF59E0B),
        Color(0xFFFBBF24)
    )
)
