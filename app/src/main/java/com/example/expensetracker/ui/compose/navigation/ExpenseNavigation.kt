package com.example.expensetracker.ui.compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import java.util.*
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

/**
 * Navigation component cho ứng dụng
 * Sử dụng Jetpack Compose Navigation
 */
@Composable
fun NoNoNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = "welcome",
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
                onSkip = {
                    navController.navigate("home")
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
                    navController.navigate("home")
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
        
        // Main App Screens
        composable("home") {
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
        
        composable("expenses") {
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
        
        composable("add_expense") {
            AddExpenseScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaveExpense = { expense ->
                    // TODO: Save to database
                    navController.popBackStack()
                },
                isDarkTheme = isDarkTheme
            )
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
            ReportsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                isDarkTheme = isDarkTheme
            )
        }
        
        composable("settings") {
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
        
        composable("edit_expense/{expenseId}") { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getString("expenseId") ?: ""
            // TODO: Get expense from database by ID
            val sampleExpense = com.example.expensetracker.ui.compose.screens.Expense(
                id = expenseId,
                title = "Cà phê",
                amount = 25000,
                category = "Ăn uống",
                date = Date(),
                note = "Cà phê sáng"
            )
            
            EditExpenseScreen(
                expense = sampleExpense,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onUpdateExpense = { expense ->
                    // TODO: Update expense in database
                },
                onDeleteExpense = { id ->
                    // TODO: Delete expense from database
                },
                isDarkTheme = isDarkTheme
            )
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
