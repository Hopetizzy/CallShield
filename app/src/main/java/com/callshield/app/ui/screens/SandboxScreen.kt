package com.callshield.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
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
import com.callshield.app.ui.theme.*

@Composable
fun SandboxScreen(viewModel: MainViewModel) {
    val sandboxState by viewModel.sandboxState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "THREAT SIMULATOR",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = NeonPurple,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "REAL-TIME OFFLINE RULE & PATTERN SANDBOX",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
            }
        }

        // Test Input Console
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberSurface)
                    .border(1.dp, NeonPurple.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "SIMULATE INCOMING CALL NUMBER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = NeonCyan
                    )

                    OutlinedTextField(
                        value = sandboxState.testNumberInput,
                        onValueChange = { viewModel.onSandboxInputChange(it) },
                        placeholder = { Text("e.g. +2342018889999, FASTLOAN, or 0700000000", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "SIMULATE SMS MESSAGE BODY (OPTIONAL)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = NeonAmber
                    )

                    OutlinedTextField(
                        value = sandboxState.testSmsBodyInput,
                        onValueChange = { viewModel.onSandboxSmsBodyChange(it) },
                        placeholder = { Text("e.g. 'Your BVN will be blocked today' or 'We will post your picture to contacts'", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonAmber,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick Sample Presets
                    Text(
                        text = "QUICK TEST PRESETS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetChip("+234 2... (VoIP)", "+2342019876543") { viewModel.onSandboxInputChange(it) }
                        PresetChip("07000... (Loan Shark)", "07000001234") { viewModel.onSandboxInputChange(it) }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetChip("Defamation Threat SMS", "FASTLOAN") {
                            viewModel.onSandboxInputChange(it)
                            viewModel.onSandboxSmsBodyChange("Your loan is overdue. We will post your picture to all your whatsapp contacts today!")
                        }
                        PresetChip("Fake BVN Freeze SMS", "NIBSS_ALERT") {
                            viewModel.onSandboxInputChange(it)
                            viewModel.onSandboxSmsBodyChange("FINAL WARNING: Your BVN block has been initiated with EFCC litigation for default.")
                        }
                    }

                    Button(
                        onClick = { viewModel.runSandboxSimulation() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = TextPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "EXECUTE SIMULATION",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // SMS Simulation Result HUD
        sandboxState.smsResult?.let { smsRes ->
            item {
                val smsBannerColor = if (smsRes.isSpam) NeonCrimson else NeonEmerald
                val smsBannerBg = if (smsRes.isSpam) NeonCrimsonGlow else NeonEmeraldGlow

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(smsBannerBg, CyberSurfaceElevated, CyberSurface)
                            )
                        )
                        .border(1.5.dp, smsBannerColor, RoundedCornerShape(16.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (smsRes.isSpam) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = smsBannerColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = if (smsRes.isSpam) "PREDATORY SMS QUARANTINED OFFLINE" else "SMS ALLOWED (CLEAN)",
                                        color = smsBannerColor,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = if (smsRes.isSpam) "Isolated from inbox. Zero notification." else "Legitimate text message.",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            if (smsRes.threatCategory != null) {
                                GlowingBadge(text = smsRes.threatCategory.name.replace("_", " "), color = smsBannerColor)
                            }
                        }

                        Divider(color = CyberCardBorder)

                        Text(
                            text = "REASON: ${smsRes.reason}",
                            color = if (smsRes.isSpam) NeonAmber else NeonEmerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GlowingBadge(text = "SCAN LATENCY: ${smsRes.evaluationTimeMs}ms", color = NeonCyan)
                            Text("100% OFFLINE SCAN", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Simulation Results HUD
        sandboxState.result?.let { result ->
            item {
                val bannerColor = if (result.isBlocked) NeonCrimson else NeonEmerald
                val bannerBg = if (result.isBlocked) NeonCrimsonGlow else NeonEmeraldGlow

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(bannerBg, CyberSurfaceElevated, CyberSurface)
                            )
                        )
                        .border(1.5.dp, bannerColor, RoundedCornerShape(16.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (result.isBlocked) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = bannerColor,
                                    modifier = Modifier.size(28.dp)
                                )
                                Column {
                                    Text(
                                        text = if (result.isBlocked) "CALL WOULD BE INTERCEPTED & DROPPED" else "CALL WOULD BE ALLOWED TO RING",
                                        color = bannerColor,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = if (result.isBlocked) "Phone remains silent. Zero ring." else "Legitimate caller. Phone will ring.",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Divider(color = CyberCardBorder)

                        // Normalized Formats & Subnet Trunk
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("E.164 INTERNATIONAL", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text(sandboxState.normalizedInternational.ifEmpty { "N/A" }, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("DETECTED TRUNK SUBNET", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text(sandboxState.detectedTrunk.ifEmpty { "N/A" }, color = NeonAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                        }

                        // Burst Lockout Alert
                        if (sandboxState.isAdaptiveSubnetTriggered) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeonAmber.copy(alpha = 0.15f))
                                    .border(1.dp, NeonAmber, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = NeonAmber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "⚡ ADAPTIVE SUBNET SHIELD ACTIVATED: Rotating trunk [${sandboxState.detectedTrunk}*] locked out for 24 hours.",
                                        color = NeonAmber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Trigger Details
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberSurfaceElevated)
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("EVALUATION REASON", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text(
                                    text = result.reason,
                                    color = if (result.isBlocked) NeonCrimson else NeonEmerald,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                if (result.matchedRule != null) {
                                    Text(
                                        text = "Rule Type: ${result.matchedRule.ruleType} | Pattern: ${result.matchedRule.pattern}",
                                        color = NeonCyan,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Latency telemetry
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GlowingBadge(text = "EVALUATION LATENCY: ${result.evaluationTimeMs}ms", color = NeonCyan)
                            Text("100% OFFLINE", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PresetChip(label: String, number: String, onSelect: (String) -> Unit) {
    Button(
        onClick = { onSelect(number) },
        colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceElevated),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        modifier = Modifier.border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
    ) {
        Text(text = label, color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
    }
}
