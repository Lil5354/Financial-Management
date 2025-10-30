package com.example.expensetracker.ui.compose.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.max
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.expensetracker.ui.viewmodel.ReportsViewModel
import com.example.expensetracker.ui.viewmodel.ReportsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateBack: () -> Unit,
    isDarkTheme: Boolean = true,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val reportsState by viewModel.reportsState.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val summaryData by viewModel.summaryData.collectAsState()
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsState()
    val monthlyTrends by viewModel.monthlyTrends.collectAsState()
    val smartInsights by viewModel.smartInsights.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // Load data khi screen được tạo
    LaunchedEffect(Unit) {
        viewModel.loadReportsData()
    }
    
    // Reload data khi period thay đổi
    LaunchedEffect(selectedPeriod) {
        viewModel.loadReportsData(selectedPeriod)
    }
    
    // Refresh data khi screen được focus lại (navigate back)
    // Sử dụng một key duy nhất để tránh vòng lặp vô hạn
    var refreshTrigger by remember { mutableStateOf(0) }
    
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            viewModel.refreshData()
        }
    }
    
    // Tăng refreshTrigger khi screen được focus lại
    DisposableEffect(Unit) {
        refreshTrigger++
        onDispose { }
    }
    
    when (val state = reportsState) {
        is ReportsState.Loading -> {
            LoadingScreen(isDarkTheme = isDarkTheme)
        }
        is ReportsState.Error -> {
            ErrorScreen(
                message = state.message,
                onRetry = { viewModel.loadReportsData() },
                isDarkTheme = isDarkTheme
            )
        }
        is ReportsState.Success -> {
            ReportsContent(
                selectedPeriod = selectedPeriod,
                summaryData = summaryData,
                categoryBreakdown = categoryBreakdown,
                monthlyTrends = monthlyTrends,
                smartInsights = smartInsights,
                isLoading = isLoading,
                onPeriodChange = { period -> 
                    viewModel.loadReportsData(period) 
                },
                onNavigateBack = onNavigateBack,
                onRefresh = { viewModel.refreshData() },
                isDarkTheme = isDarkTheme
            )
        }
    }
}

@Composable
fun LoadingScreen(isDarkTheme: Boolean = true) {
    val backgroundColor = if (isDarkTheme) Color(0xFF0F0F0F) else Color(0xFFFAFAFA)
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color(0xFF10B981),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Đang tải báo cáo...",
                color = textColor,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    isDarkTheme: Boolean = true
) {
    val backgroundColor = if (isDarkTheme) Color(0xFF0F0F0F) else Color(0xFFFAFAFA)
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
    val cardColor = if (isDarkTheme) Color(0xFF1F2937) else Color.White
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Có lỗi xảy ra",
                    color = textColor,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    color = textColor.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981)
                    )
                ) {
                    Text(
                        text = "Thử lại",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ReportsContent(
    selectedPeriod: ReportsViewModel.ReportPeriod,
    summaryData: ReportsViewModel.SummaryData?,
    categoryBreakdown: List<ReportsViewModel.CategoryReport>,
    monthlyTrends: List<ReportsViewModel.MonthlyTrend>,
    smartInsights: List<ReportsViewModel.SmartInsight>,
    isLoading: Boolean,
    onPeriodChange: (ReportsViewModel.ReportPeriod) -> Unit,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    isDarkTheme: Boolean = true
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
        // Header
        item {
            ReportsHeader(
                onNavigateBack = onNavigateBack,
                onRefresh = onRefresh,
                textColor = textColor
            )
        }
        
        // Period Selector
        item {
            PeriodSelector(
                selectedPeriod = selectedPeriod,
                onPeriodChange = onPeriodChange,
                textColor = textColor,
                cardColor = cardColor
            )
        }
        
        // Summary Cards
        summaryData?.let { data ->
        item {
                SummaryCardsRow(
                    summaryData = data,
                    cardColor = cardColor,
                    textColor = textColor,
                    mutedTextColor = mutedTextColor
                )
            }
        }
        
        // Category Breakdown
        item {
            CategoryBreakdownSection(
                categoryBreakdown = categoryBreakdown,
                cardColor = cardColor,
                textColor = textColor,
                mutedTextColor = mutedTextColor
            )
        }
        
        // Monthly Trends
        item {
            MonthlyTrendsSection(
                monthlyTrends = monthlyTrends,
                cardColor = cardColor,
                                textColor = textColor,
                                mutedTextColor = mutedTextColor
                            )
        }
        
        // Smart Insights
        item {
            SmartInsightsSection(
                smartInsights = smartInsights,
                cardColor = cardColor,
                                textColor = textColor,
                                mutedTextColor = mutedTextColor
                            )
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

// ==================== COMPONENT FUNCTIONS ====================

@Composable
fun ReportsHeader(
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    textColor: Color
) {
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
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onRefresh,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Làm mới",
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodSelector(
    selectedPeriod: ReportsViewModel.ReportPeriod,
    onPeriodChange: (ReportsViewModel.ReportPeriod) -> Unit,
    textColor: Color,
    cardColor: Color
) {
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
            items(ReportsViewModel.ReportPeriod.values().toList()) { period ->
                FilterChip(
                    onClick = { onPeriodChange(period) },
                    label = { 
                        Text(
                            period.displayName, 
                            color = if (selectedPeriod == period) Color.White else textColor
                        ) 
                    },
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

@Composable
fun SummaryCardsRow(
    summaryData: ReportsViewModel.SummaryData,
    cardColor: Color,
    textColor: Color,
    mutedTextColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = "Tổng thu nhập",
            amount = formatAmountReports(summaryData.totalIncome),
            icon = Icons.Default.TrendingUp,
            color = Color(0xFF10B981),
            cardColor = cardColor,
            textColor = textColor,
            mutedTextColor = mutedTextColor,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Tổng chi tiêu",
            amount = formatAmountReports(summaryData.totalExpense),
            icon = Icons.Default.TrendingDown,
            color = Color(0xFFEF4444),
            cardColor = cardColor,
            textColor = textColor,
            mutedTextColor = mutedTextColor,
            modifier = Modifier.weight(1f)
        )
    }
    
    Spacer(modifier = Modifier.height(12.dp))
    
    SummaryCard(
        title = "Số dư",
        amount = formatAmountReports(summaryData.balance),
        icon = Icons.Default.AccountBalanceWallet,
        color = Color(0xFF3B82F6),
        cardColor = cardColor,
        textColor = textColor,
        mutedTextColor = mutedTextColor,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun CategoryBreakdownSection(
    categoryBreakdown: List<ReportsViewModel.CategoryReport>,
    cardColor: Color,
    textColor: Color,
    mutedTextColor: Color
) {
    Column {
        Text(
            text = "Chi tiêu theo danh mục",
            style = MaterialTheme.typography.titleLarge,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        if (categoryBreakdown.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không có dữ liệu chi tiêu",
                        color = mutedTextColor,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    categoryBreakdown.forEach { report ->
                        CategoryReportItem(
                            report = report,
                            textColor = textColor,
                            mutedTextColor = mutedTextColor
                        )
                        if (report != categoryBreakdown.last()) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyTrendsSection(
    monthlyTrends: List<ReportsViewModel.MonthlyTrend>,
    cardColor: Color,
    textColor: Color,
    mutedTextColor: Color
) {
    Column {
        Text(
            text = "Xu hướng theo tháng",
            style = MaterialTheme.typography.titleLarge,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        if (monthlyTrends.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không có dữ liệu xu hướng",
                        color = mutedTextColor,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Chart
                    MonthlyTrendsChart(
                        monthlyTrends = monthlyTrends,
                        textColor = textColor,
                        mutedTextColor = mutedTextColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        ChartLegend(
                            label = "Thu nhập",
                            color = Color(0xFF10B981),
                            textColor = textColor
                        )
                        ChartLegend(
                            label = "Chi tiêu",
                            color = Color(0xFFEF4444),
                            textColor = textColor
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Trend items
                    monthlyTrends.forEach { trend ->
                        MonthlyTrendItem(
                            trend = trend,
                            textColor = textColor,
                            mutedTextColor = mutedTextColor
                        )
                        if (trend != monthlyTrends.last()) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmartInsightsSection(
    smartInsights: List<ReportsViewModel.SmartInsight>,
    cardColor: Color,
    textColor: Color,
    mutedTextColor: Color
) {
    Column {
        Text(
            text = "Thông tin thông minh",
            style = MaterialTheme.typography.titleLarge,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        if (smartInsights.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có thông tin thông minh",
                        color = mutedTextColor,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    smartInsights.forEach { insight ->
                        SmartInsightItem(
                            insight = insight,
                            textColor = textColor,
                            mutedTextColor = mutedTextColor
                        )
                        if (insight != smartInsights.last()) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryReportItem(
    report: ReportsViewModel.CategoryReport,
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
                    color = Color(android.graphics.Color.parseColor(report.color)).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getIconFromString(report.icon),
                contentDescription = report.categoryName,
                tint = Color(android.graphics.Color.parseColor(report.color)),
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = report.categoryName,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${String.format("%.1f", report.percentage)}%",
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

@Composable
fun MonthlyTrendItem(
    trend: ReportsViewModel.MonthlyTrend,
    textColor: Color,
    mutedTextColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = trend.month,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${formatAmountReports(trend.income)}đ",
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
                    text = "-${formatAmountReports(trend.expense)}đ",
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
                    text = "${formatAmountReports(trend.balance)}đ",
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

@Composable
fun SmartInsightItem(
    insight: ReportsViewModel.SmartInsight,
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
                    color = Color(android.graphics.Color.parseColor(insight.color)).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getIconFromString(insight.icon),
                contentDescription = insight.title,
                tint = Color(android.graphics.Color.parseColor(insight.color)),
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
        }
    }
}

// Helper functions
@Composable
fun MonthlyTrendsChart(
    monthlyTrends: List<ReportsViewModel.MonthlyTrend>,
    textColor: Color,
    mutedTextColor: Color,
    modifier: Modifier = Modifier
) {
    if (monthlyTrends.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Không có dữ liệu",
                color = mutedTextColor
            )
        }
        return
    }
    
    val maxValue = remember(monthlyTrends) {
        max(
            monthlyTrends.maxOfOrNull { it.income } ?: 0L,
            monthlyTrends.maxOfOrNull { it.expense } ?: 0L
        ).toFloat().coerceAtLeast(1000f)
    }
    
    val padding = 40.dp
    val chartHeight = 180.dp
    
    Column(modifier = modifier) {
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(chartHeight)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val chartAreaHeight = canvasHeight - (padding.toPx() * 2)
            val chartAreaTop = padding.toPx()
            val chartAreaBottom = chartAreaTop + chartAreaHeight
            val chartAreaWidth = canvasWidth - (padding.toPx() * 2)
            
            // Draw grid lines
            val gridLineColor = mutedTextColor.copy(alpha = 0.2f)
            val gridLines = 5
            for (i in 0..gridLines) {
                val y = chartAreaTop + (chartAreaHeight / gridLines) * i
                drawLine(
                    color = gridLineColor,
                    start = Offset(padding.toPx(), y),
                    end = Offset(canvasWidth - padding.toPx(), y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            // Draw income line
            val incomeColor = Color(0xFF10B981)
            val incomePath = androidx.compose.ui.graphics.Path().apply {
                monthlyTrends.forEachIndexed { index, trend ->
                    val x = padding.toPx() + (chartAreaWidth / (monthlyTrends.size - 1).coerceAtLeast(1)) * index
                    val y = chartAreaBottom - ((trend.income.toFloat() / maxValue) * chartAreaHeight)
                    
                    if (index == 0) {
                        moveTo(x, y)
                    } else {
                        lineTo(x, y)
                    }
                }
            }
            drawPath(
                path = incomePath,
                color = incomeColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            
            // Draw expense line
            val expenseColor = Color(0xFFEF4444)
            val expensePath = androidx.compose.ui.graphics.Path().apply {
                monthlyTrends.forEachIndexed { index, trend ->
                    val x = padding.toPx() + (chartAreaWidth / (monthlyTrends.size - 1).coerceAtLeast(1)) * index
                    val y = chartAreaBottom - ((trend.expense.toFloat() / maxValue) * chartAreaHeight)
                    
                    if (index == 0) {
                        moveTo(x, y)
                    } else {
                        lineTo(x, y)
                    }
                }
            }
            drawPath(
                path = expensePath,
                color = expenseColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            
            // Draw points
            monthlyTrends.forEachIndexed { index, trend ->
                val x = padding.toPx() + (chartAreaWidth / (monthlyTrends.size - 1).coerceAtLeast(1)) * index
                
                // Income point
                val incomeY = chartAreaBottom - ((trend.income.toFloat() / maxValue) * chartAreaHeight)
                drawCircle(
                    color = incomeColor,
                    radius = 5.dp.toPx(),
                    center = Offset(x, incomeY)
                )
                
                // Expense point
                val expenseY = chartAreaBottom - ((trend.expense.toFloat() / maxValue) * chartAreaHeight)
                drawCircle(
                    color = expenseColor,
                    radius = 5.dp.toPx(),
                    center = Offset(x, expenseY)
                )
            }
        }
        
        // Month labels
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            monthlyTrends.forEach { trend ->
                Text(
                    text = trend.month.take(3),
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedTextColor,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ChartLegend(
    label: String,
    color: Color,
    textColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = textColor
        )
    }
}

private fun formatAmountReports(amount: Long): String {
    return String.format("%,d", amount)
}

private fun getIconFromString(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "restaurant" -> Icons.Default.Restaurant
        "directions_car" -> Icons.Default.DirectionsCar
        "shopping_bag" -> Icons.Default.ShoppingBag
        "movie" -> Icons.Default.Movie
        "local_hospital" -> Icons.Default.LocalHospital
        "school" -> Icons.Default.School
        "flight" -> Icons.Default.Flight
        "warning" -> Icons.Default.Warning
        "star" -> Icons.Default.Star
        "trending_up" -> Icons.Default.TrendingUp
        "trending_down" -> Icons.Default.TrendingDown
        "error" -> Icons.Default.Error
        else -> Icons.Default.Category
    }
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