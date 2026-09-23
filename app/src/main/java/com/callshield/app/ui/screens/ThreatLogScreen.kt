package com.callshield.app.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callshield.app.ui.ExportState
import com.callshield.app.ui.MainViewModel
import com.callshield.app.ui.components.GlowingBadge
import com.callshield.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class ThreatLedgerTab {
    CALLS,
    SMS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreatLogScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val blockedCalls by viewModel.allBlockedCalls.collectAsState()
    val quarantinedSms by viewModel.allQuarantinedSms.collectAsState()
    val exportState by viewModel.exportState.collectAsState()

    var selectedTab by remember { mutableStateOf(ThreatLedgerTab.CALLS) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }

    // Handle Export State changes
    LaunchedEffect(exportState) {
        when (val state = exportState) {
            is ExportState.Success -> {
                try {
                    val chooserTitle = if (state.isEmail) "File FCCPC Complaint via Email" else "Share Evidence File"
                    val chooser = Intent.createChooser(state.intent, chooserTitle).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(chooser)
                } catch (e: Exception) {
                    Toast.makeText(context, "No app available to handle export: ${e.message}", Toast.LENGTH_LONG).show()
                }
                viewModel.resetExportState()
                showExportSheet = false
            }
            is ExportState.Error -> {
                Toast.makeText(context, "Export Error: ${state.message}", Toast.LENGTH_LONG).show()
                viewModel.resetExportState()
            }
            else -> {}
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "THREAT LEDGER",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = NeonCyan,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "AUDIT TRAIL OF INTERCEPTED CALLS & SMS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (blockedCalls.isNotEmpty() || quarantinedSms.isNotEmpty()) {
                        IconButton(
                            onClick = { showExportSheet = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonCyan.copy(alpha = 0.12f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = "Legal Evidence Exporter",
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { showClearDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonCrimson.copy(alpha = 0.12f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear History",
                                tint = NeonCrimson,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Segment Tab Switcher
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyberSurface)
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Calls Tab Button
                val isCallsActive = selectedTab == ThreatLedgerTab.CALLS
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isCallsActive) NeonCyan.copy(alpha = 0.15f) else CyberSurface)
                        .border(1.dp, if (isCallsActive) NeonCyan else CyberCardBorder, RoundedCornerShape(8.dp))
                        .clickable { selectedTab = ThreatLedgerTab.CALLS }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CALLS (${blockedCalls.size})",
                        color = if (isCallsActive) NeonCyan else TextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // SMS Tab Button
                val isSmsActive = selectedTab == ThreatLedgerTab.SMS
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSmsActive) NeonAmber.copy(alpha = 0.15f) else CyberSurface)
                        .border(1.dp, if (isSmsActive) NeonAmber else CyberCardBorder, RoundedCornerShape(8.dp))
                        .clickable { selectedTab = ThreatLedgerTab.SMS }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SMS (${quarantinedSms.size})",
                        color = if (isSmsActive) NeonAmber else TextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (blockedCalls.isNotEmpty() || quarantinedSms.isNotEmpty()) {
            item {
                // One-Tap Quick Export Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberSurface)
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { showExportSheet = true }
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
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "LEGAL EVIDENCE EXPORTER",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Export ${blockedCalls.size} Calls & ${quarantinedSms.size} SMS to FCCPC Dossier",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.IosShare,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // ==================== TAB 1: CALLS ====================
        if (selectedTab == ThreatLedgerTab.CALLS) {
            if (blockedCalls.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No Intercepted Calls",
                        subtitle = "When an unauthorized VoIP or predatory call is dropped, it will appear here."
                    )
                }
            } else {
                items(blockedCalls, key = { "call_${it.id}" }) { record ->
                    val timeFormat = SimpleDateFormat("HH:mm:ss · dd MMM yyyy", Locale.getDefault())
                    val formattedTime = timeFormat.format(Date(record.timestamp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CyberSurface)
                            .border(1.dp, CyberCardBorder, RoundedCornerShape(14.dp))
                            .padding(16.dp)
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
                                        imageVector = Icons.Default.CallEnd,
                                        contentDescription = null,
                                        tint = NeonCrimson,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column {
                                        Text(
                                            text = record.rawNumber,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 16.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "E.164: ${record.normalizedNumber}",
                                            color = TextMuted,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                GlowingBadge(text = "SILENTLY DROPPED", color = NeonCrimson)
                            }

                            Divider(color = CyberCardBorder)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "TRIGGERED DEFENSE RULE",
                                        color = TextMuted,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${record.matchedRuleName} [${record.matchedPattern}]",
                                        color = NeonCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = formattedTime,
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    if (record.interceptionLatencyMs > 0) {
                                        Text(
                                            text = "Latency: ${record.interceptionLatencyMs}ms",
                                            color = NeonEmerald,
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
        }

        // ==================== TAB 2: QUARANTINED SMS ====================
        if (selectedTab == ThreatLedgerTab.SMS) {
            if (quarantinedSms.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No Quarantined SMS",
                        subtitle = "Predatory debt recovery texts, BVN freeze scares, and defamation messages will be isolated here."
                    )
                }
            } else {
                items(quarantinedSms, key = { "sms_${it.id}" }) { sms ->
                    val timeFormat = SimpleDateFormat("HH:mm:ss · dd MMM yyyy", Locale.getDefault())
                    val formattedTime = timeFormat.format(Date(sms.timestamp))

                    val categoryColor = when (sms.threatCategory) {
                        com.callshield.app.data.local.entity.SmsThreatCategory.DEFAMATION_HARASSMENT -> NeonCrimson
                        com.callshield.app.data.local.entity.SmsThreatCategory.BVN_REGULATORY_SCARE -> NeonAmber
                        com.callshield.app.data.local.entity.SmsThreatCategory.PREDATORY_LOAN_RECOVERY -> NeonPurple
                        com.callshield.app.data.local.entity.SmsThreatCategory.SENDER_BLACKLISTED -> NeonCyan
                        com.callshield.app.data.local.entity.SmsThreatCategory.SUSPICIOUS_PHISHING -> NeonAmber
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CyberSurface)
                            .border(1.dp, categoryColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(16.dp)
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
                                        imageVector = Icons.Default.Message,
                                        contentDescription = null,
                                        tint = categoryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = sms.sender,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = formattedTime,
                                            color = TextMuted,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                GlowingBadge(text = sms.threatCategory.name.replace("_", " "), color = categoryColor)
                            }

                            // Message Body Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyberSurfaceElevated)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = sms.body,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 16.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "FLAGGED TRIGGER: '${sms.matchedKeywordOrPattern}'",
                                    color = categoryColor,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )

                                IconButton(
                                    onClick = { viewModel.deleteQuarantinedSms(sms) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteSweep,
                                        contentDescription = "Delete SMS",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }

    // Export Evidence Modal Sheet
    if (showExportSheet) {
        ModalBottomSheet(
            onDismissRequest = { showExportSheet = false },
            containerColor = CyberSurface,
            scrimColor = CyberBackground.copy(alpha = 0.8f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = "LEGAL EVIDENCE EXPORTER",
                        color = NeonCyan,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Statutory documentation for regulatory enforcement",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Divider(color = CyberCardBorder)

                // Option 1: PDF Complaint for FCCPC
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberCardBorder.copy(alpha = 0.3f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable(enabled = exportState !is ExportState.Generating) {
                            viewModel.exportFccpcPdf(context)
                        }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(28.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "File FCCPC Complaint (PDF)",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                GlowingBadge(text = "RECOMMENDED", color = NeonCyan)
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Formal legal evidence dossier addressed to lenderstaskforce@fccpc.gov.ng with cryptographic timestamp and SHA-256 seal.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Option 2: Forensic CSV Export
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberCardBorder.copy(alpha = 0.3f))
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                        .clickable(enabled = exportState !is ExportState.Generating) {
                            viewModel.exportEvidenceCsv(context)
                        }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(28.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Export Threat Ledger (CSV)",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Raw tabular forensic dataset containing E.164 numbers, detected trunk subnets, rule tags, and latency benchmarks.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                if (exportState is ExportState.Generating) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = NeonCyan,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Generating Offline Forensic Dossier...",
                            color = NeonCyan,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Purge History Confirmation Dialog
    if (showClearDialog) {
        val targetName = if (selectedTab == ThreatLedgerTab.CALLS) "INTERCEPTED CALLS" else "QUARANTINED SMS"
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = CyberSurface,
            title = {
                Text(
                    text = "PURGE $targetName?",
                    color = NeonCrimson,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will permanently remove all $targetName records from your phone's offline database.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedTab == ThreatLedgerTab.CALLS) {
                            viewModel.clearHistory()
                        } else {
                            viewModel.clearAllQuarantinedSms()
                        }
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCrimson)
                ) {
                    Text("PURGE ALL", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("CANCEL", color = TextMuted)
                }
            }
        )
    }
}

@Composable
fun EmptyStateCard(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CyberSurface)
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
