package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CCTVPanel(
    outputLines: List<String>,
    isScanning: Boolean,
    onDiscoverCameras: (String) -> Unit,
    onCrackRtsp: (String, Int) -> Unit,
    onDiscoverOnvif: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    var subnet by remember { mutableStateOf("192.168.1.0/24") }
    var rtspHost by remember { mutableStateOf("192.168.1.100") }
    var rtspPort by remember { mutableStateOf("554") }
    var selectedTab by remember { mutableIntStateOf(0) }

    val infiniteTransition = rememberInfiniteTransition(label = "cctv")
    val scanPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scan_pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SecOpsBackground)
    ) {
        // ── Header ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF0A1A0A), Color(0xFF001020), Color(0xFF0A1A0A))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Videocam,
                    null,
                    tint = TerminalGreen,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "CCTV CRACK",
                        color = TerminalGreen,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        letterSpacing = 2.sp
                    )
                    Text(
                        "RTSP Camera Discovery & Default Credential Tester",
                        color = Color.Gray,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(Modifier.weight(1f))
                if (isScanning) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                TerminalGreen.copy(alpha = scanPulse),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("LIVE", color = TerminalGreen, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // ── Sub-tabs ──
        val tabs = listOf("DISCOVER", "RTSP CRACK", "ONVIF", "HELP")
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF070F07),
            contentColor = TerminalGreen,
            edgePadding = 0.dp
        ) {
            tabs.forEachIndexed { i, label ->
                Tab(
                    selected = selectedTab == i,
                    onClick = { selectedTab = i },
                    text = {
                        Text(
                            label,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (selectedTab == i) TerminalGreen else Color.Gray
                        )
                    }
                )
            }
        }

        // ── Content ──
        when (selectedTab) {
            0 -> {
                // Network Discovery
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("SUBNET SCAN", color = TerminalCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Spacer(Modifier.height(8.dp))
                    TerminalTextField(
                        value = subnet,
                        onValueChange = { subnet = it },
                        label = "Subnet (CIDR)"
                    )
                    Spacer(Modifier.height(12.dp))
                    ActionButton(
                        label = if (isScanning) "SCANNING..." else "DISCOVER CAMERAS",
                        icon = Icons.Default.Search,
                        enabled = !isScanning,
                        onClick = { onDiscoverCameras(subnet) }
                    )
                }
            }
            1 -> {
                // RTSP Credential Cracker
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("TARGET CAMERA", color = TerminalCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Spacer(Modifier.height(8.dp))
                    TerminalTextField(value = rtspHost, onValueChange = { rtspHost = it }, label = "Camera IP")
                    Spacer(Modifier.height(8.dp))
                    TerminalTextField(value = rtspPort, onValueChange = { rtspPort = it }, label = "RTSP Port")
                    Spacer(Modifier.height(12.dp))
                    ActionButton(
                        label = if (isScanning) "CRACKING..." else "CRACK CREDENTIALS",
                        icon = Icons.Default.Lock,
                        enabled = !isScanning,
                        onClick = { onCrackRtsp(rtspHost, rtspPort.toIntOrNull() ?: 554) }
                    )
                    Spacer(Modifier.height(8.dp))
                    // Common creds preview
                    Surface(
                        color = Color(0xFF070F07),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, TerminalGreen.copy(0.2f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Testing ${21} credential pairs", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("admin:admin, admin:, root:, admin:12345...", color = Color(0xFF507050), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
            2 -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("ONVIF WS-DISCOVERY", color = TerminalCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Spacer(Modifier.height(4.dp))
                    Text("Broadcasts UDP multicast to detect ONVIF cameras on local network", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace, lineHeight = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    ActionButton(
                        label = if (isScanning) "SCANNING..." else "DISCOVER ONVIF DEVICES",
                        icon = Icons.Default.CameraAlt,
                        enabled = !isScanning,
                        onClick = onDiscoverOnvif
                    )
                }
            }
            3 -> {
                LazyColumn(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    item {
                        HelpSection(
                            title = "CCTV CRACK — Quick Guide",
                            content = listOf(
                                "1. DISCOVER: Scans subnet for open RTSP ports (554, 8554)",
                                "2. RTSP CRACK: Tests 21 default username/password combinations",
                                "3. ONVIF: Broadcasts discovery for IP cameras on LAN",
                                "",
                                "Requirements:",
                                "  • Connected to same network as cameras",
                                "  • Root access (for nmap-based discovery)",
                                "  • nmap binary in /data/local/tmp/ (optional)",
                                "",
                                "Common vulnerable brands:",
                                "  Hikvision, Dahua, Axis, Foscam, Reolink",
                                "",
                                "After finding credentials:",
                                "  View: vlc rtsp://user:pass@IP:554/stream",
                                "  Record: ffmpeg -i rtsp://... -c copy out.mp4"
                            )
                        )
                    }
                }
            }
        }

        // ── Terminal output ──
        HorizontalDivider(color = TerminalGreen.copy(0.2f))
        TerminalOutput(
            lines = outputLines,
            isRunning = isScanning,
            onStop = onStop,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SqlMapPanel(
    outputLines: List<String>,
    isRunning: Boolean,
    onRunScan: (String, String?, Boolean) -> Unit,
    onDetectWaf: (String) -> Unit,
    onRunBuiltIn: (String, String?) -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    var targetUrl by remember { mutableStateOf("http://testphp.vulnweb.com/artists.php?artist=1") }
    var postData by remember { mutableStateOf("") }
    var dumpAll by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SecOpsBackground)
    ) {
        // ── Header ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF1A0A00), Color(0xFF200A00), Color(0xFF1A0A00))
                    )
                )
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Storage, null, tint = Color(0xFFFF6B00), modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "SQLMAP",
                        color = Color(0xFFFF6B00),
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        letterSpacing = 2.sp
                    )
                    Text(
                        "SQL Injection Detection & Exploitation",
                        color = Color.Gray,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        val tabs = listOf("SCAN", "WAF DETECT", "BUILT-IN PROBE", "HELP")
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF0F0A07),
            contentColor = Color(0xFFFF6B00),
            edgePadding = 0.dp
        ) {
            tabs.forEachIndexed { i, label ->
                Tab(
                    selected = selectedTab == i,
                    onClick = { selectedTab = i },
                    text = {
                        Text(
                            label,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (selectedTab == i) Color(0xFFFF6B00) else Color.Gray
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("TARGET URL", color = Color(0xFFFF6B00), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Spacer(Modifier.height(8.dp))
                TerminalTextField(value = targetUrl, onValueChange = { targetUrl = it }, label = "URL with parameter")
                Spacer(Modifier.height(8.dp))
                TerminalTextField(value = postData, onValueChange = { postData = it }, label = "POST Data (optional)")
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = dumpAll,
                        onCheckedChange = { dumpAll = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFF6B00))
                    )
                    Text("Dump all databases", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { onRunScan(targetUrl, postData.ifBlank { null }, dumpAll) },
                    enabled = !isRunning,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B00), contentColor = Color.Black),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isRunning) "SCANNING..." else "RUN SQLMAP", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
            1 -> Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("WAF DETECTION", color = Color(0xFFFF6B00), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Spacer(Modifier.height(8.dp))
                TerminalTextField(value = targetUrl, onValueChange = { targetUrl = it }, label = "Target URL")
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { onDetectWaf(targetUrl) },
                    enabled = !isRunning,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC5500), contentColor = Color.Black),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isRunning) "DETECTING..." else "DETECT WAF", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
            2 -> Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("BUILT-IN SQLi PROBE", color = Color(0xFFFF6B00), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Spacer(Modifier.height(4.dp))
                Text("No binary required — tests ${20} SQL injection payloads", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Spacer(Modifier.height(8.dp))
                TerminalTextField(value = targetUrl, onValueChange = { targetUrl = it }, label = "Target URL")
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { onRunBuiltIn(targetUrl, postData.ifBlank { null }) },
                    enabled = !isRunning,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAA4400)),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isRunning) "PROBING..." else "RUN BUILT-IN PROBE", color = Color.White, fontFamily = FontFamily.Monospace)
                }
            }
            3 -> LazyColumn(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                item {
                    HelpSection(
                        title = "SQLMap — Quick Guide",
                        content = listOf(
                            "1. SCAN: Runs sqlmap with level 3, risk 2 (balanced)",
                            "2. WAF DETECT: Identifies firewall protection headers",
                            "3. BUILT-IN: Native probe — no binary needed",
                            "",
                            "Example targets:",
                            "  http://site.com/page.php?id=1",
                            "  http://site.com/login (POST: user=a&pass=b)",
                            "",
                            "Install sqlmap (Termux):",
                            "  pkg install python",
                            "  pip install sqlmap",
                            "",
                            "sqlmap flags used:",
                            "  --batch (no prompts)",
                            "  --random-agent",
                            "  --level=3 --risk=2"
                        )
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFFFF6B00).copy(0.2f))
        TerminalOutput(
            lines = outputLines,
            isRunning = isRunning,
            onStop = onStop,
            modifier = Modifier.weight(1f)
        )
    }
}

// Shared helper composables
@Composable
private fun TerminalTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TerminalGreen,
            unfocusedBorderColor = TerminalGreen.copy(0.3f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.LightGray,
            focusedLabelColor = TerminalGreen,
            cursorColor = TerminalGreen
        ),
        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
        singleLine = true,
        shape = RoundedCornerShape(4.dp)
    )
}

@Composable
private fun ActionButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = TerminalGreen,
            contentColor = Color.Black,
            disabledContainerColor = TerminalGreen.copy(0.3f)
        ),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TerminalOutput(
    lines: List<String>,
    isRunning: Boolean,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) listState.animateScrollToItem(lines.size - 1)
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF050D05)).padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("OUTPUT", color = TerminalGreen.copy(0.7f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            if (isRunning) {
                TextButton(onClick = onStop, contentPadding = PaddingValues(horizontal = 4.dp)) {
                    Text("■ STOP", color = SecOpsError, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().background(Color(0xFF030803)).padding(8.dp)
        ) {
            items(lines) { line ->
                val color = when {
                    line.startsWith("[SUCCESS]") || line.startsWith("[FOUND]") -> TerminalGreen
                    line.startsWith("[ERROR]") || line.startsWith("[VULN]") -> SecOpsError
                    line.startsWith("[WARN]") || line.startsWith("[!]") -> SecOpsWarning
                    line.startsWith("[*]") || line.startsWith("[INFO]") -> TerminalCyan
                    line.startsWith("[TRY]") -> Color(0xFF506050)
                    else -> Color(0xFF90A090)
                }
                Text(
                    text = line,
                    color = color,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
private fun HelpSection(title: String, content: List<String>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF070F07)),
        border = BorderStroke(1.dp, TerminalGreen.copy(0.2f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = TerminalCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            content.forEach { line ->
                Text(
                    line,
                    color = if (line.isEmpty()) Color.Transparent else if (line.startsWith("  ")) Color(0xFF809080) else Color(0xFF90A090),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
