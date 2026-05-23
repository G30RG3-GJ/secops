package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
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
import com.example.ui.theme.SecOpsPrimary
import com.example.ui.theme.SecOpsError
import com.example.ui.theme.SecOpsWarning
import com.example.ui.theme.SecOpsSuccess

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KaliToolsPanel(
    nodes: List<NetworkNode>,
    metasploitStep: String,
    metasploitTargetNode: NetworkNode?,
    metasploitSelectedExploit: String,
    metasploitLport: String,
    metasploitMeterpreterOutput: List<String>,
    metasploitProgress: Float,
    onSelectMetasploitExploit: (String) -> Unit,
    onSetMetasploitLport: (String) -> Unit,
    onRunMetasploitExploit: (NetworkNode) -> Unit,
    onRunMeterpreterCommand: (String) -> Unit,
    burpIsScanning: Boolean,
    burpScanProgress: Float,
    burpScannedUrls: List<String>,
    burpDiscoveredVulnerabilities: List<String>,
    burpIntruderPayloadsTried: Int,
    onRunBurpScan: () -> Unit,
    onStopBurpScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeToolSubTab by remember { mutableStateOf("METASPLOIT") } // "METASPLOIT" or "BURP_SUITE"
    var meterpreterInputText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1218))
            .border(1.dp, Color(0xFF1E2638))
            .padding(12.dp)
    ) {
        // Tab selectors
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { activeToolSubTab = "METASPLOIT" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeToolSubTab == "METASPLOIT") SecOpsPrimary else Color(0xFF1B1D22)
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.weight(1f).height(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Metasploit Framework Icon",
                    tint = if (activeToolSubTab == "METASPLOIT") Color.Black else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "METASPLOIT CONSOLE",
                    color = if (activeToolSubTab == "METASPLOIT") Color.Black else Color.LightGray,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = { activeToolSubTab = "BURP_SUITE" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeToolSubTab == "BURP_SUITE") SecOpsPrimary else Color(0xFF1B1D22)
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.weight(1f).height(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = "Burp Suite Icon",
                    tint = if (activeToolSubTab == "BURP_SUITE") Color.Black else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "BURP WEB PROXY",
                    color = if (activeToolSubTab == "BURP_SUITE") Color.Black else Color.LightGray,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        HorizontalDivider(color = Color(0xFF232B3B), thickness = 1.dp)
        Spacer(modifier = Modifier.height(10.dp))

        // Tool Content
        if (activeToolSubTab == "METASPLOIT") {
            // Metasploit Panel Exploit Manager
            Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (metasploitStep == "IDLE") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "METASPLOIT AUXILIARY EXPLOIT LAUNCHER",
                            color = TerminalGreen,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Select a passive scan-discovered target host and audit payload modules below to verify vulnerabilities cleanly inside sandboxed boundaries.",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )

                        // Target Selector
                        Text("TARGET NODE SUB-HOST:", color = Color.White, style = MaterialTheme.typography.labelSmall)
                        val vulnNode = nodes.firstOrNull { it.status == "Vulnerable" } ?: nodes.firstOrNull()
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF11141C)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF232D3F), RoundedCornerShape(4.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Target details",
                                        tint = TerminalCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = vulnNode?.ip ?: "192.168.1.102",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "Ports expose: ${vulnNode?.openPorts?.joinToString(", ") ?: "22, 80, 8080"}",
                                            color = Color.Gray,
                                            fontSize = 9.sp
                                        )
                                    }
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SecOpsError.copy(alpha = 0.15f))
                                ) {
                                    Text(
                                        text = "VULNERABLE",
                                        color = SecOpsError,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Exploit catalog module selection
                        Text("EXPLOIT REMOTING MODULES:", color = Color.White, style = MaterialTheme.typography.labelSmall)
                        val exploitsList = listOf(
                            "exploit/multi/http/tomcat_mgr_deploy",
                            "exploit/linux/ssh/ssh_login_bruteforce",
                            "exploit/unix/ftp/vsftpd_234_backdoor"
                        )

                        exploitsList.forEach { exp ->
                            val isSelected = metasploitSelectedExploit == exp
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSelected) SecOpsPrimary.copy(alpha = 0.12f) else Color(0xFF11141C),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) SecOpsPrimary else Color(0xFF1E2638),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable { onSelectMetasploitExploit(exp) }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelectMetasploitExploit(exp) },
                                    colors = RadioButtonDefaults.colors(selectedColor = SecOpsPrimary)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(
                                        text = exp,
                                        color = if (isSelected) SecOpsPrimary else Color.White,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = when(exp) {
                                            "exploit/multi/http/tomcat_mgr_deploy" -> "Exploits weak administrative console deployments to inject shells."
                                            "exploit/linux/ssh/ssh_login_bruteforce" -> "Perform automated dictionary testing audits for SSH credentials configurations."
                                            else -> "Leverages unpatched legacy command shells diagnostic ports vulnerabilities."
                                        },
                                        color = Color.Gray,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }

                        // Local binding Lport parameter
                        Text("LOCAL PORT HANDLER (LPORT):", color = Color.White, style = MaterialTheme.typography.labelSmall)
                        TextField(
                            value = metasploitLport,
                            onValueChange = { onSetMetasploitLport(it) },
                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.White),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF11141C),
                                unfocusedContainerColor = Color(0xFF11141C),
                                focusedIndicatorColor = SecOpsPrimary,
                                unfocusedIndicatorColor = Color(0xFF232D3F)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { vulnNode?.let { onRunMetasploitExploit(it) } },
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Run Exploit button link", tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("DEPLOY HARVESTING EXPLOIT", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                } else {
                    // Exploit is active or shell is open
                    Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (metasploitStep == "EXPLOITING") "ATTACKING PROTOCOL ENGINE..." else "METERPRETER CLI SESSION ACTIVE",
                                    color = if (metasploitStep == "EXPLOITING") SecOpsWarning else SecOpsSuccess,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    color = if (metasploitStep == "EXPLOITING") SecOpsWarning else SecOpsSuccess,
                                    modifier = Modifier.size(12.dp)
                                )
                            }

                            Button(
                                onClick = { onRunMeterpreterCommand("exit") },
                                colors = ButtonDefaults.buttonColors(containerColor = SecOpsError),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Abort Metasploit link", tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("CLOSE SESSION", color = Color.White, fontSize = 9.sp)
                            }
                        }

                        if (metasploitStep == "EXPLOITING") {
                            LinearProgressIndicator(
                                progress = metasploitProgress,
                                color = SecOpsWarning,
                                trackColor = Color(0xFF161F2C),
                                modifier = Modifier.fillMaxWidth().height(4.dp).padding(vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Live Terminal Area
                        Card(
                            colors = CardDefaults.cardColors(containerColor = TerminalBackground),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF23324C), RoundedCornerShape(4.dp))
                        ) {
                            Column(modifier = Modifier.padding(8.dp).fillMaxSize()) {
                                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                    LazyColumn(modifier = Modifier.fillMaxSize(), reverseLayout = true) {
                                        items(metasploitMeterpreterOutput.reversed()) { line ->
                                            Text(
                                                text = line,
                                                color = if (line.startsWith("[+]") || line.contains("FOUND")) SecOpsSuccess 
                                                else if (line.startsWith("[-]")) SecOpsError 
                                                else if (line.startsWith("[*]")) SecOpsWarning
                                                else if (line.trim().startsWith("meterpreter >")) TerminalCyan
                                                else TerminalGreen,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.5.sp,
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }
                                }

                                if (metasploitStep == "METERPRETER_SHELL") {
                                    HorizontalDivider(color = Color(0xFF1F293D), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                                    // Macro triggers
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf("sysinfo", "dump_creds", "network_audit", "help").forEach { macro ->
                                            OutlinedButton(
                                                onClick = { onRunMeterpreterCommand(macro) },
                                                shape = RoundedCornerShape(2.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TerminalCyan),
                                                modifier = Modifier.height(26.dp)
                                            ) {
                                                Text(macro, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                            }
                                        }
                                    }

                                    // Custom user console input
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF040608))
                                            .border(1.dp, Color(0xFF1A263D))
                                            .padding(horizontal = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("meterpreter > ", color = TerminalCyan, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                        TextField(
                                            value = meterpreterInputText,
                                            onValueChange = { meterpreterInputText = it },
                                            singleLine = true,
                                            placeholder = {
                                                Text("sysinfo, hashdump etc...", color = Color.DarkGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                            },
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                                            modifier = Modifier.weight(1f).height(42.dp)
                                        )

                                        IconButton(
                                            onClick = {
                                                if (meterpreterInputText.isNotBlank()) {
                                                    onRunMeterpreterCommand(meterpreterInputText)
                                                    meterpreterInputText = ""
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.Send, contentDescription = "Send Meterpreter Command Link", tint = TerminalGreen, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Burp Suite Web Proxy Application Scan Layout
            Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "WEB APPLICATION SECURITY SCANNER",
                            color = TerminalGreen,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Injecting fuzz injection payloads dynamically to verify server input hygiene.",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }

                    Button(
                        onClick = { if (burpIsScanning) onStopBurpScan() else onRunBurpScan() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (burpIsScanning) SecOpsError else TerminalGreen
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = if (burpIsScanning) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = "Trigger Burp Fuzz loop",
                            tint = if (burpIsScanning) Color.White else Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (burpIsScanning) "STOP SCAN" else "START FUZZ",
                            color = if (burpIsScanning) Color.White else Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (burpIsScanning) {
                    LinearProgressIndicator(
                        progress = burpScanProgress,
                        color = TerminalCyan,
                        trackColor = Color(0xFF161F2C),
                        modifier = Modifier.fillMaxWidth().height(4.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = TerminalBackground),
                        modifier = Modifier.fillMaxWidth().border(1.dp, TerminalCyan.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "ACTIVE INTERCEPTOR PROXY: INTERCEPTING...",
                                color = TerminalCyan,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Current Payload Fuzz Stream: ${if (burpScannedUrls.isNotEmpty()) burpScannedUrls[burpIntruderPayloadsTried % burpScannedUrls.size] else "Idle"}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Form vectors evaluated: ${burpIntruderPayloadsTried} input mutations injected.",
                                color = Color.Gray,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Intercepted
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF11141C), shape = RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFF1E2638), shape = RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("URLS DISCOVERED", color = Color.Gray, fontSize = 8.5.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${burpScannedUrls.size}", color = TerminalGreen, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Vulnerability matches
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF11141C), shape = RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFF1E2638), shape = RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("THREAT VECTORS Match", color = Color.Gray, fontSize = 8.5.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${burpDiscoveredVulnerabilities.size}",
                                color = if (burpDiscoveredVulnerabilities.isNotEmpty()) SecOpsError else TerminalGreen,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Vulnerabilities List
                Text("VULNERATIVE FINDINGS REGISTERED:", color = Color.White, style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(6.dp))

                if (burpDiscoveredVulnerabilities.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(TerminalBackground, shape = RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFF1B2230), shape = RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NO ADVERSE FINDINGS PRESENT. CLICK START FUZZ.",
                            color = Color.DarkGray,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(burpDiscoveredVulnerabilities) { vulnerability ->
                            val isHigh = vulnerability.contains("High") || vulnerability.contains("SQL") || vulnerability.contains("Path")
                            val isMed = vulnerability.contains("Medium") || vulnerability.contains("XSS")
                            val isLow = vulnerability.contains("Low")

                            val severityColor = if (isHigh) SecOpsError else if (isMed) SecOpsWarning else TerminalCyan
                            val severityLabel = if (isHigh) "HIGH SECURITY RISK" else if (isMed) "MEDIUM AUDIT WARNING" else "LOW CONTEXT ALERT"

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF11141C)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, severityColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = severityLabel,
                                            color = severityColor,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = severityColor.copy(alpha = 0.12f))
                                        ) {
                                            Text(
                                                text = if (isHigh) "MUTATE" else if (isMed) "FILTER" else "AUDIT",
                                                color = severityColor,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = vulnerability,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
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
}
