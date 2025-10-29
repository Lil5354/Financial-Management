package com.example.expensetracker.ui.compose.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.expensetracker.ui.viewmodel.ChatViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    isDarkTheme: Boolean = true,
    chatViewModel: ChatViewModel = hiltViewModel()
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
            0 -> AIAssistantContent(aiInsights, cardColor, textColor, mutedTextColor, accentColor, successColor, warningColor, errorColor, chatViewModel)
            1 -> OCRContent(cardColor, textColor, mutedTextColor, accentColor, chatViewModel)
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
    errorColor: Color,
    chatViewModel: ChatViewModel
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
            VoiceRecordingCard(
                chatViewModel = chatViewModel,
                cardColor = cardColor,
                textColor = textColor,
                mutedTextColor = mutedTextColor,
                successColor = successColor,
                errorColor = errorColor
            )
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

// OCR Content - Modern Design with Camera & Image Picker
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OCRContent(
    cardColor: Color,
    textColor: Color,
    mutedTextColor: Color,
    accentColor: Color,
    chatViewModel: ChatViewModel
) {
    val context = LocalContext.current
    val scanner = chatViewModel.receiptScannerManager
    val scope = rememberCoroutineScope()
    
    val isProcessing by scanner.isProcessing.collectAsState()
    val scannedData by scanner.scannedData.collectAsState()
    val scanError by scanner.error.collectAsState()
    val capturedImage by scanner.capturedImageUri.collectAsState()
    
    // Permissions
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val imagePermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    
    // Camera launcher
    var tempImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            android.util.Log.d("OCRContent", "✅ Image captured successfully")
            scope.launch {
                scanner.processReceiptImage(tempImageUri!!)
            }
        } else {
            android.util.Log.e("OCRContent", "❌ Image capture failed")
            Toast.makeText(context, "Chụp ảnh thất bại", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            android.util.Log.d("OCRContent", "✅ Image picked: $it")
            val copiedUri = scanner.copyImageToCache(it)
            if (copiedUri != null) {
                scope.launch {
                    scanner.processReceiptImage(copiedUri)
                }
            }
        }
    }
    
    // Show error toast
    LaunchedEffect(scanError) {
        scanError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            scanner.clearError()
        }
    }
    
    // Process scanned data and create expense
    LaunchedEffect(scannedData) {
        scannedData?.let { data ->
            android.util.Log.d("OCRContent", "📊 Processing scanned data")
            chatViewModel.processReceiptData(data)
            delay(500)
            scanner.clearData()
        }
    }
    
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
        
        // Camera & Upload buttons
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
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = Color(0xFF10B981),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Đang xử lý hóa đơn...",
                            style = MaterialTheme.typography.titleMedium,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
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
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Camera button
                        Button(
                            onClick = {
                                if (cameraPermission.status.isGranted) {
                                    tempImageUri = scanner.createImageFileUri()
                                    cameraLauncher.launch(tempImageUri)
                                } else {
                                    cameraPermission.launchPermissionRequest()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
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
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Upload button
                        OutlinedButton(
                            onClick = {
                                if (imagePermission.status.isGranted) {
                                    imagePickerLauncher.launch("image/*")
                                } else {
                                    imagePermission.launchPermissionRequest()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF10B981)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Tải ảnh",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tải ảnh từ thư viện")
                        }
                    }
                }
            }
        }
        
        // Display captured image if available
        capturedImage?.let { uri ->
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
                            text = "Ảnh đã chụp",
                            style = MaterialTheme.typography.titleMedium,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Receipt",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
        
        // Display scanned result
        if (scannedData != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = Color(0xFF10B981)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Đã xử lý hóa đơn thành công!",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Chi tiêu đã được thêm vào danh sách",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

// Forecasting Content - Coming Soon Design
@Composable
fun ForecastingContent(
    cardColor: Color,
    textColor: Color,
    mutedTextColor: Color,
    accentColor: Color,
    successColor: Color,
    warningColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon với hiệu ứng gradient background
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.2f),
                                    successColor.copy(alpha = 0.2f)
                                )
                            ),
                            shape = RoundedCornerShape(30.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Coming Soon",
                        tint = accentColor,
                        modifier = Modifier.size(60.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Tiêu đề "Coming Soon"
                Text(
                    text = "Coming Soon",
                    style = MaterialTheme.typography.headlineLarge,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Mô tả tính năng
                Text(
                    text = "Dự đoán chi tiêu thông minh",
                    style = MaterialTheme.typography.titleMedium,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Thông tin chi tiết
                Text(
                    text = "Chúng tôi đang phát triển tính năng dự đoán chi tiêu dựa trên AI và Machine Learning để giúp bạn quản lý tài chính tốt hơn.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = mutedTextColor,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 24.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Các tính năng sẽ có
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureItem(
                        icon = Icons.Default.Analytics,
                        text = "Phân tích xu hướng chi tiêu",
                        textColor = textColor,
                        iconColor = accentColor
                    )
                    FeatureItem(
                        icon = Icons.Default.TrendingUp,
                        text = "Dự đoán chi phí tháng tới",
                        textColor = textColor,
                        iconColor = successColor
                    )
                    FeatureItem(
                        icon = Icons.Default.Warning,
                        text = "Cảnh báo vượt ngân sách",
                        textColor = textColor,
                        iconColor = warningColor
                    )
                    FeatureItem(
                        icon = Icons.Default.Lightbulb,
                        text = "Gợi ý tiết kiệm thông minh",
                        textColor = textColor,
                        iconColor = Color(0xFFF59E0B)
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Badge "Đang phát triển"
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = accentColor.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "In Progress",
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Đang trong quá trình phát triển",
                            style = MaterialTheme.typography.bodyMedium,
                            color = accentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Feature Item Component cho Coming Soon
@Composable
fun FeatureItem(
    icon: ImageVector,
    text: String,
    textColor: Color,
    iconColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
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

// Voice Recording Card - Hold to Record like Zalo
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VoiceRecordingCard(
    chatViewModel: ChatViewModel,
    cardColor: Color,
    textColor: Color,
    mutedTextColor: Color,
    successColor: Color,
    errorColor: Color
) {
    val context = LocalContext.current
    val isListening by chatViewModel.voiceRecognitionManager.isListening.collectAsState()
    val recognizedText by chatViewModel.voiceRecognitionManager.recognizedText.collectAsState()
    val voiceError by chatViewModel.voiceRecognitionManager.error.collectAsState()
    val isLoading by chatViewModel.isLoading.collectAsState()
    val successMessage by chatViewModel.successMessage.collectAsState()
    val errorMessage by chatViewModel.errorMessage.collectAsState()
    
    // Permission state
    val micPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    
    // Text input for testing (when mic is not available)
    var testInputText by remember { mutableStateOf("") }
    var showTestInput by remember { mutableStateOf(false) }
    
    // Timer state
    var recordingSeconds by remember { mutableStateOf(0) }
    
    // Pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Timer effect
    LaunchedEffect(isListening) {
        recordingSeconds = 0
        while (isListening) {
            delay(1000)
            recordingSeconds++
        }
    }
    
    // Handle recognized text
    LaunchedEffect(recognizedText) {
        if (recognizedText.isNotBlank()) {
            chatViewModel.processVoiceCommand(recognizedText)
            chatViewModel.voiceRecognitionManager.clearText()
        }
    }
    
    // Show success message
    LaunchedEffect(successMessage) {
        successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            chatViewModel.clearMessages()
        }
    }
    
    // Show error message  
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            chatViewModel.clearMessages()
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (isListening) errorColor.copy(alpha = 0.1f) else cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isListening) 4.dp else 0.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Voice Button - Hold to Record with Pulse Animation
            Box(
                contentAlignment = Alignment.Center
            ) {
                // Pulsing wave effect when listening
                if (isListening) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(pulseScale)
                            .background(
                                color = errorColor.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(50.dp)
                            )
                    )
                }
                
                // Main button
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            color = when {
                                isListening -> errorColor
                                isLoading -> Color(0xFF3B82F6)
                                else -> successColor
                            },
                            shape = RoundedCornerShape(50.dp)
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    // Check permission first
                                    if (!micPermission.status.isGranted) {
                                        micPermission.launchPermissionRequest()
                                        return@detectTapGestures
                                    }
                                    
                                    // Start recording when pressed
                                    chatViewModel.voiceRecognitionManager.startListening()
                                    
                                    // Wait for release
                                    tryAwaitRelease()
                                    
                                    // Stop recording when released
                                    chatViewModel.voiceRecognitionManager.stopListening()
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = Color.White,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Timer when recording
            if (isListening) {
                Text(
                    text = String.format("%02d:%02d", recordingSeconds / 60, recordingSeconds % 60),
                    style = MaterialTheme.typography.headlineMedium,
                    color = errorColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Instructions
            Text(
                text = when {
                    !micPermission.status.isGranted -> "⚠️ Cần quyền microphone"
                    isListening -> "🎤 Đang nghe... Thả ra để gửi"
                    isLoading -> "⏳ Đang xử lý..."
                    else -> "Giữ để nói lệnh"
                },
                style = MaterialTheme.typography.titleMedium,
                color = when {
                    !micPermission.status.isGranted -> Color(0xFFF59E0B)
                    isListening -> errorColor
                    isLoading -> Color(0xFF3B82F6)
                    else -> textColor
                },
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = when {
                    !micPermission.status.isGranted -> "Nhấn vào nút để cấp quyền microphone"
                    isListening -> "Nói: 'Thêm chi tiêu 50k cà phê'"
                    isLoading -> "AI đang phân tích lệnh của bạn..."
                    voiceError != null -> "❌ $voiceError"
                    recognizedText.isNotBlank() -> "✅ Đã nhận: $recognizedText"
                    else -> "Ví dụ: 'Thêm chi tiêu 100k ăn trưa' hoặc 'Thu nhập 5 triệu lương'"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    voiceError != null -> errorColor
                    recognizedText.isNotBlank() -> successColor
                    else -> mutedTextColor
                },
                textAlign = TextAlign.Center,
                maxLines = 3
            )
            
            // Divider
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(mutedTextColor.copy(alpha = 0.2f))
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // Toggle Test Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTestInput = !showTestInput },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Test Input",
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (showTestInput) "Ẩn Test Input" else "📝 Test bằng Text (Cho Emulator)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF3B82F6),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (showTestInput) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Test Input Section
            if (showTestInput) {
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = testInputText,
                    onValueChange = { testInputText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nhập lệnh để test") },
                    placeholder = { Text("VD: Thêm chi tiêu 50k cà phê") },
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3B82F6),
                        focusedLabelColor = Color(0xFF3B82F6),
                        cursorColor = Color(0xFF3B82F6)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = {
                        if (testInputText.isNotBlank()) {
                            chatViewModel.processVoiceCommand(testInputText)
                            testInputText = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = testInputText.isNotBlank() && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6),
                        disabledContainerColor = Color(0xFF3B82F6).copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đang xử lý...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gửi lệnh cho AI", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "💡 Test logic AI mà không cần microphone",
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedTextColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
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
