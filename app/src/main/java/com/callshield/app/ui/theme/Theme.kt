package com.callshield.app.ui.theme

import android.app.Activity
import androidx.compose.material3.ColorScheme
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

private val TitaniumColorScheme = darkColorScheme(
    primary = TitaniumAccent,
    onPrimary = TitaniumBackground,
    primaryContainer = TitaniumSurfaceElevated,
    onPrimaryContainer = TitaniumAccent,
    secondary = TitaniumEmerald,
    onSecondary = TitaniumBackground,
    secondaryContainer = TitaniumSurfaceElevated,
    onSecondaryContainer = TitaniumEmerald,
    tertiary = TitaniumMuted,
    error = TitaniumCrimson,
    onError = TitaniumBackground,
    background = TitaniumBackground,
    onBackground = TextPrimary,
    surface = TitaniumSurface,
    onSurface = TextPrimary,
    surfaceVariant = TitaniumSurfaceElevated,
    onSurfaceVariant = TitaniumMuted,
    outline = TitaniumCardBorder
)

private val MatrixColorScheme = darkColorScheme(
    primary = MatrixGreen,
    onPrimary = MatrixBackground,
    primaryContainer = MatrixSurfaceElevated,
    onPrimaryContainer = MatrixGreen,
    secondary = MatrixGreen,
    onSecondary = MatrixBackground,
    secondaryContainer = MatrixSurfaceElevated,
    onSecondaryContainer = MatrixGreen,
    tertiary = MatrixDimGreen,
    error = MatrixRed,
    onError = MatrixBackground,
    background = MatrixBackground,
    onBackground = MatrixGreen,
    surface = MatrixSurface,
    onSurface = MatrixGreen,
    surfaceVariant = MatrixSurfaceElevated,
    onSurfaceVariant = MatrixDimGreen,
    outline = MatrixCardBorder
)

@Composable
fun CallShieldTheme(
    theme: AppTheme = AppTheme.CYBERPUNK,
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme = when (theme) {
        AppTheme.CYBERPUNK -> CyberColorScheme
        AppTheme.TITANIUM -> TitaniumColorScheme
        AppTheme.MATRIX -> MatrixColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val bgArgb = colorScheme.background.toArgb()
            window.statusBarColor = bgArgb
            window.navigationBarColor = bgArgb
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CyberTypography,
        content = content
    )
}
