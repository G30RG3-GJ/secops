package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AboutPanel(
    appVersion: String = "v3.0",
    updateAvailable: Boolean = false,
    latestVersion: String = "",
    onCheckUpdate: () -> Unit = {},
    onDownloadUpdate: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "about_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )
    val rotAngle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing)
        ), label = "rot"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF040A0F),
                        Color(0xFF020608),
                        Color(0xFF0A1628)
                    )
                )
            )
    ) {
        // Background matrix rain effect (static decorative elements)
        MatrixBackgroundDecor()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // ── Hacker emblem / logo ──
            Box(contentAlignment = Alignment.Center) {
                // Outer ring (rotating)
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .rotate(rotAngle)
                        .border(
                            width = 2.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color.Transparent,
                                    TerminalGreen,
                                    Color.Transparent,
                                    TerminalCyan
                                )
                            ),
                            shape = CircleShape
                        )
                )
                // Inner glow ring
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    TerminalGreen.copy(alpha = glowAlpha * 0.15f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
                // Center skull icon pulsing
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .scale(pulseScale)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF0A200A), Color(0xFF040A04))
                            ),
                            shape = CircleShape
                        )
                        .border(1.dp, TerminalGreen.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "SecOps Logo",
                        tint = TerminalGreen,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Creator text ──
            Text(
                text = "Created By",
                color = Color.Gray,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "H4cK3R",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                style = LocalTextStyle.current.copy(
                    brush = Brush.horizontalGradient(
                        listOf(TerminalGreen, TerminalCyan, TerminalGreen)
                    )
                ),
                letterSpacing = 4.sp
            )

            Spacer(Modifier.height(8.dp))

            // Version badge
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF0D1A0F),
                border = BorderStroke(1.dp, TerminalGreen.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "  SecOps Console $appVersion  ",
                    color = TerminalGreen,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            // ── Features Grid ──
            Text(
                text = "// CAPABILITIES //",
                color = TerminalCyan,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(16.dp))

            val features = listOf(
                Triple(Icons.Default.Wifi, "WiFi Audit", "Aircrack-ng • WPA handshake capture"),
                Triple(Icons.Default.NetworkCheck, "Network Scan", "Nmap • Wireshark • Host discovery"),
                Triple(Icons.Default.Videocam, "CCTV Crack", "RTSP • ONVIF • Default creds"),
                Triple(Icons.Default.Storage, "SQL Injection", "SQLMap • Built-in probe engine"),
                Triple(Icons.Default.BugReport, "Metasploit", "Exploit framework integration"),
                Triple(Icons.Default.SmartToy, "AI Copilot", "Gemini 2.0 Flash powered")
            )

            features.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { (icon, title, desc) ->
                        FeatureCard(
                            icon = { Icon(icon, null, tint = TerminalGreen, modifier = Modifier.size(22.dp)) },
                            title = title,
                            description = desc,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(24.dp))

            // ── Auto-Update section ──
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF080F1A)),
                border = BorderStroke(1.dp, Color(0xFF1E3A5F)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.SystemUpdate,
                            null,
                            tint = TerminalCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "AUTO UPDATE",
                            color = TerminalCyan,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Current: $appVersion",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    if (updateAvailable) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "⬆ New version available: $latestVersion",
                            color = Color(0xFFFFCC00),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = onDownloadUpdate,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TerminalGreen,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "DOWNLOAD & INSTALL $latestVersion",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onCheckUpdate,
                            border = BorderStroke(1.dp, TerminalCyan.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                null,
                                tint = TerminalCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "CHECK FOR UPDATES",
                                color = TerminalCyan,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Legal disclaimer ──
            Surface(
                color = Color(0xFF1A0A0A),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, SecOpsError.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            null,
                            tint = SecOpsError,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "LEGAL DISCLAIMER",
                            color = SecOpsError,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "This tool is for authorized security testing and educational purposes only. " +
                        "Unauthorized access to computer systems is illegal. " +
                        "The author assumes no liability for misuse.",
                        color = Color(0xFFAA7777),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                "[ H4cK3R © 2026 — All Rights Reserved ]",
                color = Color(0xFF2A4A2A),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FeatureCard(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF07110A)),
        border = BorderStroke(1.dp, TerminalGreen.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            icon()
            Spacer(Modifier.height(6.dp))
            Text(
                title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
            Text(
                description,
                color = Color.Gray,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun MatrixBackgroundDecor() {
    // Decorative blurred glowing circles for depth effect
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset((-40).dp, 80.dp)
                .size(200.dp)
                .blur(80.dp)
                .background(
                    TerminalGreen.copy(alpha = 0.04f),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(40.dp, 0.dp)
                .size(180.dp)
                .blur(70.dp)
                .background(
                    TerminalCyan.copy(alpha = 0.03f),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(300.dp)
                .blur(100.dp)
                .background(
                    Color(0xFF001A4D).copy(alpha = 0.4f),
                    CircleShape
                )
        )
    }
}
