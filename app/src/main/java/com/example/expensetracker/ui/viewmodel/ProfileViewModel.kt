package com.example.expensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.repository.AuthRepository
import com.example.expensetracker.data.repository.ProfileRepository
import com.example.expensetracker.ui.compose.screens.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel để quản lý profile state và logic
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    /**
     * Tải profile của user
     */
    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            profileRepository.getUserProfile()
                .onSuccess { profile ->
                    _profile.value = profile
                    _isLoading.value = false
                    // Load financial stats
                    loadFinancialStats()
                }
                .onFailure { error ->
                    _errorMessage.value = getErrorMessage(error)
                    _isLoading.value = false
                }
        }
    }
    
    /**
     * Tải thống kê tài chính
     */
    private fun loadFinancialStats() {
        viewModelScope.launch {
            profileRepository.getFinancialStats()
                .onSuccess { (totalIncome, totalExpenses) ->
                    // Update profile with latest stats
                    val currentProfile = _profile.value
                    if (currentProfile != null) {
                        _profile.value = currentProfile.copy(
                            totalIncome = totalIncome,
                            totalExpenses = totalExpenses
                        )
                    }
                }
                .onFailure { error ->
                    // Don't show error for stats loading failure
                    // Just log it silently
                }
        }
    }
    
    /**
     * Cập nhật profile
     */
    fun updateProfile(
        name: String,
        email: String,
        phone: String,
        budget: String
    ) {
        if (name.isBlank()) {
            _errorMessage.value = "Vui lòng nhập họ và tên"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val currentProfile = _profile.value
            if (currentProfile == null) {
                _errorMessage.value = "Không tìm thấy thông tin profile"
                _isLoading.value = false
                return@launch
            }
            
            val budgetValue = try {
                budget.replace(",", "").toLongOrNull() ?: 0L
            } catch (e: Exception) {
                0L
            }
            
            val updatedProfile = currentProfile.copy(
                name = name,
                phone = phone,
                budget = budgetValue
            )
            
            profileRepository.updateUserProfile(updatedProfile)
                .onSuccess {
                    // Update display name in Firebase Auth
                    profileRepository.updateDisplayName(name)
                    
                    _profile.value = updatedProfile
                    _successMessage.value = "Cập nhật thông tin thành công!"
                    _isLoading.value = false
                }
                .onFailure { error ->
                    _errorMessage.value = getErrorMessage(error)
                    _isLoading.value = false
                }
        }
    }
    
    /**
     * Cập nhật mật khẩu
     */
    fun updatePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            _errorMessage.value = "Vui lòng nhập đầy đủ thông tin"
            return
        }
        
        if (newPassword.length < 6) {
            _errorMessage.value = "Mật khẩu mới phải có ít nhất 6 ký tự"
            return
        }
        
        if (newPassword != confirmPassword) {
            _errorMessage.value = "Mật khẩu xác nhận không khớp"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            // Re-authenticate user with current password
            val currentUser = authRepository.currentUser
            if (currentUser?.email != null) {
                val email = currentUser.email!!
                
                // Try to sign in with current password to verify
                authRepository.signIn(email, currentPassword)
                    .onSuccess {
                        // If successful, update password
                        authRepository.updatePassword(newPassword)
                            .onSuccess {
                                _successMessage.value = "Đổi mật khẩu thành công!"
                                _isLoading.value = false
                            }
                            .onFailure { error ->
                                _errorMessage.value = getErrorMessage(error)
                                _isLoading.value = false
                            }
                    }
                    .onFailure { error ->
                        _errorMessage.value = "Mật khẩu hiện tại không đúng"
                        _isLoading.value = false
                    }
            } else {
                _errorMessage.value = "Không thể xác thực người dùng"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Xóa tài khoản
     */
    fun deleteAccount(password: String) {
        if (password.isBlank()) {
            _errorMessage.value = "Vui lòng nhập mật khẩu để xác nhận"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val currentUser = authRepository.currentUser
            if (currentUser?.email != null) {
                val email = currentUser.email!!
                
                // Re-authenticate before deletion
                authRepository.signIn(email, password)
                    .onSuccess {
                        authRepository.deleteAccount()
                            .onSuccess {
                                _successMessage.value = "Tài khoản đã được xóa thành công"
                                _isLoading.value = false
                            }
                            .onFailure { error ->
                                _errorMessage.value = getErrorMessage(error)
                                _isLoading.value = false
                            }
                    }
                    .onFailure { error ->
                        _errorMessage.value = "Mật khẩu không đúng"
                        _isLoading.value = false
                    }
            } else {
                _errorMessage.value = "Không thể xác thực người dùng"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Xóa error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Xóa success message
     */
    fun clearSuccess() {
        _successMessage.value = null
    }
    
    /**
     * Chuyển đổi error thành message tiếng Việt
     */
    private fun getErrorMessage(error: Throwable): String {
        return when (error.message) {
            "User not authenticated" -> "Người dùng chưa đăng nhập"
            "A network error (such as timeout, interrupted connection or unreachable host) has occurred." -> 
                "Lỗi kết nối mạng. Vui lòng kiểm tra internet"
            else -> error.message ?: "Có lỗi xảy ra. Vui lòng thử lại"
        }
    }
}

