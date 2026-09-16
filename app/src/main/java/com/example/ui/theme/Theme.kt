package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MinimalVioletContainer,
    onPrimary = MinimalVioletDark,
    primaryContainer = MinimalVioletPrimary,
    onPrimaryContainer = MinimalVioletLight,
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    background = Color(0xFF141218),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1A22),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF26242E),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF49454F),
    outlineVariant = Color(0xFF383442)
)

private val LightColorScheme = lightColorScheme(
    primary = MinimalVioletPrimary,
    onPrimary = Color.White,
    primaryContainer = MinimalVioletLight,
    onPrimaryContainer = MinimalVioletDark,
    secondary = Color(0xFF625B71),
    onSecondary = Color.White,
    secondaryContainer = MinimalVioletContainer,
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color.White,
    background = CleanMinimalBg,
    onBackground = MinimalTextPrimary,
    surface = CleanMinimalSurface,
    onSurface = MinimalTextPrimary,
    surfaceVariant = CleanMinimalSurfaceVariant,
    onSurfaceVariant = MinimalTextSecondary,
    outline = MinimalBorder,
    outlineVariant = MinimalBorderSubtle
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our bespoke curated fashion palette
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

