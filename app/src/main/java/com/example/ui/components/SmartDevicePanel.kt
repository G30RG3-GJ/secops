package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SmartDevicePanel(
    outputLines: List<String>,
    isScanning: Boolean,
    onScanDevices: (String) -> Unit,
    onScanAdb: (String) -> Unit,
    onDiscoverUpnp: () -> Unit,
    onCrackRouter: (String, Int) -> Unit,
    onRunAdbCommand: (String, String) -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var subnet by remember { mutableStateOf("192.168.1") }
    var routerHost by remember { mutableStateOf("192.168.1.1") }
    var routerPort by remember { mutableStateOf("80") }
    var adbHost by remember { mutableStateOf("192.168.1.") }
    var adbCommand by remember { mutableStateOf("getprop ro.product.model") }

    val infiniteTransition = rememberInfiniteTransition(label = "iot_anim")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
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
                        listOf(Color(0xFF050820), Color(0xFF020612), Color(0xFF050820))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            TerminalPurple.copy(alpha = 0.15f),
                            CircleShape
                        )
                        .border(1.dp, TerminalPurple.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Router, null, tint = TerminalPurple, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "IOT / SMART DEVICE CONTROL",
                        color = TerminalPurple,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        "ADB • UPnP • MQTT • Router Admin • Chromecast",
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
                            .background(TerminalPurple.copy(alpha = pulseAlpha), CircleShape)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("SCANNING", color = TerminalPurple, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // ── Tab Bar ──
        val tabs = listOf("IoT SCAN", "ADB TV", "UPnP", "ROUTER", "TOOLS")
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF060610),
            contentColor = TerminalPurple,
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
                            color = if (selectedTab == i) TerminalPurple else Color.Gray
                        )
                    }
                )
            }
        }

        // ── Tab Content ──
        Column(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> {
                    // IoT Scan
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("SUBNET SCAN", color = TerminalPurple, fontSize = 11.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)

                        IotTextField(
                            value = subnet,
                            onValueChange = { subnet = it },
                            label = "Subnet (e.g. 192.168.1)"
                        )

                        IotButton(
                            label = if (isScanning) "SCANNING..." else "SCAN FOR SMART DEVICES",
                            icon = Icons.Default.Search,
                            enabled = !isScanning,
                            onClick = { onScanDevices(subnet) },
                            color = TerminalPurple
                        )

                        // Device icons grid
                        Text("DETECTABLE DEVICES:", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        DeviceChipsRow()
                    }
                }

                1 -> {
                    // ADB TV Control
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("ANDROID TV / ADB DEVICE", color = TerminalPurple, fontSize = 11.sp, fontFamily = FontFamily.Monospace)

                        IotTextField(
                            value = adbHost,
                            onValueChange = { adbHost = it },
                            label = "Device IP (e.g. 192.168.1.105)"
                        )

                        IotButton(
                            label = if (isScanning) "SCANNING..." else "SCAN FOR ADB DEVICES",
                            icon = Icons.Default.Tv,
                            enabled = !isScanning,
                            onClick = { onScanAdb(subnet) },
                            color = TerminalPurple
                        )

                        HorizontalDivider(color = TerminalPurple.copy(alpha = 0.2f))

                        Text("ADB COMMAND:", color = TerminalPurple, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        IotTextField(
                            value = adbCommand,
                            onValueChange = { adbCommand = it },
                            label = "ADB Shell Command"
                        )
                        IotButton(
                            label = "RUN ADB COMMAND",
                            icon = Icons.Default.Terminal,
                            enabled = !isScanning,
                            onClick = { onRunAdbCommand(adbHost, adbCommand) },
                            color = Color(0xFF7B2FBE)
                        )

                        // Quick ADB commands
                        Text("QUICK COMMANDS:", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        val quickCmds = listOf(
                            "getprop ro.product.model" to "Device Model",
                            "pm list packages" to "List Apps",
                            "screencap -p /sdcard/s.png" to "Screenshot",
                            "input keyevent 3" to "Home Button",
                            "reboot" to "Reboot"
                        )
                        quickCmds.forEach { (cmd, label) ->
                            Surface(
                                onClick = { adbCommand = cmd },
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF080818),
                                border = BorderStroke(1.dp, TerminalPurple.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        label,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        cmd,
                                        color = TerminalPurple.copy(alpha = 0.6f),
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // UPnP Discovery
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("UPnP / SSDP DISCOVERY", color = TerminalPurple, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Text(
                            "Discovers UPnP devices: smart TVs, routers, NAS, printers, media servers",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 14.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        IotButton(
                            label = if (isScanning) "DISCOVERING..." else "DISCOVER UPnP DEVICES",
                            icon = Icons.Default.NetworkCheck,
                            enabled = !isScanning,
                            onClick = onDiscoverUpnp,
                            color = TerminalPurple
                        )

                        // Info card
                        Surface(
                            color = Color(0xFF060614),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, TerminalPurple.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("HOW IT WORKS", color = TerminalPurple, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(6.dp))
                                listOf(
                                    "1. Sends SSDP M-SEARCH multicast to 239.255.255.250:1900",
                                    "2. Collects all responses from UPnP-enabled devices",
                                    "3. Extracts device type, location URLs, and server info",
                                    "4. Found devices can be vulnerable to UPnP exploits"
                                ).forEach {
                                    Text(it, color = Color(0xFF6060A0), fontSize = 9.sp, fontFamily = FontFamily.Monospace, lineHeight = 13.sp)
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // Router Admin Cracker
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("ROUTER ADMIN CRACKER", color = TerminalPurple, fontSize = 11.sp, fontFamily = FontFamily.Monospace)

                        IotTextField(
                            value = routerHost,
                            onValueChange = { routerHost = it },
                            label = "Router IP"
                        )
                        IotTextField(
                            value = routerPort,
                            onValueChange = { routerPort = it },
                            label = "HTTP Port"
                        )

                        IotButton(
                            label = if (isScanning) "CRACKING..." else "CRACK ADMIN PANEL",
                            icon = Icons.Default.LockOpen,
                            enabled = !isScanning,
                            onClick = { onCrackRouter(routerHost, routerPort.toIntOrNull() ?: 80) },
                            color = SecOpsError
                        )

                        Text(
                            "Testing 10 most common default credential pairs\n(admin:admin, admin:password, root:root, etc.)",
                            color = Color.Gray,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 14.sp
                        )
                    }
                }

                4 -> {
                    // Tools & Reference
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                "IoT SECURITY REFERENCE",
                                color = TerminalPurple,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        val tips = listOf(
                            "🔌 Smart Plugs" to "TP-Link Kasa: port 9999 (cleartext protocol)\nGovee/Tuya: port 6668",
                            "📺 Android TV" to "Enable: Settings > Developer > Network debugging\nADB port: 5555",
                            "🎵 Sonos" to "HTTP API on port 1400\nEndpoints: /status, /state, /play",
                            "🌡️ Smart Thermostat" to "Nest: port 4000\nEcobee: port 443 (cloud-based)",
                            "💡 Smart Lights" to "Hue Bridge: port 80 (REST API)\nLifX: UDP port 56700",
                            "📡 MQTT Hubs" to "Home Assistant: 1883 (cleartext!)\nAlways use TLS (8883)"
                        )
                        items(tips) { (title, desc) ->
                            Surface(
                                color = Color(0xFF060614),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, TerminalPurple.copy(alpha = 0.25f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(title, color = TerminalPurple, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(4.dp))
                                    Text(desc, color = Color(0xFF8080B0), fontSize = 10.sp, fontFamily = FontFamily.Monospace, lineHeight = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Terminal Output ──
        HorizontalDivider(color = TerminalPurple.copy(alpha = 0.3f))
        IotTerminalOutput(
            lines = outputLines,
            isRunning = isScanning,
            onStop = onStop,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DeviceChipsRow() {
    val devices = listOf(
        Icons.Default.Tv to "Android TV",
        Icons.Default.Router to "Router",
        Icons.Default.Videocam to "IP Cam",
        Icons.Default.SpeakerPhone to "Smart Speaker",
        Icons.Default.Devices to "IoT Hub",
        Icons.Default.LightMode to "Smart Lights"
    )
    androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(devices) { (icon, label) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF080818), CircleShape)
                        .border(1.dp, TerminalPurple.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = TerminalPurple.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.height(4.dp))
                Text(label, color = Color(0xFF6060A0), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
private fun IotTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TerminalPurple,
            unfocusedBorderColor = TerminalPurple.copy(0.3f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.LightGray,
            focusedLabelColor = TerminalPurple,
            cursorColor = TerminalPurple
        ),
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
        singleLine = true,
        shape = RoundedCornerShape(4.dp)
    )
}

@Composable
private fun IotButton(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    color: Color = TerminalPurple
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.Black,
            disabledContainerColor = color.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun IotTerminalOutput(
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
            modifier = Modifier.fillMaxWidth().background(Color(0xFF040408)).padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("IoT OUTPUT", color = TerminalPurple.copy(0.7f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            if (isRunning) {
                TextButton(onClick = onStop, contentPadding = PaddingValues(horizontal = 4.dp)) {
                    Text("■ STOP", color = SecOpsError, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().background(Color(0xFF020206)).padding(8.dp)
        ) {
            items(lines) { line ->
                val color = when {
                    line.startsWith("[FOUND]") || line.startsWith("[SUCCESS]") -> TerminalGreen
                    line.startsWith("[ERROR]") -> SecOpsError
                    line.startsWith("[!]") || line.startsWith("[WARN]") -> SecOpsWarning
                    line.startsWith("[*]") || line.startsWith("[INFO]") -> TerminalPurple
                    line.startsWith("[TRY]") -> Color(0xFF404060)
                    line.startsWith("[OUTPUT]") -> TerminalCyan
                    else -> Color(0xFF9090B0)
                }
                Text(text = line, color = color, fontSize = 10.sp, fontFamily = FontFamily.Monospace, lineHeight = 14.sp)
            }
        }
    }
}
