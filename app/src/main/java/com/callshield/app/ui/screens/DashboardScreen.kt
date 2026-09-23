package com.callshield.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callshield.app.ui.MainViewModel
import com.callshield.app.ui.components.GlowingBadge
import com.callshield.app.ui.components.HolographicRadar
import com.callshield.app.ui.components.TelemetryCard
import com.callshield.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToRules: () -> Unit,
    onNavigateToThreats: () -> Unit,
    onNavigateToSandbox: () -> Unit
) {
    val isArmed by viewModel.isShieldArmed.collectAsState()
    val totalBlocked by viewModel.totalBlockedCount.collectAsState()
    val activeRulesCount by viewModel.activeRulesCount.collectAsState()
    val recentThreats by viewModel.recentBlockedCalls.collectAsState()
    val isWhitelistOn by viewModel.isWhitelistEnabled.collectAsState()
    val isBlockPrivateOn by viewModel.blockPrivateNumbers.collectAsState()

    val totalQuarantinedSms by viewModel.totalQuarantinedSmsCount.collectAsState()
    val isSmsShieldOn by viewModel.isSmsShieldEnabled.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        ThemeSelectorDialog(
            currentTheme = currentTheme,
            onThemeSelect = { selectedTheme ->
                viewModel.setTheme(selectedTheme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CALLSHIELD AI",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "AUTONOMOUS TELECOM & SMS DEFENSE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { showThemeDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "HUD Theme",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    GlowingBadge(
                        text = if (isArmed) "GRID: ONLINE" else "GRID: OFFLINE",
                        color = if (isArmed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Quick Settings Tile Notice Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "QUICK SETTINGS TILE: Defense toggle is synced to your Android notification shade for 1-tap arm/disarm.",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // Animated Central Holographic Radar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                HolographicRadar(
                    isArmed = isArmed,
                    onToggleArm = { viewModel.toggleShield() }
                )
            }
        }

        // Live Telemetry Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TelemetryCard(
                    title = "Calls Dropped",
                    value = totalBlocked.toString(),
                    subtitle = "Calls Intercepted",
                    icon = Icons.Default.Shield,
                    accentColor = NeonCrimson,
                    modifier = Modifier.weight(1f)
                )
                TelemetryCard(
                    title = "SMS Quarantined",
                    value = totalQuarantinedSms.toString(),
                    subtitle = "Harassment Blocked",
                    icon = Icons.Default.Message,
                    accentColor = NeonAmber,
                    modifier = Modifier.weight(1f)
                )
                TelemetryCard(
                    title = "Active Rules",
                    value = activeRulesCount.toString(),
                    subtitle = "Defense Filters",
                    icon = Icons.Default.FilterAlt,
                    accentColor = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Secondary Telemetry & Safety Options
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberSurface)
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "CORE DEFENSE CONFIGURATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = NeonCyan,
                    letterSpacing = 1.sp
                )

                // Safe Contacts Whitelist Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Contacts Safe Pass-Through",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Saved contacts never get blocked, even if matching a prefix",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = isWhitelistOn,
                        onCheckedChange = { viewModel.toggleWhitelist(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberBlack,
                            checkedTrackColor = NeonEmerald,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = CyberSurfaceElevated
                        )
                    )
                }

                Divider(color = CyberCardBorder)

                // Block Private / Unknown Caller IDs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Block Restricted / Hidden Numbers",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Drop calls with concealed or blank caller identities",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = isBlockPrivateOn,
                        onCheckedChange = { viewModel.toggleBlockPrivate(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberBlack,
                            checkedTrackColor = NeonCyan,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = CyberSurfaceElevated
                        )
                    )
                }

                Divider(color = CyberCardBorder)

                // Subnet & Burst Auto-Shield (Adaptive Lockout)
                val isSubnetShieldOn by viewModel.isAutoSubnetShieldEnabled.collectAsState()
                val activeAdaptiveRules by viewModel.activeAdaptiveRules.collectAsState()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Subnet & Burst Auto-Shield",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            if (activeAdaptiveRules.isNotEmpty()) {
                                GlowingBadge(text = "${activeAdaptiveRules.size} LOCKED", color = NeonAmber)
                            }
                        }
                        Text(
                            text = "Auto-quarantines rotating /24 & /16 trunk numbers when bursts (≥3 calls/10m) are detected",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = isSubnetShieldOn,
                        onCheckedChange = { viewModel.toggleAutoSubnetShield(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberBlack,
                            checkedTrackColor = NeonAmber,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = CyberSurfaceElevated
                        )
                    )
                }

                Divider(color = CyberCardBorder)

                // Spam SMS & Recovery Message Interceptor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Spam SMS & Recovery Interceptor",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            if (totalQuarantinedSms > 0) {
                                GlowingBadge(text = "$totalQuarantinedSms THREATS", color = NeonCrimson)
                            }
                        }
                        Text(
                            text = "Silently quarantines defamation threats, fake BVN freeze scares, and predatory debt messages",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = isSmsShieldOn,
                        onCheckedChange = { viewModel.toggleSmsShield(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberBlack,
                            checkedTrackColor = NeonPurple,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = CyberSurfaceElevated
                        )
                    )
                }
            }
        }

        // Threat Simulator Quick Action Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(NeonPurple.copy(alpha = 0.2f), CyberSurface)
                        )
                    )
                    .border(1.dp, NeonPurple.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Neural Threat Simulator",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Test phone numbers before live calls to verify filter logic",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = onNavigateToSandbox,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("LAUNCH", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // Recent Intercepted Threats Preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT NEUTRALIZED THREATS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                if (recentThreats.isNotEmpty()) {
                    TextButton(onClick = onNavigateToThreats) {
                        Text("VIEW ALL", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (recentThreats.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberSurface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No threats detected yet. Defense grid is watching.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        } else {
            items(recentThreats.take(4)) { record ->
                val timeFormat = SimpleDateFormat("HH:mm:ss · dd MMM", Locale.getDefault())
                val formattedTime = timeFormat.format(Date(record.timestamp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberSurface)
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = null,
                                tint = NeonCrimson,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = record.rawNumber,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${record.matchedRuleName} (${record.matchedPattern})",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            GlowingBadge(text = "BLOCKED", color = NeonCrimson)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formattedTime,
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
    }
}

@Composable
fun ThemeSelectorDialog(
    currentTheme: AppTheme,
    onThemeSelect: (AppTheme) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CyberSurface)
                .border(1.dp, CyberCardBorder, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "HUD THEME ENGINE",
                            color = NeonCyan,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Select visual telemetry scheme",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Divider(color = CyberCardBorder)

                // Theme Options
                ThemeOptionCard(
                    title = "CYBERPUNK NEON",
                    subtitle = "High-Contrast OLED · Cyan & Crimson",
                    primaryColor = NeonCyan,
                    accentColor = NeonCrimson,
                    isSelected = currentTheme == AppTheme.CYBERPUNK,
                    onClick = { onThemeSelect(AppTheme.CYBERPUNK) }
                )

                ThemeOptionCard(
                    title = "STEALTH TITANIUM",
                    subtitle = "Gunmetal Satin · Platinum & Emerald",
                    primaryColor = TitaniumAccent,
                    accentColor = TitaniumEmerald,
                    isSelected = currentTheme == AppTheme.TITANIUM,
                    onClick = { onThemeSelect(AppTheme.TITANIUM) }
                )

                ThemeOptionCard(
                    title = "HOLOGRAPHIC MATRIX",
                    subtitle = "Phosphor Green · Terminal Cybergrid",
                    primaryColor = MatrixGreen,
                    accentColor = MatrixDimGreen,
                    isSelected = currentTheme == AppTheme.MATRIX,
                    onClick = { onThemeSelect(AppTheme.MATRIX) }
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionCard(
    title: String,
    subtitle: String,
    primaryColor: Color,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) primaryColor.copy(alpha = 0.12f) else CyberSurfaceElevated)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) primaryColor else CyberCardBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Color swatches preview
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(primaryColor)
                    )
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(accentColor)
                    )
                }

                Column {
                    Text(
                        text = title,
                        color = if (isSelected) primaryColor else TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = subtitle,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = primaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

