package com.example.expensetracker.data.repository

import com.example.expensetracker.data.entity.Expense
import com.example.expensetracker.data.entity.Category
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository để quản lý dữ liệu với Firestore
 * Cung cấp các operations CRUD cho Expense và Category
 */
@Singleton
class FirebaseRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    // ==================== EXPENSE OPERATIONS ====================
    
    /**
     * Thêm expense mới vào Firestore
     */
    suspend fun addExpense(expense: Expense): Result<String> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Result.failure(Exception("User not authenticated"))
            } else {
                val expenseWithUserId = expense.copy(
                    userId = currentUser.uid,
                    createdAt = java.util.Date(),
                    updatedAt = java.util.Date()
                )
                val docRef = firestore.collection("expenses").add(expenseWithUserId).await()
                Result.success(docRef.id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Lấy danh sách expenses của user hiện tại
     */
    suspend fun getExpenses(): Result<List<Expense>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Result.failure(Exception("User not authenticated"))
            } else {
                val snapshot = firestore.collection("expenses")
                    .whereEqualTo("userId", currentUser.uid)
                    .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
                
                val expenses = snapshot.toObjects(Expense::class.java)
                Result.success(expenses)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Lấy expense theo ID
     */
    suspend fun getExpenseById(expenseId: String): Result<Expense> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Result.failure(Exception("User not authenticated"))
            } else {
                val document = firestore.collection("expenses").document(expenseId).get().await()
                if (document.exists()) {
                    val expense = document.toObject(Expense::class.java)
                    if (expense != null && expense.userId == currentUser.uid) {
                        Result.success(expense)
                    } else {
                        Result.failure(Exception("Expense not found or access denied"))
                    }
                } else {
                    Result.failure(Exception("Expense not found"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Lấy expenses theo category
     */
    suspend fun getExpensesByCategory(category: String): Result<List<Expense>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Result.failure(Exception("User not authenticated"))
            } else {
                val snapshot = firestore.collection("expenses")
                    .whereEqualTo("userId", currentUser.uid)
                    .whereEqualTo("category", category)
                    .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
                
                val expenses = snapshot.toObjects(Expense::class.java)
                Result.success(expenses)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Lấy expenses theo khoảng thời gian
     */
    suspend fun getExpensesByDateRange(startDate: java.util.Date, endDate: java.util.Date): Result<List<Expense>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Result.failure(Exception("User not authenticated"))
            } else {
                val snapshot = firestore.collection("expenses")
                    .whereEqualTo("userId", currentUser.uid)
                    .whereGreaterThanOrEqualTo("date", startDate)
                    .whereLessThanOrEqualTo("date", endDate)
                    .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
                
                val expenses = snapshot.toObjects(Expense::class.java)
                Result.success(expenses)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cập nhật expense
     */
    suspend fun updateExpense(expense: Expense): Result<Unit> {
        return try {
            val updatedExpense = expense.copy(updatedAt = java.util.Date())
            firestore.collection("expenses").document(expense.id).set(updatedExpense).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Xóa expense
     */
    suspend fun deleteExpense(expenseId: String): Result<Unit> {
        return try {
            firestore.collection("expenses").document(expenseId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== CATEGORY OPERATIONS ====================
    
    /**
     * Thêm category mới vào Firestore
     */
    suspend fun addCategory(category: Category): Result<String> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Result.failure(Exception("User not authenticated"))
            } else {
                // Chỉ set userId nếu chưa có (để không ghi đè từ DefaultCategoryService)
                val categoryWithUserId = if (category.userId.isBlank()) {
                    category.copy(
                        userId = currentUser.uid,
                        createdAt = java.util.Date()
                    )
                } else {
                    category.copy(createdAt = java.util.Date())
                }
                
                // Sử dụng .set() với custom ID thay vì .add()
                if (category.id.isNotBlank()) {
                    firestore.collection("categories").document(category.id).set(categoryWithUserId).await()
                    Result.success(category.id)
                } else {
                    val docRef = firestore.collection("categories").add(categoryWithUserId).await()
                    Result.success(docRef.id)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Lấy danh sách categories của user hiện tại
     */
    suspend fun getCategories(): Result<List<Category>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Result.failure(Exception("User not authenticated"))
            } else {
                val snapshot = firestore.collection("categories")
                    .whereEqualTo("userId", currentUser.uid)
                    .orderBy("name")
                    .get()
                    .await()
                
                val categories = snapshot.toObjects(Category::class.java)
                Result.success(categories)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cập nhật category
     */
    suspend fun updateCategory(category: Category): Result<Unit> {
        return try {
            firestore.collection("categories").document(category.id).set(category).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Xóa category
     */
    suspend fun deleteCategory(categoryId: String): Result<Unit> {
        return try {
            firestore.collection("categories").document(categoryId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== REPORTS OPERATIONS ====================
    
    /**
     * Lấy expenses theo tháng cụ thể
     */
    suspend fun getExpensesByMonth(year: Int, month: Int): Result<List<Expense>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Result.failure(Exception("User not authenticated"))
            } else {
                val calendar = java.util.Calendar.getInstance()
                
                // Ngày đầu tháng
                calendar.set(year, month, 1, 0, 0, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val startDate = calendar.time
                
                // Ngày cuối tháng
                calendar.add(java.util.Calendar.MONTH, 1)
                calendar.add(java.util.Calendar.DAY_OF_MONTH, -1)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
                calendar.set(java.util.Calendar.MINUTE, 59)
                calendar.set(java.util.Calendar.SECOND, 59)
                calendar.set(java.util.Calendar.MILLISECOND, 999)
                val endDate = calendar.time
                
                val snapshot = firestore.collection("expenses")
                    .whereEqualTo("userId", currentUser.uid)
                    .whereGreaterThanOrEqualTo("date", startDate)
                    .whereLessThanOrEqualTo("date", endDate)
                    .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
                
                val expenses = snapshot.toObjects(Expense::class.java)
                Result.success(expenses)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Lấy tổng thu nhập trong khoảng thời gian
     */
    suspend fun getTotalIncome(startDate: java.util.Date, endDate: java.util.Date): Result<Long> {
        return try {
            val expenses = getExpensesByDateRange(startDate, endDate).getOrThrow()
            val totalIncome = expenses.filter { !it.isExpense }.sumOf { it.amount }
            Result.success(totalIncome)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Lấy tổng chi tiêu trong khoảng thời gian
     */
    suspend fun getTotalExpense(startDate: java.util.Date, endDate: java.util.Date): Result<Long> {
        return try {
            val expenses = getExpensesByDateRange(startDate, endDate).getOrThrow()
            val totalExpense = expenses.filter { it.isExpense }.sumOf { it.amount }
            Result.success(totalExpense)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Lấy chi tiêu theo danh mục trong khoảng thời gian
     */
    suspend fun getExpensesByCategoryInRange(
        category: String, 
        startDate: java.util.Date, 
        endDate: java.util.Date
    ): Result<List<Expense>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Result.failure(Exception("User not authenticated"))
            } else {
                val snapshot = firestore.collection("expenses")
                    .whereEqualTo("userId", currentUser.uid)
                    .whereEqualTo("category", category)
                    .whereEqualTo("isExpense", true)
                    .whereGreaterThanOrEqualTo("date", startDate)
                    .whereLessThanOrEqualTo("date", endDate)
                    .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
                
                val expenses = snapshot.toObjects(Expense::class.java)
                Result.success(expenses)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Lấy danh sách các danh mục có chi tiêu trong khoảng thời gian
     */
    suspend fun getCategoriesWithExpenses(startDate: java.util.Date, endDate: java.util.Date): Result<List<String>> {
        return try {
            val expenses = getExpensesByDateRange(startDate, endDate).getOrThrow()
            val categories = expenses
                .filter { it.isExpense }
                .map { it.category }
                .distinct()
                .sorted()
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Kiểm tra user có đăng nhập không
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
    
    /**
     * Lấy UID của user hiện tại
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
}
