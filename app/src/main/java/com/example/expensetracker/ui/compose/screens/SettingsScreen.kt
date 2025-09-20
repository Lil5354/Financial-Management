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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {}
) {
    val backgroundColor = if (isDarkTheme) Color(0xFF0F0F0F) else Color(0xFFFAFAFA)
    val cardColor = if (isDarkTheme) Color(0xFF1F2937) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
    val mutedTextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    
    // Settings data
    val generalSettings = remember {
        listOf(
            SettingItem(
                title = "Giao diện",
                subtitle = if (isDarkTheme) "Tối" else "Sáng",
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
                title = "Ngôn ngữ",
                subtitle = "Tiếng Việt",
                icon = Icons.Default.Language,
                onClick = { /* TODO: Language selection */ }
            ),
            SettingItem(
                title = "Đơn vị tiền tệ",
                subtitle = "VND (₫)",
                icon = Icons.Default.AttachMoney,
                onClick = { /* TODO: Currency selection */ }
            )
        )
    }
    
    val accountSettings = remember {
        listOf(
            SettingItem(
                title = "Hồ sơ cá nhân",
                subtitle = "Chỉnh sửa thông tin",
                icon = Icons.Default.Person,
                onClick = onNavigateToProfile
            ),
            SettingItem(
                title = "Bảo mật",
                subtitle = "Mật khẩu, xác thực",
                icon = Icons.Default.Security,
                onClick = { /* TODO: Security settings */ }
            ),
            SettingItem(
                title = "Thông báo",
                subtitle = "Cài đặt thông báo",
                icon = Icons.Default.Notifications,
                onClick = { /* TODO: Notification settings */ }
            )
        )
    }
    
    val dataSettings = remember {
        listOf(
            SettingItem(
                title = "Sao lưu dữ liệu",
                subtitle = "Backup toàn bộ dữ liệu",
                icon = Icons.Default.Backup,
                onClick = { /* TODO: Backup data */ }
            ),
            SettingItem(
                title = "Khôi phục dữ liệu",
                subtitle = "Restore từ backup",
                icon = Icons.Default.Restore,
                onClick = { /* TODO: Restore data */ }
            ),
            SettingItem(
                title = "Xuất dữ liệu",
                subtitle = "Export ra file Excel/CSV",
                icon = Icons.Default.FileDownload,
                onClick = { /* TODO: Export data */ }
            )
        )
    }
    
    val supportSettings = remember {
        listOf(
            SettingItem(
                title = "Trợ giúp & Hỗ trợ",
                subtitle = "FAQ, liên hệ",
                icon = Icons.Default.Help,
                onClick = { /* TODO: Help & Support */ }
            ),
            SettingItem(
                title = "Đánh giá ứng dụng",
                subtitle = "Rate trên Play Store",
                icon = Icons.Default.Star,
                onClick = { /* TODO: Rate app */ }
            ),
            SettingItem(
                title = "Chia sẻ ứng dụng",
                subtitle = "Giới thiệu cho bạn bè",
                icon = Icons.Default.Share,
                onClick = { /* TODO: Share app */ }
            )
        )
    }
    
    val aboutSettings = remember {
        listOf(
            SettingItem(
                title = "Phiên bản",
                subtitle = "v1.0.0",
                icon = Icons.Default.Info,
                onClick = { /* TODO: Version info */ }
            ),
            SettingItem(
                title = "Điều khoản sử dụng",
                subtitle = "Terms of Service",
                icon = Icons.Default.Description,
                onClick = { /* TODO: Terms of Service */ }
            ),
            SettingItem(
                title = "Chính sách bảo mật",
                subtitle = "Privacy Policy",
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
                        contentDescription = "Quay lại",
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Cài đặt",
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
                        text = "Quản lý chi tiêu thông minh",
                        style = MaterialTheme.typography.bodyMedium,
                        color = mutedTextColor
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Phiên bản 1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = mutedTextColor
                    )
                }
            }
        }
        
        // General Settings
        item {
            SettingsSection(
                title = "Cài đặt chung",
                settings = generalSettings,
                cardColor = cardColor,
                textColor = textColor,
                mutedTextColor = mutedTextColor
            )
        }
        
        // Account Settings
        item {
            SettingsSection(
                title = "Tài khoản",
                settings = accountSettings,
                cardColor = cardColor,
                textColor = textColor,
                mutedTextColor = mutedTextColor
            )
        }
        
        // Data Settings
        item {
            SettingsSection(
                title = "Dữ liệu",
                settings = dataSettings,
                cardColor = cardColor,
                textColor = textColor,
                mutedTextColor = mutedTextColor
            )
        }
        
        // Support Settings
        item {
            SettingsSection(
                title = "Hỗ trợ",
                settings = supportSettings,
                cardColor = cardColor,
                textColor = textColor,
                mutedTextColor = mutedTextColor
            )
        }
        
        // About Settings
        item {
            SettingsSection(
                title = "Về ứng dụng",
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