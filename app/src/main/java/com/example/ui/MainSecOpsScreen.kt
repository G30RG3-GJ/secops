package com.example.ui

import android.app.Application
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodels.SecurityViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainSecOpsScreen(
    viewModel: SecurityViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as Application
        )
    ),
    modifier: Modifier = Modifier
) {
    // 9 pages total
    val pagerState = rememberPagerState(pageCount = { 9 })
    val coroutineScope = rememberCoroutineScope()

    val packets by viewModel.packets.collectAsState()
    val isCapturing by viewModel.isCapturingPackets.collectAsState()
    val networkNodes by viewModel.networkNodes.collectAsState()
    val isScanningNetwork by viewModel.isScanningNetwork.collectAsState()
    val wifiNetworks by viewModel.wifiNetworks.collectAsState()
    val isScanningWifi by viewModel.isScanningWifi.collectAsState()
    val currentCrackingNetwork by viewModel.currentCrackedNetwork.collectAsState()
    val deviceStatus by viewModel.deviceStatus.collectAsState()
    val wifiScanMessage by viewModel.wifiScanMessage.collectAsState()
    val connectedNetworkInfo by viewModel.connectedNetworkInfo.collectAsState()

    // Metasploit
    val metasploitStep by viewModel.metasploitStep.collectAsState()
    val metasploitTargetNode by viewModel.metasploitTargetNode.collectAsState()
    val metasploitSelectedExploit by viewModel.metasploitSelectedExploit.collectAsState()
    val metasploitLport by viewModel.metasploitLport.collectAsState()
    val metasploitMeterpreterOutput by viewModel.metasploitMeterpreterOutput.collectAsState()
    val metasploitProgress by viewModel.metasploitProgress.collectAsState()

    // Burp
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

    // CCTV
    val cctvOutput by viewModel.cctvOutput.collectAsState()
    val isCctvScanning by viewModel.isCctvScanning.collectAsState()

    // SQLMap
    val sqlmapOutput by viewModel.sqlmapOutput.collectAsState()
    val isSqlmapRunning by viewModel.isSqlmapRunning.collectAsState()

    // Auto-update / About
    val updateAvailable by viewModel.updateAvailable.collectAsState()
    val latestVersion by viewModel.latestVersion.collectAsState()

    // Auto-check update on launch
    LaunchedEffect(Unit) {
        viewModel.checkForUpdate()
    }

    // Parallax background animation
    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")
    val bgOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "bg_offset"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SecOpsBackground,
        topBar = {
            Column {
                // Parallax top bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF060E14),
                                    Color(0xFF0A1628).copy(alpha = 0.95f),
                                    Color(0xFF060E14)
                                ),
                                startX = bgOffset * 300f,
                                endX = bgOffset * 300f + 800f
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Glowing dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(TerminalGreen, TerminalGreen.copy(0f))
                                        ),
                                        CircleShape
                                    )
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "KALI SECOPS CONSOLE",
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    letterSpacing = 1.5.sp
                                )
                                Text(
                                    text = "by H4cK3R  •  v3.0",
                                    color = TerminalGreen,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Update indicator
                            if (updateAvailable) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFFFCC00).copy(0.15f),
                                    border = BorderStroke(1.dp, Color(0xFFFFCC00).copy(0.5f))
                                ) {
                                    Text(
                                        "⬆ UPDATE",
                                        color = Color(0xFFFFCC00),
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                            .clickable {
                                                coroutineScope.launch { pagerState.animateScrollToPage(8) }
                                            }
                                    )
                                }
                            }
                            OutlinedButton(
                                onClick = { viewModel.resetToDefaultState() },
                                border = BorderStroke(1.dp, Color(0xFF1E3050)),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text(
                                    text = "RESET",
                                    color = Color.Gray,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // Status banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (deviceStatus.rootGranted) Color(0xFF071007) else Color(0xFF100D07)
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    if (deviceStatus.rootGranted) TerminalGreen.copy(0.3f) else SecOpsWarning.copy(0.3f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(0.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (deviceStatus.rootGranted) {
                                "✓ ROOT | aircrack:${if (deviceStatus.aircrackAvailable) "✓" else "✗"} airodump:${if (deviceStatus.airodumpAvailable) "✓" else "✗"} aireplay:${if (deviceStatus.aireplayAvailable) "✓" else "✗"}"
                            } else {
                                "⚠ NO ROOT — Real WiFi scan active | aircrack requires root"
                            },
                            color = if (deviceStatus.rootGranted) SecOpsSuccess else SecOpsWarning,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        if (connectedNetworkInfo.ssid != "Not Connected") {
                            Text(
                                text = "📶 ${connectedNetworkInfo.ssid}",
                                color = TerminalCyan,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            KaliTabBar(
                selectedIndex = pagerState.currentPage,
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
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> SecurityDashboardPane(
                        vulnerabilities = vulnerabilityMetrics,
                        protocols = protocolMetrics,
                        nodes = networkNodes,
                        wifiNetworks = wifiNetworks,
                        packets = packets,
                        modifier = Modifier.fillMaxSize()
                    )
                    1 -> WiresharkSimulator(
                        packets = packets,
                        isCapturing = isCapturing,
                        onToggleCapture = { viewModel.togglePacketCapture() },
                        onClearPackets = { viewModel.clearPackets() },
                        modifier = Modifier.fillMaxSize()
                    )
                    2 -> NmapSimulator(
                        nodes = networkNodes,
                        isScanning = isScanningNetwork,
                        onRunScan = { full -> viewModel.runNetworkDiscovery(full) },
                        modifier = Modifier.fillMaxSize()
                    )
                    3 -> WifiSimulator(
                        networks = wifiNetworks,
                        isScanning = isScanningWifi,
                        currentCracking = currentCrackingNetwork,
                        onToggleScan = { viewModel.toggleWifiScan() },
                        onStartCracking = { net -> viewModel.runAircrackSimulation(net) },
                        modifier = Modifier.fillMaxSize()
                    )
                    4 -> KaliToolsPanel(
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
                        onRunMeterpreterCommand = { _ -> (metasploitTargetNode ?: networkNodes.firstOrNull())?.let { viewModel.runMetasploitExploitation(it) } },
                        burpIsScanning = burpIsScanning,
                        burpScanProgress = burpScanProgress,
                        burpScannedUrls = burpScannedUrls,
                        burpDiscoveredVulnerabilities = burpDiscoveredVulnerabilities,
                        burpIntruderPayloadsTried = burpIntruderPayloadsTried,
                        onRunBurpScan = { viewModel.runBurpProxyScan() },
                        onStopBurpScan = { viewModel.stopBurpScan() },
                        modifier = Modifier.fillMaxSize()
                    )
                    5 -> CCTVPanel(
                        outputLines = cctvOutput,
                        isScanning = isCctvScanning,
                        onDiscoverCameras = { subnet -> viewModel.discoverCctvCameras(subnet) },
                        onCrackRtsp = { host, port -> viewModel.crackRtspCredentials(host, port) },
                        onDiscoverOnvif = { viewModel.discoverOnvifDevices() },
                        onStop = { viewModel.stopCctvScan() },
                        modifier = Modifier.fillMaxSize()
                    )
                    6 -> SqlMapPanel(
                        outputLines = sqlmapOutput,
                        isRunning = isSqlmapRunning,
                        onRunScan = { url, data, dump -> viewModel.runSqlmapScan(url, data, dump) },
                        onDetectWaf = { url -> viewModel.detectWaf(url) },
                        onRunBuiltIn = { url, data -> viewModel.runBuiltInSqliProbe(url, data) },
                        onStop = { viewModel.stopSqlmap() },
                        modifier = Modifier.fillMaxSize()
                    )
                    7 -> SecOpsSecurityAssistant(
                        chatHistory = chatHistory,
                        isLoading = aiLoading,
                        onSendMessage = { prompt -> viewModel.askSecurityAssistant(prompt) },
                        onClearChat = { viewModel.clearChat() },
                        modifier = Modifier.fillMaxSize()
                    )
                    8 -> AboutPanel(
                        appVersion = "v3.0",
                        updateAvailable = updateAvailable,
                        latestVersion = latestVersion,
                        onCheckUpdate = { viewModel.checkForUpdate() },
                        onDownloadUpdate = { viewModel.downloadUpdate() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Real-time log footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                RealtimeLogsViewer(
                    logs = logs,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// ── Tab bar data ──
private data class TabItem(val label: String, val icon: ImageVector)

private val tabItems = listOf(
    TabItem("DASH", Icons.Default.Dashboard),
    TabItem("PCAP", Icons.Default.NetworkCheck),
    TabItem("NMAP", Icons.Default.DeviceHub),
    TabItem("WIFI", Icons.Default.Wifi),
    TabItem("KALI", Icons.Default.Terminal),
    TabItem("CCTV", Icons.Default.Videocam),
    TabItem("SQL", Icons.Default.Storage),
    TabItem("AI", Icons.Default.SmartToy),
    TabItem("ABOUT", Icons.Default.Info)
)

@Composable
fun KaliTabBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A1020), Color(0xFF060C18))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, TerminalGreen.copy(0.3f), Color.Transparent)
                ),
                shape = RoundedCornerShape(0.dp)
            )
            .navigationBarsPadding()
            .padding(vertical = 4.dp)
    ) {
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(tabItems.size) { idx ->
                val item = tabItems[idx]
                val isSelected = selectedIndex == idx

                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) TerminalGreen.copy(0.12f) else Color.Transparent,
                    animationSpec = tween(200),
                    label = "tab_bg"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onTabSelected(idx) }
                        .background(bgColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .width(48.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) TerminalGreen else Color(0xFF4A6A5A),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        color = if (isSelected) Color.White else Color(0xFF4A6A5A),
                        fontSize = 7.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace
                    )
                    // Active indicator dot
                    if (isSelected) {
                        Spacer(Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(TerminalGreen, CircleShape)
                        )
                    }
                }
            }
        }
    }
}
