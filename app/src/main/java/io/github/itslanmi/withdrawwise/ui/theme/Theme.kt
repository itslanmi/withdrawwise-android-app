package io.github.itslanmi.withdrawwise.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val LightColors =
    lightColorScheme(
        primary = Ember,
        onPrimary = Paper,
        primaryContainer = Color(0xFFFFE6DC),
        onPrimaryContainer = Color(0xFF3B1305),
        secondary = Gunmetal,
        onSecondary = Paper,
        secondaryContainer = Mist200,
        onSecondaryContainer = Ink,
        tertiary = FreeLight,
        onTertiary = Paper,
        tertiaryContainer = Color(0xFFD9F5E8),
        onTertiaryContainer = Color(0xFF00391F),
        error = HighLight,
        onError = Paper,
        errorContainer = Color(0xFFFFE1E1),
        onErrorContainer = Color(0xFF5C0A0D),
        background = Paper,
        onBackground = Ink,
        surface = Paper,
        onSurface = Ink,
        surfaceVariant = Mist100,
        onSurfaceVariant = InkMuted,
        surfaceTint = Color.Transparent,
        surfaceBright = Paper,
        surfaceDim = Mist200,
        surfaceContainerLowest = Paper,
        surfaceContainerLow = Mist50,
        surfaceContainer = Mist100,
        surfaceContainerHigh = Mist200,
        surfaceContainerHighest = Mist300,
        outline = Line,
        outlineVariant = LineSoft,
        inverseSurface = Ink,
        inverseOnSurface = Paper,
        inversePrimary = EmberBright,
        scrim = Color.Black
    )

private val DarkColors =
    darkColorScheme(
        primary = EmberBright,
        onPrimary = Color(0xFF2A0C02),
        primaryContainer = Color(0xFF5C2612),
        onPrimaryContainer = Color(0xFFFFDCCD),
        secondary = Color(0xFFC6CADB),
        onSecondary = Night,
        secondaryContainer = Night300,
        onSecondaryContainer = Snow,
        tertiary = FreeDark,
        onTertiary = Color(0xFF00301A),
        tertiaryContainer = Color(0xFF0E3D2A),
        onTertiaryContainer = Color(0xFFB6F5D6),
        error = HighDark,
        onError = Color(0xFF3A0005),
        errorContainer = Color(0xFF4A1316),
        onErrorContainer = Color(0xFFFFDAD9),
        background = Night,
        onBackground = Snow,
        surface = Night,
        onSurface = Snow,
        surfaceVariant = Night200,
        onSurfaceVariant = SnowMuted,
        surfaceTint = Color.Transparent,
        surfaceBright = Night300,
        surfaceDim = Night,
        surfaceContainerLowest = Night,
        surfaceContainerLow = Night50,
        surfaceContainer = Night100,
        surfaceContainerHigh = Night200,
        surfaceContainerHighest = Night300,
        outline = NightLine,
        outlineVariant = NightLineSoft,
        inverseSurface = Snow,
        inverseOnSurface = Night,
        inversePrimary = Ember,
        scrim = Color.Black
    )

/** Colours Material 3 has no slot for: fee semantics, the floating nav bar and brand gradients. */
@Immutable
data class ExtendedColors(
    val feeFree: Color,
    val feeLow: Color,
    val feeHigh: Color,
    val onFee: Color,
    val navBar: Color,
    val onNavBar: Color,
    val onNavBarMuted: Color,
    val mapControl: Color,
    val brandGradient: Brush,
    val isDark: Boolean
)

private val LightExtended =
    ExtendedColors(
        feeFree = FreeLight,
        feeLow = LowLight,
        feeHigh = HighLight,
        onFee = Color.White,
        navBar = Ink,
        onNavBar = Paper,
        onNavBarMuted = Color(0xFF8E92A0),
        mapControl = Paper,
        brandGradient = Brush.linearGradient(listOf(Ember, EmberGlow)),
        isDark = false
    )

private val DarkExtended =
    ExtendedColors(
        feeFree = FreeDark,
        feeLow = LowDark,
        feeHigh = HighDark,
        onFee = Night,
        navBar = Night200,
        onNavBar = Snow,
        onNavBarMuted = Color(0xFF7D8190),
        mapControl = Night200,
        brandGradient = Brush.linearGradient(listOf(Color(0xFFD9542A), EmberBright)),
        isDark = true
    )

val LocalExtendedColors = staticCompositionLocalOf { LightExtended }

/** Access app-specific colours next to the Material ones: `MaterialTheme.extended.feeFree`. */
val MaterialTheme.extended: ExtendedColors
    @Composable get() = LocalExtendedColors.current

@Composable
fun WithdrawWiseTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalExtendedColors provides if (darkTheme) DarkExtended else LightExtended) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = WithdrawWiseTypography,
            shapes = WithdrawWiseShapes,
            content = content
        )
    }
}
