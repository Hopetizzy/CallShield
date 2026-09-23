package com.callshield.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
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
                        color = NeonCyan,
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

                GlowingBadge(
                    text = if (isArmed) "GRID: ONLINE" else "GRID: OFFLINE",
                    color = if (isArmed) NeonEmerald else NeonCrimson
                )
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
            }
        }
    }
}
