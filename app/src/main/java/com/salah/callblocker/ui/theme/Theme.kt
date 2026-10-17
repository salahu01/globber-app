package com.salah.callblocker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private fun schemeFor(c: Ceramic) = if (c.isDark) {
    darkColorScheme(
        primary = c.ink, onPrimary = c.onInk,
        secondary = c.text2, onSecondary = c.bg,
        error = c.red, onError = c.onInk,
        background = c.bg, onBackground = c.text,
        surface = c.surface, onSurface = c.text,
        surfaceVariant = c.surface2, onSurfaceVariant = c.text2,
        outline = c.line,
        surfaceContainerLowest = c.bg, surfaceContainerLow = c.surface,
        surfaceContainer = c.surface, surfaceContainerHigh = c.surface2,
        surfaceContainerHighest = c.surface2,
    )
} else {
    lightColorScheme(
        primary = c.ink, onPrimary = c.onInk,
        secondary = c.text2, onSecondary = c.bg,
        error = c.red, onError = c.onInk,
        background = c.bg, onBackground = c.text,
        surface = c.surface, onSurface = c.text,
        surfaceVariant = c.surface2, onSurfaceVariant = c.text2,
        outline = c.line,
        surfaceContainerLowest = c.surface, surfaceContainerLow = c.surface,
        surfaceContainer = c.surface, surfaceContainerHigh = c.surface2,
        surfaceContainerHighest = c.surface2,
    )
}

@Composable
fun CallBlockerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val c = if (darkTheme) CeramicDark else CeramicLight
    CompositionLocalProvider(LocalCeramic provides c) {
        MaterialTheme(
            colorScheme = schemeFor(c),
            typography = Typography,
            shapes = AppShapes,
            content = content,
        )
    }
}
