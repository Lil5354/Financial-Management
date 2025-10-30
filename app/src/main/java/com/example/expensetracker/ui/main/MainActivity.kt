package com.example.expensetracker.ui.main

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.core.view.WindowCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.expensetracker.ui.compose.components.BottomNavigationBar
import com.example.expensetracker.ui.compose.navigation.NoNoNavigation
import com.example.expensetracker.ui.compose.theme.NoNoTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

/**
 * MainActivity sử dụng Jetpack Compose
 * Giao diện hiện đại với Material Design 3
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { updateLocale(it) } ?: newBase)
    }
    
    private fun updateLocale(context: Context): Context {
        val languagePrefs = context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
        val languageCode = languagePrefs.getString("selected_language", "vi") ?: "vi"
        
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            context
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            NoNoTheme {
                NoNoApp()
            }
        }
    }
}

@Composable
fun NoNoApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "welcome"
    
    // State để quản lý theme với SharedPreferences
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { 
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) 
    }
    var isDarkTheme by remember { 
        mutableStateOf(prefs.getBoolean("is_dark_theme", true)) 
    }
    
    // Danh sách các route cần hiển thị bottom navigation
    val mainAppRoutes = listOf("home", "expenses", "ai", "reports", "settings")
    val shouldShowBottomBar = currentRoute in mainAppRoutes
    
    // Cấu hình System UI để hiển thị thanh trạng thái
    val view = LocalView.current
    LaunchedEffect(isDarkTheme) {
        val window = (view.context as ComponentActivity).window
        window.statusBarColor = Color.Transparent.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkTheme
        
        // Lưu trạng thái theme vào SharedPreferences
        prefs.edit().putBoolean("is_dark_theme", isDarkTheme).apply()
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = if (isDarkTheme) Color(0xFF0F0F0F) else Color(0xFFFAFAFA)
    ) {
        Scaffold(
            topBar = { }, // Ẩn TopAppBar
            bottomBar = {
                if (shouldShowBottomBar) {
                    BottomNavigationBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            },
            containerColor = if (isDarkTheme) Color(0xFF0F0F0F) else Color(0xFFFAFAFA)
        ) { paddingValues ->
            NoNoNavigation(
                navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (shouldShowBottomBar) paddingValues else PaddingValues(0.dp)),
                isDarkTheme = isDarkTheme,
                onToggleTheme = { isDarkTheme = !isDarkTheme }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoNoAppPreview() {
    NoNoTheme {
        NoNoApp()
    }
}
