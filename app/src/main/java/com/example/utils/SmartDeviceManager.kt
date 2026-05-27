package com.example.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.Socket

/**
 * SmartDeviceManager — IoT and Smart Device Discovery & Control
 *
 * LEGAL NOTICE: Only use on devices you own or have explicit permission to audit.
 *
 * Capabilities:
 * - ADB-over-network scanner (Android TV, Phones with adb debugging)
 * - mDNS/Zeroconf discovery (Chromecast, Apple TV, smart speakers)
 * - MQTT broker detection (smart home hubs)
 * - Router admin panel detection
 * - UPnP device discovery
 * - Smart TV ADB command execution
 */
class SmartDeviceManager {

    companion object {
        // ADB over TCP default port
        const val ADB_PORT = 5555

        // Common smart device ports
        val SMART_DEVICE_PORTS = mapOf(
            5555 to "ADB (Android TV/Phone)",
            7000 to "AirPlay (Apple TV)",
            8008 to "Chromecast HTTP",
            8009 to "Chromecast Control",
            1883 to "MQTT (Smart Home Hub)",
            8883 to "MQTT TLS",
            9000 to "Hue Bridge",
            80   to "HTTP Admin Panel",
            443  to "HTTPS Admin",
            23   to "Telnet (Legacy Cam/Router)",
            2323 to "Telnet Alt",
            8080 to "Alt HTTP",
            8443 to "Alt HTTPS",
            9999 to "TP-Link Kasa Smart Plug",
            4343 to "Insteon Hub",
            55443 to "Mi Home",
            1400 to "Sonos"
        )

        // Common router admin default credentials
        val ROUTER_CREDENTIALS = listOf(
            Pair("admin", "admin"),
            Pair("admin", "password"),
            Pair("admin", ""),
            Pair("admin", "1234"),
            Pair("root", "root"),
            Pair("root", "admin"),
            Pair("admin", "admin123"),
            Pair("user", "user"),
            Pair("admin", "12345"),
            Pair("admin", "pass")
        )

        // mDNS multicast address
        const val MDNS_ADDR = "224.0.0.251"
        const val MDNS_PORT = 5353

        // UPnP multicast
        const val UPNP_ADDR = "239.255.255.250"
        const val UPNP_PORT = 1900
    }

    /**
     * Scan subnet for smart devices on common IoT ports.
     */
    fun scanForSmartDevices(subnet: String): Flow<String> = flow {
        emit("[*] Starting IoT/Smart Device scan on $subnet.0/24")
        emit("[*] Checking ports: ${SMART_DEVICE_PORTS.keys.take(8).joinToString(", ")}...")
        emit("[*] This may take 30-60 seconds...")
        emit("")

        val found = mutableListOf<String>()

        for (i in 1..254) {
            val host = "$subnet.$i"
            try {
                val addr = InetAddress.getByName(host)
                if (addr.isReachable(500)) {
                    val openPorts = mutableListOf<Pair<Int, String>>()

                    for ((port, desc) in SMART_DEVICE_PORTS) {
                        try {
                            val sock = Socket()
                            sock.connect(java.net.InetSocketAddress(host, port), 500)
                            openPorts.add(Pair(port, desc))
                            sock.close()
                        } catch (_: Exception) {}
                    }

                    if (openPorts.isNotEmpty()) {
                        found.add(host)
                        emit("[FOUND] Smart Device at: $host")
                        openPorts.forEach { (port, desc) ->
                            emit("   └─ Port $port: $desc")
                        }
                        // Detect device type
                        val deviceType = detectDeviceType(host, openPorts.map { it.first })
                        if (deviceType.isNotEmpty()) {
                            emit("   └─ Likely: $deviceType")
                        }
                        emit("")
                    }
                }
            } catch (_: Exception) {}
        }

        if (found.isEmpty()) {
            emit("[INFO] No smart devices found on $subnet.0/24")
            emit("[INFO] Ensure you are on the same WiFi network as target devices")
        } else {
            emit("[+] Scan complete. Found ${found.size} smart device(s).")
        }
    }.flowOn(Dispatchers.IO)

    private fun detectDeviceType(host: String, openPorts: List<Int>): String {
        return when {
            5555 in openPorts -> "Android TV / Android Device with ADB"
            8008 in openPorts || 8009 in openPorts -> "Google Chromecast / Nest Device"
            7000 in openPorts -> "Apple TV / AirPlay Device"
            1883 in openPorts || 8883 in openPorts -> "MQTT Smart Home Hub (Home Assistant, etc.)"
            9999 in openPorts -> "TP-Link Kasa Smart Plug/Switch"
            1400 in openPorts -> "Sonos Speaker"
            23 in openPorts -> "Legacy Device (Camera/Router with Telnet)"
            else -> ""
        }
    }

    /**
     * Scan for ADB-enabled Android devices (Android TVs, phones with developer mode).
     */
    fun scanForAdbDevices(subnet: String): Flow<String> = flow {
        emit("[*] Scanning for ADB-enabled Android devices on $subnet.0/24")
        emit("[*] Port: $ADB_PORT (adb over TCP)")
        emit("[!] NOTE: Device must have 'ADB over network' enabled")
        emit("")

        var foundCount = 0

        for (i in 1..254) {
            val host = "$subnet.$i"
            try {
                val sock = Socket()
                sock.connect(java.net.InetSocketAddress(host, ADB_PORT), 800)
                sock.close()

                foundCount++
                emit("[FOUND] ADB device at $host:$ADB_PORT")
                emit("[INFO] Connect with: adb connect $host:$ADB_PORT")
                emit("[INFO] Then: adb -s $host:$ADB_PORT shell")

                // Try to get device info via root
                try {
                    val proc = Runtime.getRuntime().exec(
                        arrayOf("su", "-c", "adb connect $host:$ADB_PORT")
                    )
                    proc.waitFor()
                    emit("[+] Auto-connected to $host:$ADB_PORT")
                } catch (_: Exception) {}

                emit("")
            } catch (_: Exception) {}
        }

        emit("[*] ADB scan complete. Found $foundCount device(s).")
        if (foundCount == 0) {
            emit("[INFO] No ADB devices found. Enable 'ADB over Network' on Android TV:")
            emit("[INFO]   Settings > Device Preferences > Developer Options > ADB debugging ON")
            emit("[INFO]   Then: Network debugging ON")
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Send ADB command to a remote Android TV / device.
     */
    fun runAdbCommand(host: String, command: String): Flow<String> = flow {
        emit("[*] Running ADB command on $host:$ADB_PORT")
        emit("[*] Command: $command")
        emit("")

        try {
            // First connect
            val connectProc = Runtime.getRuntime().exec(
                arrayOf("su", "-c", "adb connect $host:$ADB_PORT 2>&1")
            )
            val connectOut = connectProc.inputStream.bufferedReader().readText().trim()
            connectProc.waitFor()
            emit("[*] Connection: $connectOut")

            if (connectOut.contains("connected") || connectOut.contains("already")) {
                // Run the command
                val cmdProc = Runtime.getRuntime().exec(
                    arrayOf("su", "-c", "adb -s $host:$ADB_PORT shell $command 2>&1")
                )
                val cmdOut = cmdProc.inputStream.bufferedReader().readText()
                cmdProc.waitFor()

                emit("[OUTPUT]")
                cmdOut.lines().forEach { emit(it) }
                emit("")
                emit("[+] Command executed successfully.")
            } else {
                emit("[ERROR] Could not connect to $host:$ADB_PORT")
                emit("[INFO] Ensure ADB over network is enabled on the target device")
            }
        } catch (e: Exception) {
            emit("[ERROR] ADB not available: ${e.message}")
            emit("[INFO] ADB binary required: install via Termux or Kali NetHunter")
            emit("[INFO] pkg install android-tools")
        }
    }.flowOn(Dispatchers.IO)

    /**
     * UPnP device discovery via SSDP multicast.
     */
    fun discoverUpnpDevices(timeoutMs: Int = 5000): Flow<String> = flow {
        emit("[*] Broadcasting UPnP/SSDP discovery probe...")
        emit("[*] Multicast: $UPNP_ADDR:$UPNP_PORT")
        emit("")

        try {
            val socket = DatagramSocket()
            socket.soTimeout = timeoutMs
            socket.broadcast = true

            val ssdpRequest = "M-SEARCH * HTTP/1.1\r\n" +
                    "HOST: $UPNP_ADDR:$UPNP_PORT\r\n" +
                    "MAN: \"ssdp:discover\"\r\n" +
                    "MX: 3\r\n" +
                    "ST: ssdp:all\r\n\r\n"

            val requestData = ssdpRequest.toByteArray()
            val packet = DatagramPacket(
                requestData, requestData.size,
                InetAddress.getByName(UPNP_ADDR), UPNP_PORT
            )
            socket.send(packet)
            emit("[*] SSDP probe sent. Waiting for responses...")
            emit("")

            val found = mutableSetOf<String>()
            val buf = ByteArray(4096)

            try {
                while (true) {
                    val recv = DatagramPacket(buf, buf.size)
                    socket.receive(recv)
                    val ip = recv.address.hostAddress ?: continue
                    val response = String(recv.data, 0, recv.length)

                    if (!found.contains(ip)) {
                        found.add(ip)
                        emit("[FOUND] UPnP device at: $ip")

                        // Parse device info from response
                        val location = Regex("LOCATION: (.+)", RegexOption.IGNORE_CASE)
                            .find(response)?.groupValues?.get(1)?.trim()
                        val server = Regex("SERVER: (.+)", RegexOption.IGNORE_CASE)
                            .find(response)?.groupValues?.get(1)?.trim()
                        val st = Regex("ST: (.+)", RegexOption.IGNORE_CASE)
                            .find(response)?.groupValues?.get(1)?.trim()

                        if (server != null) emit("   └─ Server: $server")
                        if (st != null) emit("   └─ Type: $st")
                        if (location != null) emit("   └─ Control URL: $location")
                        emit("")
                    }
                }
            } catch (_: java.net.SocketTimeoutException) {
                emit("[*] Discovery complete. Found ${found.size} UPnP device(s).")
            }
            socket.close()
        } catch (e: Exception) {
            emit("[ERROR] UPnP discovery failed: ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Test default credentials against a router's HTTP admin panel.
     */
    fun crackRouterAdmin(host: String, port: Int = 80): Flow<String> = flow {
        emit("[*] Testing router admin panel at http://$host:$port")
        emit("[*] Testing ${ROUTER_CREDENTIALS.size} default credential pairs...")
        emit("")

        var successFound = false

        for ((user, pass) in ROUTER_CREDENTIALS) {
            if (successFound) break
            emit("[TRY] $user:$pass -> http://$host:$port/")

            try {
                val url = java.net.URL("http://$host:$port/")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                conn.requestMethod = "GET"

                // Basic Auth
                val credentials = "$user:$pass"
                val encoded = android.util.Base64.encodeToString(
                    credentials.toByteArray(),
                    android.util.Base64.NO_WRAP
                )
                conn.setRequestProperty("Authorization", "Basic $encoded")

                val code = conn.responseCode
                if (code == 200) {
                    val body = conn.inputStream.bufferedReader().readText()
                    if (!body.contains("401") && !body.contains("Unauthorized")) {
                        emit("[SUCCESS] ✓ LOGIN SUCCESS!")
                        emit("[SUCCESS] URL: http://$host:$port/")
                        emit("[SUCCESS] Credentials: $user:$pass")
                        emit("[INFO] Router admin accessible — check settings immediately!")
                        successFound = true
                    }
                }
                conn.disconnect()
            } catch (_: Exception) {}
        }

        if (!successFound) {
            emit("")
            emit("[!] No default credentials worked.")
            emit("[INFO] Router may have changed default credentials (good!)")
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Generate common ADB TV commands for entertainment/control.
     */
    fun getCommonAdbTvCommands(): List<Pair<String, String>> = listOf(
        Pair("Screenshot", "screencap -p /sdcard/screenshot.png"),
        Pair("Device Info", "getprop ro.product.model"),
        Pair("Battery Status", "dumpsys battery"),
        Pair("Installed Apps", "pm list packages"),
        Pair("Launch App", "am start -n com.android.settings/.Settings"),
        Pair("Reboot Device", "reboot"),
        Pair("Volume Up", "input keyevent 24"),
        Pair("Volume Down", "input keyevent 25"),
        Pair("Home Button", "input keyevent 3"),
        Pair("Back Button", "input keyevent 4"),
        Pair("Mute", "input keyevent 164"),
        Pair("List WiFi Networks", "wpa_cli list_networks"),
        Pair("Network Info", "ip addr show"),
        Pair("Logcat Dump", "logcat -d -t 50")
    )
}
