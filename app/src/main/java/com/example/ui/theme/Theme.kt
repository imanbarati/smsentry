package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Emerald500,
    onPrimary = Color.White,
    primaryContainer = Emerald100,
    onPrimaryContainer = Emerald900,
    secondary = Obsidian800,
    onSecondary = Color.White,
    secondaryContainer = PorcelainSurfaceVariant,
    onSecondaryContainer = Obsidian900,
    tertiary = PromotionIndigoLight,
    onTertiary = Color.White,
    tertiaryContainer = PromotionIndigoContainerLight,
    onTertiaryContainer = PromotionIndigoLight,
    background = PorcelainBg,
    onBackground = SlateTextPrimaryLight,
    surface = PorcelainSurface,
    onSurface = SlateTextPrimaryLight,
    surfaceVariant = PorcelainSurfaceVariant,
    onSurfaceVariant = SlateTextSecondaryLight,
    outline = SlateBorderLight,
    outlineVariant = Color(0xFFE5ECF0),
    error = JunkCrimsonLight,
    onError = Color.White,
    errorContainer = JunkCrimsonContainerLight,
    onErrorContainer = JunkCrimsonLight
)

private val DarkColorScheme = darkColorScheme(
    primary = Emerald400,
    onPrimary = Obsidian950,
    primaryContainer = Emerald900,
    onPrimaryContainer = Emerald100,
    secondary = Color(0xFF94B2C2),
    onSecondary = Obsidian950,
    secondaryContainer = Obsidian800,
    onSecondaryContainer = SlateTextPrimaryDark,
    tertiary = PromotionIndigoDark,
    onTertiary = Obsidian950,
    tertiaryContainer = PromotionIndigoContainerDark,
    onTertiaryContainer = Color(0xFFC7D2FE),
    background = DarkBg,
    onBackground = SlateTextPrimaryDark,
    surface = DarkSurface,
    onSurface = SlateTextPrimaryDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = SlateTextSecondaryDark,
    outline = SlateBorderDark,
    outlineVariant = Color(0xFF1C2C38),
    error = JunkCrimsonDark,
    onError = Obsidian950,
    errorContainer = JunkCrimsonContainerDark,
    onErrorContainer = Color(0xFFFECACA)
)

private val TextSentryShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TextSentryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val semanticColors = if (darkTheme) DarkTextSentryColors else LightTextSentryColors

    CompositionLocalProvider(LocalTextSentryColors provides semanticColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TextSentryTypography,
            shapes = TextSentryShapes,
            content = content
        )
    }
}
