package com.example.expensetracker.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository để quản lý Firebase Authentication
 * Cung cấp các chức năng đăng nhập, đăng ký, đăng xuất
 */
@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {
    
    /**
     * Lấy user hiện tại
     */
    val currentUser: FirebaseUser?
        get() = auth.currentUser
    
    /**
     * Kiểm tra user có đăng nhập không
     */
    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null
    
    /**
     * Đăng nhập với email và password
     */
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Sign in failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Đăng ký tài khoản mới với email và password
     */
    suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Sign up failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Gửi email reset password
     */
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Đăng xuất
     */
    fun signOut() {
        auth.signOut()
    }
    
    /**
     * Lấy UID của user hiện tại
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    /**
     * Lấy email của user hiện tại
     */
    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }
    
    /**
     * Lấy display name của user hiện tại
     */
    fun getCurrentUserDisplayName(): String? {
        return auth.currentUser?.displayName
    }
    
    /**
     * Cập nhật display name
     */
    suspend fun updateDisplayName(displayName: String): Result<Unit> {
        return try {
            val user = auth.currentUser
            if (user != null) {
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                user.updateProfile(profileUpdates).await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("User not authenticated"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cập nhật password
     */
    suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            val user = auth.currentUser
            if (user != null) {
                user.updatePassword(newPassword).await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("User not authenticated"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Xóa tài khoản
     */
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = auth.currentUser
            if (user != null) {
                user.delete().await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("User not authenticated"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

