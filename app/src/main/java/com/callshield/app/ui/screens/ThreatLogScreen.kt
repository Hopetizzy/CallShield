package com.callshield.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callshield.app.ui.MainViewModel
import com.callshield.app.ui.components.GlowingBadge
import com.callshield.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ThreatLogScreen(viewModel: MainViewModel) {
    val blockedCalls by viewModel.allBlockedCalls.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

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
                        text = "AUDIT TRAIL OF INTERCEPTED CALLS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                }

                if (blockedCalls.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear History",
                            tint = NeonCrimson
                        )
                    }
                }
            }
        }

        if (blockedCalls.isEmpty()) {
            item {
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
                            text = "Threat Ledger is currently clear.",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "When an unauthorized VoIP or predatory call is dropped, details will appear here.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(blockedCalls, key = { it.id }) { record ->
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

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = CyberSurface,
            title = {
                Text(
                    text = "PURGE THREAT LEDGER?",
                    color = NeonCrimson,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will permanently remove all intercepted call logs from your phone's offline database.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearHistory()
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
