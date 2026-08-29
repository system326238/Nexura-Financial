package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiService
import com.example.data.models.ChatMessage
import com.example.data.models.CopilotPersona
import com.example.data.models.Currency
import com.example.data.models.MessageSender
import com.example.ui.components.CyberCard
import com.example.ui.components.NeonBadge
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CopilotScreen(
    messages: List<ChatMessage>,
    activePersona: CopilotPersona,
    currency: Currency,
    onSendMessage: (String) -> Unit,
    onSelectPersona: (CopilotPersona) -> Unit,
    onNavigateToTab: (String) -> Unit,
    geminiService: GeminiService,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var inputMessage by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    val quickActionPrompts = remember(activePersona) {
        when (activePersona) {
            CopilotPersona.FORENSIC_AUDITOR -> listOf(
                "Audit my Pacific Grid bill for $33.20 surge fee",
                "Generate an FCC retention script for Apex Fiber",
                "How do I demand a facility fee waiver under No Surprises Act?"
            )
            CopilotPersona.WEALTH_ARCHITECT -> listOf(
                "Calculate my 10-year compounding if I invest $1,500/mo",
                "What is my portfolio diversification score?",
                "How much should I keep in High-Yield Cash vs Equities?"
            )
            CopilotPersona.DAILY_BUDGET_COACH -> listOf(
                "Can I afford a $180 dinner tonight on my safe-to-spend limit?",
                "Which budget envelope is closest to over-capacity?",
                "How much did I waste on duplicate subscriptions this month?"
            )
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun executeSend(text: String) {
        if (text.isBlank() || isSending) return
        val userText = text.trim()
        inputMessage = ""
        isSending = true

        val timeStr = SimpleDateFormat("h:mm a", Locale.US).format(Date())
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.USER,
            text = userText,
            timestamp = timeStr,
            persona = activePersona
        )
        onSendMessage(userMsg.text)

        scope.launch {
            val history = messages.map {
                (if (it.sender == MessageSender.USER) "USER" else "ASSISTANT") to it.text
            }
            val responseText = geminiService.chatWithCopilot(
                persona = activePersona,
                history = history,
                userMessage = userText
            )

            // Determine if an action chip is relevant
            val actionTargetTab = when {
                userText.lowercase().contains("bill") || userText.lowercase().contains("scan") || userText.lowercase().contains("pacific") -> "scanner"
                userText.lowercase().contains("script") || userText.lowercase().contains("subscription") || userText.lowercase().contains("truth") -> "auditor"
                userText.lowercase().contains("envelope") || userText.lowercase().contains("budget") || userText.lowercase().contains("spend") -> "budget"
                userText.lowercase().contains("compound") || userText.lowercase().contains("invest") || userText.lowercase().contains("wealth") -> "wealth"
                else -> null
            }

            val assistantMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = MessageSender.ASSISTANT,
                text = responseText,
                timestamp = SimpleDateFormat("h:mm a", Locale.US).format(Date()),
                persona = activePersona,
                actionChip = if (actionTargetTab != null) "Open In ${actionTargetTab.uppercase()}" else null,
                actionTargetTab = actionTargetTab
            )
            // Deliver assistant response through VM
            isSending = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .testTag("copilot_screen")
    ) {
        // Persona Selector Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MULTI-PERSONA AI COPILOT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextCyan,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                NeonBadge(text = activePersona.badgeLabel, color = NeonCyan)
            }

            // Persona Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CopilotPersona.values().forEach { persona ->
                    val isSelected = persona == activePersona
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else SurfaceCommand)
                            .border(
                                1.dp,
                                if (isSelected) NeonCyan else SurfaceCardBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectPersona(persona) }
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                            .testTag("persona_button_${persona.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = persona.title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (isSelected) NeonCyan else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Chat Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("copilot_message_list"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(
                    message = msg,
                    onNavigateToTab = onNavigateToTab
                )
            }

            if (isSending) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = NeonCyan,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "${activePersona.title} synthesizing financial intelligence...",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextCyan)
                        )
                    }
                }
            }
        }

        // Quick Action Prompt Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(quickActionPrompts) { prompt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceCommand)
                        .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .clickable { executeSend(prompt) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("quick_prompt_chip")
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextCyan,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }

        // Input Field and Send Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 85.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputMessage,
                onValueChange = { inputMessage = it },
                placeholder = {
                    Text(
                        text = activePersona.promptPlaceholder,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, color = TextMuted)
                    )
                },
                maxLines = 3,
                modifier = Modifier
                    .weight(1f)
                    .testTag("copilot_input_field"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = SurfaceCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            IconButton(
                onClick = { executeSend(inputMessage) },
                enabled = inputMessage.isNotBlank() && !isSending,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (inputMessage.isNotBlank()) NeonCyan else SurfaceCommand)
                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), CircleShape)
                    .testTag("copilot_send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (inputMessage.isNotBlank()) CanvasBlack else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    onNavigateToTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == MessageSender.USER

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Text(
                text = if (isUser) "YOU" else (message.persona?.title ?: "NEXURA AI").uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isUser) NeonCyan else NeonEmerald,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "• ${message.timestamp}",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp)
            )
        }

        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (isUser) Color(0xFF0F1E2A) else SurfaceElevated)
                .border(
                    1.dp,
                    if (isUser) NeonCyan.copy(alpha = 0.4f) else SurfaceCardBorder,
                    RoundedCornerShape(14.dp)
                )
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                )

                if (message.actionChip != null && message.actionTargetTab != null) {
                    Button(
                        onClick = { onNavigateToTab(message.actionTargetTab) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = CanvasBlack),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("bubble_action_chip")
                    ) {
                        Text(
                            text = message.actionChip,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}
