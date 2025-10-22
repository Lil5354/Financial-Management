package com.example.expensetracker.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

// Data class cho AI Insights
data class AIInsight(
    val id: String,
    val title: String,
    val description: String,
    val type: InsightType,
    val icon: ImageVector,
    val color: Color,
    val confidence: Float = 0.0f
)

enum class InsightType {
    SPENDING_PATTERN,
    BUDGET_ALERT,
    FORECAST,
    CATEGORY_ANALYSIS,
    SAVING_TIP
}

// Data class cho AI Chat Message
data class ChatMessage(
    val id: String,
    val content: String,
    val isUser: Boolean,
    val timestamp: Date = Date()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIScreen(
    onNavigateBack: () -> Unit,
    isDarkTheme: Boolean = true
) {
    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8FAFC)
    val cardColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1E293B)
    val mutedTextColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
    val accentColor = Color(0xFF06B6D4) // Cyan accent
    val successColor = Color(0xFF10B981) // Green
    val warningColor = Color(0xFFF59E0B) // Amber
    val errorColor = Color(0xFFEF4444) // Red
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Trợ lý AI", "Scan", "Dự đoán", "Chat")
    
    // AI Insights - Tối ưu hóa text
    val aiInsights = remember {
        listOf(
            AIInsight(
                "1", "Chi tiêu ăn uống tăng mạnh", 
                "Tháng này bạn chi cho ăn uống nhiều hơn 25% so với tháng trước. Hãy cân nhắc nấu ăn tại nhà để tiết kiệm.",
                InsightType.SPENDING_PATTERN, Icons.Default.Restaurant, Color(0xFFF59E0B), 0.85f
            ),
            AIInsight(
                "2", "Ngân sách sắp vượt mức", 
                "Bạn đã sử dụng 85% ngân sách tháng này. Còn 5 ngày nữa, hãy kiểm soát chi tiêu cẩn thận.",
                InsightType.BUDGET_ALERT, Icons.Default.Warning, Color(0xFFEF4444), 0.92f
            ),
            AIInsight(
                "3", "Dự đoán chi tiêu tháng tới", 
                "Dựa trên xu hướng hiện tại, tháng tới bạn có thể chi khoảng 3,200,000đ. Hãy lập kế hoạch chi tiêu phù hợp.",
                InsightType.FORECAST, Icons.Default.TrendingUp, Color(0xFF06B6D4), 0.78f
            ),
            AIInsight(
                "4", "Chi phí giao thông cao", 
                "Giao thông chiếm 30% tổng chi tiêu. Cân nhắc đi xe bus, xe máy điện hoặc đi bộ để tiết kiệm.",
                InsightType.CATEGORY_ANALYSIS, Icons.Default.DirectionsCar, Color(0xFF8B5CF6), 0.88f
            ),
            AIInsight(
                "5", "Gợi ý tiết kiệm thông minh", 
                "Bạn có thể tiết kiệm 200,000đ/tháng bằng cách nấu ăn tại nhà 3 bữa/tuần thay vì ăn ngoài.",
                InsightType.SAVING_TIP, Icons.Default.Lightbulb, Color(0xFF10B981), 0.75f
            )
        )
    }
    
    // Sample Chat Messages
    val chatMessages = remember {
        mutableStateListOf(
            ChatMessage("1", "Xin chào! Tôi là AI trợ lý tài chính của bạn. Tôi có thể giúp gì cho bạn?", false),
            ChatMessage("2", "Tôi muốn biết chi tiêu tháng này của tôi như thế nào?", true),
            ChatMessage("3", "Tháng này bạn đã chi 3,380,000đ, tăng 8.2% so với tháng trước. Chi tiêu chính là ăn uống (35.5%) và giao thông (23.6%).", false)
        )
    }
    
    var chatInput by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Header with Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.1f),
                            backgroundColor
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = cardColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = textColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "AI Trợ lý thông minh",
                            style = MaterialTheme.typography.headlineSmall,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Quản lý tài chính với công nghệ AI",
                            style = MaterialTheme.typography.bodyMedium,
                            color = mutedTextColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Modern Tab Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = cardColor,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = index },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) accentColor else Color.Transparent
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (isSelected) 2.dp else 0.dp
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (index) {
                                        0 -> Icons.Default.Psychology
                                        1 -> Icons.Default.CameraAlt
                                        2 -> Icons.Default.TrendingUp
                                        3 -> Icons.Default.Chat
                                        else -> Icons.Default.Circle
                                    },
                                    contentDescription = title,
                                    tint = if (isSelected) Color.White else mutedTextColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) Color.White else mutedTextColor,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Content based on selected tab
        when (selectedTab) {
            0 -> AIAssistantContent(aiInsights, cardColor, textColor, mutedTextColor, accentColor, successColor, warningColor, errorColor)
            1 -> OCRContent(cardColor, textColor, mutedTextColor, accentColor)
            2 -> ForecastingContent(cardColor, textColor, mutedTextColor, accentColor, successColor, warningColor)
            3 -> ChatScreen(isDarkTheme)
        }
    }
}

// AI Assistant Content - Modern Design
@Composable
fun AIAssistantContent(
    insights: List<AIInsight>,
    cardColor: Color,
    textColor: Color,
    mutedTextColor: Color,
    accentColor: Color,
    successColor: Color,
    warningColor: Color,
    errorColor: Color
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcome Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = accentColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "AI Assistant",
                            tint = accentColor,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Chào mừng đến với AI Trợ lý!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Tôi sẽ giúp bạn phân tích chi tiêu, dự đoán xu hướng và đưa ra lời khuyên tài chính thông minh. Hãy thử các tính năng bên dưới!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = mutedTextColor,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        
        // Quick Actions
        item {
            Text(
                text = "Thao tác nhanh",
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    QuickActionCard(
                        title = "Thêm chi tiêu",
                        subtitle = "Bằng giọng nói",
                        icon = Icons.Default.Mic,
                        color = successColor,
                        cardColor = cardColor,
                        textColor = textColor,
                        onClick = { /* TODO: Voice input */ }
                    )
                }
                
                Box(modifier = Modifier.weight(1f)) {
                    QuickActionCard(
                        title = "Scan hóa đơn",
                        subtitle = "Tự động nhận diện",
                        icon = Icons.Default.CameraAlt,
                        color = accentColor,
                        cardColor = cardColor,
                        textColor = textColor,
                        onClick = { /* TODO: OCR */ }
                    )
                }
            }
        }
        
        // AI Insights
        item {
            Text(
                text = "Phân tích thông minh",
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(insights) { insight ->
            ModernAIInsightCard(
                insight = insight,
                cardColor = cardColor,
                textColor = textColor,
                mutedTextColor = mutedTextColor,
                accentColor = accentColor,
                successColor = successColor,
                warningColor = warningColor,
                errorColor = errorColor
            )
        }
    }
}

// OCR Content - Modern Design
@Composable
fun OCRContent(
    cardColor: Color,
    textColor: Color,
    mutedTextColor: Color,
    accentColor: Color
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Scan hóa đơn thông minh",
                style = MaterialTheme.typography.titleLarge,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(64.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Chụp ảnh hóa đơn",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "AI sẽ tự động nhận diện số tiền, ngày tháng và mô tả từ hóa đơn của bạn",
                        style = MaterialTheme.typography.bodyMedium,
                        color = mutedTextColor,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Button(
                        onClick = { /* TODO: Open camera */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Chụp ảnh",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chụp ảnh hóa đơn")
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Kết quả scan gần đây",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Sample OCR results
                    OCRResultItem(
                        title = "Cà phê Starbucks",
                        amount = "85,000đ",
                        date = "Hôm nay",
                        confidence = 0.95f,
                        textColor = textColor,
                        mutedTextColor = mutedTextColor
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OCRResultItem(
                        title = "Xăng xe",
                        amount = "150,000đ",
                        date = "Hôm qua",
                        confidence = 0.88f,
                        textColor = textColor,
                        mutedTextColor = mutedTextColor
                    )
                }
            }
        }
    }
}

// Forecasting Content - Modern Design
@Composable
fun ForecastingContent(
    cardColor: Color,
    textColor: Color,
    mutedTextColor: Color,
    accentColor: Color,
    successColor: Color,
    warningColor: Color
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Dự đoán chi tiêu thông minh",
                style = MaterialTheme.typography.titleLarge,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Dự đoán tháng tới",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "3,200,000đ",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Dự đoán chi tiêu",
                                style = MaterialTheme.typography.bodySmall,
                                color = mutedTextColor
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+5.2%",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "So với tháng này",
                                style = MaterialTheme.typography.bodySmall,
                                color = mutedTextColor
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Độ tin cậy: 78%",
                        style = MaterialTheme.typography.bodySmall,
                        color = mutedTextColor
                    )
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Xu hướng theo danh mục",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ForecastCategoryItem(
                        category = "Ăn uống",
                        current = "1,200,000đ",
                        predicted = "1,350,000đ",
                        change = "+12.5%",
                        textColor = textColor,
                        mutedTextColor = mutedTextColor
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ForecastCategoryItem(
                        category = "Giao thông",
                        current = "800,000đ",
                        predicted = "850,000đ",
                        change = "+6.2%",
                        textColor = textColor,
                        mutedTextColor = mutedTextColor
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ForecastCategoryItem(
                        category = "Mua sắm",
                        current = "600,000đ",
                        predicted = "580,000đ",
                        change = "-3.3%",
                        textColor = textColor,
                        mutedTextColor = mutedTextColor
                    )
                }
            }
        }
    }
}

// Chat AI Content - Modern Design
@Composable
fun ChatAIContent(
    messages: MutableList<ChatMessage>,
    chatInput: String,
    onChatInputChange: (String) -> Unit,
    isListening: Boolean,
    onListeningChange: (Boolean) -> Unit,
    cardColor: Color,
    textColor: Color,
    mutedTextColor: Color,
    accentColor: Color
) {
    var showVoiceCommands by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Voice Commands Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .clickable { showVoiceCommands = true },
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice",
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Lệnh thoại nhanh",
                    style = MaterialTheme.typography.titleSmall,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Đóng",
                    tint = mutedTextColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        // Voice Commands Popup
        if (showVoiceCommands) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lệnh thoại nhanh",
                            style = MaterialTheme.typography.titleMedium,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { showVoiceCommands = false }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Đóng",
                                tint = mutedTextColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "• 'Thêm chi tiêu 50k cà phê'\n• 'Thêm thu nhập 2 triệu lương'\n• 'Xem chi tiêu tháng này'\n• 'Báo cáo tài chính'",
                        style = MaterialTheme.typography.bodyMedium,
                        color = mutedTextColor
                    )
                }
            }
        }
        
        // Chat Messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message ->
                ChatMessageBubble(
                    message = message,
                    cardColor = cardColor,
                    textColor = textColor,
                    mutedTextColor = mutedTextColor
                )
            }
        }
        
                // Chat Input
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = chatInput,
                    onValueChange = onChatInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Nhập câu hỏi hoặc nói 'Thêm chi tiêu 50k cà phê'...", color = mutedTextColor) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = mutedTextColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Voice Input Button
                IconButton(
                    onClick = { 
                        onListeningChange(!isListening)
                        if (!isListening) {
                            // TODO: Start voice recognition
                            onChatInputChange("Đang nghe... Hãy nói 'Thêm chi tiêu 50k cà phê' hoặc 'Thêm thu nhập 2 triệu lương'")
                        } else {
                            // TODO: Stop voice recognition
                            onChatInputChange("")
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (isListening) Color(0xFFEF4444) else Color(0xFF3B82F6),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                        contentDescription = if (isListening) "Dừng nghe" else "Bắt đầu nghe",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Send Button
                IconButton(
                    onClick = { 
                        if (chatInput.isNotBlank()) {
                            // TODO: Send message and process voice commands
                            val newMessage = ChatMessage(
                                id = (messages.size + 1).toString(),
                                content = chatInput,
                                isUser = true
                            )
                            messages.add(newMessage)
                            
                            // Process voice commands
                            if (chatInput.contains("thêm chi tiêu", ignoreCase = true) || 
                                chatInput.contains("thêm thu nhập", ignoreCase = true)) {
                                val aiResponse = ChatMessage(
                                    id = (messages.size + 1).toString(),
                                    content = "Tôi đã hiểu! Đang thêm giao dịch: $chatInput. Bạn có muốn xác nhận không?",
                                    isUser = false
                                )
                                messages.add(aiResponse)
                            }
                            
                            onChatInputChange("")
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color(0xFF10B981),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
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

// Quick Action Card Component
@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    cardColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = color.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF64748B),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// Modern AI Insight Card Component
@Composable
fun ModernAIInsightCard(
    insight: AIInsight,
    cardColor: Color,
    textColor: Color,
    mutedTextColor: Color,
    accentColor: Color,
    successColor: Color,
    warningColor: Color,
    errorColor: Color
) {
    val cardColorWithAlpha = when (insight.type) {
        InsightType.SPENDING_PATTERN -> warningColor.copy(alpha = 0.05f)
        InsightType.BUDGET_ALERT -> errorColor.copy(alpha = 0.05f)
        InsightType.FORECAST -> accentColor.copy(alpha = 0.05f)
        InsightType.CATEGORY_ANALYSIS -> successColor.copy(alpha = 0.05f)
        InsightType.SAVING_TIP -> successColor.copy(alpha = 0.05f)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardColorWithAlpha)
                .padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = insight.color.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = insight.icon,
                    contentDescription = insight.title,
                    tint = insight.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = insight.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = mutedTextColor,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Confidence Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Độ tin cậy:",
                        style = MaterialTheme.typography.bodySmall,
                        color = mutedTextColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .background(
                                color = mutedTextColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(3.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(insight.confidence)
                                .background(
                                    color = insight.color,
                                    shape = RoundedCornerShape(3.dp)
                                )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${(insight.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = insight.color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// AI Insight Card Component (Legacy)
@Composable
fun AIInsightCard(
    insight: AIInsight,
    cardColor: Color,
    textColor: Color,
    mutedTextColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = insight.color.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = insight.icon,
                    contentDescription = insight.title,
                    tint = insight.color,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = insight.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = mutedTextColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Độ tin cậy: ${(insight.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = insight.color
                )
            }
        }
    }
}

// OCR Result Item Component
@Composable
fun OCRResultItem(
    title: String,
    amount: String,
    date: String,
    confidence: Float,
    textColor: Color,
    mutedTextColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = mutedTextColor
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF10B981),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${(confidence * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = mutedTextColor
            )
        }
    }
}

// Forecast Category Item Component
@Composable
fun ForecastCategoryItem(
    category: String,
    current: String,
    predicted: String,
    change: String,
    textColor: Color,
    mutedTextColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = current,
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedTextColor
                )
                Text(
                    text = "Hiện tại",
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedTextColor
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = predicted,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Dự đoán",
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedTextColor
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = change,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (change.startsWith("+")) Color(0xFFEF4444) else Color(0xFF10B981),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Thay đổi",
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedTextColor
                )
            }
        }
    }
}

// Chat Message Bubble Component
@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    cardColor: Color,
    textColor: Color,
    mutedTextColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) Color(0xFF10B981) else cardColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isUser) Color.White else textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (message.isUser) Color.White.copy(alpha = 0.7f) else mutedTextColor
                )
            }
        }
    }
}

// PREVIEW - Xem giao diện ngay lập tức!
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AIScreenPreview() {
    AIScreen(
        onNavigateBack = {}
    )
}
