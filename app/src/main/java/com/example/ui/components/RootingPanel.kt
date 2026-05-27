package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.DeviceSecurityStatus
import com.example.ui.theme.*

@Composable
fun RootingPanel(
    deviceStatus: DeviceSecurityStatus,
    outputLines: List<String>,
    isWorking: Boolean,
    onDownloadMagisk: () -> Unit,
    onCheckRoot: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedMethod by remember { mutableStateOf("magisk") }

    val infiniteTransition = rememberInfiniteTransition(label = "root_anim")
    val rotAngle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(8000, easing = LinearEasing)),
        label = "rot"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SecOpsBackground)
    ) {
        // ── Header with root status ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF180808), Color(0xFF0A0404))
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated root icon
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .rotate(rotAngle)
                            .border(
                                1.dp,
                                Brush.sweepGradient(
                                    listOf(
                                        Color.Transparent,
                                        SecOpsError,
                                        Color.Transparent
                                    )
                                ),
                                CircleShape
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .scale(pulseScale)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        SecOpsError.copy(alpha = glowAlpha * 0.2f),
                                        Color.Transparent
                                    )
                                ),
                                CircleShape
                            )
                            .border(1.dp, SecOpsError.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, null, tint = SecOpsError, modifier = Modifier.size(26.dp))
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "PHONE ROOTING CENTER",
                        color = SecOpsError,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        "Root without PC • Magisk • KernelSU • SuperUser",
                        color = Color.Gray,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Root status badge
            Spacer(Modifier.height(14.dp))
        }

        // Root Status Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (deviceStatus.rootGranted) Color(0xFF001A00) else Color(0xFF1A0000),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(
                1.dp,
                if (deviceStatus.rootGranted) TerminalGreen.copy(0.5f) else SecOpsError.copy(0.5f)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (deviceStatus.rootGranted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        null,
                        tint = if (deviceStatus.rootGranted) TerminalGreen else SecOpsError,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = when {
                                deviceStatus.rootGranted -> "ROOT GRANTED ✓"
                                deviceStatus.isRooted -> "SU BINARY FOUND (not granted)"
                                else -> "NOT ROOTED"
                            },
                            color = if (deviceStatus.rootGranted) TerminalGreen else SecOpsError,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = if (deviceStatus.rootGranted)
                                "Full root access available"
                            else if (deviceStatus.isRooted)
                                "Grant permission in SuperUser app"
                            else
                                "Device needs to be rooted first",
                            color = Color.Gray,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                OutlinedButton(
                    onClick = onCheckRoot,
                    border = BorderStroke(
                        1.dp,
                        if (deviceStatus.rootGranted) TerminalGreen.copy(0.5f) else SecOpsError.copy(0.5f)
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(30.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        "RE-CHECK",
                        color = if (deviceStatus.rootGranted) TerminalGreen else SecOpsError,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // ── Tabs ──
        val tabs = listOf("MAGISK", "KERNELSU", "RECOVERY", "ADB ROOT", "SUPERUSER")
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF0A0404),
            contentColor = SecOpsError,
            edgePadding = 0.dp
        ) {
            tabs.forEachIndexed { i, label ->
                Tab(
                    selected = selectedTab == i,
                    onClick = { selectedTab = i },
                    text = {
                        Text(
                            label,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (selectedTab == i) SecOpsError else Color.Gray
                        )
                    }
                )
            }
        }

        // Tab content
        LazyColumn(
            modifier = Modifier.weight(1f).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // Magisk
                    item {
                        RootMethodCard(
                            title = "MAGISK — Systemless Root",
                            subtitle = "Most popular root solution • Zygisk support • Modules",
                            icon = Icons.Default.Security,
                            color = SecOpsError
                        )
                    }
                    item {
                        Text("REQUIREMENTS:", color = SecOpsError, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    items(listOf(
                        "Unlocked bootloader",
                        "Custom recovery (TWRP) OR Magisk Patched boot.img",
                        "ADB tools (optional, for fastboot method)"
                    )) { req ->
                        Text("  • $req", color = Color(0xFFA06060), fontSize = 10.sp, fontFamily = FontFamily.Monospace, lineHeight = 14.sp)
                    }

                    item { Spacer(Modifier.height(4.dp)) }

                    item {
                        Button(
                            onClick = onDownloadMagisk,
                            enabled = !isWorking,
                            colors = ButtonDefaults.buttonColors(containerColor = SecOpsError),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp), tint = Color.Black)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (isWorking) "DOWNLOADING..." else "DOWNLOAD MAGISK (Latest)",
                                color = Color.Black,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    item {
                        StepByStepCard(
                            title = "TWRP Method (Recommended)",
                            steps = listOf(
                                "1. Backup all data — rooting may wipe device",
                                "2. Enable 'OEM Unlocking' in Developer Options",
                                "3. Boot into fastboot: Hold Power + Vol Down",
                                "4. Run: fastboot oem unlock (will factory reset!)",
                                "5. Download and flash TWRP for your device model",
                                "6. Boot into TWRP: Hold Power + Vol Up + Vol Down",
                                "7. In TWRP: Install > Select Magisk-v28.apk > Swipe to flash",
                                "8. Reboot System",
                                "9. Open Magisk app and grant root to apps"
                            ),
                            color = SecOpsError
                        )
                    }

                    item {
                        StepByStepCard(
                            title = "Patched Boot.img Method",
                            steps = listOf(
                                "1. Find stock firmware for your exact model + build number",
                                "2. Extract boot.img from firmware",
                                "3. Copy boot.img to phone",
                                "4. Open Magisk app > Install > Select boot.img",
                                "5. Magisk creates patched_boot.img",
                                "6. Transfer patched_boot.img to PC",
                                "7. Run: fastboot flash boot patched_boot.img",
                                "8. Fastboot reboot"
                            ),
                            color = Color(0xFFCC4444)
                        )
                    }
                }

                1 -> {
                    // KernelSU
                    item {
                        RootMethodCard(
                            title = "KERNELSU — Kernel-Level Root",
                            subtitle = "Next-gen root • More secure • GKI kernel required",
                            icon = Icons.Default.Memory,
                            color = SecOpsWarning
                        )
                    }
                    item {
                        StepByStepCard(
                            title = "KernelSU Installation",
                            steps = listOf(
                                "1. Device must have Android 12+ with GKI kernel",
                                "2. Check: adb shell uname -r (must be 5.10+ GKI)",
                                "3. Download KernelSU boot.img from kernelsu.org",
                                "4. Find version matching your device + kernel version",
                                "5. fastboot flash boot kernelsu_boot.img",
                                "6. Reboot and install KernelSU Manager app",
                                "7. KernelSU works differently — no su binary",
                                "8. Apps request root through KSU Manager"
                            ),
                            color = SecOpsWarning
                        )
                    }
                    item {
                        InfoCard(
                            "ADVANTAGES OVER MAGISK",
                            listOf(
                                "✓ Harder to detect by apps (banking apps)",
                                "✓ Root at kernel level — more stable",
                                "✓ Module system compatible with Magisk",
                                "✓ Better performance on GKI devices",
                                "✗ Only works on GKI kernel (Android 12+)"
                            ),
                            SecOpsWarning
                        )
                    }
                }

                2 -> {
                    // TWRP Recovery
                    item {
                        RootMethodCard(
                            title = "TWRP CUSTOM RECOVERY",
                            subtitle = "Flash custom recovery without PC using DriveDroid/Nethunter",
                            icon = Icons.Default.PhoneAndroid,
                            color = TerminalCyan
                        )
                    }
                    item {
                        StepByStepCard(
                            title = "Flash TWRP Recovery",
                            steps = listOf(
                                "1. Find TWRP for your device at twrp.me",
                                "2. Download twrp-device-version.img file",
                                "3. Enable Developer Options + USB Debugging",
                                "4. Connect to PC with ADB",
                                "5. adb reboot bootloader",
                                "6. fastboot flash recovery twrp.img",
                                "7. fastboot reboot recovery",
                                "8. In TWRP: swipe to allow modifications",
                                "9. Now flash Magisk or any .zip mod"
                            ),
                            color = TerminalCyan
                        )
                    }
                    item {
                        InfoCard(
                            "DEVICES WITHOUT OFFICIAL TWRP",
                            listOf(
                                "• Try OrangeFox Recovery (orangefox.download)",
                                "• Try PitchBlack Recovery (pitchblackrecovery.com)",
                                "• Try unofficial TWRP builds on XDA Developers",
                                "• Search: [Your model] + TWRP on XDA Forums"
                            ),
                            TerminalCyan
                        )
                    }
                }

                3 -> {
                    // ADB Root
                    item {
                        RootMethodCard(
                            title = "ADB ROOT (Engineering Build)",
                            subtitle = "For development builds, emulators, and some Chinese phones",
                            icon = Icons.Default.DeveloperMode,
                            color = TerminalGreen
                        )
                    }
                    item {
                        StepByStepCard(
                            title = "ADB Root Method",
                            steps = listOf(
                                "1. Enable Developer Options (tap Build Number 7 times)",
                                "2. Enable USB Debugging in Developer Options",
                                "3. Connect to PC: adb devices",
                                "4. Try: adb root",
                                "5. If success: adb shell (you should see #)",
                                "6. Install apps that need root via adb install app.apk",
                                "7. NOTE: Only works on engineering builds and emulators"
                            ),
                            color = TerminalGreen
                        )
                    }
                    item {
                        InfoCard(
                            "ADB SHELL COMMANDS (after rooting)",
                            listOf(
                                "adb shell su -c 'id'          # verify root",
                                "adb shell su -c 'mount'       # show mounts",
                                "adb shell pm list packages    # list apps",
                                "adb shell settings put global always_finish_activities 1",
                                "adb shell am force-stop com.package.name"
                            ),
                            TerminalGreen
                        )
                    }
                }

                4 -> {
                    // SuperUser management
                    item {
                        RootMethodCard(
                            title = "ROOT MANAGEMENT",
                            subtitle = "Manage root permissions and SuperUser apps",
                            icon = Icons.Default.ManageAccounts,
                            color = TerminalPurple
                        )
                    }
                    item {
                        InfoCard(
                            "POPULAR ROOT MANAGER APPS",
                            listOf(
                                "📦 Magisk Manager (topjohnwu/Magisk) — most popular",
                                "📦 KernelSU Manager — for KSU users",
                                "📦 APatch — patch-based root (alternative)",
                                "📦 SuperSU (legacy) — old but stable",
                                "📦 LADB (local ADB) — wireless ADB on device"
                            ),
                            TerminalPurple
                        )
                    }
                    item {
                        InfoCard(
                            "ROOT DETECTION BYPASS",
                            listOf(
                                "• Magisk Hide → renamed to DenyList in Magisk 24+",
                                "• Enable Zygisk: Magisk Settings > Zygisk ON",
                                "• Add banking/payment apps to DenyList",
                                "• Shamiko module: additional hide support",
                                "• MagiskHide Props Config: change device fingerprint"
                            ),
                            Color(0xFF9D4EDD)
                        )
                    }
                    item {
                        InfoCard(
                            "⚠️ SAFETY WARNING",
                            listOf(
                                "• Rooting voids warranty on most devices",
                                "• Root grants ANY app full access — be careful what you install",
                                "• Banking apps may refuse to work on rooted devices",
                                "• Always backup before attempting root",
                                "• Some devices have permanent eFuse burn if bootloader is unlocked"
                            ),
                            SecOpsError
                        )
                    }
                }
            }
        }

        // Terminal output
        HorizontalDivider(color = SecOpsError.copy(alpha = 0.3f))
        RootTerminalOutput(
            lines = outputLines,
            isRunning = isWorking,
            onStop = onStop,
            modifier = Modifier.height(160.dp)
        )
    }
}

@Composable
private fun RootMethodCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, color = color, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(subtitle, color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace, lineHeight = 13.sp)
            }
        }
    }
}

@Composable
private fun StepByStepCard(
    title: String,
    steps: List<String>,
    color: Color
) {
    Surface(
        color = Color(0xFF080808),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = color, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            steps.forEach { step ->
                Text(step, color = Color(0xFFB0B0B0), fontSize = 10.sp, fontFamily = FontFamily.Monospace, lineHeight = 15.sp)
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, items: List<String>, color: Color) {
    Surface(
        color = color.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = color, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(6.dp))
            items.forEach { item ->
                Text(item, color = Color(0xFF909090), fontSize = 10.sp, fontFamily = FontFamily.Monospace, lineHeight = 14.sp)
            }
        }
    }
}

@Composable
private fun RootTerminalOutput(
    lines: List<String>,
    isRunning: Boolean,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) listState.animateScrollToItem(lines.size - 1)
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF080404)).padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("ROOT TERMINAL", color = SecOpsError.copy(0.7f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            if (isRunning) {
                TextButton(onClick = onStop, contentPadding = PaddingValues(horizontal = 4.dp)) {
                    Text("■ STOP", color = SecOpsError, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().background(Color(0xFF050202)).padding(8.dp)
        ) {
            items(lines) { line ->
                val color = when {
                    line.startsWith("[SUCCESS]") || line.startsWith("[+]") -> TerminalGreen
                    line.startsWith("[ERROR]") || line.startsWith("[-]") -> SecOpsError
                    line.startsWith("[!]") || line.startsWith("[WARN]") -> SecOpsWarning
                    line.startsWith("[*]") || line.startsWith("[INFO]") -> SecOpsError.copy(alpha = 0.7f)
                    else -> Color(0xFFA08080)
                }
                Text(text = line, color = color, fontSize = 10.sp, fontFamily = FontFamily.Monospace, lineHeight = 14.sp)
            }
        }
    }
}
