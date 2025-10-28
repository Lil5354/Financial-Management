package com.example.expensetracker.ui.compose.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensetracker.R
import com.example.expensetracker.data.entity.ChatMessageEntity
import com.example.expensetracker.ui.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

// Helper function to parse markdown-style bold text
fun parseMarkdownText(text: String, baseColor: Color): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0
        val boldPattern = Regex("""\*\*(.+?)\*\*""")
        
        boldPattern.findAll(text).forEach { matchResult ->
            // Add text before the bold part with color
            if (matchResult.range.first > currentIndex) {
                withStyle(style = SpanStyle(color = baseColor)) {
                    append(text.substring(currentIndex, matchResult.range.first))
                }
            }
            
            // Add bold text with color
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = baseColor)) {
                append(matchResult.groupValues[1])
            }
            
            currentIndex = matchResult.range.last + 1
        }
        
        // Add remaining text with color
        if (currentIndex < text.length) {
            withStyle(style = SpanStyle(color = baseColor)) {
                append(text.substring(currentIndex))
            }
        }
    }
}

@Composable
fun ChatScreen(
    isDarkTheme: Boolean = true,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8FAFC)
    val cardColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1E293B)
    val mutedTextColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
    val accentColor = Color(0xFF06B6D4)
    val successColor = Color(0xFF10B981)
    val errorColor = Color(0xFFEF4444)
    
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentInput by viewModel.currentInput.collectAsState()
    
    // Voice recognition states
    val isListening by viewModel.voiceRecognitionManager.isListening.collectAsState()
    val recognizedText by viewModel.voiceRecognitionManager.recognizedText.collectAsState()
    val voiceError by viewModel.voiceRecognitionManager.error.collectAsState()
    
    val listState = rememberLazyListState()
    
    // Auto scroll to bottom when new messages arrive
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }
    
    // Handle recognized text
    LaunchedEffect(recognizedText) {
        if (recognizedText.isNotBlank()) {
            viewModel.processVoiceCommand(recognizedText)
            viewModel.voiceRecognitionManager.clearText()
        }
    }
    
    // Handle voice error
    LaunchedEffect(voiceError) {
        voiceError?.let {
            // Show error briefly
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Chat với AI",
                style = MaterialTheme.typography.headlineSmall,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
            
            Row {
                IconButton(
                    onClick = { viewModel.clearChatHistory() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa lịch sử",
                        tint = mutedTextColor
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Error Message
        errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = errorColor.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Lỗi",
                        tint = errorColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        color = errorColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { viewModel.clearError() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = errorColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Chat Messages
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (chatMessages.isEmpty()) {
                item {
                    WelcomeMessage(cardColor, textColor, mutedTextColor)
                }
            }
            
            items(chatMessages) { message ->
                ChatMessageItem(
                    message = message,
                    cardColor = cardColor,
                    textColor = textColor,
                    mutedTextColor = mutedTextColor,
                    accentColor = accentColor,
                    onDeleteMessage = { viewModel.deleteMessage(message.id) }
                )
            }
            
            // Loading indicator
            if (isLoading) {
                item {
                    TypingIndicator(cardColor, accentColor)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Input Area
        ChatInputArea(
            currentInput = currentInput,
            onInputChange = viewModel::updateInput,
            onSendMessage = viewModel::sendMessage,
            isLoading = isLoading,
            cardColor = cardColor,
            textColor = textColor,
            accentColor = accentColor,
            successColor = successColor
        )
    }
}

@Composable
fun WelcomeMessage(
    cardColor: Color,
    textColor: Color,
    mutedTextColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_app_rounded),
                contentDescription = "NoNo Assistant",
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Xin chào! Tôi là NoNo Assistant",
                style = MaterialTheme.typography.headlineSmall,
                color = textColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tôi có thể giúp bạn:\n• Hướng dẫn sử dụng ứng dụng NoNo\n• Phân tích chi tiêu và đưa ra insights\n• Lập kế hoạch ngân sách thông minh\n• Mẹo tiết kiệm tiền hiệu quả\n• Hiểu về các báo cáo tài chính\n• Sử dụng tính năng OCR scan hóa đơn\n\nHãy hỏi tôi bất cứ điều gì về tài chính cá nhân! 💰",
                style = MaterialTheme.typography.bodyMedium,
                color = mutedTextColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessageEntity,
    cardColor: Color,
    textColor: Color,
    mutedTextColor: Color,
    accentColor: Color,
    onDeleteMessage: () -> Unit
) {
    val isUser = message.isUser
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // AI Avatar - NoNo Logo
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_app_rounded),
                    contentDescription = "NoNo AI",
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) accentColor else cardColor
                ),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                )
            ) {
                Text(
                    text = parseMarkdownText(message.content, if (isUser) Color.White else textColor),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeFormat.format(message.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedTextColor,
                    fontSize = 10.sp
                )
                
                if (isUser) {
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDeleteMessage,
                        modifier = Modifier.size(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa",
                            tint = mutedTextColor,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
        
        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // User Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = Color(0xFF10B981),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun TypingIndicator(
    cardColor: Color,
    accentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // AI Avatar - NoNo Logo
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_app_rounded),
                contentDescription = "NoNo AI",
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI đang trả lời",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF06B6D4)
                )
                Spacer(modifier = Modifier.width(8.dp))
                TypingDots()
            }
        }
    }
}

@Composable
fun TypingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val alpha3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )
    
    Row {
        Text(
            text = "•",
            color = Color(0xFF06B6D4),
            modifier = Modifier.alpha(alpha1)
        )
        Text(
            text = "•",
            color = Color(0xFF06B6D4),
            modifier = Modifier.alpha(alpha2)
        )
        Text(
            text = "•",
            color = Color(0xFF06B6D4),
            modifier = Modifier.alpha(alpha3)
        )
    }
}

@Composable
fun ChatInputArea(
    currentInput: String,
    onInputChange: (String) -> Unit,
    onSendMessage: (String?) -> Unit,
    isLoading: Boolean,
    cardColor: Color,
    textColor: Color,
    accentColor: Color,
    successColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = currentInput,
                onValueChange = onInputChange,
                placeholder = {
                    Text(
                        text = "Nhập tin nhắn...",
                        color = Color(0xFF94A3B8)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f),
                enabled = !isLoading,
                singleLine = false,
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Send Button
            IconButton(
                onClick = { onSendMessage(null) },
                enabled = currentInput.isNotBlank() && !isLoading,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (currentInput.isNotBlank() && !isLoading) successColor else Color(0xFF64748B),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Gửi",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
