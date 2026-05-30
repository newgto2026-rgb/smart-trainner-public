package com.smarttrainner.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

object SmartTrainnerColors {
    val Ink = Color(0xFF10141F)
    val InkSoft = Color(0xFF1B2B3A)
    val Muted = Color(0xFF687180)
    val Paper = Color(0xFFF6FAFC)
    val Surface = Color(0xFFFFFCF8)
    val SurfaceRaised = Color(0xFFFFFFFF)
    val Line = Color(0xFFD8E6EE)
    val Coral = Color(0xFF1187C8)
    val CoralSoft = Color(0xFFE4F6FF)
    val Green = Color(0xFF1E8AA5)
    val GreenSoft = Color(0xFFE4F8FB)
    val Amber = Color(0xFF48C7F2)
    val AmberSoft = Color(0xFFE7F8FF)
    val Steel = Color(0xFF4D6678)
    val SteelSoft = Color(0xFFE9EEF1)
}

object SmartTrainnerGradients {
    fun screen(): Brush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFCF7),
            Color(0xFFF0FAFD),
            Color(0xFFF5F9FF)
        )
    )

    fun brandLight(): Brush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFFCF8),
            Color(0xFFEAF8FF),
            Color(0xFFF1FBFF)
        )
    )

    fun brandDeep(): Brush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0A3448),
            Color(0xFF102A42),
            Color(0xFF17213A)
        )
    )
}

private val LightColors = lightColorScheme(
    primary = SmartTrainnerColors.Coral,
    onPrimary = Color.White,
    secondary = SmartTrainnerColors.Green,
    onSecondary = Color.White,
    tertiary = SmartTrainnerColors.Amber,
    background = SmartTrainnerColors.Paper,
    onBackground = SmartTrainnerColors.Ink,
    surface = SmartTrainnerColors.Surface,
    onSurface = SmartTrainnerColors.Ink,
    surfaceVariant = SmartTrainnerColors.SteelSoft,
    onSurfaceVariant = SmartTrainnerColors.Muted,
    outline = SmartTrainnerColors.Line
)

private val AppTypography = Typography().run {
    copy(
        displaySmall = displaySmall.copy(fontFamily = FontFamily.SansSerif),
        headlineMedium = headlineMedium.copy(fontFamily = FontFamily.SansSerif),
        headlineSmall = headlineSmall.copy(fontFamily = FontFamily.SansSerif),
        titleLarge = titleLarge.copy(fontFamily = FontFamily.SansSerif),
        titleMedium = titleMedium.copy(fontFamily = FontFamily.SansSerif),
        bodyLarge = bodyLarge.copy(fontFamily = FontFamily.SansSerif),
        bodyMedium = bodyMedium.copy(fontFamily = FontFamily.SansSerif),
        bodySmall = bodySmall.copy(fontFamily = FontFamily.SansSerif),
        labelLarge = labelLarge.copy(fontFamily = FontFamily.SansSerif),
        labelMedium = labelMedium.copy(fontFamily = FontFamily.SansSerif)
    )
}

@Composable
fun SmartTrainnerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content
    )
}
