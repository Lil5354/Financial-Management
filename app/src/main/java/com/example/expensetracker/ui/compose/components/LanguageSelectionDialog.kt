package com.example.expensetracker.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.expensetracker.R
import com.example.expensetracker.data.service.LanguageManager

/**
 * Dialog để chọn ngôn ngữ cho ứng dụng
 * Hiển thị danh sách ngôn ngữ hỗ trợ và cho phép người dùng chọn
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionDialog(
    isVisible: Boolean,
    currentLanguage: String,
    supportedLanguages: List<LanguageManager.SupportedLanguage>,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    isDarkTheme: Boolean = true
) {
    if (isVisible) {
        val backgroundColor = if (isDarkTheme) Color(0xFF0F0F0F) else Color(0xFFFAFAFA)
        val cardColor = if (isDarkTheme) Color(0xFF1F2937) else Color.White
        val textColor = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
        val mutedTextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
        
        var selectedLanguage by remember { mutableStateOf(currentLanguage) }
        
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.language_selection_title),
                                style = MaterialTheme.typography.headlineSmall,
                                color = textColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.language_selection_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = mutedTextColor
                            )
                        }
                        
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = mutedTextColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Language list
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(supportedLanguages) { language ->
                            LanguageItem(
                                language = language,
                                isSelected = selectedLanguage == language.code,
                                onClick = {
                                    selectedLanguage = language.code
                                },
                                textColor = textColor,
                                mutedTextColor = mutedTextColor,
                                cardColor = cardColor
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = textColor
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 1.dp
                            )
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                        
                        Button(
                            onClick = {
                                if (selectedLanguage != currentLanguage) {
                                    onLanguageSelected(selectedLanguage)
                                }
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981),
                                contentColor = Color.White
                            )
                        ) {
                            Text(stringResource(R.string.language_apply))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Component hiển thị một item ngôn ngữ
 */
@Composable
private fun LanguageItem(
    language: LanguageManager.SupportedLanguage,
    isSelected: Boolean,
    onClick: () -> Unit,
    textColor: Color,
    mutedTextColor: Color,
    cardColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                Color(0xFF10B981).copy(alpha = 0.1f)
            } else {
                cardColor
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Language flag/icon placeholder
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (language.code) {
                            "vi" -> "🇻🇳"
                            "en" -> "🇺🇸"
                            else -> "🌐"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = language.nativeName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) Color(0xFF10B981) else textColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                    Text(
                        text = language.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = mutedTextColor
                    )
                }
            }
            
            // Selection indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Not selected",
                    tint = mutedTextColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// Preview
@Preview(showBackground = true)
@Composable
fun LanguageSelectionDialogPreview() {
    val supportedLanguages = listOf(
        LanguageManager.SupportedLanguage("vi", "Vietnamese", "Tiếng Việt"),
        LanguageManager.SupportedLanguage("en", "English", "English")
    )
    
    LanguageSelectionDialog(
        isVisible = true,
        currentLanguage = "vi",
        supportedLanguages = supportedLanguages,
        onLanguageSelected = {},
        onDismiss = {},
        isDarkTheme = true
    )
}

@Preview(showBackground = true)
@Composable
fun LanguageItemPreview() {
    LanguageItem(
        language = LanguageManager.SupportedLanguage("vi", "Vietnamese", "Tiếng Việt"),
        isSelected = true,
        onClick = {},
        textColor = Color.White,
        mutedTextColor = Color(0xFF9CA3AF),
        cardColor = Color(0xFF1F2937)
    )
}

