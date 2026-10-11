package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BrightShiningColorScheme = lightColorScheme(
    primary = ContractorGreen,
    onPrimary = Color.White,
    primaryContainer = ContractorMintContainer,
    onPrimaryContainer = ContractorDarkText,
    secondary = ContractorGreenVibrant,
    onSecondary = Color.White,
    secondaryContainer = ContractorMintSoft,
    onSecondaryContainer = ContractorGreenDeep,
    tertiary = ElectricGold,
    onTertiary = Color.White,
    background = ContractorBackground,
    onBackground = ContractorTextPrimary,
    surface = ContractorSurface,
    onSurface = ContractorTextPrimary,
    surfaceVariant = ContractorSurfaceVariant,
    onSurfaceVariant = ContractorTextSecondary,
    outline = ContractorBorder,
    outlineVariant = Color(0xFFE2E8F0),
    error = ExpenseRed,
    onError = Color.White,
    errorContainer = ExpenseRedBg,
    onErrorContainer = ExpenseRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    // Keep bright, radiant, polished white-and-emerald shining theme across all modes
    MaterialTheme(
        colorScheme = BrightShiningColorScheme,
        typography = Typography,
        content = content
    )
}
