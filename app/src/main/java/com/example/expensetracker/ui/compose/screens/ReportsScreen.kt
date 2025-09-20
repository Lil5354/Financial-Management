package com.example.expensetracker.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

// Data class cho báo cáo
data class CategoryReport(
    val category: String,
    val amount: Long,
    val percentage: Float,
    val color: Color,
    val icon: ImageVector
)

data class MonthlyReport(
    val month: String,
    val income: Long,
    val expense: Long,
    val balance: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateBack: () -> Unit,
    isDarkTheme: Boolean = true
) {
    val backgroundColor = if (isDarkTheme) Color(0xFF0F0F0F) else Color(0xFFFAFAFA)
    val cardColor = if (isDarkTheme) Color(0xFF1F2937) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
    val mutedTextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    
    // Sample data cho báo cáo
    val categoryReports = remember {
        listOf(
            CategoryReport("Ăn uống", 1200000, 35.5f, Color(0xFFF59E0B), Icons.Default.Restaurant),
            CategoryReport("Giao thông", 800000, 23.6f, Color(0xFF3B82F6), Icons.Default.DirectionsCar),
            CategoryReport("Mua sắm", 600000, 17.7f, Color(0xFF8B5CF6), Icons.Default.ShoppingBag),
            CategoryReport("Giải trí", 400000, 11.8f, Color(0xFFEC4899), Icons.Default.Movie),
            CategoryReport("Sức khỏe", 200000, 5.9f, Color(0xFF10B981), Icons.Default.LocalHospital),
            CategoryReport("Khác", 180000, 5.3f, Color(0xFF6B7280), Icons.Default.Category)
        )
    }
    
    val monthlyReports = remember {
        listOf(
            MonthlyReport("Tháng 1", 5000000, 3200000, 1800000),
            MonthlyReport("Tháng 2", 5200000, 3400000, 1800000),
            MonthlyReport("Tháng 3", 4800000, 3000000, 1800000),
            MonthlyReport("Tháng 4", 5500000, 3600000, 1900000),
            MonthlyReport("Tháng 5", 5100000, 3300000, 1800000),
            MonthlyReport("Tháng 6", 5300000, 3500000, 1800000)
        )
    }
    
    var selectedPeriod by remember { mutableStateOf("Tháng này") }
    val periods = listOf("Tuần này", "Tháng này", "3 tháng", "6 tháng", "Năm nay")
    
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
        
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Báo cáo & Thống kê",
                    style = MaterialTheme.typography.headlineSmall,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Period Selector
        item {
            Column {
                Text(
                    text = "Khoảng thời gian",
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(periods) { period ->
                        FilterChip(
                            onClick = { selectedPeriod = period },
                            label = { Text(period, color = if (selectedPeriod == period) Color.White else textColor) },
                            selected = selectedPeriod == period,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF10B981),
                                containerColor = cardColor
                            )
                        )
                    }
                }
            }
        }
        
        // Summary Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Tổng thu nhập",
                    amount = "5,200,000đ",
                    icon = Icons.Default.TrendingUp,
                    color = Color(0xFF10B981),
                    cardColor = cardColor,
                    textColor = textColor,
                    mutedTextColor = mutedTextColor,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Tổng chi tiêu",
                    amount = "3,380,000đ",
                    icon = Icons.Default.TrendingDown,
                    color = Color(0xFFEF4444),
                    cardColor = cardColor,
                    textColor = textColor,
                    mutedTextColor = mutedTextColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            SummaryCard(
                title = "Số dư",
                amount = "1,820,000đ",
                icon = Icons.Default.AccountBalanceWallet,
                color = Color(0xFF3B82F6),
                cardColor = cardColor,
                textColor = textColor,
                mutedTextColor = mutedTextColor,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Category Breakdown
        item {
            Column {
                Text(
                    text = "Chi tiêu theo danh mục",
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        categoryReports.forEach { report ->
                            CategoryReportItem(
                                report = report,
                                textColor = textColor,
                                mutedTextColor = mutedTextColor
                            )
                            if (report != categoryReports.last()) {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
        
        // Monthly Trend
        item {
            Column {
                Text(
                    text = "Xu hướng theo tháng",
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        monthlyReports.forEach { report ->
                            MonthlyReportItem(
                                report = report,
                                textColor = textColor,
                                mutedTextColor = mutedTextColor
                            )
                            if (report != monthlyReports.last()) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
        
        // Insights
        item {
            Column {
                Text(
                    text = "Thông tin thông minh",
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        InsightItem(
                            icon = Icons.Default.TrendingUp,
                            title = "Chi tiêu tăng 8.2%",
                            description = "So với tháng trước, chủ yếu do ăn uống",
                            color = Color(0xFF10B981),
                            textColor = textColor,
                            mutedTextColor = mutedTextColor
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        InsightItem(
                            icon = Icons.Default.Warning,
                            title = "Cần kiểm soát chi tiêu",
                            description = "Chi tiêu ăn uống chiếm 35.5% tổng chi tiêu",
                            color = Color(0xFFF59E0B),
                            textColor = textColor,
                            mutedTextColor = mutedTextColor
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        InsightItem(
                            icon = Icons.Default.Star,
                            title = "Tiết kiệm tốt",
                            description = "Bạn đã tiết kiệm được 1,820,000đ tháng này",
                            color = Color(0xFF3B82F6),
                            textColor = textColor,
                            mutedTextColor = mutedTextColor
                        )
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// Summary Card Component
@Composable
fun SummaryCard(
    title: String,
    amount: String,
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
                    style = MaterialTheme.typography.bodyMedium,
                    color = mutedTextColor
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleLarge,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Category Report Item Component
@Composable
fun CategoryReportItem(
    report: CategoryReport,
    textColor: Color,
    mutedTextColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = report.color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = report.icon,
                contentDescription = report.category,
                tint = report.color,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = report.category,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${report.percentage}%",
                style = MaterialTheme.typography.bodySmall,
                color = mutedTextColor
            )
        }
        
        Text(
            text = "${formatAmountReports(report.amount)}đ",
            style = MaterialTheme.typography.titleMedium,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

// Monthly Report Item Component
@Composable
fun MonthlyReportItem(
    report: MonthlyReport,
    textColor: Color,
    mutedTextColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = report.month,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${formatAmountReports(report.income)}đ",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Thu nhập",
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedTextColor
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "-${formatAmountReports(report.expense)}đ",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Chi tiêu",
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedTextColor
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${formatAmountReports(report.balance)}đ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Số dư",
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedTextColor
                )
            }
        }
    }
}

// Insight Item Component
@Composable
fun InsightItem(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color,
    textColor: Color,
    mutedTextColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = mutedTextColor
            )
        }
    }
}

// Helper function
private fun formatAmountReports(amount: Long): String {
    return String.format("%,d", amount)
}

// PREVIEW - Xem giao diện ngay lập tức!
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ReportsScreenPreview() {
    ReportsScreen(
        onNavigateBack = {}
    )
}

@Preview(showBackground = true)
@Composable
fun SummaryCardPreview() {
    SummaryCard(
        title = "Tổng thu nhập",
        amount = "5,200,000đ",
        icon = Icons.Default.TrendingUp,
        color = Color(0xFF10B981),
        cardColor = Color(0xFF1F2937),
        textColor = Color.White,
        mutedTextColor = Color(0xFF9CA3AF)
    )
}