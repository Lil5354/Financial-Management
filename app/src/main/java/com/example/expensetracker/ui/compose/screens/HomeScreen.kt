package com.example.expensetracker.ui.compose.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R

/**
 * Màn hình chính với giao diện đẹp và hiện đại
 * Sử dụng Material Design 3 và theme màu xanh lá
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToExpenses: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {}
) {
    val backgroundColor = if (isDarkTheme) Color(0xFF0F0F0F) else Color(0xFFFAFAFA)
    val cardColor = if (isDarkTheme) Color(0xFF1F2937) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
    val mutedTextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Crypto-style Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Logo NoNo
                    Image(
                        painter = painterResource(id = R.drawable.logo_app_rounded),
                        contentDescription = "NoNo Logo",
                        modifier = Modifier.size(60.dp)
                    )
                    
            Column {
                Text(
                            text = "Chào Độ! 👋",
                            style = MaterialTheme.typography.headlineLarge,
                            color = textColor,
                    fontWeight = FontWeight.Bold
                )
                        Spacer(modifier = Modifier.height(4.dp))
                Text(
                            text = "Tổng quan tài chính của bạn",
                            style = MaterialTheme.typography.bodyMedium,
                            color = mutedTextColor
                        )
                    }
                }
                
                // Theme toggle button
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = cardColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Chuyển đổi theme",
                        tint = textColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        // Crypto-style Balance Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                    Text(
                                text = "Tổng tài sản",
                                style = MaterialTheme.typography.bodyMedium,
                                color = mutedTextColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                                text = "3,800,000đ",
                        style = MaterialTheme.typography.headlineLarge,
                                color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+12.5%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = " so với tháng trước",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = mutedTextColor
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    color = Color(0xFF10B981).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Crypto-style Stats Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CryptoStatCard(
                    title = "Thu nhập",
                    amount = "5,000,000đ",
                    change = "+8.2%",
                    icon = Icons.Default.TrendingUp,
                    color = Color(0xFF10B981),
                    cardColor = cardColor,
                    textColor = textColor,
                    mutedTextColor = mutedTextColor,
                    modifier = Modifier.weight(1f)
                )
                CryptoStatCard(
                    title = "Chi tiêu",
                    amount = "1,200,000đ",
                    change = "-2.1%",
                    icon = Icons.Default.TrendingDown,
                    color = Color(0xFFEF4444),
                    cardColor = cardColor,
                    textColor = textColor,
                    mutedTextColor = mutedTextColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // AI Assistant Section
        item {
            Column {
                Text(
                    text = "Trợ lý AI",
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Nút Phân tích AI
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(16.dp),
                    onClick = { /* TODO: AI analysis functionality */ }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = Color(0xFF10B981).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = "AI Analysis",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Phân tích AI",
                                style = MaterialTheme.typography.titleMedium,
                                color = textColor,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Phân tích chi tiêu thông minh với AI",
                                style = MaterialTheme.typography.bodyMedium,
                                color = mutedTextColor
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Go to AI",
                            tint = mutedTextColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        
        // Quick Insights Section
        item {
            Column {
                Text(
                    text = "Thông tin nhanh",
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                    // Thẻ thông tin chi tiêu hôm nay
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Hôm nay",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = mutedTextColor
                                )
                                Text(
                                    text = "255,000đ",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = textColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    // Thẻ thông tin tuần này
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Tuần này",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = mutedTextColor
                                )
                                Text(
                                    text = "1,200,000đ",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = textColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Crypto-style Recent Transactions
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Giao dịch gần đây",
                        style = MaterialTheme.typography.titleLarge,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = onNavigateToExpenses,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF10B981))
                    ) {
                        Text("Xem tất cả")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Crypto-style transaction list
                repeat(3) { index ->
                    CryptoTransactionItem(
                        title = if (index == 0) "Cà phê" else if (index == 1) "Xăng xe" else "Ăn trưa",
                        amount = if (index == 0) "25,000" else if (index == 1) "150,000" else "80,000",
                        isExpense = true,
                        cardColor = cardColor,
                        textColor = textColor,
                        mutedTextColor = mutedTextColor,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (index < 2) Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// Crypto-style Stat Card Component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoStatCard(
    title: String,
    amount: String,
    change: String,
    icon: ImageVector,
    color: Color,
    cardColor: Color,
    textColor: Color,
    mutedTextColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedTextColor
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = change,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Crypto-style Action Card Component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    cardColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(90.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = Color(0xFF10B981).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(20.dp)
            )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Crypto-style Transaction Item Component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoTransactionItem(
    title: String,
    amount: String,
    isExpense: Boolean,
    cardColor: Color,
    textColor: Color,
    mutedTextColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = if (isExpense) Color(0xFFEF4444).copy(alpha = 0.1f) else Color(0xFF10B981).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isExpense) Icons.Default.Remove else Icons.Default.Add,
                        contentDescription = null,
                        tint = if (isExpense) Color(0xFFEF4444) else Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Hôm nay",
                        style = MaterialTheme.typography.bodySmall,
                        color = mutedTextColor
                    )
                }
            }
            Text(
                text = "${if (isExpense) "-" else "+"}${amount}đ",
                style = MaterialTheme.typography.titleMedium,
                color = if (isExpense) Color(0xFFEF4444) else Color(0xFF10B981),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// PREVIEW - Xem giao diện ngay lập tức!
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onNavigateToExpenses = {},
        onNavigateToReports = {},
        onNavigateToSettings = {}
    )
}

@Preview(showBackground = true)
@Composable
fun CryptoStatCardPreview() {
    CryptoStatCard(
        title = "Thu nhập",
        amount = "₫5,000,000",
        change = "+8.2%",
        icon = Icons.Default.TrendingUp,
        color = Color(0xFF10B981),
        cardColor = Color(0xFF1F2937),
        textColor = Color.White,
        mutedTextColor = Color(0xFF9CA3AF)
    )
}

@Preview(showBackground = true)
@Composable
fun CryptoActionCardPreview() {
    CryptoActionCard(
        title = "Thêm chi tiêu",
        icon = Icons.Default.Add,
        onClick = {},
        cardColor = Color(0xFF1F2937),
        textColor = Color.White
    )
}

@Preview(showBackground = true)
@Composable
fun CryptoTransactionItemPreview() {
    CryptoTransactionItem(
        title = "Cà phê",
        amount = "25,000",
        isExpense = true,
        cardColor = Color(0xFF1F2937),
        textColor = Color.White,
        mutedTextColor = Color(0xFF9CA3AF)
    )
}