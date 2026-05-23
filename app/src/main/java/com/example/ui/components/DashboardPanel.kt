package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import com.example.models.MetricItem
import com.example.models.NetworkNode
import com.example.models.PacketItem
import com.example.models.WifiNetwork
import com.example.ui.theme.TerminalBackground
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.SecOpsPrimary
import com.example.ui.theme.SecOpsError
import com.example.ui.theme.SecOpsWarning
import com.example.ui.theme.SecOpsSuccess

@Composable
fun SecurityDashboardPane(
    vulnerabilities: List<MetricItem>,
    protocols: List<MetricItem>,
    nodes: List<NetworkNode>,
    wifiNetworks: List<WifiNetwork>,
    packets: List<PacketItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F1218))
            .border(1.dp, Color(0xFF1E2638))
            .padding(12.dp)
    ) {
        // Dashboard title
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Visual dashboard index icon",
                tint = TerminalGreen,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "SECOPS SECURE TELEMETRY ENGINE",
                    color = TerminalGreen,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "System Audit Level visual telemetry dashboard",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }

        HorizontalDivider(color = Color(0xFF232B3B), thickness = 1.dp)

        Spacer(modifier = Modifier.height(12.dp))

        // Main stat items row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Stat 1: Handshakes
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(TerminalBackground, shape = RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFF131A26), shape = RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Text("ACTIVE PROBING APs", color = Color.Gray, fontSize = 9.sp, style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${wifiNetworks.size}",
                        color = TerminalCyan,
                        style = MaterialTheme.typography.displaySmall,
                        fontSize = 18.sp
                    )
                }
            }

            // Stat 2: Discovered Hosts
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(TerminalBackground, shape = RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFF131A26), shape = RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Text("ALIVE HOSTS SWEEPED", color = Color.Gray, fontSize = 9.sp, style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${nodes.size}",
                        color = TerminalGreen,
                        style = MaterialTheme.typography.displaySmall,
                        fontSize = 18.sp
                    )
                }
            }

            // Stat 3: Vulnerabilities Detected
            val vulnTotal = vulnerabilities.sumOf { it.value.toInt() }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(TerminalBackground, shape = RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFF131A26), shape = RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Text("AUDIT WARN TRACES", color = Color.Gray, fontSize = 9.sp, style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$vulnTotal",
                        color = if (vulnTotal > 0) SecOpsError else TerminalGreen,
                        style = MaterialTheme.typography.displaySmall,
                        fontSize = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Multi-view metric charts
        Text(
            text = "THREAT ANALYSIS DISCOVERY METRICS",
            color = Color.LightGray,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Circular sweep indicators row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val totalAPCount = wifiNetworks.size
            val vulnerableAPs = wifiNetworks.count { it.isVulnerable }
            val apPct = if (totalAPCount > 0) vulnerableAPs.toFloat() / totalAPCount else 0f
            SimpleCircularChart("AP Vuln", "${vulnerableAPs}/$totalAPCount", apPct, SecOpsError)

            val totalNodeCount = nodes.size
            val vulnerableNodes = nodes.count { it.status == "Vulnerable" }
            val nodePct = if (totalNodeCount > 0) vulnerableNodes.toFloat() / totalNodeCount else 0f
            SimpleCircularChart("Host Vuln", "${vulnerableNodes}/$totalNodeCount", nodePct, SecOpsWarning)

            val handshakeCaptured = wifiNetworks.count { it.handshakeCaptured }
            SimpleCircularChart("Handshakes", "$handshakeCaptured", if (totalAPCount > 0) handshakeCaptured.toFloat() / totalAPCount else 0f, SecOpsSuccess)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Raw protocols traffic analyzer bar graph simulation
        Text(
            text = "WIRESHARK PACKETS DEEP TRAFFIC SPLIT",
            color = Color.LightGray,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (protocols.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(TerminalBackground, shape = RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("WAITING FOR TRAFFIC FLOW SNIFFER ENGINE...", color = Color.DarkGray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TerminalBackground, shape = RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFF131A26), shape = RoundedCornerShape(4.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                protocols.take(4).forEach { proto ->
                    StatusMeter(
                        label = "Protocol: ${proto.label}",
                        value = proto.value,
                        maxValue = packets.size.toFloat().coerceAtLeast(1.0f),
                        unit = "Pkts",
                        color = when (proto.label) {
                            "TCP" -> TerminalGreen
                            "UDP" -> TerminalCyan
                            "DNS" -> SecOpsWarning
                            else -> Color.White
                        }
                    )
                }
            }
        }
    }
}
