package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.NetworkNode
import com.example.ui.theme.TerminalBackground
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.SecOpsError
import com.example.ui.theme.SecOpsWarning

@Composable
fun NmapSimulator(
    nodes: List<NetworkNode>,
    isScanning: Boolean,
    onRunScan: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedNodeIp by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1218))
            .border(1.dp, Color(0xFF1E2638))
            .padding(12.dp)
    ) {
        // Nmap Command Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Nmap icon",
                    tint = TerminalGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "NMAP DISCOVERY PROBE",
                        color = TerminalGreen,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Hosts Discovery & Active Port Scanner",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }

            Row {
                Button(
                    onClick = { onRunScan(false) },
                    enabled = !isScanning,
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalCyan),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Ping sweep scan link",
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PING SWEEP",
                        color = Color.Black,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onRunScan(true) },
                    enabled = !isScanning,
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Full audit port scanning link",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SERVICE AUDIT",
                        color = Color.Black,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFF232B3B), thickness = 1.dp)

        // Progress indicator
        if (isScanning) {
            LinearProgressIndicator(
                color = TerminalGreen,
                trackColor = Color(0xFF161F2C),
                modifier = Modifier.fillMaxWidth().height(4.dp).padding(vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalBackground),
                modifier = Modifier.fillMaxWidth().border(1.dp, TerminalGreen.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "EXECUTING: nmap -sV -p- -T4 --open 192.168.1.0/24",
                        color = TerminalGreen,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Synthesizing SYN packets. Probing TCP standard ports list. Standby...",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Host node discovery list
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(nodes) { node ->
                val isExpanded = expandedNodeIp == node.ip
                val isVulnerable = node.status == "Vulnerable"
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isExpanded) Color(0xFF161C26) else Color(0xFF11141C)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = if (isVulnerable) SecOpsError.copy(alpha = 0.4f) else Color(0xFF1F2837),
                            shape = RoundedCornerShape(4.dp)
                        ),
                    shape = RoundedCornerShape(4.dp),
                    onClick = { expandedNodeIp = if (isExpanded) null else node.ip }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isVulnerable) Icons.Default.Warning else Icons.Default.Info,
                                    contentDescription = "Audit state symbol",
                                    tint = if (isVulnerable) SecOpsError else TerminalGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = node.ip,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${node.vendor} | Latency: ${node.latencyMs}ms",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isVulnerable) SecOpsError.copy(alpha = 0.15f) else Color(0xFF1A382A)
                                )
                            ) {
                                Text(
                                    text = if (isVulnerable) "AUDIT ALERT" else "SECURE",
                                    color = if (isVulnerable) SecOpsError else TerminalGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Collapsible detailed listing of uncovered services/ports
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .background(TerminalBackground, shape = RoundedCornerShape(2.dp))
                                    .border(1.dp, Color(0xFF1C2230), shape = RoundedCornerShape(2.dp))
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "MAC: ${node.mac}",
                                        color = Color.LightGray,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "Ports Uncovered: ${node.openPorts.size}",
                                        color = TerminalCyan,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )
                                }

                                if (node.openPorts.isEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "No open TCP ports detected. Target is well-firewalled against simple port sweeps.",
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "PORT     STATE    SERVICE DETAILS",
                                        color = TerminalGreen,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    HorizontalDivider(color = Color(0xFF1E2837), modifier = Modifier.padding(vertical = 4.dp))
                                    
                                    node.openPorts.forEach { port ->
                                        val details = node.serviceDetails[port] ?: "TCP Diagnostic Endpoint"
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = String.format("%-8s", "$port/tcp"),
                                                color = TerminalCyan,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.sp,
                                                modifier = Modifier.width(60.dp)
                                            )
                                            Text(
                                                text = "open",
                                                color = SecOpsWarning,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.sp,
                                                modifier = Modifier.width(44.dp)
                                            )
                                            Text(
                                                text = details,
                                                color = Color.White,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.sp,
                                                maxLines = 1,
                                                modifier = Modifier.weight(1f)
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
    }
}
