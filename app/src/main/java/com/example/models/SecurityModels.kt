package com.example.models

enum class ToolCategory {
    NETWORK_FLOW, // Wireshark/Packet analysis
    RECONNAISSANCE, // Nmap/Scanner
    WIRELESS, // WiFi Auditing
    AI_ASSISTANT // Gemini intelligence interface
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
    val status: String, // "SUCCESS", "FAILED", "WARNING"
    val durationMs: Long,
    val terminalOutput: String,
    val visualizedMetrics: List<MetricItem> = emptyList()
)

data class MetricItem(
    val label: String,
    val value: Float,
    val percentage: Int,
    val category: String = ""
)

data class NetworkNode(
    val ip: String,
    val mac: String,
    val vendor: String,
    val latencyMs: Int,
    val status: String, // "Active", "Filtering", "Vulnerable"
    val openPorts: List<Int> = emptyList(),
    val serviceDetails: Map<Int, String> = emptyMap()
)

data class PacketItem(
    val id: Int,
    val time: String,
    val source: String,
    val destination: String,
    val protocol: String, // "TCP", "UDP", "DNS", "HTTP", "ARP"
    val length: Int,
    val info: String,
    val hexDump: String
)

data class WifiNetwork(
    val ssid: String,
    val bssid: String,
    val signalLevel: Int, // dBm
    val encryption: String, // "WPA3-SAE", "WPA2-PSK", "WEP", "OPEN"
    val channel: Int,
    val isVulnerable: Boolean,
    val pinProgress: Float = 0f,
    val handshakeCaptured: Boolean = false
)

data class MessageItem(
    val sender: String,
    val content: String
)

