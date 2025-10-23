package com.example.expensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.entity.Expense
import com.example.expensetracker.data.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * ViewModel để quản lý state và business logic cho Reports operations
 */
@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    // ==================== STATE MANAGEMENT ====================
    
    private val _reportsState = MutableStateFlow<ReportsState>(ReportsState.Loading)
    val reportsState: StateFlow<ReportsState> = _reportsState.asStateFlow()
    
    private val _selectedPeriod = MutableStateFlow(ReportPeriod.THIS_MONTH)
    val selectedPeriod: StateFlow<ReportPeriod> = _selectedPeriod.asStateFlow()
    
    private val _summaryData = MutableStateFlow<SummaryData?>(null)
    val summaryData: StateFlow<SummaryData?> = _summaryData.asStateFlow()
    
    private val _categoryBreakdown = MutableStateFlow<List<CategoryReport>>(emptyList())
    val categoryBreakdown: StateFlow<List<CategoryReport>> = _categoryBreakdown.asStateFlow()
    
    private val _monthlyTrends = MutableStateFlow<List<MonthlyTrend>>(emptyList())
    val monthlyTrends: StateFlow<List<MonthlyTrend>> = _monthlyTrends.asStateFlow()
    
    private val _smartInsights = MutableStateFlow<List<SmartInsight>>(emptyList())
    val smartInsights: StateFlow<List<SmartInsight>> = _smartInsights.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // ==================== DATA MODELS ====================
    
    data class SummaryData(
        val totalIncome: Long,
        val totalExpense: Long,
        val balance: Long,
        val period: ReportPeriod
    )
    
    data class CategoryReport(
        val categoryName: String,
        val amount: Long,
        val percentage: Float,
        val color: String,
        val icon: String
    )
    
    data class MonthlyTrend(
        val month: String,
        val year: Int,
        val income: Long,
        val expense: Long,
        val balance: Long
    )
    
    data class SmartInsight(
        val type: InsightType,
        val title: String,
        val description: String,
        val icon: String,
        val color: String
    )
    
    enum class ReportPeriod(val displayName: String, val days: Int) {
        THIS_WEEK("Tuần này", 7),
        THIS_MONTH("Tháng này", 30),
        THREE_MONTHS("3 tháng", 90),
        SIX_MONTHS("6 tháng", 180),
        THIS_YEAR("Năm nay", 365)
    }
    
    enum class InsightType {
        EXPENSE_INCREASE,
        EXPENSE_DECREASE,
        HIGH_CATEGORY_SPENDING,
        SAVINGS_GOAL,
        BUDGET_WARNING,
        POSITIVE_TREND
    }
    
    // ==================== BUSINESS LOGIC ====================
    
    /**
     * Load reports data cho period được chọn
     */
    fun loadReportsData(period: ReportPeriod = _selectedPeriod.value) {
        _selectedPeriod.value = period
        _isLoading.value = true
        _errorMessage.value = null
        _reportsState.value = ReportsState.Loading
        
        viewModelScope.launch {
            try {
                val dateRange = getDateRangeForPeriod(period)
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
    
    /**
     * Tính toán dữ liệu tổng quan (thu nhập, chi tiêu, số dư)
     */
    private fun calculateSummaryData(expenses: List<Expense>, period: ReportPeriod) {
        val income = expenses.filter { !it.isExpense }.sumOf { it.amount }
        val expense = expenses.filter { it.isExpense }.sumOf { it.amount }
        val balance = income - expense
        
        _summaryData.value = SummaryData(income, expense, balance, period)
    }
    
    /**
     * Tính toán chi tiêu theo danh mục
     */
    private fun calculateCategoryBreakdown(expenses: List<Expense>) {
        val expenseTransactions = expenses.filter { it.isExpense }
        val totalExpense = expenseTransactions.sumOf { it.amount }
        
        if (totalExpense == 0L) {
            _categoryBreakdown.value = emptyList()
            return
        }
        
        val categoryMap = expenseTransactions.groupBy { it.category }
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
    
    /**
     * Tính toán xu hướng theo tháng
     */
    private fun calculateMonthlyTrends(period: ReportPeriod) {
        viewModelScope.launch {
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
                            month = getMonthName(month.second),
                            year = month.second,
                            income = income,
                            expense = expense,
                            balance = balance
                        )
                    )
                }
                
                _monthlyTrends.value = trends
            } catch (e: Exception) {
                // Handle error silently for trends
                _monthlyTrends.value = emptyList()
            }
        }
    }
    
    /**
     * Tạo thông tin thông minh
     */
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
    
    /**
     * Phân tích xu hướng chi tiêu
     */
    private fun analyzeExpenseTrend(expenses: List<Expense>, period: ReportPeriod): SmartInsight? {
        val expenseTransactions = expenses.filter { it.isExpense }
        if (expenseTransactions.isEmpty()) return null
        
        val totalExpense = expenseTransactions.sumOf { it.amount }
        val avgExpensePerDay = totalExpense.toFloat() / period.days
        
        return when {
            avgExpensePerDay > 100000 -> {
                SmartInsight(
                    type = InsightType.BUDGET_WARNING,
                    title = "Chi tiêu cao",
                    description = "Trung bình ${formatAmount(avgExpensePerDay.toLong())}/ngày",
                    icon = "trending_up",
                    color = "#EF4444"
                )
            }
            avgExpensePerDay < 50000 -> {
                SmartInsight(
                    type = InsightType.POSITIVE_TREND,
                    title = "Chi tiêu hợp lý",
                    description = "Trung bình ${formatAmount(avgExpensePerDay.toLong())}/ngày",
                    icon = "trending_down",
                    color = "#10B981"
                )
            }
            else -> null
        }
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Lấy khoảng thời gian cho period
     */
    private fun getDateRangeForPeriod(period: ReportPeriod): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        
        calendar.add(Calendar.DAY_OF_YEAR, -period.days)
        val startDate = calendar.time
        
        return Pair(startDate, endDate)
    }
    
    /**
     * Lấy danh sách tháng cho period
     */
    private fun getMonthsForPeriod(period: ReportPeriod): List<Pair<Int, Int>> {
        val calendar = Calendar.getInstance()
        val months = mutableListOf<Pair<Int, Int>>()
        
        when (period) {
            ReportPeriod.THIS_WEEK -> {
                // Chỉ hiển thị tháng hiện tại
                months.add(Pair(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)))
            }
            ReportPeriod.THIS_MONTH -> {
                // Chỉ hiển thị tháng hiện tại
                months.add(Pair(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)))
            }
            ReportPeriod.THREE_MONTHS -> {
                // 3 tháng gần nhất
                for (i in 0..2) {
                    months.add(Pair(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)))
                    calendar.add(Calendar.MONTH, -1)
                }
            }
            ReportPeriod.SIX_MONTHS -> {
                // 6 tháng gần nhất
                for (i in 0..5) {
                    months.add(Pair(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)))
                    calendar.add(Calendar.MONTH, -1)
                }
            }
            ReportPeriod.THIS_YEAR -> {
                // 12 tháng của năm hiện tại
                calendar.set(Calendar.MONTH, 0) // Tháng 1
                for (i in 0..11) {
                    months.add(Pair(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)))
                    calendar.add(Calendar.MONTH, 1)
                }
            }
        }
        
        return months.reversed() // Sắp xếp từ cũ đến mới
    }
    
    /**
     * Lấy khoảng thời gian của tháng
     */
    private fun getMonthDateRange(month: Int, year: Int): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        
        // Ngày đầu tháng
        calendar.set(year, month, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.time
        
        // Ngày cuối tháng
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endDate = calendar.time
        
        return Pair(startDate, endDate)
    }
    
    /**
     * Lấy tên tháng
     */
    private fun getMonthName(month: Int): String {
        val monthNames = arrayOf(
            "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
            "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
        )
        
        return monthNames[month]
    }
    
    /**
     * Lấy màu cho category
     */
    private fun getCategoryColor(categoryName: String): String {
        return when (categoryName.lowercase()) {
            "ăn uống", "food", "restaurant" -> "#F59E0B"
            "giao thông", "transport", "car" -> "#3B82F6"
            "mua sắm", "shopping", "bag" -> "#8B5CF6"
            "giải trí", "entertainment", "movie" -> "#EC4899"
            "sức khỏe", "health", "medical" -> "#10B981"
            "học tập", "education", "school" -> "#06B6D4"
            "du lịch", "travel", "flight" -> "#84CC16"
            "khác", "other", "misc" -> "#6B7280"
            else -> "#6B7280"
        }
    }
    
    /**
     * Lấy icon cho category
     */
    private fun getCategoryIcon(categoryName: String): String {
        return when (categoryName.lowercase()) {
            "ăn uống", "food", "restaurant" -> "restaurant"
            "giao thông", "transport", "car" -> "directions_car"
            "mua sắm", "shopping", "bag" -> "shopping_bag"
            "giải trí", "entertainment", "movie" -> "movie"
            "sức khỏe", "health", "medical" -> "local_hospital"
            "học tập", "education", "school" -> "school"
            "du lịch", "travel", "flight" -> "flight"
            "khác", "other", "misc" -> "category"
            else -> "category"
        }
    }
    
    /**
     * Format số tiền
     */
    private fun formatAmount(amount: Long): String {
        return String.format("%,d", amount)
    }
    
    /**
     * Xóa error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Refresh data
     */
    fun refreshData() {
        loadReportsData(_selectedPeriod.value)
    }
}

/**
 * Sealed class để quản lý các trạng thái của Reports operations
 */
sealed class ReportsState {
    object Loading : ReportsState()
    object Success : ReportsState()
    data class Error(val message: String) : ReportsState()
}
