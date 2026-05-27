package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.utils.KaliExtendedTools

@Composable
fun KaliExtendedToolsPanel(
    outputLines: List<String>,
    isRunning: Boolean,
    toolsStatus: Map<String, Boolean>,
    onRunHydra: (String, String, Int?) -> Unit,
    onRunJohn: (String, String?) -> Unit,
    onRunGobuster: (String, String) -> Unit,
    onRunNikto: (String) -> Unit,
    onRunEnum4linux: (String) -> Unit,
    onRunNetcat: (String, Int) -> Unit,
    onRunDnsZoneTransfer: (String) -> Unit,
    onRunWhois: (String) -> Unit,
    onRunMasscan: (String, String) -> Unit,
    onRunHarvester: (String, String) -> Unit,
    onRunHashcat: (String, Int, String) -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTool by remember { mutableStateOf("") }

    // Input states for various tools
    var hydraTarget by remember { mutableStateOf("192.168.1.1") }
    var hydraService by remember { mutableStateOf("ssh") }
    var hydraPort by remember { mutableStateOf("22") }

    var johnHash by remember { mutableStateOf("") }
    var johnFormat by remember { mutableStateOf("") }

    var gobusterUrl by remember { mutableStateOf("http://target.com") }
    var gobusterWordlist by remember { mutableStateOf("/data/local/tmp/common.txt") }

    var niktoTarget by remember { mutableStateOf("http://target.com") }

    var enum4Target by remember { mutableStateOf("192.168.1.1") }

    var ncPort by remember { mutableStateOf("4444") }
    var ncHost by remember { mutableStateOf("") }
    var ncMode by remember { mutableStateOf("listen") }

    var dnsTarget by remember { mutableStateOf("example.com") }

    var whoisTarget by remember { mutableStateOf("example.com") }

    var masscanTarget by remember { mutableStateOf("192.168.1.0/24") }
    var masscanPorts by remember { mutableStateOf("1-1024") }

    var harvesterDomain by remember { mutableStateOf("target.com") }
    var harvesterSource by remember { mutableStateOf("google") }

    var hashcatHash by remember { mutableStateOf("") }
    var hashcatType by remember { mutableIntStateOf(0) }
    var hashcatWordlist by remember { mutableStateOf("/data/local/tmp/rockyou.txt") }

    var revShellLhost by remember { mutableStateOf("192.168.1.100") }
    var revShellLport by remember { mutableStateOf("4444") }
    var revShellType by remember { mutableStateOf("bash") }
    var revShellOutput by remember { mutableStateOf(listOf<String>()) }

    val tools = KaliExtendedTools().getAllTools()

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
                        listOf(Color(0xFF0A0A00), Color(0xFF181800), Color(0xFF0A0A00))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Terminal,
                    null,
                    tint = TerminalYellow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "KALI EXTENDED TOOLKIT",
                        color = TerminalYellow,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        "14+ Professional Penetration Testing Tools",
                        color = Color.Gray,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(Modifier.weight(1f))
                // Running indicator
                if (isRunning) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = TerminalYellow,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // ── Tool Grid ──
        if (activeTool.isEmpty()) {
            // Show tool selection grid
            LazyColumn(modifier = Modifier.weight(1f).padding(12.dp)) {
                item {
                    Text(
                        "SELECT TOOL",
                        color = TerminalYellow,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                // Group tools by category
                val byCategory = tools.groupBy { it.category }
                byCategory.forEach { (category, categoryTools) ->
                    item {
                        Text(
                            "── $category ──",
                            color = TerminalYellow.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                    // Tools in rows of 2
                    categoryTools.chunked(2).forEach { row ->
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { tool ->
                                    val isAvailable = toolsStatus[tool.id] ?: false
                                    ToolCard(
                                        tool = tool,
                                        isAvailable = isAvailable,
                                        onClick = { activeTool = tool.id },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (row.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        } else {
            // Show tool configuration panel
            Column(modifier = Modifier.weight(1f)) {
                // Back button + tool name
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF080800))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { activeTool = "" }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ArrowBack, null, tint = TerminalYellow, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        tools.find { it.id == activeTool }?.name?.uppercase() ?: activeTool.uppercase(),
                        color = TerminalYellow,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                LazyColumn(
                    modifier = Modifier.weight(1f).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (activeTool) {
                        "hydra" -> {
                            item { KaliLabel("HYDRA — NETWORK BRUTE FORCE") }
                            item { KaliTextField(hydraTarget, { hydraTarget = it }, "Target IP/Host") }
                            item {
                                // Service selector
                                Text("Service:", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(listOf("ssh", "ftp", "http", "https", "smtp", "mysql", "rdp")) { svc ->
                                        FilterChip(
                                            selected = hydraService == svc,
                                            onClick = { hydraService = svc },
                                            label = { Text(svc, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = TerminalYellow.copy(alpha = 0.2f),
                                                selectedLabelColor = TerminalYellow
                                            )
                                        )
                                    }
                                }
                            }
                            item { KaliTextField(hydraPort, { hydraPort = it }, "Port (optional)") }
                            item {
                                KaliRunButton("RUN HYDRA") {
                                    onRunHydra(hydraTarget, hydraService, hydraPort.toIntOrNull())
                                }
                            }
                            item {
                                KaliInfoBox(listOf(
                                    "Wordlists required: /data/local/tmp/users.txt + passwords.txt",
                                    "Install: pkg install hydra",
                                    "Example: hydra -l admin -P rockyou.txt ssh://target"
                                ))
                            }
                        }

                        "john" -> {
                            item { KaliLabel("JOHN THE RIPPER — HASH CRACKER") }
                            item { KaliTextField(johnHash, { johnHash = it }, "Hash file path or hash string") }
                            item {
                                Text("Format:", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(listOf("auto", "md5crypt", "sha512crypt", "nt", "bcrypt")) { fmt ->
                                        FilterChip(
                                            selected = johnFormat == fmt,
                                            onClick = { johnFormat = if (fmt == "auto") "" else fmt },
                                            label = { Text(fmt, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = TerminalYellow.copy(alpha = 0.2f),
                                                selectedLabelColor = TerminalYellow
                                            )
                                        )
                                    }
                                }
                            }
                            item { KaliRunButton("RUN JOHN") { onRunJohn(johnHash, johnFormat.ifBlank { null }) } }
                        }

                        "hashcat" -> {
                            item { KaliLabel("HASHCAT — GPU HASH CRACKER") }
                            item { KaliTextField(hashcatHash, { hashcatHash = it }, "Hash to crack") }
                            item { KaliTextField(hashcatWordlist, { hashcatWordlist = it }, "Wordlist path") }
                            item {
                                Text("Hash type:", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(listOf(0 to "MD5", 100 to "SHA1", 1400 to "SHA256", 1000 to "NTLM", 1800 to "sha512crypt", 22000 to "WPA")) { (type, name) ->
                                        FilterChip(
                                            selected = hashcatType == type,
                                            onClick = { hashcatType = type },
                                            label = { Text("$type ($name)", fontSize = 9.sp, fontFamily = FontFamily.Monospace) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = TerminalYellow.copy(alpha = 0.2f),
                                                selectedLabelColor = TerminalYellow
                                            )
                                        )
                                    }
                                }
                            }
                            item { KaliRunButton("RUN HASHCAT") { onRunHashcat(hashcatHash, hashcatType, hashcatWordlist) } }
                        }

                        "gobuster" -> {
                            item { KaliLabel("GOBUSTER — DIRECTORY SCANNER") }
                            item { KaliTextField(gobusterUrl, { gobusterUrl = it }, "Target URL") }
                            item { KaliTextField(gobusterWordlist, { gobusterWordlist = it }, "Wordlist path") }
                            item { KaliRunButton("RUN GOBUSTER") { onRunGobuster(gobusterUrl, gobusterWordlist) } }
                            item { KaliInfoBox(listOf("Install: pkg install gobuster", "Wordlists: /data/local/tmp/common.txt")) }
                        }

                        "nikto" -> {
                            item { KaliLabel("NIKTO — WEB VULNERABILITY SCANNER") }
                            item { KaliTextField(niktoTarget, { niktoTarget = it }, "Target URL") }
                            item { KaliRunButton("RUN NIKTO") { onRunNikto(niktoTarget) } }
                            item { KaliInfoBox(listOf("Checks 6700+ dangerous files", "Detects outdated server software", "Install: pkg install perl")) }
                        }

                        "enum4linux" -> {
                            item { KaliLabel("ENUM4LINUX — SMB ENUMERATION") }
                            item { KaliTextField(enum4Target, { enum4Target = it }, "Target IP") }
                            item { KaliRunButton("RUN ENUM4LINUX") { onRunEnum4linux(enum4Target) } }
                            item { KaliInfoBox(listOf("Enumerates SMB shares, users, groups", "Requires Samba: pkg install samba")) }
                        }

                        "nc" -> {
                            item { KaliLabel("NETCAT — TCP/UDP SWISS ARMY KNIFE") }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("listen" to "LISTEN", "connect" to "CONNECT").forEach { (mode, label) ->
                                        FilterChip(
                                            selected = ncMode == mode,
                                            onClick = { ncMode = mode },
                                            label = { Text(label, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = TerminalYellow.copy(alpha = 0.2f),
                                                selectedLabelColor = TerminalYellow
                                            )
                                        )
                                    }
                                }
                            }
                            item {
                                if (ncMode == "connect") {
                                    KaliTextField(ncHost, { ncHost = it }, "Host to connect to")
                                }
                            }
                            item { KaliTextField(ncPort, { ncPort = it }, "Port") }
                            item { KaliRunButton("START NETCAT") { onRunNetcat(ncHost, ncPort.toIntOrNull() ?: 4444) } }
                        }

                        "dig" -> {
                            item { KaliLabel("DNS ZONE TRANSFER") }
                            item { KaliTextField(dnsTarget, { dnsTarget = it }, "Domain (e.g. example.com)") }
                            item { KaliRunButton("RUN ZONE TRANSFER") { onRunDnsZoneTransfer(dnsTarget) } }
                            item { KaliInfoBox(listOf("Attempts DNS AXFR zone transfer", "Most servers block this — testing only", "Command: dig @ns1.domain axfr domain")) }
                        }

                        "whois" -> {
                            item { KaliLabel("WHOIS LOOKUP") }
                            item { KaliTextField(whoisTarget, { whoisTarget = it }, "Domain or IP") }
                            item { KaliRunButton("RUN WHOIS") { onRunWhois(whoisTarget) } }
                        }

                        "masscan" -> {
                            item { KaliLabel("MASSCAN — ULTRA-FAST PORT SCANNER") }
                            item { KaliTextField(masscanTarget, { masscanTarget = it }, "Target (IP/CIDR)") }
                            item { KaliTextField(masscanPorts, { masscanPorts = it }, "Port range (e.g. 1-1024)") }
                            item { KaliRunButton("RUN MASSCAN") { onRunMasscan(masscanTarget, masscanPorts) } }
                            item { KaliInfoBox(listOf("10x faster than nmap", "Requires root for raw packets", "Install: pkg install masscan")) }
                        }

                        "theHarvester" -> {
                            item { KaliLabel("THEHARVERSTER — OSINT COLLECTOR") }
                            item { KaliTextField(harvesterDomain, { harvesterDomain = it }, "Target domain") }
                            item {
                                Text("Source:", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(listOf("google", "bing", "linkedin", "twitter", "shodan", "censys")) { src ->
                                        FilterChip(
                                            selected = harvesterSource == src,
                                            onClick = { harvesterSource = src },
                                            label = { Text(src, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = TerminalYellow.copy(alpha = 0.2f),
                                                selectedLabelColor = TerminalYellow
                                            )
                                        )
                                    }
                                }
                            }
                            item { KaliRunButton("RUN HARVESTER") { onRunHarvester(harvesterDomain, harvesterSource) } }
                        }

                        "revshell" -> {
                            item { KaliLabel("REVERSE SHELL GENERATOR") }
                            item { KaliTextField(revShellLhost, { revShellLhost = it }, "Your IP (LHOST)") }
                            item { KaliTextField(revShellLport, { revShellLport = it }, "Port (LPORT)") }
                            item {
                                Text("Shell type:", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(listOf("bash", "python", "php", "perl", "ruby", "nc", "powershell", "java")) { type ->
                                        FilterChip(
                                            selected = revShellType == type,
                                            onClick = { revShellType = type },
                                            label = { Text(type, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = TerminalYellow.copy(alpha = 0.2f),
                                                selectedLabelColor = TerminalYellow
                                            )
                                        )
                                    }
                                }
                            }
                            item {
                                Button(
                                    onClick = {
                                        val kali = KaliExtendedTools()
                                        revShellOutput = kali.generateReverseShell(
                                            revShellLhost, revShellLport.toIntOrNull() ?: 4444, revShellType
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TerminalYellow, contentColor = Color.Black),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("GENERATE PAYLOAD", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                }
                            }
                            item {
                                if (revShellOutput.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        revShellOutput.forEachIndexed { i, payload ->
                                            Surface(
                                                color = Color(0xFF080800),
                                                shape = RoundedCornerShape(4.dp),
                                                border = BorderStroke(1.dp, TerminalYellow.copy(alpha = 0.3f))
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Text(
                                                        "Payload ${i + 1}:",
                                                        color = TerminalYellow.copy(alpha = 0.7f),
                                                        fontSize = 9.sp,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                    Spacer(Modifier.height(4.dp))
                                                    Text(
                                                        payload,
                                                        color = TerminalGreen,
                                                        fontSize = 9.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        lineHeight = 13.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            item {
                                if (revShellOutput.isNotEmpty()) {
                                    KaliInfoBox(listOf(
                                        "Listener: nc -lvnp ${revShellLport}",
                                        "Or use Metasploit: use exploit/multi/handler",
                                        "Make sure firewall allows port $revShellLport"
                                    ))
                                }
                            }
                        }

                        "shodan" -> {
                            item { KaliLabel("SHODAN QUERY BUILDER") }
                            item { KaliTextField(whoisTarget, { whoisTarget = it }, "Target (IP, domain, org)") }
                            item {
                                val kali = KaliExtendedTools()
                                val queryTypes = listOf("ip", "org", "vuln", "cctv", "default_creds")
                                queryTypes.forEach { qType ->
                                    val queries = kali.buildShodanQuery(whoisTarget, qType)
                                    Surface(
                                        color = Color(0xFF080800),
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(1.dp, TerminalYellow.copy(alpha = 0.2f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("[$qType]".uppercase(), color = TerminalYellow.copy(0.6f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                            queries.forEach { q ->
                                                Text(q, color = TerminalGreen, fontSize = 9.sp, fontFamily = FontFamily.Monospace, lineHeight = 13.sp)
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                }
                            }
                        }

                        else -> {
                            item { Text("Tool: $activeTool", color = Color.Gray) }
                        }
                    }
                }
            }
        }

        // ── Terminal Output ──
        if (!isRunning && outputLines.isEmpty() && activeTool.isEmpty()) {
            // Show tool status
            Surface(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                color = Color(0xFF0A0A00),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, TerminalYellow.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("TOOL STATUS", color = TerminalYellow, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    toolsStatus.entries.chunked(2).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            row.forEach { (tool, available) ->
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(6.dp)
                                            .background(
                                                if (available) TerminalGreen else SecOpsError,
                                                CircleShape
                                            )
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        tool,
                                        color = if (available) Color(0xFF609060) else Color(0xFF604040),
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            HorizontalDivider(color = TerminalYellow.copy(alpha = 0.2f))
            KaliTerminalOutput(outputLines, isRunning, onStop, Modifier.weight(1f).heightIn(min = 150.dp))
        }
    }
}

@Composable
private fun ToolCard(
    tool: KaliExtendedTools.KaliTool,
    isAvailable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColors = mapOf(
        "BRUTE-FORCE" to SecOpsError,
        "CRACKING" to TerminalOrange,
        "WEB" to TerminalCyan,
        "RECON" to TerminalGreen,
        "NETWORK" to TerminalPurple,
        "SCANNING" to SecOpsWarning,
        "OSINT" to Color(0xFF00BCD4),
        "EXPLOITATION" to SecOpsError
    )
    val color = categoryColors[tool.category] ?: TerminalYellow

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF080808),
        border = BorderStroke(1.dp, color.copy(alpha = if (isAvailable) 0.5f else 0.2f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    tool.name,
                    color = color,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Box(
                    modifier = Modifier.size(6.dp)
                        .background(
                            if (isAvailable) TerminalGreen else SecOpsError,
                            CircleShape
                        )
                )
            }
            Text(
                tool.description,
                color = Color(0xFF707070),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(Modifier.height(6.dp))
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text(
                    tool.category,
                    color = color.copy(alpha = 0.8f),
                    fontSize = 7.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun KaliLabel(text: String) {
    Text(text, color = TerminalYellow, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
}

@Composable
private fun KaliTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TerminalYellow,
            unfocusedBorderColor = TerminalYellow.copy(0.3f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.LightGray,
            focusedLabelColor = TerminalYellow,
            cursorColor = TerminalYellow
        ),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        ),
        singleLine = true,
        shape = RoundedCornerShape(4.dp)
    )
}

@Composable
private fun KaliRunButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = TerminalYellow, contentColor = Color.Black),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun KaliInfoBox(items: List<String>) {
    Surface(
        color = Color(0xFF0A0A00),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, TerminalYellow.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            items.forEach { item ->
                Text(item, color = Color(0xFF807040), fontSize = 9.sp, fontFamily = FontFamily.Monospace, lineHeight = 13.sp)
            }
        }
    }
}

@Composable
private fun KaliTerminalOutput(
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
            modifier = Modifier.fillMaxWidth().background(Color(0xFF080800)).padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("OUTPUT", color = TerminalYellow.copy(0.7f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            if (isRunning) {
                TextButton(onClick = onStop, contentPadding = PaddingValues(horizontal = 4.dp)) {
                    Text("■ STOP", color = SecOpsError, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().background(Color(0xFF050500)).padding(8.dp)
        ) {
            items(lines) { line ->
                val color = when {
                    line.startsWith("[SUCCESS]") || line.startsWith("[+]") || line.startsWith("[FOUND]") -> TerminalGreen
                    line.startsWith("[ERROR]") || line.startsWith("[-]") -> SecOpsError
                    line.startsWith("[!]") || line.startsWith("[WARN]") -> SecOpsWarning
                    line.startsWith("[*]") || line.startsWith("[INFO]") -> TerminalYellow.copy(alpha = 0.7f)
                    line.startsWith("[TRY]") || line.startsWith("[CMD]") -> Color(0xFF606040)
                    else -> Color(0xFF909070)
                }
                Text(text = line, color = color, fontSize = 10.sp, fontFamily = FontFamily.Monospace, lineHeight = 14.sp)
            }
        }
    }
}
