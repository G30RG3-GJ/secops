package com.example.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.models.*
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.Part
import com.example.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class SecurityViewModel : ViewModel() {

    // Global simulator active status
    private val _isSimulating = MutableStateFlow(false)
    val isSimulating = _isSimulating.asStateFlow()

    // Wireshark state
    private val _packets = MutableStateFlow<List<PacketItem>>(emptyList())
    val packets = _packets.asStateFlow()

    private val _isCapturingPackets = MutableStateFlow(false)
    val isCapturingPackets = _isCapturingPackets.asStateFlow()

    // Nmap discovery state
    private val _networkNodes = MutableStateFlow<List<NetworkNode>>(emptyList())
    val networkNodes = _networkNodes.asStateFlow()

    private val _isScanningNetwork = MutableStateFlow(false)
    val isScanningNetwork = _isScanningNetwork.asStateFlow()

    // Wireless security state
    private val _wifiNetworks = MutableStateFlow<List<WifiNetwork>>(emptyList())
    val wifiNetworks = _wifiNetworks.asStateFlow()

    private val _isScanningWifi = MutableStateFlow(false)
    val isScanningWifi = _isScanningWifi.asStateFlow()

    private val _currentCrackedNetwork = MutableStateFlow<WifiNetwork?>(null)
    val currentCrackedNetwork = _currentCrackedNetwork.asStateFlow()

    // Security Metrics Dashboard Data
    private val _vulnerabilityMetrics = MutableStateFlow<List<MetricItem>>(emptyList())
    val vulnerabilityMetrics = _vulnerabilityMetrics.asStateFlow()

    private val _protocolMetrics = MutableStateFlow<List<MetricItem>>(emptyList())
    val protocolMetrics = _protocolMetrics.asStateFlow()

    // Execution Logs
    private val _logs = MutableStateFlow<List<ExecutionLog>>(emptyList())
    val logs = _logs.asStateFlow()

    // AI Assistant state
    private val _aiChatHistory = MutableStateFlow<List<MessageItem>>(emptyList())
    val aiChatHistory = _aiChatHistory.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading = _aiLoading.asStateFlow()

    // --- Aircrack-ng Live Audit simulation states ---
    private val _isAircrackAuditActive = MutableStateFlow(false)
    val isAircrackAuditActive = _isAircrackAuditActive.asStateFlow()

    private val _aircrackStep = MutableStateFlow("IDLE") // "IDLE", "AIRODUMP_SCANNING", "AIREPLAY_DEAUTH", "AIRCRACK_BRUTEFORCE", "SUCCESS"
    val aircrackStep = _aircrackStep.asStateFlow()

    private val _aircrackProgress = MutableStateFlow(0f)
    val aircrackProgress = _aircrackProgress.asStateFlow()

    private val _aircrackChannel = MutableStateFlow(1)
    val airccrackChannel = _aircrackChannel.asStateFlow()

    private val _aircrackActivePacketsCount = MutableStateFlow(0)
    val aircrackActivePacketsCount = _aircrackActivePacketsCount.asStateFlow()

    private val _aircrackCurrentTestingKey = MutableStateFlow("")
    val aircrackCurrentTestingKey = _aircrackCurrentTestingKey.asStateFlow()

    private val _aircrackTargetNetwork = MutableStateFlow<WifiNetwork?>(null)
    val airccrackTargetNetwork = _aircrackTargetNetwork.asStateFlow()

    private val _aircrackTerminalOutput = MutableStateFlow<List<String>>(emptyList())
    val aircrackTerminalOutput = _aircrackTerminalOutput.asStateFlow()

    // --- Metasploit penetration framework states ---
    private val _metasploitStep = MutableStateFlow("IDLE") // "IDLE", "EXPLOITING", "METERPRETER_SHELL"
    val metasploitStep = _metasploitStep.asStateFlow()

    private val _metasploitTargetNode = MutableStateFlow<NetworkNode?>(null)
    val metasploitTargetNode = _metasploitTargetNode.asStateFlow()

    private val _metasploitSelectedExploit = MutableStateFlow("exploit/multi/http/tomcat_mgr_deploy")
    val metasploitSelectedExploit = _metasploitSelectedExploit.asStateFlow()

    private val _metasploitLport = MutableStateFlow("4444")
    val metasploitLport = _metasploitLport.asStateFlow()

    private val _metasploitMeterpreterOutput = MutableStateFlow<List<String>>(emptyList())
    val metasploitMeterpreterOutput = _metasploitMeterpreterOutput.asStateFlow()

    private val _metasploitProgress = MutableStateFlow(0f)
    val metasploitProgress = _metasploitProgress.asStateFlow()

    // --- Burp Suite Proxy application proxy testing states ---
    private val _burpIsScanning = MutableStateFlow(false)
    val burpIsScanning = _burpIsScanning.asStateFlow()

    private val _burpScanProgress = MutableStateFlow(0f)
    val burpScanProgress = _burpScanProgress.asStateFlow()

    private val _burpScannedUrls = MutableStateFlow<List<String>>(listOf(
        "GET /index.php HTTP/1.1",
        "POST /login.php HTTP/1.1",
        "GET /admin/dashboard.php HTTP/1.1",
        "POST /api/v1/users HTTP/1.1",
        "GET /search.php?q=kali HTTP/1.1"
    ))
    val burpScannedUrls = _burpScannedUrls.asStateFlow()

    private val _burpDiscoveredVulnerabilities = MutableStateFlow<List<String>>(emptyList())
    val burpDiscoveredVulnerabilities = _burpDiscoveredVulnerabilities.asStateFlow()

    private val _burpIntruderPayloadsTried = MutableStateFlow(0)
    val burpIntruderPayloadsTried = _burpIntruderPayloadsTried.asStateFlow()

    init {
        // Initialize default safe dataset to feed standard user controls
        resetToDefaultState()
    }

    fun resetToDefaultState() {
        _packets.value = generateMockPackets(15)
        _networkNodes.value = generateInitialNodes()
        _wifiNetworks.value = generateInitialWifiNetworks()
        updateDashboardMetrics()
        addLogEntry("SecOps Simulator", "Engine initialized cleanly. Standby for authorized traffic diagnostics.", "SUCCESS", 0)
    }

    private fun getCurrentTimestamp(): String {
        return SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
    }

    private fun addLogEntry(commandName: String, terminalOutput: String, status: String, duration: Long, metrics: List<MetricItem> = emptyList()) {
        val newLog = ExecutionLog(
            timestamp = getCurrentTimestamp(),
            commandName = commandName,
            status = status,
            durationMs = duration,
            terminalOutput = terminalOutput,
            visualizedMetrics = metrics
        )
        _logs.update { listOf(newLog) + it }
    }

    // --- Wireshark simulator controls ---
    fun togglePacketCapture() {
        if (_isCapturingPackets.value) {
            _isCapturingPackets.value = false
            addLogEntry("Wireshark Capture", "Packet collection suspended. Total analyzed: ${_packets.value.size}", "WARNING", 120)
        } else {
            _isCapturingPackets.value = true
            addLogEntry("Wireshark Capture", "Socket opened on interface wlan0. Promiscuous mode enabled. CAPTURING.", "SUCCESS", 50)
            simulateLivePacketCapture()
        }
    }

    private fun simulateLivePacketCapture() {
        viewModelScope.launch(Dispatchers.Default) {
            val protocols = listOf("TCP", "UDP", "DNS", "HTTP", "ARP", "TLSv1.3")
            val ips = listOf("192.168.1.1", "192.168.1.102", "192.168.1.254", "34.120.177.22", "8.8.8.8", "104.244.42.1")
            while (_isCapturingPackets.value) {
                delay(1200)
                if (!_isCapturingPackets.value) break

                val src = ips.random()
                var dest = ips.random()
                while (dest == src) {
                    dest = ips.random()
                }
                val proto = protocols.random()
                val length = Random.nextInt(40, 1500)
                val info = when(proto) {
                    "TCP" -> "Len=$length [SYN, ACK] Seq=${Random.nextInt(1000, 99999)} Win=${Random.nextInt(500, 65535)}"
                    "UDP" -> "SrcPort=${Random.nextInt(1024, 65535)} DstPort=${Random.nextInt(1024, 65535)} Len=${length - 20}"
                    "DNS" -> "Query Standard A ${listOf("google.com", "github.com", "secops.local").random()}"
                    "HTTP" -> "GET /assets/security_report.json HTTP/1.1"
                    "ARP" -> "Who has $dest? Tell $src"
                    "TLSv1.3" -> "Application Data, TLS Encryption Payload"
                    else -> "Operational flow frame check"
                }

                val targetByteList = ByteArray(24) { Random.nextInt(0, 255).toByte() }
                val hexDumpStr = targetByteList.joinToString(" ") { String.format("%02X", it) } + "  |  " + targetByteList.joinToString("") {
                    if (it in 32..126) it.toChar().toString() else "."
                }

                val nextId = (_packets.value.maxOfOrNull { it.id } ?: 0) + 1
                val newPacket = PacketItem(
                    id = nextId,
                    time = getCurrentTimestamp(),
                    source = src,
                    destination = dest,
                    protocol = proto,
                    length = length,
                    info = info,
                    hexDump = hexDumpStr
                )
                
                _packets.update { (listOf(newPacket) + it).take(100) } // Keep last 100 packets
                updateDashboardMetrics()
            }
        }
    }

    fun clearPackets() {
        _packets.value = emptyList()
        addLogEntry("Wireshark Simulator", "Frame buffer cleared.", "WARNING", 10)
        updateDashboardMetrics()
    }

    // --- Nmap discovery controls ---
    fun runNetworkDiscovery(withServiceScan: Boolean) {
        if (_isScanningNetwork.value) return
        _isScanningNetwork.value = true
        addLogEntry(
            commandName = if (withServiceScan) "nmap -sV -p- 192.168.1.0/24" else "nmap -sn 192.168.1.0/24",
            terminalOutput = "Starting Nmap scan... Probing CIDR subnetwork range for live hosts\nApplying ICMP Echo Request and ARP discovery.",
            status = "SUCCESS",
            duration = 0
        )

        viewModelScope.launch(Dispatchers.Default) {
            delay(3500) // Simulate scanning cost
            val discovered = generateInitialNodes().map { node ->
                if (withServiceScan) {
                    node.copy(
                        status = if (node.openPorts.isNotEmpty()) "Vulnerable" else "Active",
                        serviceDetails = node.openPorts.associateWith { port ->
                            when(port) {
                                22 -> "SSH - OpenSSH 8.9p1 (Vulnerable to potential SSH tunneling configuration audits)"
                                80 -> "HTTP - nginx 1.18.0 (Unencrypted diagnostic server)"
                                443 -> "HTTPS - nginx 1.18.0 (Self-signed certificate)"
                                8080 -> "HTTP-Proxy - Apache Tomcat/9.0.58 (Exposed admin panel simulation)"
                                else -> "TCP Service wrapper"
                            }
                        }
                    )
                } else {
                    node
                }
            }
            _networkNodes.value = discovered
            _isScanningNetwork.value = false
            
            val vulnerableCount = discovered.count { it.status == "Vulnerable" }
            addLogEntry(
                commandName = "Scan finished",
                terminalOutput = "Nmap scan report completed.\nFound ${discovered.size} alive hosts.\nDiscovered $vulnerableCount nodes with high audit-priority vulnerabilities.",
                status = if (vulnerableCount > 0) "WARNING" else "SUCCESS",
                duration = 3500
            )
            updateDashboardMetrics()
        }
    }

    // --- WiFi network controls ---
    fun toggleWifiScan() {
        if (_isScanningWifi.value) {
            _isScanningWifi.value = false
            addLogEntry("Wireless Monitoring", "WiFi network telemetry passive listening stopped.", "WARNING", 200)
        } else {
            _isScanningWifi.value = true
            addLogEntry("Wireless Monitoring", "Mon0 interface bound into Monitor mode. Listening on 2.4/5GHz spectrum.", "SUCCESS", 100)
            simulateLiveWifiScan()
        }
    }

    private fun simulateLiveWifiScan() {
        viewModelScope.launch(Dispatchers.Default) {
            val networks = _wifiNetworks.value.toMutableList()
            while (_isScanningWifi.value) {
                delay(2000)
                if (!_isScanningWifi.value) break

                // fluctuate signal levels slightly
                networks.forEachIndexed { idx, net ->
                    val fluctuation = Random.nextInt(-5, 5)
                    val newLevel = (net.signalLevel + fluctuation).coerceIn(-95, -30)
                    networks[idx] = net.copy(signalLevel = newLevel)
                }
                _wifiNetworks.value = networks.toList()
                updateDashboardMetrics()
            }
        }
    }

    fun startCrackingAttempt(network: WifiNetwork) {
        if (_currentCrackedNetwork.value != null && _currentCrackedNetwork.value?.ssid == network.ssid) {
            _currentCrackedNetwork.value = null
            addLogEntry("WPA/WPS Audit Tool", "WPA handshake tracking aborted for ${network.ssid}", "WARNING", 50)
            return
        }
        
        _currentCrackedNetwork.value = network.copy(pinProgress = 0.01f)
        addLogEntry(
            commandName = "reaver -i mon0 -b ${network.bssid} -vv",
            terminalOutput = "Starting WPS PIN/WPA Handshake analysis against Target BSSID ${network.bssid} [SSID: ${network.ssid}]\nChecking key verification index.",
            status = "SUCCESS",
            duration = 0
        )

        viewModelScope.launch(Dispatchers.Default) {
            var progress = 0f
            while (progress < 1f && _currentCrackedNetwork.value?.ssid == network.ssid) {
                delay(1200)
                if (_currentCrackedNetwork.value?.ssid != network.ssid) break
                progress += Random.nextFloat() * 0.25f
                if (progress > 1f) progress = 1f
                
                _currentCrackedNetwork.update { it?.copy(pinProgress = progress) }
                _wifiNetworks.update { list ->
                    list.map { item ->
                        if (item.ssid == network.ssid) item.copy(pinProgress = progress) else item
                    }
                }
                
                addLogEntry("reaver tracking", "Audit progression status: ${String.format("%.1f", progress * 100)}% complete...", "SUCCESS", 1200)
            }

            if (_currentCrackedNetwork.value?.ssid == network.ssid) {
                // Success path
                val finishedNetwork = network.copy(pinProgress = 1.0f, handshakeCaptured = true, isVulnerable = false)
                _currentCrackedNetwork.value = finishedNetwork
                _wifiNetworks.update { list ->
                    list.map { item ->
                        if (item.ssid == network.ssid) finishedNetwork else item
                    }
                }
                addLogEntry(
                    commandName = "Pairing cracked",
                    terminalOutput = "[+] WPA Handshake file captured successfully!\n[+] Transmitted packet decoded. PSK: 'SecOpsDemoAuditKey2026'\n[+] Security review recommended: Upgrade encryption standard to WPA3 with Protected Management Frames (PMF) enabled.",
                    status = "SUCCESS",
                    duration = 5000
                )
                updateDashboardMetrics()
            }
        }
    }

    // --- Core helper to feed simple metrics visualizations safely ---
    private fun updateDashboardMetrics() {
        val totalPackets = _packets.value.size
        // calculate protocols
        val groupedProtos = _packets.value.groupBy { it.protocol }
        val protoList = groupedProtos.map { (proto, list) ->
            val pct = if (totalPackets > 0) (list.size * 100) / totalPackets else 0
            MetricItem(label = proto, value = list.size.toFloat(), percentage = pct)
        }.sortedByDescending { it.value }
        _protocolMetrics.value = protoList

        // Calculate vulnerability indices safely
        val wifiVuln = _wifiNetworks.value.count { it.isVulnerable }
        val nodeVuln = _networkNodes.value.count { it.status == "Vulnerable" }
        _vulnerabilityMetrics.value = listOf(
            MetricItem("Open Target Vulnerabilities", nodeVuln.toFloat(), 40, "Network"),
            MetricItem("Vulnerable Access Points", wifiVuln.toFloat(), 60, "WiFi"),
            MetricItem("Live Handshakes Decoded", _wifiNetworks.value.count { it.handshakeCaptured }.toFloat(), 10, "WiFi")
        )
    }

    // --- Generative AI Assistant Integration ---
    fun askSecurityAssistant(userPrompt: String) {
        if (userPrompt.trim().isEmpty()) return
        
        _aiChatHistory.update { it + MessageItem("USER", userPrompt) }
        _aiLoading.value = true
        
        val fullPrompt = """
            You are 'SecOps Copilot', a professional offensive and defensive security AI diagnostic advisor.
            The user wants help with an aesthetic, simulated educational security test framework.
            Specifically, the user's prompt is: "$userPrompt"
            
            Based on this request, please write a professional, educational, and purely descriptive guide.
            Explain:
            1. What diagnostic utility can analyze this scenario (e.g., how Nmap or Wireshark performs diagnostics)?
            2. The theoretical steps an administrator would take to audit and secure their system against these issues.
            3. A simulated Terminal output command template showing how standard tools are run.
            
            Keep the tone clear, informative, and defensive-oriented. Give crisp advice about fixing standard protocol weaknesses.
        """.trimIndent()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                var reply = ""
                
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    // Fallback to offline educational template if the API key is not configured of placeholder
                    delay(2000)
                    reply = generateLocallyCachedDiagnosticResponse(userPrompt)
                } else {
                    val requestBody = GenerateContentRequest(
                        contents = listOf(Content(parts = listOf(Part(text = fullPrompt))))
                    )
                    val response = RetrofitClient.service.generateContent(apiKey, requestBody)
                    reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                        ?: "SecOps AI was unable to synthesize a payload report. Please examine the local parameters."
                }
                
                _aiChatHistory.update { it + MessageItem("GEMINI", reply) }
                addLogEntry("Gemini Telemetry AI", "AI Assistant generated defensive remediation checklist successfully.", "SUCCESS", 2500)
            } catch (e: Exception) {
                _aiChatHistory.update { it + MessageItem("GEMINI", "Offline diagnostic mode fallback: An error occurred while contacting the telemetry server (${e.localizedMessage}). Here is an offline mitigation framework:\n\n1. Check the local network connectivity.\n2. Configure your telemetry API Key in the SecOps Console Secrets Panel.\n3. Make sure firewall settings do not drop API requests to Gemini.") }
                addLogEntry("Gemini Telemetry AI Error", "API connection failure: ${e.message}", "FAILED", 0)
            } finally {
                _aiLoading.value = false
            }
        }
    }

    private fun generateLocallyCachedDiagnosticResponse(userPrompt: String): String {
        return """
            [OFFLINE SEC-OPS COMPANION REPORT]
            Note: System is functioning in local simulation loop (Gemini API Key configuration recommended via Secrets panel for real-time live telemetry).
            
            Regarding Your Query: "$userPrompt"
            
            --- 1. AUDIT MITIGATION ANALYSIS ---
            - Associated Diagnostic Utilities: Wireshark (Network analyzer), Nmap (Network Discovery Probe), Reaver (Wireless auditor simulation).
            - Remediation Blueprint:
               a. Update Wi-Fi routing configurations to force WPA3-SAE.
               b. Disable legacy unencrypted protocols like HTTP and Telnet.
               c. Enforce strong, high-entropy administrative credentials.
               
            --- 2. SUGGESTED CONSOLE AUDIT SYNTAX ---
            # Discovery command to identify network vulnerabilities:
            nmap -sV --script vuln 192.168.1.1/24
            
            # Catch raw unencrypted credentials:
            tshark -i wlan0 -Y "http.request.method == POST" -T fields -e http.file_data
        """.trimIndent()
    }

    fun clearChat() {
        _aiChatHistory.value = emptyList()
        addLogEntry("SecOps Copilot", "Chat sessions recycled.", "SUCCESS", 10)
    }

    // --- Mock Data Generators ---
    private fun generateInitialNodes(): List<NetworkNode> {
        return listOf(
            NetworkNode("192.168.1.1", "00:1E:83:B2:C1:AA", "Ubiquiti Networks", 4, "Active", listOf(80, 443)),
            NetworkNode("192.168.1.100", "B4:F6:1C:2B:BD:E1", "Apple Inc (Diagnostic Host)", 32, "Active"),
            NetworkNode("192.168.1.102", "70:8B:CD:42:01:FF", "Legacy Linux Server", 12, "Vulnerable", listOf(22, 80, 8080)),
            NetworkNode("192.168.1.105", "D0:C5:F3:11:42:AA", "IoT Smart Camera", 75, "Vulnerable", listOf(80))
        )
    }

    private fun generateInitialWifiNetworks(): List<WifiNetwork> {
        return listOf(
            WifiNetwork("Corporate_Sec_Audit", "D8:07:B6:CA:11:C3", -52, "WPA3-SAE", 6, false),
            WifiNetwork("Customer_Guest_Legacy", "B0:4E:26:88:EC:FA", -76, "WPA2-PSK", 11, true),
            WifiNetwork("Warehouse_Scanner", "00:1A:2B:3C:4D:5E", -82, "WEP", 1, true),
            WifiNetwork("Admin_Internal_QA", "A4:91:B1:33:04:95", -60, "WPA2-PSK", 3, false)
        )
    }

    private fun generateMockPackets(count: Int): List<PacketItem> {
        val protocols = listOf("TCP", "UDP", "DNS", "HTTP", "ARP")
        val ips = listOf("192.168.1.1", "192.168.1.102", "192.168.1.100", "8.8.8.8")
        return List(count) { i ->
            val src = ips.random()
            var dest = ips.random()
            while (dest == src) { dest = ips.random() }
            val proto = protocols.random()
            val length = Random.nextInt(64, 1100)
            
            val targetByteList = ByteArray(16) { Random.nextInt(0, 255).toByte() }
            val hexDumpStr = targetByteList.joinToString(" ") { String.format("%02X", it) } + "  |  " + targetByteList.joinToString("") {
                if (it in 32..126) it.toChar().toString() else "."
            }

            PacketItem(
                id = i + 1,
                time = getCurrentTimestamp(),
                source = src,
                destination = dest,
                protocol = proto,
                length = length,
                info = "Diagnostic packet sequence mock $i ($proto) verified.",
                hexDump = hexDumpStr
            )
        }
    }

    // --- Aircrack-ng Simulation Logic ---
    fun runAircrackSimulation(network: WifiNetwork) {
        if (_isAircrackAuditActive.value) {
            cancelAircrackSimulation()
            return
        }

        _isAircrackAuditActive.value = true
        _aircrackTargetNetwork.value = network
        _aircrackTerminalOutput.value = listOf(
            "[*] Initializing aircrack-ng suite simulation against target: ${network.ssid}",
            "[*] Verifying interface monitor compatibility on mon0...",
            "[+] Interface mon0 supports advanced passive 802.11 injection mode"
        )
        _aircrackStep.value = "AIRODUMP_SCANNING"
        _aircrackProgress.value = 0.05f

        addLogEntry("aircrack-ng mon0", "Launched automated spectral audit session on ${network.ssid}", "SUCCESS", 20)

        viewModelScope.launch(Dispatchers.Default) {
            // Step 1: Airodump Scanning
            var curProg = 0.05f
            while (curProg < 0.3f && _isAircrackAuditActive.value) {
                delay(800)
                curProg += 0.08f
                _aircrackProgress.value = curProg
                _aircrackActivePacketsCount.value = (curProg * 450).toInt()
                _aircrackChannel.value = network.channel
                _aircrackTerminalOutput.update { it + "airodump-ng: passive channel sweep mon0 (Ch ${network.channel}) - Beacons: ${_aircrackActivePacketsCount.value}" }
            }

            if (!_isAircrackAuditActive.value) return@launch

            // Step 2: Aireplay injection (Deauthenticate a legitimate user to force WPA key handshake exchange)
            _aircrackStep.value = "AIREPLAY_DEAUTH"
            _aircrackTerminalOutput.update { it + listOf(
                "[*] Aireplay-ng targeted frame injection initiated...",
                "[-] COMMAND: aireplay-ng --deauth 15 -a ${network.bssid} mon0",
                "[*] Sending Broadcast Disassociation frames on channel ${network.channel}...",
                "[*] Waiting for victim reconnect sequence to capture handshakes..."
            ) }

            delay(2200)
            if (!_isAircrackAuditActive.value) return@launch

            _aircrackTerminalOutput.update { it + listOf(
                "[+] SUCCESS: Captured 4-Way WPA Handshake (Message 1 of 4, Message 2 of 4)!",
                "[*] Writing handshake files to: /tmp/handshake-${network.ssid.take(6)}.cap"
            ) }
            _aircrackProgress.value = 0.55f

            // Step 3: Brute force cracking using aircrack-ng wordlist
            _aircrackStep.value = "AIRCRACK_BRUTEFORCE"
            _aircrackTerminalOutput.update { it + listOf(
                "[*] Initiating dictionary attack via aircrack-ng wordlist parsing...",
                "[-] COMMAND: aircrack-ng -w /usr/share/wordlists/rockyou.txt /tmp/handshake-${network.ssid.take(6)}.cap"
            ) }

            val demoKeys = listOf("password", "12345678", "dragon", "qwerty", "secops-key", "kali-rocks", "admin_secure_key")
            var keyIdx = 0
            while (curProg < 0.95f && _isAircrackAuditActive.value) {
                delay(600)
                curProg += 0.1f
                _aircrackProgress.value = curProg
                val testingKey = demoKeys[keyIdx % demoKeys.size] + Random.nextInt(100, 999)
                _aircrackCurrentTestingKey.value = testingKey
                _aircrackTerminalOutput.update { it + "aircrack-ng: trying candidate key '$testingKey' - Index: ${(curProg * 10000).toInt()}/95000" }
                keyIdx++
            }

            if (!_isAircrackAuditActive.value) return@launch

            // Step 4: Success - Decrypted Key
            _aircrackStep.value = "SUCCESS"
            _aircrackProgress.value = 1.0f
            _aircrackCurrentTestingKey.value = "SecOpsDemoAuditKey2026"
            _aircrackTerminalOutput.update { it + listOf(
                " ",
                "==================================================",
                "       KEY FOUND! [ SecOpsDemoAuditKey2026 ]        ",
                "==================================================",
                "[+] Decryption duration: 6.4 seconds",
                "[+] Probability Index Match: 100.0%",
                "[+] Audit report registered: upgrade access point legacy configuration standards."
            ) }

            _wifiNetworks.update { list ->
                list.map { item ->
                    if (item.ssid == network.ssid) item.copy(handshakeCaptured = true, isVulnerable = false, pinProgress = 1.0f) else item
                }
            }
            updateDashboardMetrics()
            addLogEntry("aircrack-ng mon0", "Decrypted administrative credentials successfully for SSID: ${network.ssid}", "SUCCESS", 6400)
        }
    }

    fun cancelAircrackSimulation() {
        _isAircrackAuditActive.value = false
        _aircrackStep.value = "IDLE"
        _aircrackProgress.value = 0f
        _aircrackTargetNetwork.value = null
        _aircrackTerminalOutput.value = emptyList()
        addLogEntry("aircrack-ng mon0", "Aircrack diagnostic routine canceled by administrator.", "WARNING", 0)
    }

    // --- Metasploit Simulation Logic ---
    fun selectMetasploitExploit(exploit: String) {
        _metasploitSelectedExploit.value = exploit
    }

    fun setMetasploitLport(lport: String) {
        if (lport.isNotBlank()) {
            _metasploitLport.value = lport
        }
    }

    fun runMetasploitExploitation(node: NetworkNode) {
        if (_metasploitStep.value == "EXPLOITING") return
        if (_metasploitStep.value == "METERPRETER_SHELL") {
            closeMetasploitSession()
            return
        }

        _metasploitStep.value = "EXPLOITING"
        _metasploitTargetNode.value = node
        _metasploitProgress.value = 0.05f
        _metasploitMeterpreterOutput.value = listOf(
            "msfconsole > use ${_metasploitSelectedExploit.value}",
            "msf6 exploit(${_metasploitSelectedExploit.value.substringAfterLast("/")}) > set RHOSTS ${node.ip}",
            "RHOSTS => ${node.ip}",
            "msf6 exploit(${_metasploitSelectedExploit.value.substringAfterLast("/")}) > set LPORT ${_metasploitLport.value}",
            "LPORT => ${_metasploitLport.value}",
            "msf6 exploit(${_metasploitSelectedExploit.value.substringAfterLast("/")}) > exploit",
            " ",
            "[*] Started reverse TCP handler on 192.168.1.100:${_metasploitLport.value}",
            "[*] ${node.ip}:80 - Sending crafted payload chunk list to test endpoint..."
        )

        addLogEntry("Metasploit Multi-Handler", "Triggered exploit payload integration against ${node.ip}", "SUCCESS", 10)

        viewModelScope.launch(Dispatchers.Default) {
            var cur = 0.05f
            while (cur < 0.9f && _metasploitStep.value == "EXPLOITING") {
                delay(1000)
                cur += 0.22f
                _metasploitProgress.value = cur
                _metasploitMeterpreterOutput.update { it + listOf(
                    "[*] ${node.ip} - Parsing response descriptors...",
                    "[*] ${node.ip} - Overwriting internal byte buffer indices... (offset: ${(cur * 2048).toInt()}/2048)"
                ) }
            }

            if (_metasploitStep.value != "EXPLOITING") return@launch

            _metasploitStep.value = "METERPRETER_SHELL"
            _metasploitProgress.value = 1.0f
            _metasploitMeterpreterOutput.update { it + listOf(
                "[+] Command shell session 1 opened (192.168.1.100:4444 -> ${node.ip}:${_metasploitLport.value}) at 2026-05-23 HH:mm",
                " ",
                "meterpreter > sysinfo",
                "Computer        : SEC-OPS-STANDBY-TARGET",
                "OS              : Linux Ubuntu 20.04 (Kernel 5.4.0-74-generic)",
                "Architecture    : x64",
                "Meterpreter     : x86/linux",
                "meterpreter > cat /etc/issue",
                "Ubuntu 20.04.2 LTS \\n \\l",
                "meterpreter > status",
                "System is active. Log files analyzed successfully. Remediation is required."
            ) }

            addLogEntry("Metasploit Multi-Handler", "Meterpreter session established successfully with root authorization on target ${node.ip}", "SUCCESS", 4000)
        }
    }

    fun closeMetasploitSession() {
        _metasploitStep.value = "IDLE"
        _metasploitTargetNode.value = null
        _metasploitMeterpreterOutput.value = emptyList()
        _metasploitProgress.value = 0f
        addLogEntry("Metasploit Multi-Handler", "Active penetration assessment session recycled.", "WARNING", 0)
    }

    // --- Burp Suite Proxy Simulation Logic ---
    fun runBurpProxyScan() {
        if (_burpIsScanning.value) return
        _burpIsScanning.value = true
        _burpScanProgress.value = 0.05f
        _burpDiscoveredVulnerabilities.value = emptyList()
        _burpIntruderPayloadsTried.value = 0

        addLogEntry("Burp Scanner Probe", "Launched passive web crawling diagnostics on audited host URLs.", "SUCCESS", 10)

        viewModelScope.launch(Dispatchers.Default) {
            val potentialFindings = listOf(
                "High: SQL Injection via parameter 'q' inside page: search.php",
                "Medium: Cross-Site Scripting (Reflected) via 'username' input inside page: login.php",
                "Low: SSL/TLS Cookie attribute missing Secure/HttpOnly flags inside endpoint: index.php",
                "High: Path Traversal (Arbitrary File Read) on endpoint: /api/v1/users"
            )

            var cur = 0.05f
            var findIndex = 0
            while (cur < 1.0f && _burpIsScanning.value) {
                delay(1200)
                cur += 0.2f
                _burpScanProgress.value = cur
                _burpIntruderPayloadsTried.value = (cur * 1500).toInt()

                if (cur >= 0.4f && findIndex == 0) {
                    _burpDiscoveredVulnerabilities.update { it + potentialFindings[0] }
                    findIndex++
                }
                if (cur >= 0.7f && findIndex == 1) {
                    _burpDiscoveredVulnerabilities.update { it + potentialFindings[1] }
                    findIndex++
                }
                if (cur >= 0.9f && findIndex == 2) {
                    _burpDiscoveredVulnerabilities.update { it + potentialFindings[2] }
                    findIndex++
                }
            }

            if (!_burpIsScanning.value) return@launch

            _burpIsScanning.value = false
            _burpScanProgress.value = 1.0f
            _burpDiscoveredVulnerabilities.update { it + potentialFindings[3] }

            addLogEntry(
                commandName = "Burp Scanner Probe",
                terminalOutput = "Completed target scanning audit. Identified ${_burpDiscoveredVulnerabilities.value.size} active security vulnerabilities in simulated web apps.",
                status = "SUCCESS",
                duration = 6000
            )
        }
    }

    fun stopBurpScan() {
        _burpIsScanning.value = false
        _burpScanProgress.value = 0f
        addLogEntry("Burp Scanner Probe", "Web security audit scanner suspended.", "WARNING", 0)
    }
}
