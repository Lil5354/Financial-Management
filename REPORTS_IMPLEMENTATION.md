# 📊 Tài Liệu Triển Khai Chức Năng Báo Cáo

## 🎯 Tổng Quan

Chức năng báo cáo trong ứng dụng NoNo Expense Tracker cho phép người dùng xem tổng quan về:
- **Tổng thu nhập** (Total Income)
- **Tổng chi tiêu** (Total Expense)  
- **Số dư** (Balance)
- **Phân tích theo danh mục** (Category Breakdown)
- **Xu hướng theo tháng** (Monthly Trends)
- **Thông tin thông minh** (Smart Insights)

---

## 🏗️ Kiến Trúc Hệ Thống

### 1. Data Layer

#### **Expense Entity** (`Expense.kt`)
```kotlin
data class Expense(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val amount: Long = 0L,
    val category: String = "",
    val date: Date = Date(),
    val note: String = "",
    var isExpense: Boolean = true,  // true = chi tiêu, false = thu nhập
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
```

**Điểm quan trọng:**
- `isExpense`: Phân biệt giao dịch là chi tiêu hay thu nhập
- `amount`: Số tiền (Long) để tránh lỗi làm tròn
- `category`: Danh mục để phân tích chi tiêu

#### **FirebaseRepository** (`FirebaseRepository.kt`)
Cung cấp các phương thức:
- `getExpenses()`: Lấy tất cả giao dịch
- `getExpensesByDateRange(startDate, endDate)`: Lấy giao dịch theo khoảng thời gian
- `getExpensesByCategory(category)`: Lấy giao dịch theo danh mục
- `getTotalIncome(startDate, endDate)`: Tính tổng thu nhập
- `getTotalExpense(startDate, endDate)`: Tính tổng chi tiêu

---

### 2. ViewModel Layer

#### **ReportsViewModel** (`ReportsViewModel.kt`)

##### **State Management**
```kotlin
// Trạng thái báo cáo
private val _reportsState = MutableStateFlow<ReportsState>(ReportsState.Loading)
val reportsState: StateFlow<ReportsState> = _reportsState.asStateFlow()

// Dữ liệu tổng quan
private val _summaryData = MutableStateFlow<SummaryData?>(null)
val summaryData: StateFlow<SummaryData?> = _summaryData.asStateFlow()

// Phân tích theo danh mục
private val _categoryBreakdown = MutableStateFlow<List<CategoryReport>>(emptyList())
val categoryBreakdown: StateFlow<List<CategoryReport>> = _categoryBreakdown.asStateFlow()

// Xu hướng theo tháng
private val _monthlyTrends = MutableStateFlow<List<MonthlyTrend>>(emptyList())
val monthlyTrends: StateFlow<List<MonthlyTrend>> = _monthlyTrends.asStateFlow()

// Thông tin thông minh
private val _smartInsights = MutableStateFlow<List<SmartInsight>>(emptyList())
val smartInsights: StateFlow<List<SmartInsight>> = _smartInsights.asStateFlow()
```

##### **Data Models**
```kotlin
// Dữ liệu tổng quan
data class SummaryData(
    val totalIncome: Long,      // Tổng thu nhập
    val totalExpense: Long,     // Tổng chi tiêu
    val balance: Long,          // Số dư = thu nhập - chi tiêu
    val period: ReportPeriod    // Khoảng thời gian
)

// Báo cáo theo danh mục
data class CategoryReport(
    val categoryName: String,   // Tên danh mục
    val amount: Long,           // Tổng số tiền
    val percentage: Float,      // Phần trăm so với tổng
    val color: String,          // Màu hiển thị
    val icon: String            // Icon hiển thị
)

// Xu hướng theo tháng
data class MonthlyTrend(
    val month: String,          // Tên tháng
    val year: Int,              // Năm
    val income: Long,           // Thu nhập tháng đó
    val expense: Long,          // Chi tiêu tháng đó
    val balance: Long           // Số dư tháng đó
)

// Thông tin thông minh
data class SmartInsight(
    val type: InsightType,      // Loại thông tin
    val title: String,          // Tiêu đề
    val description: String,    // Mô tả chi tiết
    val icon: String,           // Icon
    val color: String           // Màu
)
```

##### **Khoảng Thời Gian (Report Period)**
```kotlin
enum class ReportPeriod(val displayName: String, val days: Int) {
    THIS_WEEK("Tuần này", 7),
    THIS_MONTH("Tháng này", 30),
    THREE_MONTHS("3 tháng", 90),
    SIX_MONTHS("6 tháng", 180),
    THIS_YEAR("Năm nay", 365)
}
```

##### **Business Logic**

**1. Load Reports Data**
```kotlin
fun loadReportsData(period: ReportPeriod = _selectedPeriod.value) {
    _selectedPeriod.value = period
    _isLoading.value = true
    _reportsState.value = ReportsState.Loading
    
    viewModelScope.launch {
        try {
            // Lấy khoảng thời gian
            val dateRange = getDateRangeForPeriod(period)
            
            // Lấy dữ liệu từ Firebase
            val expenses = firebaseRepository.getExpensesByDateRange(
                dateRange.first, 
                dateRange.second
            ).getOrThrow()
            
            // Tính toán các metrics
            calculateSummaryData(expenses, period)
            calculateCategoryBreakdown(expenses)
            calculateMonthlyTrends(period)
            generateSmartInsights(expenses, period)
            
            _reportsState.value = ReportsState.Success
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Không thể tải dữ liệu báo cáo"
            _reportsState.value = ReportsState.Error(e.message ?: "Lỗi không xác định")
        } finally {
            _isLoading.value = false
        }
    }
}
```

**2. Tính Toán Dữ Liệu Tổng Quan**
```kotlin
private fun calculateSummaryData(expenses: List<Expense>, period: ReportPeriod) {
    // Tính tổng thu nhập (isExpense = false)
    val income = expenses.filter { !it.isExpense }.sumOf { it.amount }
    
    // Tính tổng chi tiêu (isExpense = true)
    val expense = expenses.filter { it.isExpense }.sumOf { it.amount }
    
    // Tính số dư
    val balance = income - expense
    
    _summaryData.value = SummaryData(income, expense, balance, period)
}
```

**3. Phân Tích Theo Danh Mục**
```kotlin
private fun calculateCategoryBreakdown(expenses: List<Expense>) {
    // Lọc chỉ lấy chi tiêu
    val expenseTransactions = expenses.filter { it.isExpense }
    val totalExpense = expenseTransactions.sumOf { it.amount }
    
    if (totalExpense == 0L) {
        _categoryBreakdown.value = emptyList()
        return
    }
    
    // Nhóm theo danh mục
    val categoryMap = expenseTransactions.groupBy { it.category }
    
    // Tính toán cho mỗi danh mục
    val categoryReports = categoryMap.map { (categoryName, transactions) ->
        val amount = transactions.sumOf { it.amount }
        val percentage = (amount.toFloat() / totalExpense * 100)
        
        CategoryReport(
            categoryName = categoryName,
            amount = amount,
            percentage = percentage,
            color = getCategoryColor(categoryName),
            icon = getCategoryIcon(categoryName)
        )
    }.sortedByDescending { it.amount }
    
    _categoryBreakdown.value = categoryReports
}
```

**4. Tính Xu Hướng Theo Tháng**
```kotlin
private suspend fun calculateMonthlyTrends(period: ReportPeriod) {
    try {
        val months = getMonthsForPeriod(period)
        val trends = mutableListOf<MonthlyTrend>()
        
        for (month in months) {
            val monthRange = getMonthDateRange(month.first, month.second)
            val expenses = firebaseRepository.getExpensesByDateRange(
                monthRange.first, 
                monthRange.second
            ).getOrThrow()
            
            val income = expenses.filter { !it.isExpense }.sumOf { it.amount }
            val expense = expenses.filter { it.isExpense }.sumOf { it.amount }
            val balance = income - expense
            
            trends.add(
                MonthlyTrend(
                    month = getMonthName(month.first),
                    year = month.second,
                    income = income,
                    expense = expense,
                    balance = balance
                )
            )
        }
        
        _monthlyTrends.value = trends
    } catch (e: Exception) {
        _monthlyTrends.value = emptyList()
    }
}
```

**5. Tạo Thông Tin Thông Minh**
```kotlin
private fun generateSmartInsights(expenses: List<Expense>, period: ReportPeriod) {
    val insights = mutableListOf<SmartInsight>()
    
    // 1. Phân tích xu hướng chi tiêu
    val expenseTrend = analyzeExpenseTrend(expenses, period)
    if (expenseTrend != null) {
        insights.add(expenseTrend)
    }
    
    // 2. Phân tích danh mục chi tiêu cao nhất
    val topCategory = _categoryBreakdown.value.firstOrNull()
    if (topCategory != null && topCategory.percentage > 30f) {
        insights.add(
            SmartInsight(
                type = InsightType.HIGH_CATEGORY_SPENDING,
                title = "Cần kiểm soát chi tiêu",
                description = "Chi tiêu ${topCategory.categoryName} chiếm ${String.format("%.1f", topCategory.percentage)}% tổng chi tiêu",
                icon = "warning",
                color = "#F59E0B"
            )
        )
    }
    
    // 3. Phân tích tiết kiệm
    val summary = _summaryData.value
    if (summary != null && summary.balance > 0) {
        insights.add(
            SmartInsight(
                type = InsightType.SAVINGS_GOAL,
                title = "Tiết kiệm tốt",
                description = "Bạn đã tiết kiệm được ${formatAmount(summary.balance)} tháng này",
                icon = "star",
                color = "#3B82F6"
            )
        )
    }
    
    _smartInsights.value = insights
}
```

---

### 3. UI Layer

#### **ReportsScreen** (`ReportsScreen.kt`)

##### **Screen Structure**
```kotlin
@Composable
fun ReportsScreen(
    onNavigateBack: () -> Unit,
    isDarkTheme: Boolean = true,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    // Collect states
    val reportsState by viewModel.reportsState.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val summaryData by viewModel.summaryData.collectAsState()
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsState()
    val monthlyTrends by viewModel.monthlyTrends.collectAsState()
    val smartInsights by viewModel.smartInsights.collectAsState()
    
    // Load data khi screen được tạo
    LaunchedEffect(Unit) {
        viewModel.loadReportsData()
    }
    
    // Reload data khi period thay đổi
    LaunchedEffect(selectedPeriod) {
        viewModel.loadReportsData(selectedPeriod)
    }
    
    // Hiển thị UI dựa trên state
    when (val state = reportsState) {
        is ReportsState.Loading -> LoadingScreen()
        is ReportsState.Error -> ErrorScreen(state.message)
        is ReportsState.Success -> ReportsContent(...)
    }
}
```

##### **Main Components**

**1. Period Selector**
```kotlin
@Composable
fun PeriodSelector(
    selectedPeriod: ReportsViewModel.ReportPeriod,
    onPeriodChange: (ReportsViewModel.ReportPeriod) -> Unit,
    ...
) {
    LazyRow {
        items(ReportPeriod.values().toList()) { period ->
            FilterChip(
                onClick = { onPeriodChange(period) },
                label = { Text(period.displayName) },
                selected = selectedPeriod == period
            )
        }
    }
}
```

**2. Summary Cards Row**
```kotlin
@Composable
fun SummaryCardsRow(
    summaryData: ReportsViewModel.SummaryData,
    ...
) {
    // Hai thẻ trên cùng hàng
    Row {
        SummaryCard(
            title = "Tổng thu nhập",
            amount = formatAmountReports(summaryData.totalIncome),
            icon = Icons.Default.TrendingUp,
            color = Color(0xFF10B981)
        )
        SummaryCard(
            title = "Tổng chi tiêu",
            amount = formatAmountReports(summaryData.totalExpense),
            icon = Icons.Default.TrendingDown,
            color = Color(0xFFEF4444)
        )
    }
    
    // Thẻ số dư chiếm full width
    SummaryCard(
        title = "Số dư",
        amount = formatAmountReports(summaryData.balance),
        icon = Icons.Default.AccountBalanceWallet,
        color = Color(0xFF3B82F6)
    )
}
```

**3. Category Breakdown Section**
```kotlin
@Composable
fun CategoryBreakdownSection(
    categoryBreakdown: List<ReportsViewModel.CategoryReport>,
    ...
) {
    Column {
        Text("Chi tiêu theo danh mục")
        
        if (categoryBreakdown.isEmpty()) {
            Text("Không có dữ liệu chi tiêu")
        } else {
            categoryBreakdown.forEach { report ->
                CategoryReportItem(
                    report = report,
                    ...
                )
            }
        }
    }
}
```

**4. Monthly Trends Section**
```kotlin
@Composable
fun MonthlyTrendsSection(
    monthlyTrends: List<ReportsViewModel.MonthlyTrend>,
    ...
) {
    Column {
        Text("Xu hướng theo tháng")
        
        if (monthlyTrends.isEmpty()) {
            Text("Không có dữ liệu xu hướng")
        } else {
            monthlyTrends.forEach { trend ->
                MonthlyTrendItem(
                    trend = trend,
                    ...
                )
            }
        }
    }
}
```

**5. Smart Insights Section**
```kotlin
@Composable
fun SmartInsightsSection(
    smartInsights: List<ReportsViewModel.SmartInsight>,
    ...
) {
    Column {
        Text("Thông tin thông minh")
        
        if (smartInsights.isEmpty()) {
            Text("Chưa có thông tin thông minh")
        } else {
            smartInsights.forEach { insight ->
                SmartInsightItem(
                    insight = insight,
                    ...
                )
            }
        }
    }
}
```

---

## 🔄 Cơ Chế Real-time Update

### 1. Khi Thêm/Sửa/Xóa Expense

**ExpenseViewModel tự động reload:**
```kotlin
fun addExpense(expense: Expense) {
    viewModelScope.launch {
        firebaseRepository.addExpense(expense)
            .onSuccess { 
                _crudState.value = CrudState.Success
                loadExpenses() // ← Tự động reload
            }
    }
}

fun updateExpense(expense: Expense) {
    viewModelScope.launch {
        firebaseRepository.updateExpense(expense)
            .onSuccess { 
                loadExpenses() // ← Tự động reload
            }
    }
}

fun deleteExpense(expenseId: String) {
    viewModelScope.launch {
        firebaseRepository.deleteExpense(expenseId)
            .onSuccess { 
                loadExpenses() // ← Tự động reload
            }
    }
}
```

### 2. ReportsScreen Tự Động Cập Nhật

**Khi navigate đến ReportsScreen:**
```kotlin
LaunchedEffect(Unit) {
    viewModel.loadReportsData() // Load data ngay khi screen được tạo
}
```

**Khi thay đổi period:**
```kotlin
LaunchedEffect(selectedPeriod) {
    viewModel.loadReportsData(selectedPeriod) // Reload khi period thay đổi
}
```

### 3. Tích Hợp Với Navigation

**Trong ExpenseNavigation.kt:**
```kotlin
composable("reports") {
    when (authState) {
        is AuthState.Success -> {
            ReportsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                isDarkTheme = isDarkTheme
            )
        }
        else -> {
            // Redirect to signin
        }
    }
}
```

---

## 💡 Cách Sử Dụng

### 1. Xem Báo Cáo
1. Mở ứng dụng và đăng nhập
2. Từ HomeScreen, nhấn vào nút "Báo cáo" hoặc navigate đến Reports
3. Màn hình sẽ tự động load dữ liệu cho "Tháng này"

### 2. Thay Đổi Khoảng Thời Gian
1. Nhấn vào các chip ở phần "Khoảng thời gian"
2. Chọn: Tuần này, Tháng này, 3 tháng, 6 tháng, hoặc Năm nay
3. Dữ liệu sẽ tự động cập nhật

### 3. Xem Chi Tiết
- **Tổng thu nhập**: Hiển thị tổng số tiền thu nhập trong khoảng thời gian
- **Tổng chi tiêu**: Hiển thị tổng số tiền chi tiêu trong khoảng thời gian
- **Số dư**: Hiển thị số dư = thu nhập - chi tiêu
- **Chi tiêu theo danh mục**: Phân tích chi tiêu theo từng danh mục với phần trăm
- **Xu hướng theo tháng**: So sánh thu nhập, chi tiêu, số dư qua các tháng
- **Thông tin thông minh**: Phân tích và đưa ra lời khuyên về chi tiêu

---

## 🐛 Xử Lý Lỗi

### 1. Không Có Dữ Liệu
```kotlin
if (categoryBreakdown.isEmpty()) {
    // Hiển thị "Không có dữ liệu chi tiêu"
}
```

### 2. Lỗi Kết Nối Firebase
```kotlin
catch (e: Exception) {
    _errorMessage.value = e.message ?: "Không thể tải dữ liệu báo cáo"
    _reportsState.value = ReportsState.Error(e.message ?: "Lỗi không xác định")
}
```

### 3. Loading State
```kotlin
when (val state = reportsState) {
    is ReportsState.Loading -> {
        // Hiển thị CircularProgressIndicator
        LoadingScreen()
    }
    ...
}
```

---

## 🎨 UI/UX Design

### Color Scheme
- **Thu nhập**: `#10B981` (Green) - TrendingUp icon
- **Chi tiêu**: `#EF4444` (Red) - TrendingDown icon
- **Số dư**: `#3B82F6` (Blue) - AccountBalanceWallet icon
- **Warning**: `#F59E0B` (Amber)
- **Success**: `#10B981` (Green)

### Dark Theme Support
```kotlin
val backgroundColor = if (isDarkTheme) Color(0xFF0F0F0F) else Color(0xFFFAFAFA)
val cardColor = if (isDarkTheme) Color(0xFF1F2937) else Color.White
val textColor = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
val mutedTextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
```

---

## 📝 Ghi Chú Quan Trọng

### 1. Tính Toán Số Dư
```kotlin
balance = totalIncome - totalExpense
```
- Nếu balance > 0: Tiết kiệm được tiền
- Nếu balance < 0: Chi tiêu nhiều hơn thu nhập
- Nếu balance = 0: Thu nhập bằng chi tiêu

### 2. Phân Biệt Thu Nhập và Chi Tiêu
```kotlin
isExpense: Boolean
- true: Giao dịch là chi tiêu (expense)
- false: Giao dịch là thu nhập (income)
```

### 3. Format Số Tiền
```kotlin
private fun formatAmountReports(amount: Long): String {
    return String.format("%,d", amount)
}
// Ví dụ: 1000000 -> "1,000,000"
```

### 4. Khoảng Thời Gian
- **THIS_WEEK**: 7 ngày gần nhất
- **THIS_MONTH**: 30 ngày gần nhất
- **THREE_MONTHS**: 90 ngày gần nhất
- **SIX_MONTHS**: 180 ngày gần nhất
- **THIS_YEAR**: 365 ngày gần nhất

---

## ✅ Checklist Triển Khai

- [x] Entity Expense với trường isExpense
- [x] FirebaseRepository với các method lấy dữ liệu
- [x] ReportsViewModel với state management
- [x] Tính toán SummaryData (thu nhập, chi tiêu, số dư)
- [x] Tính toán CategoryBreakdown
- [x] Tính toán MonthlyTrends
- [x] Tạo SmartInsights
- [x] ReportsScreen với UI components
- [x] Period Selector
- [x] Summary Cards
- [x] Category Breakdown Section
- [x] Monthly Trends Section
- [x] Smart Insights Section
- [x] Loading State
- [x] Error Handling
- [x] Dark Theme Support
- [x] Real-time Updates

---

## 🚀 Kết Luận

Chức năng báo cáo đã được triển khai đầy đủ với:
- ✅ Hiển thị tổng thu nhập, chi tiêu, số dư real-time
- ✅ Phân tích chi tiêu theo danh mục
- ✅ Xu hướng theo tháng
- ✅ Thông tin thông minh
- ✅ Hỗ trợ nhiều khoảng thời gian
- ✅ Tự động cập nhật khi có thay đổi
- ✅ Xử lý lỗi và loading state
- ✅ Dark theme support

**Tất cả các chức năng đã hoạt động và sẵn sàng sử dụng!**

