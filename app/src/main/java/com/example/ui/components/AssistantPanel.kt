package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.MessageItem
import com.example.ui.theme.*

@Composable
fun SecOpsSecurityAssistant(
    chatHistory: List<MessageItem>,
    isLoading: Boolean,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rawInputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new message
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    // Header pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "ai_anim")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )

    // Quick prompts
    val quickPrompts = listOf(
        "🔍 Nmap scan strategy",
        "🛡️ WPA2 attack steps",
        "💉 SQLi payload list",
        "🔓 Root methods 2025",
        "📡 MITM attack guide",
        "🎯 CVE lookup tips"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SecOpsBackground)
    ) {
        // Background glow effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .blur(120.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            TerminalGreen.copy(alpha = 0.04f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Premium Header ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF051005), Color(0xFF030803))
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                TerminalGreen.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(0.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Gemini AI icon with glow
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .scale(glowScale)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            TerminalGreen.copy(alpha = 0.2f),
                                            Color.Transparent
                                        )
                                    ),
                                    CircleShape
                                )
                                .border(
                                    1.dp,
                                    TerminalGreen.copy(alpha = pulseAlpha),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = TerminalGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "GEMINI AI COPILOT",
                                color = TerminalGreen,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                letterSpacing = 2.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            TerminalGreen.copy(alpha = pulseAlpha),
                                            CircleShape
                                        )
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Gemini 2.0 Flash • Security Expert",
                                    color = TerminalGreen.copy(alpha = 0.6f),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    // Clear button
                    if (chatHistory.isNotEmpty()) {
                        OutlinedButton(
                            onClick = onClearChat,
                            border = BorderStroke(1.dp, SecOpsError.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                null,
                                tint = SecOpsError,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "CLEAR",
                                color = SecOpsError,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // ── Quick Prompt Chips ──
            if (chatHistory.isEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickPrompts) { prompt ->
                        Surface(
                            onClick = {
                                onSendMessage(prompt.substringAfter(" "))
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF0A1A0A),
                            border = BorderStroke(1.dp, TerminalGreen.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = prompt,
                                color = TerminalGreen.copy(alpha = 0.85f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // ── Chat Area ──
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (chatHistory.isEmpty()) {
                    // Empty state
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            TerminalGreen.copy(alpha = 0.1f),
                                            Color.Transparent
                                        )
                                    ),
                                    CircleShape
                                )
                                .border(1.dp, TerminalGreen.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Psychology,
                                null,
                                tint = TerminalGreen.copy(alpha = 0.6f),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "GEMINI SECURITY ASSISTANT",
                            color = TerminalGreen.copy(alpha = 0.8f),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 2.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Ask anything about cybersecurity,\npentesting, tools, or vulnerabilities.",
                            color = Color(0xFF4A7A4A),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))

                        // Feature hints
                        listOf(
                            "🔐 Aircrack-ng WPA2 attack walkthrough",
                            "🌐 Network reconnaissance techniques",
                            "💾 SQL injection bypass methods",
                            "📱 Android rooting with Magisk"
                        ).forEach { hint ->
                            Text(
                                text = "  > $hint",
                                color = Color(0xFF2A4A2A),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(chatHistory) { msg ->
                            val isUser = msg.sender == "USER"
                            AiChatBubble(
                                message = msg,
                                isUser = isUser
                            )
                        }

                        if (isLoading) {
                            item {
                                TypingIndicator()
                            }
                        }
                    }
                }
            }

            // ── Loading bar ──
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LinearProgressIndicator(
                    color = TerminalGreen,
                    trackColor = Color(0xFF051005),
                    modifier = Modifier.fillMaxWidth().height(2.dp)
                )
            }

            // ── Input Row ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF050F05))
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                TerminalGreen.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(0.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFF071207),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            TerminalGreen.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        ">> ",
                        color = TerminalGreen.copy(alpha = 0.6f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                    TextField(
                        value = rawInputText,
                        onValueChange = { rawInputText = it },
                        placeholder = {
                            Text(
                                "Ask about security, exploits, tools...",
                                color = Color(0xFF2A4A2A),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFD0F0C0),
                            cursorColor = TerminalGreen
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Send
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onSend = {
                                if (rawInputText.isNotBlank() && !isLoading) {
                                    onSendMessage(rawInputText.trim())
                                    rawInputText = ""
                                }
                            }
                        ),
                        maxLines = 4
                    )

                    // Send button
                    IconButton(
                        onClick = {
                            if (rawInputText.isNotBlank() && !isLoading) {
                                onSendMessage(rawInputText.trim())
                                rawInputText = ""
                            }
                        },
                        enabled = !isLoading && rawInputText.isNotBlank(),
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                if (!isLoading && rawInputText.isNotBlank())
                                    TerminalGreen.copy(alpha = 0.15f)
                                else Color.Transparent,
                                CircleShape
                            )
                            .border(
                                1.dp,
                                if (!isLoading && rawInputText.isNotBlank())
                                    TerminalGreen.copy(alpha = 0.5f)
                                else Color.Transparent,
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            null,
                            tint = if (!isLoading && rawInputText.isNotBlank())
                                TerminalGreen else Color(0xFF1A3A1A),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AiChatBubble(
    message: MessageItem,
    isUser: Boolean
) {
    val enterAnim = remember {
        slideInHorizontally(
            initialOffsetX = { if (isUser) it else -it }
        ) + fadeIn()
    }

    AnimatedVisibility(
        visible = true,
        enter = enterAnim
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            // AI avatar (left side)
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF0A200A), Color(0xFF050D05))
                            ),
                            CircleShape
                        )
                        .border(1.dp, TerminalGreen.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SmartToy,
                        null,
                        tint = TerminalGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
            }

            // Message bubble
            Column(
                modifier = Modifier.weight(1f, fill = false),
                horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
            ) {
                // Sender label
                Text(
                    text = if (isUser) "YOU" else "GEMINI AI",
                    color = if (isUser) TerminalCyan.copy(alpha = 0.7f) else TerminalGreen.copy(alpha = 0.7f),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp, end = 4.dp)
                )

                Surface(
                    shape = if (isUser)
                        RoundedCornerShape(12.dp, 12.dp, 2.dp, 12.dp)
                    else
                        RoundedCornerShape(12.dp, 12.dp, 12.dp, 2.dp),
                    color = if (isUser)
                        Color(0xFF051A25)
                    else
                        Color(0xFF051505),
                    border = BorderStroke(
                        1.dp,
                        if (isUser)
                            TerminalCyan.copy(alpha = 0.25f)
                        else
                            TerminalGreen.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.widthIn(max = 300.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Parse code blocks
                        val content = message.content
                        if (content.contains("```")) {
                            // Has code blocks — render specially
                            CodeFormattedText(content = content, isUser = isUser)
                        } else {
                            Text(
                                text = content,
                                color = if (isUser) Color(0xFFD0E8F0) else Color(0xFFD0F0D0),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            // User avatar (right side)
            if (isUser) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF001A25), Color(0xFF05080D))
                            ),
                            CircleShape
                        )
                        .border(1.dp, TerminalCyan.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        tint = TerminalCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeFormattedText(content: String, isUser: Boolean) {
    val parts = content.split("```")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        parts.forEachIndexed { index, part ->
            if (index % 2 == 0) {
                // Regular text
                if (part.isNotBlank()) {
                    Text(
                        text = part.trim(),
                        color = if (isUser) Color(0xFFD0E8F0) else Color(0xFFD0F0D0),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 17.sp
                    )
                }
            } else {
                // Code block
                val lines = part.split("\n")
                val lang = lines.firstOrNull()?.trim() ?: ""
                val code = if (lines.size > 1) lines.drop(1).joinToString("\n") else part

                Surface(
                    color = Color(0xFF010D01),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, TerminalGreen.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        if (lang.isNotBlank()) {
                            Text(
                                text = lang.uppercase(),
                                color = TerminalGreen.copy(alpha = 0.5f),
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                        Text(
                            text = code.trim(),
                            color = TerminalGreen,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val dot1 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(0)
        ), label = "d1"
    )
    val dot2 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(200)
        ), label = "d2"
    )
    val dot3 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(400)
        ), label = "d3"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFF0A200A), CircleShape)
                .border(1.dp, TerminalGreen.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.SmartToy, null, tint = TerminalGreen, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(8.dp))

        Surface(
            color = Color(0xFF051505),
            shape = RoundedCornerShape(12.dp, 12.dp, 12.dp, 2.dp),
            border = BorderStroke(1.dp, TerminalGreen.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(8.dp).background(TerminalGreen.copy(alpha = dot1), CircleShape))
                Box(Modifier.size(8.dp).background(TerminalGreen.copy(alpha = dot2), CircleShape))
                Box(Modifier.size(8.dp).background(TerminalGreen.copy(alpha = dot3), CircleShape))
            }
        }
    }
}
