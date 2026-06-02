package com.smarttrainner.core.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

enum class SmartTrainnerThemeTone(val storageValue: String) {
    Red("red"),
    Blue("blue"),
    Green("green"),
    Black("black");

    companion object {
        val Default = Blue

        fun fromStorageValue(value: String?): SmartTrainnerThemeTone =
            entries.firstOrNull { it.storageValue == value } ?: Default
    }
}

@Immutable
data class SmartTrainnerPalette(
    val ink: Color,
    val inkSoft: Color,
    val muted: Color,
    val paper: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val line: Color,
    val coral: Color,
    val coralSoft: Color,
    val green: Color,
    val greenSoft: Color,
    val amber: Color,
    val amberSoft: Color,
    val steel: Color,
    val steelSoft: Color,
    val danger: Color,
    val dangerSoft: Color,
    val screenGradient: List<Color>,
    val brandLightGradient: List<Color>,
    val brandDeepGradient: List<Color>,
    val isDark: Boolean = false
)

object SmartTrainnerColors {
    val Ink: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalSmartTrainnerPalette.current.ink
    val InkSoft: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalSmartTrainnerPalette.current.inkSoft
    val Muted: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalSmartTrainnerPalette.current.muted
    val Paper: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalSmartTrainnerPalette.current.paper
    val Surface: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalSmartTrainnerPalette.current.surface
    val SurfaceRaised: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalSmartTrainnerPalette.current.surfaceRaised
    val Line: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalSmartTrainnerPalette.current.line
    val Coral: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalSmartTrainnerPalette.current.coral
    val CoralSoft: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalSmartTrainnerPalette.current.coralSoft
    val Green: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalSmartTrainnerPalette.current.green
    val GreenSoft: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalSmartTrainnerPalette.current.greenSoft
    val Amber: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalSmartTrainnerPalette.current.amber
    val AmberSoft: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalSmartTrainnerPalette.current.amberSoft
    val Steel: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalSmartTrainnerPalette.current.steel
    val SteelSoft: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalSmartTrainnerPalette.current.steelSoft
    val Danger: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalSmartTrainnerPalette.current.danger
    val DangerSoft: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalSmartTrainnerPalette.current.dangerSoft
}

object SmartTrainnerGradients {
    @Composable
    @ReadOnlyComposable
    fun screen(): Brush = Brush.verticalGradient(
        colors = LocalSmartTrainnerPalette.current.screenGradient
    )

    @Composable
    @ReadOnlyComposable
    fun brandLight(): Brush = Brush.linearGradient(
        colors = LocalSmartTrainnerPalette.current.brandLightGradient
    )

    @Composable
    @ReadOnlyComposable
    fun brandDeep(): Brush = Brush.linearGradient(
        colors = LocalSmartTrainnerPalette.current.brandDeepGradient
    )
}

private val BaseTypography = Typography()

private val AppTypography = BaseTypography.run {
    copy(
        displaySmall = displaySmall.copy(
            fontFamily = FontFamily.SansSerif,
            fontSize = 30.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.Black
        ),
        headlineMedium = headlineMedium.copy(
            fontFamily = FontFamily.SansSerif,
            fontSize = 25.sp,
            lineHeight = 31.sp,
            fontWeight = FontWeight.ExtraBold
        ),
        headlineSmall = headlineSmall.copy(
            fontFamily = FontFamily.SansSerif,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.ExtraBold
        ),
        titleLarge = titleLarge.copy(
            fontFamily = FontFamily.SansSerif,
            fontSize = 21.sp,
            lineHeight = 27.sp,
            fontWeight = FontWeight.Bold
        ),
        titleMedium = titleMedium.copy(
            fontFamily = FontFamily.SansSerif,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Bold
        ),
        titleSmall = titleSmall.copy(
            fontFamily = FontFamily.SansSerif,
            fontSize = 15.sp,
            lineHeight = 21.sp,
            fontWeight = FontWeight.Bold
        ),
        bodyLarge = bodyLarge.copy(
            fontFamily = FontFamily.SansSerif,
            fontSize = 16.sp,
            lineHeight = 24.sp
        ),
        bodyMedium = bodyMedium.copy(
            fontFamily = FontFamily.SansSerif,
            fontSize = 14.sp,
            lineHeight = 21.sp
        ),
        bodySmall = bodySmall.copy(
            fontFamily = FontFamily.SansSerif,
            fontSize = 12.sp,
            lineHeight = 18.sp
        ),
        labelLarge = labelLarge.copy(
            fontFamily = FontFamily.SansSerif,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.SemiBold
        ),
        labelMedium = labelMedium.copy(
            fontFamily = FontFamily.SansSerif,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.SemiBold
        ),
        labelSmall = labelSmall.copy(
            fontFamily = FontFamily.SansSerif,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    )
}

@Composable
fun SmartTrainnerTheme(
    themeTone: SmartTrainnerThemeTone = SmartTrainnerThemeTone.Default,
    content: @Composable () -> Unit
) {
    val palette = paletteFor(themeTone)
    MaterialTheme(
        colorScheme = colorSchemeFor(palette),
        typography = AppTypography,
        content = {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalSmartTrainnerPalette provides palette,
                LocalContentColor provides palette.ink,
                content = content
            )
        }
    )
}

private val LocalSmartTrainnerPalette = staticCompositionLocalOf {
    bluePalette
}

private fun paletteFor(themeTone: SmartTrainnerThemeTone): SmartTrainnerPalette = when (themeTone) {
    SmartTrainnerThemeTone.Red -> redPalette
    SmartTrainnerThemeTone.Blue -> bluePalette
    SmartTrainnerThemeTone.Green -> greenPalette
    SmartTrainnerThemeTone.Black -> blackPalette
}

private fun colorSchemeFor(palette: SmartTrainnerPalette): ColorScheme {
    return if (palette.isDark) {
        darkColorScheme(
            primary = palette.coral,
            onPrimary = Color(0xFF071016),
            secondary = palette.green,
            onSecondary = Color(0xFF061211),
            tertiary = palette.amber,
            onTertiary = Color(0xFF111111),
            background = palette.paper,
            onBackground = palette.ink,
            surface = palette.surface,
            onSurface = palette.ink,
            surfaceVariant = palette.steelSoft,
            onSurfaceVariant = palette.muted,
            outline = palette.line,
            error = palette.danger,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = palette.coral,
            onPrimary = Color.White,
            secondary = palette.green,
            onSecondary = Color.White,
            tertiary = palette.amber,
            onTertiary = Color(0xFF111111),
            background = palette.paper,
            onBackground = palette.ink,
            surface = palette.surface,
            onSurface = palette.ink,
            surfaceVariant = palette.steelSoft,
            onSurfaceVariant = palette.muted,
            outline = palette.line,
            error = palette.danger,
            onError = Color.White
        )
    }
}

private val redPalette = SmartTrainnerPalette(
    ink = Color(0xFF15141A),
    inkSoft = Color(0xFF2D2730),
    muted = Color(0xFF746A72),
    paper = Color(0xFFF9F6F7),
    surface = Color(0xFFFCFAFB),
    surfaceRaised = Color(0xFFFFFFFF),
    line = Color(0xFFE5D7DC),
    coral = Color(0xFFC34D5D),
    coralSoft = Color(0xFFFFEEF1),
    green = Color(0xFF247C78),
    greenSoft = Color(0xFFE6F4F2),
    amber = Color(0xFFC58B2A),
    amberSoft = Color(0xFFFFF4D9),
    steel = Color(0xFF6B6170),
    steelSoft = Color(0xFFF0EAEE),
    danger = Color(0xFFC84646),
    dangerSoft = Color(0xFFFFF1F1),
    screenGradient = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFFFF5F6),
        Color(0xFFF6F8FC)
    ),
    brandLightGradient = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFFFF0F2),
        Color(0xFFFFF8E8)
    ),
    brandDeepGradient = listOf(
        Color(0xFF461521),
        Color(0xFF232132),
        Color(0xFF171B29)
    )
)

private val bluePalette = SmartTrainnerPalette(
    ink = Color(0xFF10141F),
    inkSoft = Color(0xFF1B2B3A),
    muted = Color(0xFF687180),
    paper = Color(0xFFF6FAFC),
    surface = Color(0xFFFBFCFE),
    surfaceRaised = Color(0xFFFFFFFF),
    line = Color(0xFFD8E6EE),
    coral = Color(0xFF1187C8),
    coralSoft = Color(0xFFE4F6FF),
    green = Color(0xFF1E8AA5),
    greenSoft = Color(0xFFE4F8FB),
    amber = Color(0xFFD09A2D),
    amberSoft = Color(0xFFFFF4D8),
    steel = Color(0xFF4D6678),
    steelSoft = Color(0xFFE9EEF1),
    danger = Color(0xFFC84646),
    dangerSoft = Color(0xFFFFF1F1),
    screenGradient = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFF0FAFD),
        Color(0xFFF5F9FF)
    ),
    brandLightGradient = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFEAF8FF),
        Color(0xFFF4FAFF)
    ),
    brandDeepGradient = listOf(
        Color(0xFF0A3448),
        Color(0xFF102A42),
        Color(0xFF17213A)
    )
)

private val greenPalette = SmartTrainnerPalette(
    ink = Color(0xFF10141F),
    inkSoft = Color(0xFF1B2B3A),
    muted = Color(0xFF687180),
    paper = Color(0xFFF5F7FA),
    surface = Color(0xFFFBFCFE),
    surfaceRaised = Color(0xFFFFFFFF),
    line = Color(0xFFD6E0E6),
    coral = Color(0xFF2F6F5E),
    coralSoft = Color(0xFFE8F2ED),
    green = Color(0xFF347D8B),
    greenSoft = Color(0xFFE7F2F4),
    amber = Color(0xFFB7791F),
    amberSoft = Color(0xFFFFF4D7),
    steel = Color(0xFF4D6678),
    steelSoft = Color(0xFFECEFF4),
    danger = Color(0xFFC84646),
    dangerSoft = Color(0xFFFFF1F1),
    screenGradient = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFF2F7F3),
        Color(0xFFF3F6FB)
    ),
    brandLightGradient = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFEEF6F0),
        Color(0xFFF8F3E8)
    ),
    brandDeepGradient = listOf(
        Color(0xFF12382F),
        Color(0xFF102A42),
        Color(0xFF17213A)
    )
)

private val blackPalette = SmartTrainnerPalette(
    ink = Color(0xFFF4F7FB),
    inkSoft = Color(0xFFDCE7F2),
    muted = Color(0xFF98A6B7),
    paper = Color(0xFF070A0F),
    surface = Color(0xFF0E141C),
    surfaceRaised = Color(0xFF121B25),
    line = Color(0xFF263241),
    coral = Color(0xFF5BA7FF),
    coralSoft = Color(0xFF182A3D),
    green = Color(0xFF42D1B1),
    greenSoft = Color(0xFF112D2A),
    amber = Color(0xFFF0B84A),
    amberSoft = Color(0xFF342813),
    steel = Color(0xFFB5C4D5),
    steelSoft = Color(0xFF1C2633),
    danger = Color(0xFFFF6B6B),
    dangerSoft = Color(0xFF3A191D),
    screenGradient = listOf(
        Color(0xFF070A0F),
        Color(0xFF0E1722),
        Color(0xFF111B29)
    ),
    brandLightGradient = listOf(
        Color(0xFF121B25),
        Color(0xFF132A3D),
        Color(0xFF0E141C)
    ),
    brandDeepGradient = listOf(
        Color(0xFF06131E),
        Color(0xFF0E1724),
        Color(0xFF111827)
    ),
    isDark = true
)
