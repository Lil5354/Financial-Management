package com.example.expensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.repository.AuthRepository
import com.example.expensetracker.data.service.DefaultCategoryService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel để quản lý authentication state và logic
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val defaultCategoryService: DefaultCategoryService
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    /**
     * Kiểm tra trạng thái đăng nhập hiện tại
     */
    fun checkAuthState() {
        val currentUser = authRepository.currentUser
        if (currentUser != null) {
            _authState.value = AuthState.Success(currentUser)
        } else {
            _authState.value = AuthState.Idle
        }
    }
    
    /**
     * Đăng nhập với email và password
     */
    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Vui lòng nhập đầy đủ thông tin"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            authRepository.signIn(email.trim(), password)
                .onSuccess { user ->
                    _authState.value = AuthState.Success(user)
                    _isLoading.value = false
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(getErrorMessage(error))
                    _errorMessage.value = getErrorMessage(error)
                    _isLoading.value = false
                }
        }
    }
    
    /**
     * Đăng ký tài khoản mới
     */
    fun signUp(email: String, password: String, fullName: String = "", confirmPassword: String = "", acceptTerms: Boolean = true) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Vui lòng nhập đầy đủ thông tin"
            return
        }
        
        if (password.length < 6) {
            _errorMessage.value = "Mật khẩu phải có ít nhất 6 ký tự"
            return
        }
        
        if (confirmPassword.isNotBlank() && password != confirmPassword) {
            _errorMessage.value = "Mật khẩu xác nhận không khớp"
            return
        }
        
        if (!acceptTerms) {
            _errorMessage.value = "Vui lòng đồng ý với điều khoản sử dụng"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            authRepository.signUp(email.trim(), password)
                .onSuccess { user ->
                    // Cập nhật display name nếu có
                    if (fullName.isNotBlank()) {
                        authRepository.updateDisplayName(fullName)
                    }
                    
                    // Tạo default categories cho user mới
                    defaultCategoryService.createDefaultCategoriesForNewUser()
                    
                    // Không tự động set AuthState.Success sau khi đăng ký
                    // User cần đăng nhập lại để vào ứng dụng
                    _authState.value = AuthState.Idle
                    _isLoading.value = false
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(getErrorMessage(error))
                    _errorMessage.value = getErrorMessage(error)
                    _isLoading.value = false
                }
        }
    }
    
    /**
     * Gửi email reset password
     */
    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _errorMessage.value = "Vui lòng nhập email"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            authRepository.resetPassword(email.trim())
                .onSuccess {
                    _errorMessage.value = "Email reset password đã được gửi đến ${email.trim()}"
                    _isLoading.value = false
                }
                .onFailure { error ->
                    _errorMessage.value = getErrorMessage(error)
                    _isLoading.value = false
                }
        }
    }
    
    /**
     * Đăng xuất
     */
    fun signOut() {
        authRepository.signOut()
        _authState.value = AuthState.Idle
        _errorMessage.value = null
    }
    
    /**
     * Xóa error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Chuyển đổi Firebase error thành message tiếng Việt
     */
    private fun getErrorMessage(error: Throwable): String {
        return when (error.message) {
            "The email address is badly formatted." -> "Email không đúng định dạng"
            "The password is invalid or the user does not have a password." -> "Mật khẩu không đúng"
            "There is no user record corresponding to this identifier. The user may have been deleted." -> "Tài khoản không tồn tại"
            "The email address is already in use by another account." -> "Email này đã được sử dụng"
            "Password should be at least 6 characters" -> "Mật khẩu phải có ít nhất 6 ký tự"
            "A network error (such as timeout, interrupted connection or unreachable host) has occurred." -> "Lỗi kết nối mạng. Vui lòng kiểm tra internet"
            "An internal error has occurred. [ INVALID_LOGIN_CREDENTIALS ]" -> "Email hoặc mật khẩu không đúng"
            "An internal error has occurred. [ TOO_MANY_ATTEMPTS_TRY_LATER ]" -> "Quá nhiều lần thử. Vui lòng thử lại sau"
            else -> error.message ?: "Có lỗi xảy ra. Vui lòng thử lại"
        }
    }
}
