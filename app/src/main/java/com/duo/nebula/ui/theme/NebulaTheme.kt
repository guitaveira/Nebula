package com.duo.nebula.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NebulaDarkScheme = darkColorScheme(
    primary = Violet400,
    onPrimary = TextPrimary,
    primaryContainer = Violet500,
    onPrimaryContainer = TextPrimary,
    secondary = Violet300,
    onSecondary = Violet950,
    tertiary = Violet200,
    onTertiary = Violet950,
    background = Violet950,
    onBackground = TextPrimary,
    surface = Violet850,
    onSurface = TextPrimary,
    surfaceVariant = Violet700,
    onSurfaceVariant = TextMuted,
    surfaceContainerHighest = Violet800,
    outline = Violet600,
    error = ErrorCoral,
    onError = TextPrimary
)

private val NebulaLightScheme = lightColorScheme(
    primary = Violet500,
    onPrimary = TextPrimary,
    secondary = Violet400,
    background = Violet100,
    onBackground = Violet950,
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Violet200,
    error = ErrorCoral
)

/**
 * Tema visual do Nébula. Segue harmonia cromática monocromática (um único
 * matiz violeta em diferentes tons), por padrão no esquema escuro para
 * manter a identidade de "céu noturno" — mas respeita o tema claro se preferido.
 */
@Composable
fun NebulaTheme(
    useDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) NebulaDarkScheme else NebulaLightScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = NebulaTypography,
        content = content
    )
}
