package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val FourRideLightColorScheme = lightColorScheme(
    primary = BrandOrange,
    onPrimary = PureWhite,
    primaryContainer = BrandOrangeContainer,
    onPrimaryContainer = ObsidianBlack,
    secondary = ObsidianBlack,
    onSecondary = PureWhite,
    secondaryContainer = SurfaceMuted,
    onSecondaryContainer = TextPrimary,
    tertiary = BrandOrangeDark,
    onTertiary = PureWhite,
    background = PureWhite,
    onBackground = TextPrimary,
    surface = PureWhite,
    onSurface = TextPrimary,
    surfaceVariant = OffWhiteBg,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    outlineVariant = SurfaceMuted,
    error = ErrorRed,
    onError = PureWhite,
    errorContainer = ErrorRedBg,
    onErrorContainer = ErrorRed
)

val FourRideShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun FourRideTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FourRideLightColorScheme,
        typography = Typography,
        shapes = FourRideShapes,
        content = content
    )
}
