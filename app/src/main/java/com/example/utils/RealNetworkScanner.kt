package com.example.utils

import com.example.models.NetworkNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class RealNetworkScanner {

    /**
     * Discover live hosts in a /24 subnet. e.g. subnet = "192.168.1"
     * Returns list of reachable IP addresses.
     */
    suspend fun discoverHosts(
        subnet: String,
        timeout: Int = 1500,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): List<String> = withContext(Dispatchers.IO) {
        coroutineScope {
            val total = 254
            var completed = 0
            (1..total).map { i ->
                async {
                    val host = "$subnet.$i"
                    val reachable = try {
                        withTimeoutOrNull(timeout.toLong()) {
                            InetAddress.getByName(host).isReachable(timeout)
                        } ?: false
                    } catch (e: Exception) {
                        false
                    }
                    synchronized(this@RealNetworkScanner) {
                        completed++
                        onProgress(completed, total)
                    }
                    if (reachable) host else null
                }
            }.awaitAll().filterNotNull().sorted()
        }
    }

    /**
     * Scan common ports on a host. Returns list of open ports.
     */
    suspend fun scanCommonPorts(
        host: String,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): List<Int> = withContext(Dispatchers.IO) {
        coroutineScope {
            val commonPorts = intArrayOf(
                21, 22, 23, 25, 53, 80, 110, 135, 139, 143,
                443, 445, 993, 995, 1723, 3306, 3389, 5900, 8080, 8443
            )
            var completed = 0
            commonPorts.map { port ->
                async {
                    val open = isPortOpen(host, port, 800)
                    synchronized(this@RealNetworkScanner) {
                        completed++
                        onProgress(completed, commonPorts.size)
                    }
                    if (open) port else null
                }
            }.awaitAll().filterNotNull().sorted()
        }
    }

    /**
     * Scan a range of ports on a host.
     */
    suspend fun scanPortRange(
        host: String,
        startPort: Int,
        endPort: Int,
        timeout: Int = 500,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): List<Int> = withContext(Dispatchers.IO) {
        coroutineScope {
            val total = endPort - startPort + 1
            var completed = 0
            (startPort..endPort).map { port ->
                async {
                    val open = isPortOpen(host, port, timeout)
                    synchronized(this@RealNetworkScanner) {
                        completed++
                        onProgress(completed, total)
                    }
                    if (open) port else null
                }
            }.awaitAll().filterNotNull().sorted()
        }
    }

    private fun isPortOpen(host: String, port: Int, timeout: Int): Boolean {
        return try {
            val socket = Socket()
            socket.connect(InetSocketAddress(host, port), timeout)
            socket.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Ping a host and measure latency.
     */
    suspend fun pingHost(host: String, count: Int = 4): PingResult = withContext(Dispatchers.IO) {
        try {
            val result = RealCommandExecutor.executeSync("ping -c $count -W 2 $host")
            val lines = result.stdout.lines()

            // Parse ping statistics
            val statsLine = lines.firstOrNull { it.contains("min/avg/max") }
            val packetLine = lines.firstOrNull { it.contains("packets transmitted") }

            val stats = statsLine?.let {
                val parts = it.substringAfter("=").trim().split("/")
                if (parts.size >= 3) Triple(parts[0].toFloatOrNull(), parts[1].toFloatOrNull(), parts[2].toFloatOrNull()) else null
            }

            val received = packetLine?.let {
                Regex("(\\d+) received").find(it)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            } ?: 0

            PingResult(
                host = host,
                success = received > 0,
                output = result.stdout,
                minMs = stats?.first,
                avgMs = stats?.second,
                maxMs = stats?.third,
                packetsReceived = received,
                packetsSent = count
            )
        } catch (e: Exception) {
            PingResult(host = host, success = false, output = "Error: ${e.message}")
        }
    }

    /**
     * DNS lookup - forward and reverse.
     */
    suspend fun dnsLookup(hostname: String): DnsResult = withContext(Dispatchers.IO) {
        try {
            val addresses = InetAddress.getAllByName(hostname)
            val ips = addresses.map { it.hostAddress ?: "" }

            // Reverse lookup for first IP
            val reverseLookup = try {
                InetAddress.getByName(ips.firstOrNull()).canonicalHostName
            } catch (e: Exception) { null }

            DnsResult(
                hostname = hostname,
                addresses = ips,
                reverseDns = reverseLookup,
                success = true
            )
        } catch (e: Exception) {
            DnsResult(hostname = hostname, success = false, error = e.message)
        }
    }

    /**
     * Traceroute using system command.
     */
    fun traceroute(host: String): Flow<String> = flow {
        emit("[*] Starting traceroute to $host")
        try {
            val process = Runtime.getRuntime().exec(arrayOf("traceroute", "-n", "-m", "30", host))
            val reader = process.inputStream.bufferedReader()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                emit(line!!)
            }
            process.waitFor()
        } catch (e: Exception) {
            // Fallback: use ping-based traceroute substitute
            emit("[!] traceroute not available, using ping-based path discovery...")
            for (ttl in 1..15) {
                val result = RealCommandExecutor.executeSync("ping -c 1 -W 1 -t $ttl $host")
                if (result.stdout.contains("From") || result.stdout.contains("bytes from")) {
                    val hopLine = result.stdout.lines().firstOrNull { it.contains("From") || it.contains("bytes from") }
                    emit("$ttl  ${hopLine?.trim() ?: "* * *"}")
                    if (result.stdout.contains(host)) break
                } else {
                    emit("$ttl  * * *  (timeout)")
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get hostname for an IP.
     */
    suspend fun reverseHostname(ip: String): String = withContext(Dispatchers.IO) {
        try {
            InetAddress.getByName(ip).canonicalHostName
        } catch (e: Exception) {
            ip
        }
    }

    /**
     * Get local subnet from an IP like "192.168.1.100" -> "192.168.1"
     */
    fun getSubnet(ip: String): String = ip.substringBeforeLast(".")

    data class PingResult(
        val host: String,
        val success: Boolean,
        val output: String = "",
        val minMs: Float? = null,
        val avgMs: Float? = null,
        val maxMs: Float? = null,
        val packetsReceived: Int = 0,
        val packetsSent: Int = 0
    )

    data class DnsResult(
        val hostname: String,
        val addresses: List<String> = emptyList(),
        val reverseDns: String? = null,
        val success: Boolean = false,
        val error: String? = null
    )
}
