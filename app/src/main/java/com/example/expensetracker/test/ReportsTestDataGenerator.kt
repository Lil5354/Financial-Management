package com.example.expensetracker.test

import com.example.expensetracker.data.entity.Expense
import com.example.expensetracker.data.entity.Category
import java.util.*

/**
 * Test data generator cho chức năng báo cáo
 * Sử dụng để tạo dữ liệu mẫu khi test
 */
object ReportsTestDataGenerator {
    
    /**
     * Tạo danh sách categories mẫu
     */
    fun generateSampleCategories(userId: String): List<Category> {
        return listOf(
            Category(
                id = "cat_food_$userId",
                userId = userId,
                name = "Ăn uống",
                icon = "restaurant",
                color = "#F59E0B",
                isDefault = true,
                createdAt = Date()
            ),
            Category(
                id = "cat_transport_$userId",
                userId = userId,
                name = "Giao thông",
                icon = "directions_car",
                color = "#3B82F6",
                isDefault = true,
                createdAt = Date()
            ),
            Category(
                id = "cat_shopping_$userId",
                userId = userId,
                name = "Mua sắm",
                icon = "shopping_bag",
                color = "#8B5CF6",
                isDefault = true,
                createdAt = Date()
            ),
            Category(
                id = "cat_entertainment_$userId",
                userId = userId,
                name = "Giải trí",
                icon = "movie",
                color = "#EC4899",
                isDefault = true,
                createdAt = Date()
            ),
            Category(
                id = "cat_health_$userId",
                userId = userId,
                name = "Sức khỏe",
                icon = "local_hospital",
                color = "#10B981",
                isDefault = true,
                createdAt = Date()
            ),
            Category(
                id = "cat_income_$userId",
                userId = userId,
                name = "Thu nhập",
                icon = "account_balance_wallet",
                color = "#10B981",
                isDefault = true,
                createdAt = Date()
            )
        )
    }
    
    /**
     * Tạo danh sách expenses mẫu cho tháng hiện tại
     */
    fun generateSampleExpensesForCurrentMonth(userId: String): List<Expense> {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        return listOf(
            // Thu nhập
            Expense(
                id = "exp_income_1_$userId",
                userId = userId,
                title = "Lương tháng",
                amount = 5000000L,
                category = "Thu nhập",
                date = getDate(currentYear, currentMonth, 1),
                note = "Lương tháng ${currentMonth + 1}",
                isExpense = false,
                createdAt = Date(),
                updatedAt = Date()
            ),
            
            // Chi tiêu ăn uống
            Expense(
                id = "exp_food_1_$userId",
                userId = userId,
                title = "Cà phê sáng",
                amount = 25000L,
                category = "Ăn uống",
                date = getDate(currentYear, currentMonth, 2),
                note = "Cà phê tại quán gần nhà",
                isExpense = true,
                createdAt = Date(),
                updatedAt = Date()
            ),
            Expense(
                id = "exp_food_2_$userId",
                userId = userId,
                title = "Ăn trưa",
                amount = 80000L,
                category = "Ăn uống",
                date = getDate(currentYear, currentMonth, 3),
                note = "Cơm trưa tại văn phòng",
                isExpense = true,
                createdAt = Date(),
                updatedAt = Date()
            ),
            Expense(
                id = "exp_food_3_$userId",
                userId = userId,
                title = "Ăn tối",
                amount = 120000L,
                category = "Ăn uống",
                date = getDate(currentYear, currentMonth, 4),
                note = "Ăn tối với gia đình",
                isExpense = true,
                createdAt = Date(),
                updatedAt = Date()
            ),
            
            // Chi tiêu giao thông
            Expense(
                id = "exp_transport_1_$userId",
                userId = userId,
                title = "Xăng xe",
                amount = 150000L,
                category = "Giao thông",
                date = getDate(currentYear, currentMonth, 5),
                note = "Đổ xăng cho xe máy",
                isExpense = true,
                createdAt = Date(),
                updatedAt = Date()
            ),
            Expense(
                id = "exp_transport_2_$userId",
                userId = userId,
                title = "Grab",
                amount = 45000L,
                category = "Giao thông",
                date = getDate(currentYear, currentMonth, 6),
                note = "Đi taxi về nhà",
                isExpense = true,
                createdAt = Date(),
                updatedAt = Date()
            ),
            
            // Chi tiêu mua sắm
            Expense(
                id = "exp_shopping_1_$userId",
                userId = userId,
                title = "Mua quần áo",
                amount = 300000L,
                category = "Mua sắm",
                date = getDate(currentYear, currentMonth, 7),
                note = "Mua áo sơ mi mới",
                isExpense = true,
                createdAt = Date(),
                updatedAt = Date()
            ),
            Expense(
                id = "exp_shopping_2_$userId",
                userId = userId,
                title = "Mua sách",
                amount = 150000L,
                category = "Mua sắm",
                date = getDate(currentYear, currentMonth, 8),
                note = "Mua sách lập trình",
                isExpense = true,
                createdAt = Date(),
                updatedAt = Date()
            ),
            
            // Chi tiêu giải trí
            Expense(
                id = "exp_entertainment_1_$userId",
                userId = userId,
                title = "Xem phim",
                amount = 200000L,
                category = "Giải trí",
                date = getDate(currentYear, currentMonth, 9),
                note = "Xem phim tại rạp",
                isExpense = true,
                createdAt = Date(),
                updatedAt = Date()
            ),
            
            // Chi tiêu sức khỏe
            Expense(
                id = "exp_health_1_$userId",
                userId = userId,
                title = "Khám bệnh",
                amount = 500000L,
                category = "Sức khỏe",
                date = getDate(currentYear, currentMonth, 10),
                note = "Khám định kỳ",
                isExpense = true,
                createdAt = Date(),
                updatedAt = Date()
            )
        )
    }
    
    /**
     * Tạo danh sách expenses mẫu cho các tháng trước
     */
    fun generateSampleExpensesForPreviousMonths(userId: String): List<Expense> {
        val calendar = Calendar.getInstance()
        val expenses = mutableListOf<Expense>()
        
        // Tạo dữ liệu cho 6 tháng trước
        for (monthOffset in 1..6) {
            calendar.add(Calendar.MONTH, -monthOffset)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            
            // Thu nhập
            expenses.add(
                Expense(
                    id = "exp_income_${monthOffset}_$userId",
                    userId = userId,
                    title = "Lương tháng",
                    amount = (4500000L + (monthOffset * 100000L)), // Thu nhập tăng dần
                    category = "Thu nhập",
                    date = getDate(year, month, 1),
                    note = "Lương tháng ${month + 1}",
                    isExpense = false,
                    createdAt = Date(),
                    updatedAt = Date()
                )
            )
            
            // Chi tiêu ăn uống (35% tổng chi tiêu)
            expenses.add(
                Expense(
                    id = "exp_food_${monthOffset}_$userId",
                    userId = userId,
                    title = "Ăn uống tháng",
                    amount = (1200000L + (monthOffset * 50000L)),
                    category = "Ăn uống",
                    date = getDate(year, month, 15),
                    note = "Chi tiêu ăn uống tháng ${month + 1}",
                    isExpense = true,
                    createdAt = Date(),
                    updatedAt = Date()
                )
            )
            
            // Chi tiêu giao thông (25% tổng chi tiêu)
            expenses.add(
                Expense(
                    id = "exp_transport_${monthOffset}_$userId",
                    userId = userId,
                    title = "Giao thông tháng",
                    amount = (800000L + (monthOffset * 30000L)),
                    category = "Giao thông",
                    date = getDate(year, month, 20),
                    note = "Chi tiêu giao thông tháng ${month + 1}",
                    isExpense = true,
                    createdAt = Date(),
                    updatedAt = Date()
                )
            )
            
            // Chi tiêu mua sắm (20% tổng chi tiêu)
            expenses.add(
                Expense(
                    id = "exp_shopping_${monthOffset}_$userId",
                    userId = userId,
                    title = "Mua sắm tháng",
                    amount = (600000L + (monthOffset * 20000L)),
                    category = "Mua sắm",
                    date = getDate(year, month, 25),
                    note = "Chi tiêu mua sắm tháng ${month + 1}",
                    isExpense = true,
                    createdAt = Date(),
                    updatedAt = Date()
                )
            )
            
            // Chi tiêu giải trí (15% tổng chi tiêu)
            expenses.add(
                Expense(
                    id = "exp_entertainment_${monthOffset}_$userId",
                    userId = userId,
                    title = "Giải trí tháng",
                    amount = (400000L + (monthOffset * 15000L)),
                    category = "Giải trí",
                    date = getDate(year, month, 28),
                    note = "Chi tiêu giải trí tháng ${month + 1}",
                    isExpense = true,
                    createdAt = Date(),
                    updatedAt = Date()
                )
            )
            
            // Chi tiêu sức khỏe (5% tổng chi tiêu)
            expenses.add(
                Expense(
                    id = "exp_health_${monthOffset}_$userId",
                    userId = userId,
                    title = "Sức khỏe tháng",
                    amount = (200000L + (monthOffset * 10000L)),
                    category = "Sức khỏe",
                    date = getDate(year, month, 30),
                    note = "Chi tiêu sức khỏe tháng ${month + 1}",
                    isExpense = true,
                    createdAt = Date(),
                    updatedAt = Date()
                )
            )
            
            calendar.add(Calendar.MONTH, monthOffset) // Reset calendar
        }
        
        return expenses
    }
    
    /**
     * Tạo tất cả dữ liệu mẫu
     */
    fun generateAllSampleData(userId: String): Pair<List<Category>, List<Expense>> {
        val categories = generateSampleCategories(userId)
        val currentMonthExpenses = generateSampleExpensesForCurrentMonth(userId)
        val previousMonthsExpenses = generateSampleExpensesForPreviousMonths(userId)
        
        val allExpenses = currentMonthExpenses + previousMonthsExpenses
        
        return Pair(categories, allExpenses)
    }
    
    /**
     * Helper function để tạo Date
     */
    private fun getDate(year: Int, month: Int, day: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, 12, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }
    
    /**
     * Tính toán kết quả mong đợi cho test
     */
    fun getExpectedResultsForCurrentMonth(): Map<String, Any> {
        return mapOf(
            "totalIncome" to 5000000L,
            "totalExpense" to 1380000L, // 25k + 80k + 120k + 150k + 45k + 300k + 150k + 200k + 500k
            "balance" to 3620000L,
            "categoryBreakdown" to mapOf(
                "Ăn uống" to mapOf("amount" to 225000L, "percentage" to 16.3f),
                "Giao thông" to mapOf("amount" to 195000L, "percentage" to 14.1f),
                "Mua sắm" to mapOf("amount" to 450000L, "percentage" to 32.6f),
                "Giải trí" to mapOf("amount" to 200000L, "percentage" to 14.5f),
                "Sức khỏe" to mapOf("amount" to 500000L, "percentage" to 36.2f)
            )
        )
    }
}




