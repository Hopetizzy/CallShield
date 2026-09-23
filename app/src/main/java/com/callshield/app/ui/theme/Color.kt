package com.callshield.app.ui.theme

import androidx.compose.ui.graphics.Color

enum class AppTheme(val displayName: String, val subtitle: String) {
    CYBERPUNK("Cyberpunk Neon", "High-contrast OLED black with electric cyan & crimson"),
    TITANIUM("Stealth Titanium", "Minimalist luxury matte graphite with brushed slate"),
    MATRIX("Holographic Matrix", "Retro green phosphor terminal with dark CRT grid")
}

// -------------------------------------------------------------
// 1. CYBERPUNK NEON (DEFAULT)
// -------------------------------------------------------------
val CyberBlack = Color(0xFF070A10)
val CyberBackground = Color(0xFF0A0E17)
val CyberSurface = Color(0xFF121826)
val CyberSurfaceElevated = Color(0xFF1A2234)
val CyberCardBorder = Color(0xFF222F48)

val NeonCyan = Color(0xFF00F0FF)
val NeonCyanGlow = Color(0x3300F0FF)
val NeonEmerald = Color(0xFF00FF88)
val NeonEmeraldGlow = Color(0x3300FF88)
val NeonCrimson = Color(0xFFFF0055)
val NeonCrimsonGlow = Color(0x33FF0055)
val NeonAmber = Color(0xFFFFB800)
val NeonPurple = Color(0xFF9D00FF)

val TextPrimary = Color(0xFFF1F5F9)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)
val TextAccent = Color(0xFF38BDF8)

// -------------------------------------------------------------
// 2. STEALTH TITANIUM (MINIMALIST LUXURY)
// -------------------------------------------------------------
val TitaniumBackground = Color(0xFF0C0E12)
val TitaniumSurface = Color(0xFF14171E)
val TitaniumSurfaceElevated = Color(0xFF1D222C)
val TitaniumCardBorder = Color(0xFF2C3342)
val TitaniumAccent = Color(0xFFE2E8F0)
val TitaniumMuted = Color(0xFF717F94)
val TitaniumEmerald = Color(0xFF10B981)
val TitaniumCrimson = Color(0xFFEF4444)

// -------------------------------------------------------------
// 3. HOLOGRAPHIC MATRIX (RETRO MONOSPACE PHOSPHOR)
// -------------------------------------------------------------
val MatrixBackground = Color(0xFF020703)
val MatrixSurface = Color(0xFF051508)
val MatrixSurfaceElevated = Color(0xFF0B2410)
val MatrixCardBorder = Color(0xFF123B1B)
val MatrixGreen = Color(0xFF00FF66)
val MatrixGreenGlow = Color(0x3300FF66)
val MatrixDimGreen = Color(0xFF4E9A68)
val MatrixAmber = Color(0xFFFFB800)
val MatrixRed = Color(0xFFFF3333)
