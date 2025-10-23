package com.example.expensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.entity.Category
import com.example.expensetracker.data.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel để quản lý state và business logic cho Category operations
 */
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    // ==================== STATE MANAGEMENT ====================
    
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _crudState = MutableStateFlow<CrudState>(CrudState.Idle)
    val crudState: StateFlow<CrudState> = _crudState.asStateFlow()
    
    // ==================== CATEGORY OPERATIONS ====================
    
    /**
     * Load tất cả categories của user hiện tại
     */
    fun loadCategories() {
        _isLoading.value = true
        _errorMessage.value = null
        
        viewModelScope.launch {
            firebaseRepository.getCategories()
                .onSuccess { categoryList ->
                    if (categoryList.isEmpty()) {
                        // Nếu không có categories, tạo default categories
                        createDefaultCategoriesIfNeeded()
                    } else {
                        _categories.value = categoryList
                    }
                }
                .onFailure { error ->
                    // Fallback: sử dụng default categories nếu Firebase fail
                    _categories.value = getDefaultCategories()
                    _errorMessage.value = "Sử dụng danh mục mặc định"
                }
            _isLoading.value = false
        }
    }
    
    /**
     * Thêm category mới
     */
    fun addCategory(category: Category) {
        _isLoading.value = true
        _errorMessage.value = null
        _crudState.value = CrudState.Loading
        
        viewModelScope.launch {
            firebaseRepository.addCategory(category)
                .onSuccess { categoryId ->
                    _crudState.value = CrudState.Success
                    // Reload categories để cập nhật UI
                    loadCategories()
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Không thể thêm danh mục"
                    _crudState.value = CrudState.Error(error.message ?: "Lỗi không xác định")
                }
            _isLoading.value = false
        }
    }
    
    /**
     * Cập nhật category
     */
    fun updateCategory(category: Category) {
        _isLoading.value = true
        _errorMessage.value = null
        _crudState.value = CrudState.Loading
        
        viewModelScope.launch {
            firebaseRepository.updateCategory(category)
                .onSuccess {
                    _crudState.value = CrudState.Success
                    // Reload categories để cập nhật UI
                    loadCategories()
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Không thể cập nhật danh mục"
                    _crudState.value = CrudState.Error(error.message ?: "Lỗi không xác định")
                }
            _isLoading.value = false
        }
    }
    
    /**
     * Xóa category
     */
    fun deleteCategory(categoryId: String) {
        _isLoading.value = true
        _errorMessage.value = null
        _crudState.value = CrudState.Loading
        
        viewModelScope.launch {
            firebaseRepository.deleteCategory(categoryId)
                .onSuccess {
                    _crudState.value = CrudState.Success
                    // Reload categories để cập nhật UI
                    loadCategories()
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Không thể xóa danh mục"
                    _crudState.value = CrudState.Error(error.message ?: "Lỗi không xác định")
                }
            _isLoading.value = false
        }
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Xóa error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Reset CRUD state
     */
    fun resetCrudState() {
        _crudState.value = CrudState.Idle
    }
    
    
    /**
     * Kiểm tra category có tồn tại không
     */
    fun categoryExists(categoryName: String): Boolean {
        return _categories.value.any { it.name.equals(categoryName, ignoreCase = true) }
    }
    
    /**
     * Lấy category theo tên
     */
    fun getCategoryByName(name: String): Category? {
        return _categories.value.find { it.name.equals(name, ignoreCase = true) }
    }
    
    /**
     * Tạo default categories nếu cần thiết
     */
    private fun createDefaultCategoriesIfNeeded() {
        val currentUserId = firebaseRepository.getCurrentUserId()
        if (currentUserId != null) {
            val defaultCategories = getDefaultCategories()
            _categories.value = defaultCategories
        }
    }
    
    /**
     * Lấy danh sách default categories
     */
    private fun getDefaultCategories(): List<Category> {
        val currentUserId = firebaseRepository.getCurrentUserId() ?: return emptyList()
        
        return listOf(
            Category(
                id = "default_food_${currentUserId}",
                userId = currentUserId,
                name = "Ăn uống",
                icon = "restaurant",
                color = "#F59E0B",
                isDefault = true
            ),
            Category(
                id = "default_transport_${currentUserId}",
                userId = currentUserId,
                name = "Giao thông",
                icon = "directions_car",
                color = "#3B82F6",
                isDefault = true
            ),
            Category(
                id = "default_shopping_${currentUserId}",
                userId = currentUserId,
                name = "Mua sắm",
                icon = "shopping_bag",
                color = "#8B5CF6",
                isDefault = true
            ),
            Category(
                id = "default_entertainment_${currentUserId}",
                userId = currentUserId,
                name = "Giải trí",
                icon = "movie",
                color = "#EC4899",
                isDefault = true
            ),
            Category(
                id = "default_health_${currentUserId}",
                userId = currentUserId,
                name = "Sức khỏe",
                icon = "local_hospital",
                color = "#10B981",
                isDefault = true
            ),
            Category(
                id = "default_education_${currentUserId}",
                userId = currentUserId,
                name = "Giáo dục",
                icon = "school",
                color = "#06B6D4",
                isDefault = true
            ),
            Category(
                id = "default_income_${currentUserId}",
                userId = currentUserId,
                name = "Thu nhập",
                icon = "account_balance_wallet",
                color = "#10B981",
                isDefault = true
            ),
            Category(
                id = "default_other_${currentUserId}",
                userId = currentUserId,
                name = "Khác",
                icon = "category",
                color = "#6B7280",
                isDefault = true
            )
        )
    }
}
