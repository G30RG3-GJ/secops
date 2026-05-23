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
import com.example.models.PacketItem
import com.example.ui.theme.TerminalBackground
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.SecOpsError

@Composable
fun WiresharkSimulator(
    packets: List<PacketItem>,
    isCapturing: Boolean,
    onToggleCapture: () -> Unit,
    onClearPackets: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPacket by remember { mutableStateOf<PacketItem?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1218))
            .border(1.dp, Color(0xFF1E2638))
            .padding(12.dp)
    ) {
        // Control Bar Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Wireshark simulation icon",
                    tint = TerminalGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "WIRESHARK CAPTURE CONSOLE",
                        color = TerminalGreen,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Dynamic Socket Promiscuous Sniffer Simulation",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
            
            Row {
                Button(
                    onClick = onToggleCapture,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCapturing) SecOpsError else TerminalGreen
                    ),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = if (isCapturing) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = "Trigger capture flow control",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCapturing) "STOP MONITOR" else "START CAPTURE",
                        color = Color.Black,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onClearPackets,
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = "CLEAR BUFFER",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFF232B3B), thickness = 1.dp)

        // Panel Split Layout (Packets timeline on top, details raw ASCII hex at bottom)
        Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 8.dp)) {
            if (packets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "NO NETWORK FRAMES CAPTURED. START AUDIT SNIFFER...",
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(packets) { packet ->
                        val isSelected = selectedPacket?.id == packet.id
                        val protoColor = when(packet.protocol) {
                            "TCP" -> Color(0xFFD4E3FC)
                            "UDP" -> Color(0xFFFBE4D8)
                            "DNS" -> Color(0xFFE2F0D9)
                            "HTTP" -> Color(0xFFFFF2CC)
                            "ARP" -> Color(0xFFE4F1FE)
                            else -> Color(0xFFE5E5E5)
                        }
                        val textCol = Color.Black

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .background(
                                    if (isSelected) TerminalGreen.copy(alpha = 0.25f) else protoColor,
                                    shape = RoundedCornerShape(2.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) TerminalGreen else Color.Transparent,
                                    shape = RoundedCornerShape(2.dp)
                                )
                                .clickable { selectedPacket = packet }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = String.format("%03d", packet.id),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) TerminalGreen else textCol,
                                modifier = Modifier.width(36.dp)
                            )
                            Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "${packet.source} ➔ ${packet.destination}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) Color.White else textCol
                                    )
                                    Text(
                                        text = "[${packet.protocol}]",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) TerminalGreen else textCol
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = packet.info,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    color = if (isSelected) Color.LightGray else Color.DarkGray
                                )
                            }
                            Text(
                                text = "${packet.length}B",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = if (isSelected) Color.White else textCol,
                                modifier = Modifier.width(42.dp)
                            )
                        }
                    }
                }
            }
        }

        // Hex & Details View at bottom (collapsible sliding section)
        AnimatedVisibility(
            visible = selectedPacket != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            selectedPacket?.let { packet ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(top = 8.dp)
                        .background(TerminalBackground, shape = RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFF2B364C), shape = RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FRAME HIGHLIGHT HEX DUMP DETAILS (ID: ${packet.id})",
                            color = TerminalCyan,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Offset: 0000 | Proto: ${packet.protocol} | Src: ${packet.source} | Dst: ${packet.destination}",
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF030508))
                            .padding(6.dp)
                    ) {
                        Text(
                            text = packet.hexDump,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TerminalGreen,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}
