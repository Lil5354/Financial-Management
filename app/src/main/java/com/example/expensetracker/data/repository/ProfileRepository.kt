package com.example.expensetracker.data.repository

import com.example.expensetracker.ui.compose.screens.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository để quản lý thông tin profile của user
 */
@Singleton
class ProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    companion object {
        private const val USERS_COLLECTION = "users"
    }
    
    /**
     * Lấy profile của user hiện tại từ Firestore
     */
    suspend fun getUserProfile(): Result<UserProfile> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Result.failure(Exception("User not authenticated"))
            } else {
                val document = firestore.collection(USERS_COLLECTION)
                    .document(currentUser.uid)
                    .get()
                    .await()
                
                if (document.exists()) {
                    val profile = document.toUserProfile()
                    Result.success(profile)
                } else {
                    // Tạo profile mặc định nếu chưa có
                    val defaultProfile = UserProfile(
                        id = currentUser.uid,
                        name = currentUser.displayName ?: "No Name",
                        email = currentUser.email ?: "",
                        phone = currentUser.phoneNumber ?: "",
                        avatar = currentUser.photoUrl?.toString(),
                        joinDate = java.util.Date(),
                        totalExpenses = 0,
                        totalIncome = 0,
                        budget = 0,
                        currency = "VND"
                    )
                    // Lưu profile mặc định
                    updateUserProfile(defaultProfile)
                    Result.success(defaultProfile)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cập nhật profile của user
     */
    suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Result.failure(Exception("User not authenticated"))
            } else {
                val profileMap = mapOf(
                    "id" to profile.id,
                    "name" to profile.name,
                    "email" to profile.email,
                    "phone" to profile.phone,
                    "avatar" to profile.avatar,
                    "joinDate" to profile.joinDate,
                    "totalExpenses" to profile.totalExpenses,
                    "totalIncome" to profile.totalIncome,
                    "budget" to profile.budget,
                    "currency" to profile.currency,
                    "updatedAt" to java.util.Date()
                )
                
                firestore.collection(USERS_COLLECTION)
                    .document(currentUser.uid)
                    .set(profileMap)
                    .await()
                
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cập nhật tên hiển thị
     */
    suspend fun updateDisplayName(name: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Result.failure(Exception("User not authenticated"))
            } else {
                // Cập nhật trong Firestore
                firestore.collection(USERS_COLLECTION)
                    .document(currentUser.uid)
                    .update("name", name)
                    .await()
                
                // Cập nhật trong Firebase Auth
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                currentUser.updateProfile(profileUpdates).await()
                
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cập nhật số điện thoại
     */
    suspend fun updatePhone(phone: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Result.failure(Exception("User not authenticated"))
            } else {
                firestore.collection(USERS_COLLECTION)
                    .document(currentUser.uid)
                    .update("phone", phone)
                    .await()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cập nhật ngân sách hàng tháng
     */
    suspend fun updateBudget(budget: Long): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Result.failure(Exception("User not authenticated"))
            } else {
                firestore.collection(USERS_COLLECTION)
                    .document(currentUser.uid)
                    .update("budget", budget)
                    .await()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cập nhật ảnh đại diện
     */
    suspend fun updateAvatar(avatarUrl: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Result.failure(Exception("User not authenticated"))
            } else {
                // Cập nhật trong Firestore
                firestore.collection(USERS_COLLECTION)
                    .document(currentUser.uid)
                    .update("avatar", avatarUrl)
                    .await()
                
                // Cập nhật trong Firebase Auth
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setPhotoUri(android.net.Uri.parse(avatarUrl))
                    .build()
                currentUser.updateProfile(profileUpdates).await()
                
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Lấy thống kê tài chính của user
     */
    suspend fun getFinancialStats(): Result<Pair<Long, Long>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Result.failure(Exception("User not authenticated"))
            } else {
                // Lấy thống kê từ collection expenses
                val expensesSnapshot = firestore.collection("expenses")
                    .whereEqualTo("userId", currentUser.uid)
                    .get()
                    .await()
                
                val expenses = expensesSnapshot.toObjects(com.example.expensetracker.data.entity.Expense::class.java)
                val totalIncome = expenses.filter { !it.isExpense }.sumOf { it.amount }
                val totalExpenses = expenses.filter { it.isExpense }.sumOf { it.amount }
                
                // Cập nhật vào profile
                val profile = getUserProfile().getOrNull()
                if (profile != null) {
                    updateUserProfile(profile.copy(totalIncome = totalIncome, totalExpenses = totalExpenses))
                }
                
                Result.success(Pair(totalIncome, totalExpenses))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Extension function để convert DocumentSnapshot thành UserProfile
     */
    private fun DocumentSnapshot.toUserProfile(): UserProfile {
        return UserProfile(
            id = getString("id") ?: "",
            name = getString("name") ?: "No Name",
            email = getString("email") ?: "",
            phone = getString("phone") ?: "",
            avatar = getString("avatar"),
            joinDate = getDate("joinDate") ?: java.util.Date(),
            totalExpenses = (getLong("totalExpenses") ?: 0L),
            totalIncome = (getLong("totalIncome") ?: 0L),
            budget = (getLong("budget") ?: 0L),
            currency = getString("currency") ?: "VND"
        )
    }
}

