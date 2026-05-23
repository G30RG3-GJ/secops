package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.MessageItem
import com.example.ui.theme.TerminalBackground
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.SecOpsPrimary
import com.example.ui.theme.SecOpsError
import com.example.ui.theme.SecOpsBackground
import com.example.ui.theme.SecOpsSurface
import com.example.ui.theme.SecOpsBorder

@Composable
fun SecOpsSecurityAssistant(
    chatHistory: List<MessageItem>,
    isLoading: Boolean,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rawInputText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SecOpsBackground)
            .border(1.dp, SecOpsBorder)
            .padding(12.dp)
    ) {
        // AI Header Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "AI Security Companion Agent Symbol",
                    tint = TerminalGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "GEMINI SEC-OPS COPILOT",
                        color = TerminalGreen,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "AI-Driven Vulnerability & Threat Mitigation Advisory",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }

            OutlinedButton(
                onClick = onClearChat,
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                modifier = Modifier.height(30.dp)
            ) {
                Text(
                    text = "PURGE",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp
                )
            }
        }

        HorizontalDivider(color = SecOpsBorder, thickness = 1.dp)

        Spacer(modifier = Modifier.height(8.dp))

        // Chats lists histories
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (chatHistory.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "SEC-OPS DIRECT ADVISORY ENCRYPTED FEED",
                            color = TerminalCyan,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Ask about Kali auditing command mechanics (Nmap configuration flags, Wireshark payload decrypt formats, or WPA handshake decryption mitigation).",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(chatHistory) { msg ->
                        val isUser = msg.sender == "USER"
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUser) SecOpsSurface else Color(0xFF101216)
                                ),
                                shape = if (isUser) RoundedCornerShape(8.dp, 8.dp, 0.dp, 8.dp) else RoundedCornerShape(8.dp, 8.dp, 8.dp, 0.dp),
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .border(
                                        width = 1.dp,
                                        color = if (isUser) TerminalCyan.copy(alpha = 0.3f) else TerminalGreen.copy(alpha = 0.3f),
                                        shape = if (isUser) RoundedCornerShape(8.dp, 8.dp, 0.dp, 8.dp) else RoundedCornerShape(8.dp, 8.dp, 8.dp, 0.dp)
                                    )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = msg.sender,
                                        color = if (isUser) TerminalCyan else TerminalGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = msg.content,
                                        color = Color.White,
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
        }

        // Processing / loading line indicator
        Row(
            modifier = Modifier.fillMaxWidth().height(16.dp).padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                LinearProgressIndicator(
                    color = TerminalCyan,
                    trackColor = Color(0xFF161F2C),
                    modifier = Modifier.fillMaxWidth().height(2.dp)
                )
            }
        }

        // Input send layout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF07090C), shape = RoundedCornerShape(4.dp))
                .border(1.dp, SecOpsBorder, shape = RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = rawInputText,
                onValueChange = { rawInputText = it },
                placeholder = {
                    Text(
                        "Input diagnostic trigger prompt...",
                        color = Color.DarkGray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            )

            IconButton(
                onClick = {
                    if (rawInputText.isNotBlank()) {
                        onSendMessage(rawInputText)
                        rawInputText = ""
                    }
                },
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Post target audit command",
                    tint = if (isLoading) Color.Gray else TerminalGreen,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
