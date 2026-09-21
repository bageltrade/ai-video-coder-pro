package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = StudioCyan,
    onPrimary = Color(0xFF00363A),
    primaryContainer = Color(0xFF004F53),
    onPrimaryContainer = Color(0xFF70F5FF),
    secondary = StudioMagenta,
    onSecondary = Color(0xFF5E002B),
    secondaryContainer = Color(0xFF86003E),
    onSecondaryContainer = Color(0xFFFFD9E2),
    tertiary = StudioAmber,
    onTertiary = Color(0xFF452B00),
    background = StudioBackground,
    onBackground = TextPrimary,
    surface = StudioSurface,
    onSurface = TextPrimary,
    surfaceVariant = StudioSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = StudioSurfaceBorder,
    outlineVariant = Color(0xFF1E283D)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // We enforce the Studio Dark theme for high-end video editing and coder experience
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
