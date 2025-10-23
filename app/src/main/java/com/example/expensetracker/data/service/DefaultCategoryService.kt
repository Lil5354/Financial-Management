package com.example.expensetracker.data.service

import com.example.expensetracker.data.entity.Category
import com.example.expensetracker.data.repository.FirebaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service để quản lý default categories cho user mới
 */
@Singleton
class DefaultCategoryService @Inject constructor(
    private val firebaseRepository: FirebaseRepository
) {
    
    /**
     * Tạo default categories cho user mới
     */
    fun createDefaultCategoriesForNewUser() {
        val currentUserId = firebaseRepository.getCurrentUserId()
        if (currentUserId == null) {
            android.util.Log.e("DefaultCategoryService", "User not authenticated, cannot create default categories")
            return
        }
        
        val defaultCategories = getDefaultCategories()
        android.util.Log.d("DefaultCategoryService", "Creating ${defaultCategories.size} default categories for user: $currentUserId")
        
        CoroutineScope(Dispatchers.IO).launch {
            defaultCategories.forEach { category ->
                firebaseRepository.addCategory(category)
                    .onSuccess { categoryId ->
                        android.util.Log.d("DefaultCategoryService", "Successfully created category: ${category.name} with ID: $categoryId")
                    }
                    .onFailure { error ->
                        android.util.Log.e("DefaultCategoryService", "Failed to create category: ${category.name}", error)
                    }
            }
        }
    }
    
    /**
     * Lấy danh sách default categories
     */
    private fun getDefaultCategories(): List<Category> {
        val currentUserId = firebaseRepository.getCurrentUserId()
        
        // currentUserId đã được kiểm tra null ở createDefaultCategoriesForNewUser()
        // Nếu đến đây thì currentUserId chắc chắn không null
        val userId = currentUserId!!
        
        return listOf(
            Category(
                id = "default_food_${userId}",
                userId = userId,
                name = "Ăn uống",
                icon = "restaurant",
                color = "#F59E0B",
                isDefault = true
            ),
            Category(
                id = "default_transport_${userId}",
                userId = userId,
                name = "Giao thông",
                icon = "directions_car",
                color = "#3B82F6",
                isDefault = true
            ),
            Category(
                id = "default_shopping_${userId}",
                userId = userId,
                name = "Mua sắm",
                icon = "shopping_bag",
                color = "#8B5CF6",
                isDefault = true
            ),
            Category(
                id = "default_entertainment_${userId}",
                userId = userId,
                name = "Giải trí",
                icon = "movie",
                color = "#EC4899",
                isDefault = true
            ),
            Category(
                id = "default_health_${userId}",
                userId = userId,
                name = "Sức khỏe",
                icon = "local_hospital",
                color = "#10B981",
                isDefault = true
            ),
            Category(
                id = "default_education_${userId}",
                userId = userId,
                name = "Giáo dục",
                icon = "school",
                color = "#06B6D4",
                isDefault = true
            ),
            Category(
                id = "default_income_${userId}",
                userId = userId,
                name = "Thu nhập",
                icon = "account_balance_wallet",
                color = "#10B981",
                isDefault = true
            ),
            Category(
                id = "default_other_${userId}",
                userId = userId,
                name = "Khác",
                icon = "category",
                color = "#6B7280",
                isDefault = true
            )
        )
    }
}
