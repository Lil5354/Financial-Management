package com.example.expensetracker.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensetracker.ui.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.*

// Data class cho User Profile
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val avatar: String? = null,
    val joinDate: Date,
    val totalExpenses: Long,
    val totalIncome: Long,
    val budget: Long,
    val currency: String = "VND"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    isDarkTheme: Boolean = true,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val backgroundColor = if (isDarkTheme) Color(0xFF0F0F0F) else Color(0xFFFAFAFA)
    val cardColor = if (isDarkTheme) Color(0xFF1F2937) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
    val mutedTextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    
    // Get profile from ViewModel
    val profile by profileViewModel.profile.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val errorMessage by profileViewModel.errorMessage.collectAsState()
    val successMessage by profileViewModel.successMessage.collectAsState()
    
    // Form state
    var name by remember { mutableStateOf(profile?.name ?: "") }
    var email by remember { mutableStateOf(profile?.email ?: "") }
    var phone by remember { mutableStateOf(profile?.phone ?: "") }
    var budget by remember { mutableStateOf((profile?.budget ?: 0L).toString()) }
    var isEditing by remember { mutableStateOf(false) }
    var showAvatarPicker by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    
    // Load profile on first composition
    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }
    
    // Update form fields when profile changes
    LaunchedEffect(profile) {
        profile?.let {
            name = it.name
            email = it.email
            phone = it.phone
            budget = it.budget.toString()
        }
    }
    
    // Handle success/error messages
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            // Close editing mode on success
            isEditing = false
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = textColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Hồ sơ cá nhân",
                        style = MaterialTheme.typography.headlineSmall,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Edit/Save button
                    IconButton(
                        onClick = { 
                            if (isEditing) {
                                // Save profile
                                profileViewModel.updateProfile(name, email, phone, budget)
                            } else {
                                isEditing = true
                            }
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Save else Icons.Default.Edit,
                            contentDescription = if (isEditing) "Lưu" else "Chỉnh sửa",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
        
        // Profile Content
        profile?.let { currentProfile ->
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
            // Avatar Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentProfile.avatar != null) {
                            // TODO: Load avatar image
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(60.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = currentProfile.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Thành viên từ ${SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(currentProfile.joinDate)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = mutedTextColor
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Change Avatar Button
                    OutlinedButton(
                        onClick = { showAvatarPicker = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF10B981)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Đổi ảnh",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đổi ảnh đại diện")
                    }
                }
            }
            
            // Personal Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Thông tin cá nhân",
                        style = MaterialTheme.typography.titleLarge,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Họ và tên", color = mutedTextColor) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Tên",
                                tint = mutedTextColor
                            )
                        },
                        enabled = isEditing,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = mutedTextColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedLabelColor = Color(0xFF10B981),
                            unfocusedLabelColor = mutedTextColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email", color = mutedTextColor) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = mutedTextColor
                            )
                        },
                        enabled = isEditing,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = mutedTextColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedLabelColor = Color(0xFF10B981),
                            unfocusedLabelColor = mutedTextColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Phone
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Số điện thoại", color = mutedTextColor) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Điện thoại",
                                tint = mutedTextColor
                            )
                        },
                        enabled = isEditing,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = mutedTextColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedLabelColor = Color(0xFF10B981),
                            unfocusedLabelColor = mutedTextColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
            
            // Financial Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Tổng quan tài chính",
                        style = MaterialTheme.typography.titleLarge,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Budget Setting
                    OutlinedTextField(
                        value = budget,
                        onValueChange = { budget = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Ngân sách hàng tháng", color = mutedTextColor) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Ngân sách",
                                tint = mutedTextColor
                            )
                        },
                        trailingIcon = {
                            Text(
                                text = "đ",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        enabled = isEditing,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = mutedTextColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedLabelColor = Color(0xFF10B981),
                            unfocusedLabelColor = mutedTextColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Financial Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Total Income
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${formatAmountProfile(currentProfile.totalIncome)}đ",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tổng thu nhập",
                                style = MaterialTheme.typography.bodySmall,
                                color = mutedTextColor
                            )
                        }
                        
                        // Total Expenses
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${formatAmountProfile(currentProfile.totalExpenses)}đ",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tổng chi tiêu",
                                style = MaterialTheme.typography.bodySmall,
                                color = mutedTextColor
                            )
                        }
                        
                        // Balance
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${formatAmountProfile(currentProfile.totalIncome - currentProfile.totalExpenses)}đ",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF3B82F6),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Số dư",
                                style = MaterialTheme.typography.bodySmall,
                                color = mutedTextColor
                            )
                        }
                    }
                }
            }
            
            // Account Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Cài đặt tài khoản",
                        style = MaterialTheme.typography.titleLarge,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Change Password
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showChangePasswordDialog = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Mật khẩu",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Đổi mật khẩu",
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Mở",
                            tint = mutedTextColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Divider(color = mutedTextColor.copy(alpha = 0.1f))
                    
                    // Privacy Settings
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PrivacyTip,
                            contentDescription = "Bảo mật",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Cài đặt bảo mật",
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Mở",
                            tint = mutedTextColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Divider(color = mutedTextColor.copy(alpha = 0.1f))
                    
                    // Delete Account
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDeleteAccountDialog = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa tài khoản",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Xóa tài khoản",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Mở",
                            tint = mutedTextColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    } ?: Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Text("Không có thông tin profile")
        }
    }
    }
    
    // Avatar Picker Dialog
    if (showAvatarPicker) {
        AlertDialog(
            onDismissRequest = { showAvatarPicker = false },
            title = {
                Text(
                    text = "Đổi ảnh đại diện",
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Chọn cách thay đổi ảnh đại diện của bạn",
                    color = textColor
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        // TODO: Open camera
                        showAvatarPicker = false
                    }
                ) {
                    Text("Chụp ảnh", color = Color(0xFF10B981))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        // TODO: Open gallery
                        showAvatarPicker = false
                    }
                ) {
                    Text("Chọn từ thư viện", color = Color(0xFF10B981))
                }
            },
            containerColor = cardColor
        )
    }
    
    // Change Password Dialog
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onConfirm = { currentPassword, newPassword, confirmPassword ->
                profileViewModel.updatePassword(currentPassword, newPassword, confirmPassword)
                showChangePasswordDialog = false
            },
            isLoading = isLoading,
            cardColor = cardColor,
            textColor = textColor,
            mutedTextColor = mutedTextColor
        )
    }
    
    // Delete Account Dialog
    if (showDeleteAccountDialog) {
        DeleteAccountDialog(
            onDismiss = { showDeleteAccountDialog = false },
            onConfirm = { password ->
                profileViewModel.deleteAccount(password)
                showDeleteAccountDialog = false
            },
            isLoading = isLoading,
            cardColor = cardColor,
            textColor = textColor,
            mutedTextColor = mutedTextColor
        )
    }
    
    // Show error snackbar
    errorMessage?.let { error ->
        LaunchedEffect(error) {
            // TODO: Show snackbar
            kotlinx.coroutines.delay(3000)
            profileViewModel.clearError()
        }
    }
    
    // Show success snackbar
    successMessage?.let { success ->
        LaunchedEffect(success) {
            // TODO: Show snackbar
            kotlinx.coroutines.delay(3000)
            profileViewModel.clearSuccess()
        }
    }
}

// Helper function
fun formatAmountProfile(amount: Long): String {
    return String.format("%,d", amount)
}

/**
 * Dialog để đổi mật khẩu
 */
@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
    isLoading: Boolean,
    cardColor: Color,
    textColor: Color,
    mutedTextColor: Color
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Đổi mật khẩu",
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Mật khẩu hiện tại") },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Mật khẩu mới") },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Xác nhận mật khẩu mới") },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(currentPassword, newPassword, confirmPassword) },
                enabled = !isLoading && currentPassword.isNotBlank() && newPassword.isNotBlank() && confirmPassword.isNotBlank()
            ) {
                Text("Đổi mật khẩu", color = Color(0xFF10B981))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Hủy", color = mutedTextColor)
            }
        },
        containerColor = cardColor
    )
}

/**
 * Dialog để xóa tài khoản
 */
@Composable
fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isLoading: Boolean,
    cardColor: Color,
    textColor: Color,
    mutedTextColor: Color
) {
    var password by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Xóa tài khoản",
                color = Color(0xFFEF4444),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Bạn có chắc chắn muốn xóa tài khoản? Hành động này không thể hoàn tác.",
                    color = textColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Nhập mật khẩu để xác nhận") },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(password) },
                enabled = !isLoading && password.isNotBlank()
            ) {
                Text("Xóa tài khoản", color = Color(0xFFEF4444))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Hủy", color = mutedTextColor)
            }
        },
        containerColor = cardColor
    )
}

// PREVIEW - Xem giao diện ngay lập tức!
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(
        onNavigateBack = {}
    )
}
