package com.example.expensetracker.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensetracker.R
import com.example.expensetracker.data.service.LanguageManager
import com.example.expensetracker.data.service.ExcelExportService
import com.example.expensetracker.ui.compose.components.LanguageSelectionDialog
import com.example.expensetracker.ui.compose.components.ExportDialog
import com.example.expensetracker.ui.compose.components.ExportFormat
import com.example.expensetracker.ui.viewmodel.ExpenseViewModel
import com.example.expensetracker.ui.viewmodel.AuthViewModel
import android.content.Intent
import android.app.Activity

// Data class cho Setting Item
data class SettingItem(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val trailing: @Composable (() -> Unit)? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onSignOut: () -> Unit = {},
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {},
    languageManager: LanguageManager? = null,
    expenseViewModel: ExpenseViewModel = hiltViewModel(),
    excelExportService: ExcelExportService? = null,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val localLanguageManager = remember { languageManager ?: LanguageManager(context) }
    val localExcelExportService = remember { excelExportService ?: ExcelExportService(context) }
    
    // Language state
    val currentLanguage by localLanguageManager.currentLanguage.collectAsState()
    val isLanguageChanged by localLanguageManager.isLanguageChanged.collectAsState()
    val currentLanguageName = localLanguageManager.getCurrentLanguageNativeName()
    
    // Export state
    val exportState by localExcelExportService.exportState.collectAsState()
    val exportProgress by localExcelExportService.progress.collectAsState()
    val expenses by expenseViewModel.expenses.collectAsState()
    
    // Dialog states
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showExportSuccess by remember { mutableStateOf(false) }
    var exportedFileName by remember { mutableStateOf("") }
    
    // Load expenses for export
    LaunchedEffect(Unit) {
        expenseViewModel.loadExpenses()
    }
    
    // Handle language change - restart activity to apply new language
    LaunchedEffect(isLanguageChanged) {
        if (isLanguageChanged) {
            localLanguageManager.resetLanguageChangeState()
            // Restart activity to apply language change
            if (context is Activity) {
                val intent = Intent(context, context.javaClass)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
                if (context is Activity) {
                    context.finish()
                }
            }
        }
    }
    
    // Handle export state changes
    LaunchedEffect(exportState) {
        when (val state = exportState) {
            is ExcelExportService.ExportState.Success -> {
                showExportDialog = false
                exportedFileName = state.fileName
                showExportSuccess = true
                localExcelExportService.resetExportState()
            }
            is ExcelExportService.ExportState.Error -> {
                showExportDialog = false
                // TODO: Show error snackbar
                localExcelExportService.resetExportState()
            }
            else -> {}
        }
    }
    val backgroundColor = if (isDarkTheme) Color(0xFF0F0F0F) else Color(0xFFFAFAFA)
    val cardColor = if (isDarkTheme) Color(0xFF1F2937) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
    val mutedTextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    
    // Get string resources outside of remember
    val settingsTheme = stringResource(R.string.settings_theme)
    val settingsThemeDark = stringResource(R.string.settings_theme_dark)
    val settingsThemeLight = stringResource(R.string.settings_theme_light)
    val settingsLanguage = stringResource(R.string.settings_language)
    val settingsProfile = stringResource(R.string.settings_profile)
    val settingsProfileDesc = stringResource(R.string.settings_profile_desc)
    val settingsSecurity = stringResource(R.string.settings_security)
    val settingsSecurityDesc = stringResource(R.string.settings_security_desc)
    val settingsNotifications = stringResource(R.string.settings_notifications)
    val settingsNotificationsDesc = stringResource(R.string.settings_notifications_desc)
    val settingsLogout = stringResource(R.string.settings_logout)
    val settingsLogoutDesc = stringResource(R.string.settings_logout_desc)
    val settingsLogoutConfirm = stringResource(R.string.settings_logout_confirm)
    val settingsBackup = stringResource(R.string.settings_backup)
    val settingsBackupDesc = stringResource(R.string.settings_backup_desc)
    val settingsRestore = stringResource(R.string.settings_restore)
    val settingsRestoreDesc = stringResource(R.string.settings_restore_desc)
    val settingsExport = stringResource(R.string.settings_export)
    val settingsExportDesc = stringResource(R.string.settings_export_desc)
    val settingsHelp = stringResource(R.string.settings_help)
    val settingsHelpDesc = stringResource(R.string.settings_help_desc)
    val settingsRate = stringResource(R.string.settings_rate)
    val settingsRateDesc = stringResource(R.string.settings_rate_desc)
    val settingsShareApp = stringResource(R.string.settings_share_app)
    val settingsShareAppDesc = stringResource(R.string.settings_share_app_desc)
    val settingsVersion = stringResource(R.string.settings_version)
    val settingsVersionNumber = stringResource(R.string.settings_version_number)
    val settingsTerms = stringResource(R.string.settings_terms)
    val settingsPrivacy = stringResource(R.string.settings_privacy)
    val settingsGeneral = stringResource(R.string.settings_general)
    val settingsAccount = stringResource(R.string.settings_account)
    val settingsData = stringResource(R.string.settings_data)
    val settingsSupport = stringResource(R.string.settings_support)
    val settingsAboutApp = stringResource(R.string.settings_about_app)
    val settingsAppDescription = stringResource(R.string.settings_app_description)
    
    // Settings data
    val generalSettings = remember(isDarkTheme, settingsTheme, settingsThemeDark, settingsThemeLight, settingsLanguage, currentLanguageName) {
        listOf(
            SettingItem(
                title = settingsTheme,
                subtitle = if (isDarkTheme) settingsThemeDark else settingsThemeLight,
                icon = Icons.Default.Palette,
                onClick = onToggleTheme,
                trailing = {
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { onToggleTheme() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFF6B7280)
                        )
                    )
                }
            ),
            SettingItem(
                title = settingsLanguage,
                subtitle = currentLanguageName,
                icon = Icons.Default.Language,
                onClick = { showLanguageDialog = true }
            )
        )
    }
    
    val accountSettings = remember(settingsProfile, settingsProfileDesc, settingsSecurity, settingsSecurityDesc, settingsNotifications, settingsNotificationsDesc, settingsLogout, settingsLogoutDesc) {
        listOf(
            SettingItem(
                title = settingsProfile,
                subtitle = settingsProfileDesc,
                icon = Icons.Default.Person,
                onClick = onNavigateToProfile
            ),
            SettingItem(
                title = settingsSecurity,
                subtitle = settingsSecurityDesc,
                icon = Icons.Default.Security,
                onClick = { /* TODO: Security settings */ }
            ),
            SettingItem(
                title = settingsNotifications,
                subtitle = settingsNotificationsDesc,
                icon = Icons.Default.Notifications,
                onClick = { /* TODO: Notification settings */ }
            ),
            SettingItem(
                title = settingsLogout,
                subtitle = settingsLogoutDesc,
                icon = Icons.Default.ExitToApp,
                onClick = { showLogoutDialog = true }
            )
        )
    }
    
    val dataSettings = remember(settingsBackup, settingsBackupDesc, settingsRestore, settingsRestoreDesc, settingsExport, settingsExportDesc) {
        listOf(
            SettingItem(
                title = settingsBackup,
                subtitle = settingsBackupDesc,
                icon = Icons.Default.Backup,
                onClick = { /* TODO: Backup data */ }
            ),
            SettingItem(
                title = settingsRestore,
                subtitle = settingsRestoreDesc,
                icon = Icons.Default.Restore,
                onClick = { /* TODO: Restore data */ }
            ),
            SettingItem(
                title = settingsExport,
                subtitle = settingsExportDesc,
                icon = Icons.Default.FileDownload,
                onClick = { showExportDialog = true }
            )
        )
    }
    
    val supportSettings = remember(settingsHelp, settingsHelpDesc, settingsRate, settingsRateDesc, settingsShareApp, settingsShareAppDesc) {
        listOf(
            SettingItem(
                title = settingsHelp,
                subtitle = settingsHelpDesc,
                icon = Icons.Default.Help,
                onClick = { /* TODO: Help & Support */ }
            ),
            SettingItem(
                title = settingsRate,
                subtitle = settingsRateDesc,
                icon = Icons.Default.Star,
                onClick = { /* TODO: Rate app */ }
            ),
            SettingItem(
                title = settingsShareApp,
                subtitle = settingsShareAppDesc,
                icon = Icons.Default.Share,
                onClick = { /* TODO: Share app */ }
            )
        )
    }
    
    val aboutSettings = remember(settingsVersion, settingsVersionNumber, settingsTerms, settingsPrivacy) {
        listOf(
            SettingItem(
                title = settingsVersion,
                subtitle = settingsVersionNumber,
                icon = Icons.Default.Info,
                onClick = { /* TODO: Version info */ }
            ),
            SettingItem(
                title = settingsTerms,
                subtitle = settingsTerms,
                icon = Icons.Default.Description,
                onClick = { /* TODO: Terms of Service */ }
            ),
            SettingItem(
                title = settingsPrivacy,
                subtitle = settingsPrivacy,
                icon = Icons.Default.PrivacyTip,
                onClick = { /* TODO: Privacy Policy */ }
            )
        )
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Header
        item {
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
                        contentDescription = stringResource(R.string.back),
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // App Info Card
        item {
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
                    // App Logo
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = Color(0xFF10B981).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "NoNo Logo",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "NoNo",
                        style = MaterialTheme.typography.headlineMedium,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = settingsAppDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = mutedTextColor
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = settingsVersionNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = mutedTextColor
                    )
                }
            }
        }
        
        // General Settings
        item {
            SettingsSection(
                title = settingsGeneral,
                settings = generalSettings,
                cardColor = cardColor,
                textColor = textColor,
                mutedTextColor = mutedTextColor
            )
        }
        
        // Account Settings
        item {
            SettingsSection(
                title = settingsAccount,
                settings = accountSettings,
                cardColor = cardColor,
                textColor = textColor,
                mutedTextColor = mutedTextColor
            )
        }
        
        // Data Settings
        item {
            SettingsSection(
                title = settingsData,
                settings = dataSettings,
                cardColor = cardColor,
                textColor = textColor,
                mutedTextColor = mutedTextColor
            )
        }
        
        // Support Settings
        item {
            SettingsSection(
                title = settingsSupport,
                settings = supportSettings,
                cardColor = cardColor,
                textColor = textColor,
                mutedTextColor = mutedTextColor
            )
        }
        
        // About Settings
        item {
            SettingsSection(
                title = settingsAboutApp,
                settings = aboutSettings,
                cardColor = cardColor,
                textColor = textColor,
                mutedTextColor = mutedTextColor
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
    
    // Language Selection Dialog
    LanguageSelectionDialog(
        isVisible = showLanguageDialog,
        currentLanguage = currentLanguage,
        supportedLanguages = localLanguageManager.supportedLanguages,
        onLanguageSelected = { languageCode ->
            localLanguageManager.setLanguage(languageCode)
        },
        onDismiss = { showLanguageDialog = false },
        isDarkTheme = isDarkTheme
    )
    
    // Export Dialog
    val coroutineScope = rememberCoroutineScope()
    ExportDialog(
        isVisible = showExportDialog,
        onExport = { startDate, endDate, format ->
            when (format) {
                ExportFormat.EXCEL -> {
                    coroutineScope.launch {
                        localExcelExportService.exportExpensesToExcel(
                            expenses = expenses,
                            startDate = startDate,
                            endDate = endDate,
                            includeCharts = true
                        )
                    }
                }
                ExportFormat.CSV -> {
                    // TODO: Implement CSV export
                }
            }
        },
        onDismiss = { 
            if (exportState !is ExcelExportService.ExportState.InProgress) {
                showExportDialog = false
            }
        },
        isExporting = exportState is ExcelExportService.ExportState.InProgress,
        exportProgress = exportProgress,
        isDarkTheme = isDarkTheme
    )
    
    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = settingsLogout,
                    color = textColor,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = settingsLogoutConfirm,
                    color = textColor
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.signOut()
                        onSignOut()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFEF4444)
                    )
                ) {
                    Text(
                        text = settingsLogout,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        color = mutedTextColor
                    )
                }
            },
            containerColor = cardColor
        )
    }
    
    // Export Success Dialog
    if (showExportSuccess) {
        AlertDialog(
            onDismissRequest = { showExportSuccess = false },
            title = {
                Text("Xuất dữ liệu thành công!")
            },
            text = {
                Text("File '$exportedFileName' đã được lưu vào thư mục Downloads/NoNo")
            },
            confirmButton = {
                TextButton(
                    onClick = { showExportSuccess = false }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

// Settings Section Component
@Composable
fun SettingsSection(
    title: String,
    settings: List<SettingItem>,
    cardColor: Color,
    textColor: Color,
    mutedTextColor: Color
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                settings.forEachIndexed { index, setting ->
                    SettingItemRow(
                        setting = setting,
                        textColor = textColor,
                        mutedTextColor = mutedTextColor
                    )
                    if (index < settings.size - 1) {
                        Divider(
                            color = mutedTextColor.copy(alpha = 0.1f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

// Setting Item Row Component
@Composable
fun SettingItemRow(
    setting: SettingItem,
    textColor: Color,
    mutedTextColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { setting.onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = setting.icon,
            contentDescription = setting.title,
            tint = Color(0xFF10B981),
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = setting.title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
            if (setting.subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = setting.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedTextColor
                )
            }
        }
        
        if (setting.trailing != null) {
            setting.trailing?.invoke()
        } else {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Mở",
                tint = mutedTextColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// PREVIEW - Xem giao diện ngay lập tức!
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(
        onNavigateBack = {}
    )
}

@Preview(showBackground = true)
@Composable
fun SettingItemRowPreview() {
    SettingItemRow(
        setting = SettingItem(
            title = "Giao diện",
            subtitle = "Tối",
            icon = Icons.Default.Palette,
            onClick = {}
        ),
        textColor = Color.White,
        mutedTextColor = Color(0xFF9CA3AF)
    )
}