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
    primary = ZiaGreenDarkPrimary,
    onPrimary = Color.Black,
    primaryContainer = ZiaGreenDark,
    onPrimaryContainer = ZiaGreenLight,
    secondary = ZiaAmberWarning,
    background = ZiaDarkBackground,
    surface = ZiaDarkSurface,
    surfaceVariant = ZiaDarkSurfaceVariant,
    outline = ZiaDarkOutline,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ZiaGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = ZiaGreenContainer,
    onPrimaryContainer = ZiaGreenOnContainer,
    secondary = ZiaAmberWarning,
    background = ZiaBackgroundLight,
    surface = ZiaSurfaceLight,
    surfaceVariant = ZiaSurfaceVariant,
    outline = ZiaOutline,
    onBackground = ZiaTextPrimary,
    onSurface = ZiaTextPrimary,
    onSurfaceVariant = ZiaTextSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our handcrafted emerald commerce theme by default
    content: @Composable () -> Unit
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
