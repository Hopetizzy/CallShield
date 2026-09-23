package com.callshield.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callshield.app.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HolographicRadar(
    isArmed: Boolean,
    onToggleArm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarTransition")

    // Radar rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarRotation"
    )

    // Pulse wave
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val activeColor = if (isArmed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
    val surfaceElevated = MaterialTheme.colorScheme.surfaceVariant
    val surface = MaterialTheme.colorScheme.surface
    val bg = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .size(240.dp)
            .clip(CircleShape)
            .clickable { onToggleArm() },
        contentAlignment = Alignment.Center
    ) {
        // Background Radar Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2 - 16.dp.toPx()

            // Concentric Radar Rings
            drawCircle(
                color = activeColor.copy(alpha = 0.15f),
                radius = radius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = activeColor.copy(alpha = 0.25f),
                radius = radius * 0.7f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = activeColor.copy(alpha = 0.35f),
                radius = radius * 0.4f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Animated Pulse Wave
            if (isArmed) {
                drawCircle(
                    color = activeColor.copy(alpha = (1.3f - pulseScale) * 0.4f),
                    radius = radius * pulseScale,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Rotating Scanner Beam
                val angleRad = Math.toRadians(rotation.toDouble())
                val beamEnd = Offset(
                    x = center.x + radius * cos(angleRad).toFloat(),
                    y = center.y + radius * sin(angleRad).toFloat()
                )
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(activeColor.copy(alpha = 0.8f), Color.Transparent),
                        start = center,
                        end = beamEnd
                    ),
                    start = center,
                    end = beamEnd,
                    strokeWidth = 2.5.dp.toPx()
                )
            }
        }

        // Center Cyber Shield Core
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(surfaceElevated, surface, bg)
                    )
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isArmed) Icons.Filled.GppGood else Icons.Filled.GppMaybe,
                    contentDescription = if (isArmed) "Armed" else "Disarmed",
                    tint = activeColor,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isArmed) "ARMED" else "DISARMED",
                    color = activeColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (isArmed) "DEFENSE ON" else "TAP TO ARM",
                    color = TextMuted,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
