package com.example.expensetracker.ui.compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import java.util.*
import com.example.expensetracker.data.entity.Expense
import com.example.expensetracker.ui.compose.screens.HomeScreen
import com.example.expensetracker.ui.compose.screens.ExpenseListScreen
import com.example.expensetracker.ui.compose.screens.AddExpenseScreen
import com.example.expensetracker.ui.compose.screens.EditExpenseScreen
import com.example.expensetracker.ui.compose.screens.AIScreen
import com.example.expensetracker.ui.compose.screens.ReportsScreen
import com.example.expensetracker.ui.compose.screens.SettingsScreen
import com.example.expensetracker.ui.compose.screens.ProfileScreen
import com.example.expensetracker.ui.compose.screens.WelcomeScreen
import com.example.expensetracker.ui.compose.screens.SignInScreen
import com.example.expensetracker.ui.compose.screens.SignUpScreen
import com.example.expensetracker.ui.compose.screens.ForgotPasswordScreen
import com.example.expensetracker.ui.compose.screens.ResetPasswordScreen
import com.example.expensetracker.ui.viewmodel.AuthViewModel
import com.example.expensetracker.ui.viewmodel.AuthState
import com.example.expensetracker.ui.viewmodel.ExpenseViewModel

/**
 * Navigation component cho ứng dụng
 * Sử dụng Jetpack Compose Navigation với Firebase Authentication
 */
@Composable
fun NoNoNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    var isInitialLoad by remember { mutableStateOf(true) }
    var hasUserLoggedIn by remember { mutableStateOf(false) }
    
    // Kiểm tra trạng thái authentication khi khởi động
    LaunchedEffect(Unit) {
        authViewModel.checkAuthState()
        isInitialLoad = false
    }
    
    // Xử lý navigation dựa trên authentication state
    LaunchedEffect(authState, isInitialLoad, hasUserLoggedIn) {
        // Không xử lý navigation trong lần load đầu tiên
        if (isInitialLoad) return@LaunchedEffect
        
        when (authState) {
            is AuthState.Success -> {
                // User đã đăng nhập, chỉ navigate đến home nếu đang ở signin/signup
                // và user đã thực sự đăng nhập (không phải từ session cũ)
                if ((navController.currentDestination?.route == "signin" ||
                    navController.currentDestination?.route == "signup") && hasUserLoggedIn) {
                    navController.navigate("home") {
                        popUpTo("signin") { inclusive = true }
                    }
                }
            }
            is AuthState.Error -> {
                // Có lỗi authentication, navigate về signin
                if (navController.currentDestination?.route != "signin") {
                    navController.navigate("signin") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is AuthState.Idle -> {
                // Chưa đăng nhập, đảm bảo ở signin screen
                if (navController.currentDestination?.route == "home" ||
                    navController.currentDestination?.route == "expenses" ||
                    navController.currentDestination?.route == "reports" ||
                    navController.currentDestination?.route == "settings") {
                    navController.navigate("signin") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            else -> {
                // Không cần xử lý gì cho Loading state
            }
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = "signin",
        modifier = modifier
    ) {
        // Welcome/Onboarding Screen
        composable("welcome") {
            WelcomeScreen(
                onNavigateToSignIn = {
                    navController.navigate("signin")
                },
                onNavigateToSignUp = {
                    navController.navigate("signup")
                },
                isDarkTheme = isDarkTheme
            )
        }
        
        // Authentication Screens
        composable("signin") {
            SignInScreen(
                onNavigateToSignUp = {
                    navController.navigate("signup")
                },
                onNavigateToForgotPassword = {
                    navController.navigate("forgot_password")
                },
                onSignInSuccess = {
                    // Chỉ navigate khi user thực sự đăng nhập
                    hasUserLoggedIn = true
                    navController.navigate("home")
                },
                isDarkTheme = isDarkTheme
            )
        }
        
        composable("signup") {
            SignUpScreen(
                onNavigateToSignIn = {
                    navController.navigate("signin")
                },
                onSignUpSuccess = {
                    // Sau khi đăng ký thành công, quay về màn hình đăng nhập
                    navController.navigate("signin") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                isDarkTheme = isDarkTheme
            )
        }
        
        composable("forgot_password") {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToResetPassword = {
                    navController.navigate("reset_password")
                },
                isDarkTheme = isDarkTheme
            )
        }
        
        composable("reset_password") {
            ResetPasswordScreen(
                onNavigateToSignIn = {
                    navController.navigate("signin") {
                        popUpTo("welcome") { inclusive = false }
                    }
                },
                isDarkTheme = isDarkTheme
            )
        }
        
        // Main App Screens - Chỉ cho phép truy cập khi đã đăng nhập
        composable("home") {
            when (authState) {
                is AuthState.Success -> {
                    HomeScreen(
                        onNavigateToExpenses = {
                            navController.navigate("expenses")
                        },
                        onNavigateToReports = {
                            navController.navigate("reports")
                        },
                        onNavigateToSettings = {
                            navController.navigate("settings")
                        },
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme
                    )
                }
                else -> {
                    // Redirect to signin if not authenticated
                    LaunchedEffect(Unit) {
                        navController.navigate("signin") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
        }
        
        composable("expenses") {
            when (authState) {
                is AuthState.Success -> {
                    ExpenseListScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToAddExpense = {
                            navController.navigate("add_expense")
                        },
                        onNavigateToEditExpense = { expenseId ->
                            navController.navigate("edit_expense/$expenseId")
                        },
                        isDarkTheme = isDarkTheme
                    )
                }
                else -> {
                    LaunchedEffect(Unit) {
                        navController.navigate("signin") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
        }
        
        composable("add_expense") {
            when (authState) {
                is AuthState.Success -> {
                    AddExpenseScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onSaveExpense = { expense ->
                            // Expense will be saved via ViewModel
                            navController.popBackStack()
                        },
                        isDarkTheme = isDarkTheme
                    )
                }
                else -> {
                    LaunchedEffect(Unit) {
                        navController.navigate("signin") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
        }
        
        composable("ai") {
            AIScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                isDarkTheme = isDarkTheme
            )
        }
        
        composable("reports") {
            when (authState) {
                is AuthState.Success -> {
                    ReportsScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        isDarkTheme = isDarkTheme
                    )
                }
                else -> {
                    LaunchedEffect(Unit) {
                        navController.navigate("signin") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
        }
        
        composable("settings") {
            when (authState) {
                is AuthState.Success -> {
                    SettingsScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToProfile = {
                            navController.navigate("profile")
                        },
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme
                    )
                }
                else -> {
                    LaunchedEffect(Unit) {
                        navController.navigate("signin") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
        }
        
        composable("edit_expense/{expenseId}") { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getString("expenseId") ?: ""
            val expenseViewModel: ExpenseViewModel = hiltViewModel()
            
            // Load expense data from database
            LaunchedEffect(expenseId) {
                if (expenseId.isNotBlank()) {
                    expenseViewModel.getExpenseById(expenseId)
                }
            }
            
            val currentExpense by expenseViewModel.currentExpense.collectAsState()
            val isLoading by expenseViewModel.isLoading.collectAsState()
            val errorMessage by expenseViewModel.errorMessage.collectAsState()
            
            when {
                isLoading -> {
                    // Show loading screen
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF10B981)
                        )
                    }
                }
                errorMessage != null -> {
                    // Show error screen
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Lỗi: $errorMessage",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { navController.popBackStack() }
                            ) {
                                Text("Quay lại")
                            }
                        }
                    }
                }
                currentExpense != null -> {
                    // Show edit screen with real data
                    EditExpenseScreen(
                        expense = currentExpense!!,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onUpdateExpense = { expense ->
                            expenseViewModel.updateExpense(expense)
                        },
                        onDeleteExpense = { id ->
                            expenseViewModel.deleteExpense(id)
                        },
                        isDarkTheme = isDarkTheme
                    )
                }
                else -> {
                    // Show not found screen
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Không tìm thấy chi tiêu",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { navController.popBackStack() }
                            ) {
                                Text("Quay lại")
                            }
                        }
                    }
                }
            }
        }
        
        composable("profile") {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                isDarkTheme = isDarkTheme
            )
        }
    }
}
