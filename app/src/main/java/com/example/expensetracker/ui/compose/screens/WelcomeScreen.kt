package com.example.expensetracker.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onNavigateToSignIn: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    isDarkTheme: Boolean
) {
    val cardColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val mutedTextColor = if (isDarkTheme) Color(0xFFB0B0B0) else Color(0xFF666666)
    val primaryColor = Color(0xFF10B981)
    val accentColor = Color(0xFF3B82F6)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDarkTheme) listOf(
                        Color(0xFF0F0F0F),
                        Color(0xFF1A1A1A)
                    ) else listOf(
                        Color(0xFFF8FAFC),
                        Color(0xFFE2E8F0)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Main content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // App Logo/Icon
                Card(
                    modifier = Modifier.size(120.dp),
                    colors = CardDefaults.cardColors(containerColor = primaryColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "NoNo Logo",
                            tint = Color.White,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
                
                // Welcome text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Chào mừng đến với NoNo",
                        style = MaterialTheme.typography.headlineLarge,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "Ứng dụng quản lý chi tiêu cá nhân thông minh với AI",
                        style = MaterialTheme.typography.bodyLarge,
                        color = mutedTextColor,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
                
                // Features
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FeatureItem(
                        icon = Icons.Default.Analytics,
                        title = "Phân tích thông minh",
                        description = "AI phân tích chi tiêu và đưa ra lời khuyên",
                        iconColor = accentColor,
                        textColor = textColor,
                        mutedTextColor = mutedTextColor
                    )
                    
                    FeatureItem(
                        icon = Icons.Default.CameraAlt,
                        title = "Scan hóa đơn",
                        description = "Tự động nhận diện và nhập chi tiêu",
                        iconColor = primaryColor,
                        textColor = textColor,
                        mutedTextColor = mutedTextColor
                    )
                    
                    FeatureItem(
                        icon = Icons.Default.Security,
                        title = "Bảo mật tuyệt đối",
                        description = "Dữ liệu được mã hóa và bảo vệ an toàn",
                        iconColor = Color(0xFF8B5CF6),
                        textColor = textColor,
                        mutedTextColor = mutedTextColor
                    )
                }
            }
            
            // Bottom buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onNavigateToSignUp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Tạo tài khoản",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                OutlinedButton(
                    onClick = onNavigateToSignIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = textColor
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(
                            colors = listOf(primaryColor, accentColor)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Đã có tài khoản? Đăng nhập",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    iconColor: Color,
    textColor: Color,
    mutedTextColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.size(48.dp),
            colors = CardDefaults.cardColors(
                containerColor = iconColor.copy(alpha = 0.1f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = mutedTextColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    MaterialTheme {
        WelcomeScreen(
            onNavigateToSignIn = {},
            onNavigateToSignUp = {},
            isDarkTheme = false
        )
    }
}
