package com.example.expensetracker.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dialog để cấu hình và thực hiện xuất dữ liệu
 * Cho phép chọn khoảng thời gian và định dạng file
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDialog(
    isVisible: Boolean,
    onExport: (startDate: Date?, endDate: Date?, format: ExportFormat) -> Unit,
    onDismiss: () -> Unit,
    isExporting: Boolean = false,
    exportProgress: Float = 0f,
    isDarkTheme: Boolean = true
) {
    if (isVisible) {
        val backgroundColor = if (isDarkTheme) Color(0xFF0F0F0F) else Color(0xFFFAFAFA)
        val cardColor = if (isDarkTheme) Color(0xFF1F2937) else Color.White
        val textColor = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
        val mutedTextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
        
        var selectedRange by remember { mutableStateOf(DateRange.ALL_TIME) }
        var selectedFormat by remember { mutableStateOf(ExportFormat.EXCEL) }
        var startDate by remember { mutableStateOf<Date?>(null) }
        var endDate by remember { mutableStateOf<Date?>(null) }
        var showStartDatePicker by remember { mutableStateOf(false) }
        var showEndDatePicker by remember { mutableStateOf(false) }
        
        Dialog(onDismissRequest = { if (!isExporting) onDismiss() }) {
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
                                text = stringResource(R.string.export_title),
                                style = MaterialTheme.typography.headlineSmall,
                                color = textColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.export_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = mutedTextColor
                            )
                        }
                        
                        if (!isExporting) {
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
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (isExporting) {
                        // Progress indicator
                        ExportProgressSection(
                            progress = exportProgress,
                            textColor = textColor,
                            mutedTextColor = mutedTextColor
                        )
                    } else {
                        // Date range selection
                        DateRangeSection(
                            selectedRange = selectedRange,
                            startDate = startDate,
                            endDate = endDate,
                            onRangeChanged = { range ->
                                selectedRange = range
                                when (range) {
                                    DateRange.THIS_MONTH -> {
                                        val calendar = Calendar.getInstance()
                                        calendar.set(Calendar.DAY_OF_MONTH, 1)
                                        startDate = calendar.time
                                        calendar.add(Calendar.MONTH, 1)
                                        calendar.add(Calendar.DAY_OF_MONTH, -1)
                                        endDate = calendar.time
                                    }
                                    DateRange.LAST_MONTH -> {
                                        val calendar = Calendar.getInstance()
                                        calendar.add(Calendar.MONTH, -1)
                                        calendar.set(Calendar.DAY_OF_MONTH, 1)
                                        startDate = calendar.time
                                        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                                        endDate = calendar.time
                                    }
                                    DateRange.CUSTOM -> {
                                        // Keep existing dates or reset to null
                                    }
                                    DateRange.ALL_TIME -> {
                                        startDate = null
                                        endDate = null
                                    }
                                }
                            },
                            onStartDateClick = { showStartDatePicker = true },
                            onEndDateClick = { showEndDatePicker = true },
                            textColor = textColor,
                            mutedTextColor = mutedTextColor,
                            cardColor = cardColor
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Format selection
                        FormatSelectionSection(
                            selectedFormat = selectedFormat,
                            onFormatChanged = { selectedFormat = it },
                            textColor = textColor,
                            mutedTextColor = mutedTextColor,
                            cardColor = cardColor
                        )
                        
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
                                )
                            ) {
                                Text(stringResource(R.string.cancel))
                            }
                            
                            Button(
                                onClick = {
                                    val finalStartDate = if (selectedRange == DateRange.CUSTOM || 
                                                            selectedRange == DateRange.THIS_MONTH || 
                                                            selectedRange == DateRange.LAST_MONTH) startDate else null
                                    val finalEndDate = if (selectedRange == DateRange.CUSTOM || 
                                                          selectedRange == DateRange.THIS_MONTH || 
                                                          selectedRange == DateRange.LAST_MONTH) endDate else null
                                    onExport(finalStartDate, finalEndDate, selectedFormat)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF10B981),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.export_start))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Section hiển thị tiến trình xuất
 */
@Composable
private fun ExportProgressSection(
    progress: Float,
    textColor: Color,
    mutedTextColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.FileDownload,
            contentDescription = null,
            tint = Color(0xFF10B981),
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.export_progress),
            style = MaterialTheme.typography.titleMedium,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = Color(0xFF10B981),
            trackColor = Color(0xFF374151)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = mutedTextColor
        )
    }
}

/**
 * Section chọn khoảng thời gian
 */
@Composable
private fun DateRangeSection(
    selectedRange: DateRange,
    startDate: Date?,
    endDate: Date?,
    onRangeChanged: (DateRange) -> Unit,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    textColor: Color,
    mutedTextColor: Color,
    cardColor: Color
) {
    Column {
        Text(
            text = stringResource(R.string.export_date_range),
            style = MaterialTheme.typography.titleMedium,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Range options
        DateRange.values().forEach { range ->
            DateRangeOption(
                range = range,
                isSelected = selectedRange == range,
                onClick = { onRangeChanged(range) },
                textColor = textColor,
                mutedTextColor = mutedTextColor,
                cardColor = cardColor
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Custom date selection
        if (selectedRange == DateRange.CUSTOM) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start date
                CustomDateField(
                    label = stringResource(R.string.export_from),
                    date = startDate,
                    onClick = onStartDateClick,
                    textColor = textColor,
                    mutedTextColor = mutedTextColor,
                    modifier = Modifier.weight(1f)
                )
                
                // End date  
                CustomDateField(
                    label = stringResource(R.string.export_to),
                    date = endDate,
                    onClick = onEndDateClick,
                    textColor = textColor,
                    mutedTextColor = mutedTextColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Option cho date range
 */
@Composable
private fun DateRangeOption(
    range: DateRange,
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
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = range.getDisplayName(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) Color(0xFF10B981) else textColor,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Field cho custom date
 */
@Composable
private fun CustomDateField(
    label: String,
    date: Date?,
    onClick: () -> Unit,
    textColor: Color,
    mutedTextColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = mutedTextColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = mutedTextColor.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = date?.let { 
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                    } ?: "Chọn ngày",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (date != null) textColor else mutedTextColor
                )
            }
        }
    }
}

/**
 * Section chọn định dạng file
 */
@Composable
private fun FormatSelectionSection(
    selectedFormat: ExportFormat,
    onFormatChanged: (ExportFormat) -> Unit,
    textColor: Color,
    mutedTextColor: Color,
    cardColor: Color
) {
    Column {
        Text(
            text = stringResource(R.string.export_format),
            style = MaterialTheme.typography.titleMedium,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExportFormat.values().forEach { format ->
                FormatOption(
                    format = format,
                    isSelected = selectedFormat == format,
                    onClick = { onFormatChanged(format) },
                    textColor = textColor,
                    mutedTextColor = mutedTextColor,
                    cardColor = cardColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Option cho export format
 */
@Composable
private fun FormatOption(
    format: ExportFormat,
    isSelected: Boolean,
    onClick: () -> Unit,
    textColor: Color,
    mutedTextColor: Color,
    cardColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                Color(0xFF10B981).copy(alpha = 0.1f)
            } else {
                cardColor
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (format == ExportFormat.EXCEL) Icons.Default.TableChart else Icons.Default.Description,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF10B981) else mutedTextColor,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = format.getDisplayName(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) Color(0xFF10B981) else textColor,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

// Enums
enum class DateRange {
    ALL_TIME,
    THIS_MONTH, 
    LAST_MONTH,
    CUSTOM;
    
    fun getDisplayName(): String {
        return when (this) {
            ALL_TIME -> "Toàn bộ thời gian"
            THIS_MONTH -> "Tháng này"
            LAST_MONTH -> "Tháng trước"
            CUSTOM -> "Tùy chọn"
        }
    }
}

enum class ExportFormat {
    EXCEL,
    CSV;
    
    fun getDisplayName(): String {
        return when (this) {
            EXCEL -> "Excel (.xlsx)"
            CSV -> "CSV (.csv)"
        }
    }
}

// Preview
@Preview(showBackground = true)
@Composable
fun ExportDialogPreview() {
    ExportDialog(
        isVisible = true,
        onExport = { _, _, _ -> },
        onDismiss = {},
        isDarkTheme = true
    )
}

