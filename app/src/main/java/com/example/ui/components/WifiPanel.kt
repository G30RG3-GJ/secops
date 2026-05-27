package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.WifiNetwork
import com.example.ui.theme.TerminalBackground
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.SecOpsPrimary
import com.example.ui.theme.SecOpsError
import com.example.ui.theme.SecOpsWarning
import com.example.ui.theme.SecOpsSuccess

@Composable
fun WifiSimulator(
    networks: List<WifiNetwork>,
    isScanning: Boolean,
    currentCracking: WifiNetwork?,
    onToggleScan: () -> Unit,
    onStartCracking: (WifiNetwork) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1218))
            .border(1.dp, Color(0xFF1E2638))
            .padding(12.dp)
    ) {
        // WiFi monitoring configuration bar header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Mon0 Wi-Fi radio audit tool",
                    tint = TerminalGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "WIFI SPECTRAL AUDIT",
                        color = TerminalGreen,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "802.11 Monitor-Mode Passive handshakes tracker",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }

            Button(
                onClick = onToggleScan,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isScanning) SecOpsWarning else TerminalGreen
                ),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    imageVector = if (isScanning) Icons.Default.Stop else Icons.Default.RadioButtonChecked,
                    contentDescription = "Trigger spectral sweep scan",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isScanning) "STOP MONITOR" else "WIFI SWEEP",
                    color = Color.Black,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp
                )
            }
        }

        HorizontalDivider(color = Color(0xFF232B3B), thickness = 1.dp)

        // Capture in progress bar
        if (currentCracking != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(1.dp, SecOpsWarning.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OFF-LINE WPA DECRYPTION ATTEMPT: reaver",
                            color = SecOpsWarning,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace
                        )
                        IconButton(
                            onClick = { onStartCracking(currentCracking) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Abort cracking loop",
                                tint = SecOpsError,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Target Access point SSID: ${currentCracking.ssid}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val pct = (currentCracking.pinProgress * 100).toInt()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = { currentCracking.pinProgress },
                            color = if (pct >= 100) SecOpsSuccess else SecOpsWarning,
                            trackColor = Color(0xFF161F2C),
                            modifier = Modifier.weight(1f).height(6.5.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (pct >= 100) "CRACKED" else "$pct%",
                            color = if (pct >= 100) SecOpsSuccess else SecOpsWarning,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Available wifi systems
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(networks) { net ->
                val isTargeted = currentCracking?.ssid == net.ssid
                val hasHandshake = net.handshakeCaptured
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF11141C)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isTargeted) SecOpsWarning.copy(alpha = 0.6f) else Color(0xFF1F2837),
                            RoundedCornerShape(4.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "WiFi signal lock details",
                                    tint = if (hasHandshake) SecOpsSuccess else if (net.isVulnerable) SecOpsError else TerminalCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = net.ssid,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${net.bssid} | Channel: ${net.channel}",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${net.signalLevel} dBm",
                                    color = if (net.signalLevel > -60) SecOpsSuccess else if (net.signalLevel > -80) SecOpsWarning else SecOpsError,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (hasHandshake) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF14301B))
                                    ) {
                                        Text(
                                            text = "HANDSHAKE CAPTURED",
                                            color = SecOpsSuccess,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                } else if (net.isVulnerable) {
                                    IconButton(
                                        onClick = { onStartCracking(net) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Run crack test loop against access point",
                                            tint = SecOpsError,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2633))
                                    ) {
                                        Text(
                                            text = net.encryption,
                                            color = TerminalCyan,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
