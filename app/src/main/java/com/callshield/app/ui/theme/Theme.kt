package com.callshield.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CyberColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = CyberBlack,
    primaryContainer = CyberSurfaceElevated,
    onPrimaryContainer = NeonCyan,
    secondary = NeonEmerald,
    onSecondary = CyberBlack,
    secondaryContainer = CyberSurfaceElevated,
    onSecondaryContainer = NeonEmerald,
    tertiary = NeonPurple,
    error = NeonCrimson,
    onError = CyberBlack,
    background = CyberBackground,
    onBackground = TextPrimary,
    surface = CyberSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = CyberCardBorder
)

@Composable
fun CallShieldTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = CyberBackground.toArgb()
            window.navigationBarColor = CyberBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = CyberColorScheme,
        typography = CyberTypography,
        content = content
    )
}
