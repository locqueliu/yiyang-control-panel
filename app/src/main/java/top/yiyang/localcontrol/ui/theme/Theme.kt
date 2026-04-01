package top.yiyang.localcontrol.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = TealPrimary,
    primaryContainer = TealPrimaryContainer,
    secondary = TealSecondary,
    secondaryContainer = TealSecondaryContainer,
    tertiary = SandTertiary,
    tertiaryContainer = SandTertiaryContainer,
    background = SurfaceLight,
    surface = CardLight,
    surfaceVariant = SurfaceVariantLight,
    outline = BorderLight,
    outlineVariant = BorderMuted,
    onBackground = InkDark,
    onSurface = InkDark,
    onSurfaceVariant = InkSoft,
)

private val DarkColors = darkColorScheme(
    primary = TealPrimaryContainer,
    secondary = TealSecondary,
    tertiary = SandTertiary,
)

@Composable
fun YiyangTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}


