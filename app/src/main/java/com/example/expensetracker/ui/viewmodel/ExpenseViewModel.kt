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
 * ViewModel để quản lý state và business logic cho Expense operations
 */
@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    // ==================== STATE MANAGEMENT ====================
    
    private val _expenseState = MutableStateFlow<ExpenseState>(ExpenseState.Idle)
    val expenseState: StateFlow<ExpenseState> = _expenseState.asStateFlow()
    
    private val _crudState = MutableStateFlow<CrudState>(CrudState.Idle)
    val crudState: StateFlow<CrudState> = _crudState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow("Tất cả")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()
    
    private val _currentExpense = MutableStateFlow<Expense?>(null)
    val currentExpense: StateFlow<Expense?> = _currentExpense.asStateFlow()
    
    // ==================== EXPENSE OPERATIONS ====================
    
    /**
     * Load tất cả expenses của user hiện tại
     */
    fun loadExpenses() {
        _isLoading.value = true
        _errorMessage.value = null
        _expenseState.value = ExpenseState.Loading
        
        viewModelScope.launch {
            firebaseRepository.getExpenses()
                .onSuccess { expenseList ->
                    _expenses.value = expenseList
                    _expenseState.value = ExpenseState.Success(expenseList)
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Không thể tải danh sách chi tiêu"
                    _expenseState.value = ExpenseState.Error(error.message ?: "Lỗi không xác định")
                }
            _isLoading.value = false
        }
    }
    
    /**
     * Thêm expense mới
     */
    fun addExpense(expense: Expense) {
        _isLoading.value = true
        _errorMessage.value = null
        _crudState.value = CrudState.Loading
        
        viewModelScope.launch {
            firebaseRepository.addExpense(expense)
                .onSuccess { expenseId ->
                    _crudState.value = CrudState.Success
                    // Reload expenses để cập nhật UI
                    loadExpenses()
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Không thể thêm chi tiêu"
                    _crudState.value = CrudState.Error(error.message ?: "Lỗi không xác định")
                }
            _isLoading.value = false
        }
    }
    
    /**
     * Cập nhật expense
     */
    fun updateExpense(expense: Expense) {
        _isLoading.value = true
        _errorMessage.value = null
        _crudState.value = CrudState.Loading
        
        viewModelScope.launch {
            firebaseRepository.updateExpense(expense)
                .onSuccess {
                    _crudState.value = CrudState.Success
                    // Reload expenses để cập nhật UI
                    loadExpenses()
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Không thể cập nhật chi tiêu"
                    _crudState.value = CrudState.Error(error.message ?: "Lỗi không xác định")
                }
            _isLoading.value = false
        }
    }
    
    /**
     * Xóa expense
     */
    fun deleteExpense(expenseId: String) {
        _isLoading.value = true
        _errorMessage.value = null
        _crudState.value = CrudState.Loading
        
        viewModelScope.launch {
            firebaseRepository.deleteExpense(expenseId)
                .onSuccess {
                    _crudState.value = CrudState.Success
                    // Reload expenses để cập nhật UI
                    loadExpenses()
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Không thể xóa chi tiêu"
                    _crudState.value = CrudState.Error(error.message ?: "Lỗi không xác định")
                }
            _isLoading.value = false
        }
    }
    
    /**
     * Lấy expense theo ID
     */
    fun getExpenseById(expenseId: String) {
        _isLoading.value = true
        _errorMessage.value = null
        _currentExpense.value = null
        
        viewModelScope.launch {
            firebaseRepository.getExpenseById(expenseId)
                .onSuccess { expense ->
                    _currentExpense.value = expense
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Không thể tải chi tiêu"
                }
            _isLoading.value = false
        }
    }
    
    // ==================== FILTER & SEARCH OPERATIONS ====================
    
    /**
     * Tìm kiếm expenses
     */
    fun searchExpenses(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * Lọc expenses theo category
     */
    fun filterByCategory(category: String) {
        _selectedCategory.value = category
        
        if (category == "Tất cả") {
            loadExpenses()
        } else {
            _isLoading.value = true
            _errorMessage.value = null
            _expenseState.value = ExpenseState.Loading
            
            viewModelScope.launch {
                firebaseRepository.getExpensesByCategory(category)
                    .onSuccess { expenseList ->
                        _expenses.value = expenseList
                        _expenseState.value = ExpenseState.Success(expenseList)
                    }
                    .onFailure { error ->
                        _errorMessage.value = error.message ?: "Không thể lọc theo danh mục"
                        _expenseState.value = ExpenseState.Error(error.message ?: "Lỗi không xác định")
                    }
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Lọc expenses theo khoảng thời gian
     */
    fun filterByDateRange(startDate: Date, endDate: Date) {
        _isLoading.value = true
        _errorMessage.value = null
        _expenseState.value = ExpenseState.Loading
        
        viewModelScope.launch {
            firebaseRepository.getExpensesByDateRange(startDate, endDate)
                .onSuccess { expenseList ->
                    _expenses.value = expenseList
                    _expenseState.value = ExpenseState.Success(expenseList)
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Không thể lọc theo thời gian"
                    _expenseState.value = ExpenseState.Error(error.message ?: "Lỗi không xác định")
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
     * Reset search và filter
     */
    fun resetFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = "Tất cả"
        loadExpenses()
    }
    
    /**
     * Lấy filtered expenses dựa trên search query
     */
    fun getFilteredExpenses(): List<Expense> {
        val currentExpenses = _expenses.value
        val query = _searchQuery.value.lowercase()
        
        return if (query.isBlank()) {
            currentExpenses
        } else {
            currentExpenses.filter { expense ->
                expense.title.lowercase().contains(query) ||
                expense.note.lowercase().contains(query) ||
                expense.category.lowercase().contains(query)
            }
        }
    }
    
    /**
     * Tính tổng chi tiêu
     */
    fun getTotalExpenses(): Long {
        return _expenses.value
            .filter { it.isExpense }
            .sumOf { it.amount }
    }
    
    /**
     * Tính tổng thu nhập
     */
    fun getTotalIncome(): Long {
        return _expenses.value
            .filter { !it.isExpense }
            .sumOf { it.amount }
    }
    
    /**
     * Tính số dư
     */
    fun getBalance(): Long {
        return getTotalIncome() - getTotalExpenses()
    }
}
