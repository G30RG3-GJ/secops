package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SsidChart
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.models.ToolCategory
import com.example.ui.components.*
import com.example.ui.theme.TerminalBackground
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.SecOpsPrimary
import com.example.ui.theme.SecOpsBackground
import com.example.ui.theme.SecOpsSurface
import com.example.ui.theme.SecOpsSurfaceVariant
import com.example.ui.theme.SecOpsOnSurface
import com.example.viewmodels.SecurityViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainSecOpsScreen(
    viewModel: SecurityViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 6 })
    val coroutineScope = rememberCoroutineScope()

    val packets by viewModel.packets.collectAsState()
    val isCapturing by viewModel.isCapturingPackets.collectAsState()

    val networkNodes by viewModel.networkNodes.collectAsState()
    val isScanningNetwork by viewModel.isScanningNetwork.collectAsState()

    val wifiNetworks by viewModel.wifiNetworks.collectAsState()
    val isScanningWifi by viewModel.isScanningWifi.collectAsState()
    val currentCrackingNetwork by viewModel.currentCrackedNetwork.collectAsState()

    // Metasploit states
    val metasploitStep by viewModel.metasploitStep.collectAsState()
    val metasploitTargetNode by viewModel.metasploitTargetNode.collectAsState()
    val metasploitSelectedExploit by viewModel.metasploitSelectedExploit.collectAsState()
    val metasploitLport by viewModel.metasploitLport.collectAsState()
    val metasploitMeterpreterOutput by viewModel.metasploitMeterpreterOutput.collectAsState()
    val metasploitProgress by viewModel.metasploitProgress.collectAsState()

    // Burp Proxy states
    val burpIsScanning by viewModel.burpIsScanning.collectAsState()
    val burpScanProgress by viewModel.burpScanProgress.collectAsState()
    val burpScannedUrls by viewModel.burpScannedUrls.collectAsState()
    val burpDiscoveredVulnerabilities by viewModel.burpDiscoveredVulnerabilities.collectAsState()
    val burpIntruderPayloadsTried by viewModel.burpIntruderPayloadsTried.collectAsState()

    val vulnerabilityMetrics by viewModel.vulnerabilityMetrics.collectAsState()
    val protocolMetrics by viewModel.protocolMetrics.collectAsState()
    val logs by viewModel.logs.collectAsState()

    val chatHistory by viewModel.aiChatHistory.collectAsState()
    val aiLoading by viewModel.aiLoading.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize().background(SecOpsBackground),
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .background(SecOpsSurface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(TerminalGreen, shape = RoundedCornerShape(5.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "KALI SECOPS CONSOLE",
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = " v2026.1",
                            color = TerminalGreen,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Reset button to default state
                    OutlinedButton(
                        onClick = { viewModel.resetToDefaultState() },
                        border = BorderStroke(1.dp, Color(0xFF263345)),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "RESET MATRIX",
                            color = Color.LightGray,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                
                // Secondary Banner warning users about educational simulation nature of Android environments safely
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13110C)),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.fillMaxWidth().border(width = 1.dp, color = Color(0xFF332010))
                    ) {
                        Text(
                            text = "⚠ WARNING: Diagnostics are designed safely inside an Android sandboxed JVM simulation for policy compliant testing and training.",
                            color = Color(0xFFEAA04C),
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
            }
        },
        bottomBar = {
            // Elegant scrolling Tab/Index Selector representing Kali diagnostic targets
            ScrollableTabRow(
                selectedPageIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SecOpsBackground)
        ) {
            // Display main slider panels representing Wireshark, Nmap, WiFi, Dashboard, and AI Copilot
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> {
                        // Visual Analytics Panel
                        SecurityDashboardPane(
                            vulnerabilities = vulnerabilityMetrics,
                            protocols = protocolMetrics,
                            nodes = networkNodes,
                            wifiNetworks = wifiNetworks,
                            packets = packets,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    1 -> {
                        // Wireshark simulate frame audits
                        WiresharkSimulator(
                            packets = packets,
                            isCapturing = isCapturing,
                            onToggleCapture = { viewModel.togglePacketCapture() },
                            onClearPackets = { viewModel.clearPackets() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    2 -> {
                        // Nmap host port sweep discovery
                        NmapSimulator(
                            nodes = networkNodes,
                            isScanning = isScanningNetwork,
                            onRunScan = { full -> viewModel.runNetworkDiscovery(full) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    3 -> {
                        // WiFi spectrum security probe
                        WifiSimulator(
                            networks = wifiNetworks,
                            isScanning = isScanningWifi,
                            currentCracking = currentCrackingNetwork,
                            onToggleScan = { viewModel.toggleWifiScan() },
                            onStartCracking = { net -> viewModel.startCrackingAttempt(net) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    4 -> {
                        // Kali advanced tools dashboard (Metasploit + Burp Suite simulation)
                        KaliToolsPanel(
                            nodes = networkNodes,
                            metasploitStep = metasploitStep,
                            metasploitTargetNode = metasploitTargetNode,
                            metasploitSelectedExploit = metasploitSelectedExploit,
                            metasploitLport = metasploitLport,
                            metasploitMeterpreterOutput = metasploitMeterpreterOutput,
                            metasploitProgress = metasploitProgress,
                            onSelectMetasploitExploit = { exp -> viewModel.selectMetasploitExploit(exp) },
                            onSetMetasploitLport = { lp -> viewModel.setMetasploitLport(lp) },
                            onRunMetasploitExploit = { host -> viewModel.runMetasploitExploitation(host) },
                            onRunMeterpreterCommand = { cmd -> (metasploitTargetNode ?: networkNodes.firstOrNull())?.let { viewModel.runMetasploitExploitation(it) } }, // Simulates continuous console input
                            burpIsScanning = burpIsScanning,
                            burpScanProgress = burpScanProgress,
                            burpScannedUrls = burpScannedUrls,
                            burpDiscoveredVulnerabilities = burpDiscoveredVulnerabilities,
                            burpIntruderPayloadsTried = burpIntruderPayloadsTried,
                            onRunBurpScan = { viewModel.runBurpProxyScan() },
                            onStopBurpScan = { viewModel.stopBurpScan() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    5 -> {
                        // Gemini security assistant advisory logic panel
                        SecOpsSecurityAssistant(
                            chatHistory = chatHistory,
                            isLoading = aiLoading,
                            onSendMessage = { prompt -> viewModel.askSecurityAssistant(prompt) },
                            onClearChat = { viewModel.clearChat() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Real-time shell console log feedback at bottom (collapsible screen footer showing exact CLI execution logic)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                RealtimeLogsViewer(
                    logs = logs,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun ScrollableTabRow(
    selectedPageIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Pair("ANALYTICS", Icons.Default.Dashboard),
        Pair("WIRESHARK", Icons.Default.NetworkCheck),
        Pair("NMAP SCAN", Icons.Default.DeviceHub),
        Pair("WIFI PROBE", Icons.Default.Radio),
        Pair("KALI TOOLS", Icons.Default.Terminal),
        Pair("AI COPILOT", Icons.Default.SmartToy)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SecOpsSurface)
            .navigationBarsPadding()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { idx, pair ->
            val isSelected = selectedPageIndex == idx
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onTabSelected(idx) }
                    .padding(vertical = 4.dp)
                    .width(60.dp)
            ) {
                Icon(
                    imageVector = pair.second,
                    contentDescription = pair.first,
                    tint = if (isSelected) TerminalGreen else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = pair.first,
                    color = if (isSelected) Color.White else Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
