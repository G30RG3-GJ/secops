package com.example.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import com.example.models.WifiNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RealWifiScanner(private val context: Context) {

    private val wifiManager: WifiManager by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    /**
     * Trigger a WiFi scan and return real scan results.
     * Requires ACCESS_FINE_LOCATION permission on Android 9+.
     */
    @SuppressLint("MissingPermission")
    suspend fun scanNetworks(): List<WifiNetwork> = withContext(Dispatchers.IO) {
        try {
            // Request a fresh scan
            val scanStarted = wifiManager.startScan()
            if (!scanStarted) {
                // On Android 9+, app-triggered scans are throttled to 4/2min
                // Still return cached results
            }
            // Small delay to let scan complete
            kotlinx.coroutines.delay(2000)
            val results = wifiManager.scanResults ?: emptyList()
            results.map { it.toWifiNetwork() }.sortedByDescending { it.signalLevel }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get cached scan results immediately (no fresh scan).
     */
    @SuppressLint("MissingPermission")
    fun getCachedNetworks(): List<WifiNetwork> {
        return try {
            (wifiManager.scanResults ?: emptyList())
                .map { it.toWifiNetwork() }
                .sortedByDescending { it.signalLevel }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get detailed info about the currently connected network.
     */
    @SuppressLint("MissingPermission")
    fun getConnectedNetworkInfo(): ConnectedNetworkInfo {
        return try {
            val connInfo = wifiManager.connectionInfo
            val dhcpInfo = wifiManager.dhcpInfo
            ConnectedNetworkInfo(
                ssid = connInfo.ssid?.removeSurrounding("\"") ?: "Not Connected",
                bssid = connInfo.bssid ?: "N/A",
                ipAddress = intToIp(connInfo.ipAddress),
                gateway = intToIp(dhcpInfo.gateway),
                dns1 = intToIp(dhcpInfo.dns1),
                dns2 = intToIp(dhcpInfo.dns2),
                linkSpeed = "${connInfo.linkSpeed} Mbps",
                frequency = "${connInfo.frequency} MHz",
                rssi = "${connInfo.rssi} dBm",
                signalStrength = WifiManager.calculateSignalLevel(connInfo.rssi, 5),
                networkId = connInfo.networkId
            )
        } catch (e: Exception) {
            ConnectedNetworkInfo()
        }
    }

    /**
     * Check if WiFi is enabled.
     */
    fun isWifiEnabled(): Boolean = wifiManager.isWifiEnabled

    private fun ScanResult.toWifiNetwork(): WifiNetwork {
        return WifiNetwork(
            ssid = if (SSID.isNullOrEmpty()) "<Hidden Network>" else SSID,
            bssid = BSSID ?: "00:00:00:00:00:00",
            signalLevel = level,
            encryption = getEncryptionType(capabilities),
            channel = frequencyToChannel(frequency),
            isVulnerable = isVulnerableNetwork(capabilities),
            frequency = frequency,
            capabilities = capabilities ?: ""
        )
    }

    private fun getEncryptionType(capabilities: String?): String {
        if (capabilities == null) return "UNKNOWN"
        return when {
            capabilities.contains("WPA3") && capabilities.contains("SAE") -> "WPA3-SAE"
            capabilities.contains("WPA3") -> "WPA3"
            capabilities.contains("WPA2") && capabilities.contains("PSK") -> "WPA2-PSK"
            capabilities.contains("WPA2") && capabilities.contains("EAP") -> "WPA2-EAP"
            capabilities.contains("WPA") -> "WPA-PSK"
            capabilities.contains("WEP") -> "WEP"
            capabilities.contains("OWE") -> "OWE (Enhanced Open)"
            capabilities.contains("[ESS]") && !capabilities.contains("WPA") &&
                    !capabilities.contains("WEP") -> "OPEN (No Encryption)"
            else -> "UNKNOWN"
        }
    }

    private fun isVulnerableNetwork(capabilities: String?): Boolean {
        if (capabilities == null) return false
        return capabilities.contains("WEP") ||
                (capabilities.contains("[ESS]") &&
                        !capabilities.contains("WPA") &&
                        !capabilities.contains("WEP"))
    }

    private fun frequencyToChannel(freq: Int): Int {
        return when {
            freq == 2484 -> 14
            freq in 2412..2472 -> (freq - 2412) / 5 + 1
            freq in 5170..5825 -> (freq - 5000) / 5
            freq in 5955..7115 -> (freq - 5955) / 5 + 1  // 6GHz band (WiFi 6E)
            else -> 0
        }
    }

    private fun intToIp(ipInt: Int): String {
        return if (ipInt == 0) "N/A" else
            "${ipInt and 0xFF}.${(ipInt shr 8) and 0xFF}.${(ipInt shr 16) and 0xFF}.${(ipInt shr 24) and 0xFF}"
    }

    data class ConnectedNetworkInfo(
        val ssid: String = "Not Connected",
        val bssid: String = "N/A",
        val ipAddress: String = "N/A",
        val gateway: String = "N/A",
        val dns1: String = "N/A",
        val dns2: String = "N/A",
        val linkSpeed: String = "N/A",
        val frequency: String = "N/A",
        val rssi: String = "N/A",
        val signalStrength: Int = 0,
        val networkId: Int = -1
    )
}
