package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.ExecutionLog
import com.example.ui.theme.TerminalBackground
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.SecOpsError
import com.example.ui.theme.SecOpsWarning

@Composable
fun RealtimeLogsViewer(
    logs: List<ExecutionLog>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBackground)
            .border(1.dp, Color(0xFF1E2638))
            .padding(10.dp)
    ) {
        // Log Viewer Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Console logs indicator terminal symbol",
                tint = TerminalGreen,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "KALI SEC-OPS LIVE SHELL TELEMETRY",
                color = TerminalGreen,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }

        HorizontalDivider(color = Color(0xFF232B3B), thickness = 1.dp)

        Spacer(modifier = Modifier.height(6.dp))

        // Log Entries Lazy Column List
        if (logs.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NO CONSOLE STREAM EVENTS REGISTERED.",
                    color = Color.DarkGray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(logs) { log ->
                    val statusColor = when(log.status) {
                        "SUCCESS" -> TerminalGreen
                        "FAILED" -> SecOpsError
                        "WARNING" -> SecOpsWarning
                        else -> Color.White
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF04060A))
                            .border(1.dp, Color(0xFF131824))
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "[${log.timestamp}] secops-shell# ${log.commandName}",
                                color = TerminalCyan,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            Card(
                                colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.12f))
                            ) {
                                Text(
                                    text = log.status,
                                    color = statusColor,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = log.terminalOutput,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                        if (log.durationMs > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Command executed in ${log.durationMs}ms",
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
