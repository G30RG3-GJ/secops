package com.example.models

enum class ToolCategory {
    NETWORK_FLOW,    // Wireshark/Packet analysis
    RECONNAISSANCE,  // Nmap/Scanner
    WIRELESS,        // WiFi Auditing / Aircrack-ng
    AI_ASSISTANT,    // Gemini intelligence interface
    EXPLOITATION,    // Penetration testing tools
    WEB_AUDIT        // HTTP/SSL/Header analysis
}

data class AuditCommand(
    val id: String,
    val name: String,
    val description: String,
    val commandTemplate: String,
    val category: ToolCategory,
    val parameters: List<String> = emptyList()
)

data class ExecutionLog(
    val timestamp: String,
    val commandName: String,
    val status: String,               // "SUCCESS", "FAILED", "WARNING", "REAL"
    val durationMs: Long,
    val terminalOutput: String,
    val visualizedMetrics: List<MetricItem> = emptyList(),
    val isRealData: Boolean = false   // true = real data, false = simulated
)

data class MetricItem(
    val label: String,
    val value: Float,
    val percentage: Int,
    val category: String = ""
)

data class NetworkNode(
    val ip: String,
    val mac: String = "Unknown",
    val vendor: String = "Unknown",
    val latencyMs: Int = 0,
    val status: String,               // "Active", "Filtering", "Vulnerable"
    val openPorts: List<Int> = emptyList(),
    val serviceDetails: Map<Int, String> = emptyMap(),
    val hostname: String = "",
    val isRealData: Boolean = false   // true = discovered by real scan
)

data class PacketItem(
    val id: Int,
    val time: String,
    val source: String,
    val destination: String,
    val protocol: String,             // "TCP", "UDP", "DNS", "HTTP", "ARP"
    val length: Int,
    val info: String,
    val hexDump: String
)

data class WifiNetwork(
    val ssid: String,
    val bssid: String,
    val signalLevel: Int,             // dBm (e.g. -52)
    val encryption: String,           // "WPA3-SAE", "WPA2-PSK", "WEP", "OPEN"
    val channel: Int,
    val isVulnerable: Boolean,
    val pinProgress: Float = 0f,
    val handshakeCaptured: Boolean = false,
    val frequency: Int = 0,           // MHz
    val capabilities: String = "",    // Raw capabilities string from Android
    val isRealData: Boolean = false   // true = from Android WifiManager
)

data class MessageItem(
    val sender: String,
    val content: String
)

// Root/environment status
data class DeviceSecurityStatus(
    val isRooted: Boolean = false,
    val rootGranted: Boolean = false,
    val aircrackAvailable: Boolean = false,
    val airodumpAvailable: Boolean = false,
    val aireplayAvailable: Boolean = false,
    val airmonAvailable: Boolean = false,
    val tcpdumpAvailable: Boolean = false,
    val nmapAvailable: Boolean = false,
    val toolsInfo: String = ""
) {
    val canRunAircrack: Boolean get() = isRooted && rootGranted && aircrackAvailable
    val statusSummary: String
        get() = buildString {
            appendLine("ROOT: ${if (rootGranted) "✓ GRANTED" else if (isRooted) "⚠ BINARY FOUND" else "✗ NOT ROOTED"}")
            appendLine("aircrack-ng: ${if (aircrackAvailable) "✓ FOUND" else "✗ NOT FOUND"}")
            appendLine("airodump-ng: ${if (airodumpAvailable) "✓ FOUND" else "✗ NOT FOUND"}")
            appendLine("aireplay-ng: ${if (aireplayAvailable) "✓ FOUND" else "✗ NOT FOUND"}")
        }
}

// Aircrack session state
data class AircrackSession(
    val targetSsid: String = "",
    val targetBssid: String = "",
    val channel: Int = 0,
    val monitorInterface: String = "",
    val capturePath: String = "",
    val wordlistPath: String = "",
    val phase: AircrackPhase = AircrackPhase.IDLE,
    val progress: Float = 0f,
    val terminalLines: List<String> = emptyList(),
    val keyFound: String? = null,
    val packetsCount: Int = 0
)

enum class AircrackPhase {
    IDLE,
    MONITOR_MODE,
    AIRODUMP_SCANNING,
    AIREPLAY_DEAUTH,
    AIRCRACK_CRACKING,
    SUCCESS,
    FAILED
}

// Network scan session
data class NetworkScanSession(
    val subnet: String = "",
    val phase: ScanPhase = ScanPhase.IDLE,
    val hostsFound: List<String> = emptyList(),
    val currentTarget: String = "",
    val progress: Float = 0f,
    val terminalLines: List<String> = emptyList()
)

enum class ScanPhase {
    IDLE,
    HOST_DISCOVERY,
    PORT_SCANNING,
    COMPLETE
}

// DNS/Network tool result
data class ToolResult(
    val toolName: String,
    val target: String,
    val output: String,
    val success: Boolean,
    val timestamp: String
)
