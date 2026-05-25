package com.example.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object NetworkUtils {

    /**
     * Get public IP address.
     */
    suspend fun getPublicIp(): String = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.ipify.org")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.inputStream.bufferedReader().readText().trim()
        } catch (e: Exception) {
            "N/A"
        }
    }

    /**
     * Get IP geolocation info.
     */
    suspend fun getIpGeoInfo(ip: String): Map<String, String> = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://ipapi.co/$ip/json/")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.setRequestProperty("User-Agent", "SecOps-Console/2.0")
            val json = conn.inputStream.bufferedReader().readText()

            // Simple JSON parsing
            val result = mutableMapOf<String, String>()
            val fields = listOf("ip", "city", "region", "country_name", "org", "asn", "latitude", "longitude", "timezone")
            fields.forEach { field ->
                val pattern = Regex("\"$field\"\\s*:\\s*\"?([^\"\\n,}]+)\"?")
                pattern.find(json)?.groupValues?.get(1)?.trim()?.let {
                    result[field] = it
                }
            }
            result
        } catch (e: Exception) {
            mapOf("error" to (e.message ?: "Unknown error"))
        }
    }

    /**
     * Simple Whois via web API.
     */
    suspend fun getWhoisInfo(domain: String): String = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://www.whoisxmlapi.com/whoisserver/WhoisService?apiKey=at_demo&domainName=$domain&outputFormat=json")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            val json = conn.inputStream.bufferedReader().readText()
            json
        } catch (e: Exception) {
            // Fallback: system whois
            try {
                val result = RealCommandExecutor.executeSync("whois $domain")
                result.stdout.take(2000)
            } catch (ex: Exception) {
                "Whois lookup failed: ${e.message}"
            }
        }
    }

    /**
     * Check if SSL certificate is valid for a host.
     */
    suspend fun checkSslCert(host: String, port: Int = 443): SslInfo = withContext(Dispatchers.IO) {
        try {
            val factory = javax.net.ssl.SSLSocketFactory.getDefault()
            val socket = factory.createSocket(host, port) as javax.net.ssl.SSLSocket
            socket.soTimeout = 5000
            socket.startHandshake()
            val session = socket.session
            val cert = session.peerCertificates.firstOrNull() as? java.security.cert.X509Certificate
            socket.close()

            SslInfo(
                valid = true,
                subject = cert?.subjectDN?.name ?: "N/A",
                issuer = cert?.issuerDN?.name ?: "N/A",
                validFrom = cert?.notBefore?.toString() ?: "N/A",
                validTo = cert?.notAfter?.toString() ?: "N/A",
                protocol = session.protocol,
                cipherSuite = session.cipherSuite
            )
        } catch (e: javax.net.ssl.SSLException) {
            SslInfo(valid = false, error = "SSL Error: ${e.message}")
        } catch (e: Exception) {
            SslInfo(valid = false, error = e.message ?: "Connection failed")
        }
    }

    /**
     * Check HTTP response headers for security headers.
     */
    suspend fun checkHttpHeaders(urlStr: String): HttpHeadersAnalysis = withContext(Dispatchers.IO) {
        try {
            val url = URL(if (urlStr.startsWith("http")) urlStr else "https://$urlStr")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.requestMethod = "HEAD"
            conn.instanceFollowRedirects = true
            conn.connect()

            val headers = conn.headerFields.mapValues { it.value.joinToString(", ") }
            val statusCode = conn.responseCode
            conn.disconnect()

            val securityHeaders = mapOf(
                "Strict-Transport-Security" to (headers["Strict-Transport-Security"] ?: "MISSING ⚠️"),
                "Content-Security-Policy" to (headers["Content-Security-Policy"] ?: "MISSING ⚠️"),
                "X-Frame-Options" to (headers["X-Frame-Options"] ?: "MISSING ⚠️"),
                "X-Content-Type-Options" to (headers["X-Content-Type-Options"] ?: "MISSING ⚠️"),
                "Referrer-Policy" to (headers["Referrer-Policy"] ?: "MISSING ⚠️"),
                "Permissions-Policy" to (headers["Permissions-Policy"] ?: "MISSING ⚠️"),
                "X-XSS-Protection" to (headers["X-XSS-Protection"] ?: "MISSING ⚠️")
            )

            val score = securityHeaders.values.count { !it.contains("MISSING") }

            HttpHeadersAnalysis(
                url = urlStr,
                statusCode = statusCode,
                server = headers["Server"] ?: "Not disclosed",
                securityHeaders = securityHeaders,
                securityScore = "$score/7",
                allHeaders = headers
            )
        } catch (e: Exception) {
            HttpHeadersAnalysis(url = urlStr, error = e.message)
        }
    }

    /**
     * Hash lookup via online APIs.
     */
    suspend fun lookupHash(hash: String): String = withContext(Dispatchers.IO) {
        try {
            // Try MD5 lookup
            if (hash.length == 32) {
                val url = URL("https://md5decrypt.net/Api/api.php?hash=$hash&hash_type=md5&email=test@test.com&code=")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                val result = conn.inputStream.bufferedReader().readText().trim()
                if (result.isNotEmpty() && result != "Not found") return@withContext "MD5 Cracked: $result"
            }
            "Hash not found in online database"
        } catch (e: Exception) {
            "Lookup failed: ${e.message}"
        }
    }

    data class SslInfo(
        val valid: Boolean,
        val subject: String = "",
        val issuer: String = "",
        val validFrom: String = "",
        val validTo: String = "",
        val protocol: String = "",
        val cipherSuite: String = "",
        val error: String? = null
    )

    data class HttpHeadersAnalysis(
        val url: String,
        val statusCode: Int = 0,
        val server: String = "",
        val securityHeaders: Map<String, String> = emptyMap(),
        val securityScore: String = "0/7",
        val allHeaders: Map<String, String> = emptyMap(),
        val error: String? = null
    )
}
