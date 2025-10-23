package com.example.expensetracker.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensetracker.data.entity.Expense
import com.example.expensetracker.ui.viewmodel.ExpenseViewModel
import com.example.expensetracker.ui.viewmodel.CategoryViewModel
import com.example.expensetracker.ui.viewmodel.CrudState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onNavigateBack: () -> Unit,
    onSaveExpense: (Expense) -> Unit = {},
    isDarkTheme: Boolean = true,
    expenseViewModel: ExpenseViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val backgroundColor = if (isDarkTheme) Color(0xFF0F0F0F) else Color(0xFFFAFAFA)
    val cardColor = if (isDarkTheme) Color(0xFF1F2937) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
    val mutedTextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    
    // Collect states from ViewModels
    val isLoading by expenseViewModel.isLoading.collectAsState()
    val errorMessage by expenseViewModel.errorMessage.collectAsState()
    val crudState by expenseViewModel.crudState.collectAsState()
    val categories by categoryViewModel.categories.collectAsState()
    val isLoadingCategories by categoryViewModel.isLoading.collectAsState()
    val categoryErrorMessage by categoryViewModel.errorMessage.collectAsState()
    
    // Form state
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<com.example.expensetracker.data.entity.Category?>(null) }
    var selectedDate by remember { mutableStateOf(Date()) }
    var isExpense by remember { mutableStateOf(true) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Load categories when screen is first displayed
    LaunchedEffect(Unit) {
        categoryViewModel.loadCategories()
    }
    
    // Handle successful save
    LaunchedEffect(crudState) {
        when (crudState) {
            is CrudState.Success -> {
                onNavigateBack()
            }
            else -> { /* No-op */ }
        }
    }
    
    // Clear error when form fields change
    LaunchedEffect(title, amount, note) {
        if (errorMessage != null) {
            expenseViewModel.clearError()
        }
    }
    
    // Validation
    val isFormValid = title.isNotBlank() && amount.isNotBlank() && selectedCategory != null
    
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
                        text = if (isExpense) "Thêm chi tiêu" else "Thêm thu nhập",
                        style = MaterialTheme.typography.headlineSmall,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Expense/Income Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        onClick = { isExpense = true },
                        label = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = null,
                                    tint = if (isExpense) Color.White else textColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Chi tiêu", color = if (isExpense) Color.White else textColor)
                            }
                        },
                        selected = isExpense,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEF4444),
                            containerColor = cardColor
                        )
                    )
                    
                    FilterChip(
                        onClick = { isExpense = false },
                        label = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = if (!isExpense) Color.White else textColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Thu nhập", color = if (!isExpense) Color.White else textColor)
                            }
                        },
                        selected = !isExpense,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF10B981),
                            containerColor = cardColor
                        )
                    )
                }
            }
        }
        
        // Form Content
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tiêu đề", color = mutedTextColor) },
                placeholder = { Text("Ví dụ: Cà phê, Xăng xe...", color = mutedTextColor) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Tiêu đề",
                        tint = mutedTextColor
                    )
                },
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
            
            // Amount Input
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Số tiền", color = mutedTextColor) },
                placeholder = { Text("Nhập số tiền...", color = mutedTextColor) },
                trailingIcon = {
                    Text(
                        text = "đ",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                },
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
            
            // Error message
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Lỗi",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFEF4444),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = { expenseViewModel.clearError() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Đóng",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            
            // Category Selection
            Column {
                Text(
                    text = "Danh mục",
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                when {
                    isLoadingCategories -> {
                        // Hiển thị loading spinner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF10B981),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    categoryErrorMessage != null -> {
                        // Hiển thị error message với retry button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Lỗi: ${categoryErrorMessage}",
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = { categoryViewModel.loadCategories() }
                                ) {
                                    Text(
                                        text = "Thử lại",
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                    }
                    categories.isEmpty() -> {
                        // Hiển thị thông báo không có danh mục
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Không có danh mục nào",
                                color = mutedTextColor,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    else -> {
                        // Hiển thị danh sách categories
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(categories) { category ->
                                CategoryChip(
                                    category = category,
                                    isSelected = selectedCategory?.id == category.id,
                                    onClick = { selectedCategory = category },
                                    textColor = textColor
                                )
                            }
                        }
                    }
                }
            }
            
            // Date Selection
            Column {
                Text(
                    text = "Ngày",
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(12.dp),
                    onClick = { showDatePicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Ngày",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate),
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Chọn ngày",
                            tint = mutedTextColor
                        )
                    }
                }
            }
            
            // Note Input
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Ghi chú (tùy chọn)", color = mutedTextColor) },
                placeholder = { Text("Thêm ghi chú...", color = mutedTextColor) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Note,
                        contentDescription = "Ghi chú",
                        tint = mutedTextColor
                    )
                },
                minLines = 2,
                maxLines = 4,
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
            
            // Save Button
            Button(
                onClick = {
                    if (isFormValid) {
                        val expense = Expense(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            amount = amount.toLongOrNull() ?: 0L,
                            category = selectedCategory?.name ?: "",
                            date = selectedDate,
                            note = note,
                            isExpense = isExpense
                        )
                        expenseViewModel.addExpense(expense)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isFormValid && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    contentColor = Color.White,
                    disabledContainerColor = mutedTextColor,
                    disabledContentColor = textColor
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Lưu",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isExpense) "Lưu chi tiêu" else "Lưu thu nhập",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            selectedDate = selectedDate,
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
            cardColor = cardColor,
            textColor = textColor
        )
    }
}

// Category Chip Component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChip(
    category: com.example.expensetracker.data.entity.Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    textColor: Color
) {
    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(android.graphics.Color.parseColor(category.color)) else Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = getCategoryIconFromString(category.icon),
                contentDescription = category.name,
                tint = if (isSelected) Color.White else Color(android.graphics.Color.parseColor(category.color)),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) Color.White else textColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

// Helper function to get icon from string
fun getCategoryIconFromString(iconString: String): ImageVector {
    return when (iconString) {
        "restaurant" -> Icons.Default.Restaurant
        "directions_car" -> Icons.Default.DirectionsCar
        "shopping_bag" -> Icons.Default.ShoppingBag
        "movie" -> Icons.Default.Movie
        "local_hospital" -> Icons.Default.LocalHospital
        "school" -> Icons.Default.School
        "account_balance_wallet" -> Icons.Default.AccountBalanceWallet
        "category" -> Icons.Default.Category
        else -> Icons.Default.Category
    }
}

// Date Picker Dialog Component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit,
    cardColor: Color,
    textColor: Color
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.time
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Chọn ngày",
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Color(0xFF10B981),
                    todayDateBorderColor = Color(0xFF10B981)
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(Date(millis))
                    }
                }
            ) {
                Text("Chọn", color = Color(0xFF10B981))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = textColor)
            }
        },
        containerColor = cardColor
    )
}

// PREVIEW - Xem giao diện ngay lập tức!
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddExpenseScreenPreview() {
    AddExpenseScreen(
        onNavigateBack = {}
    )
}

@Preview(showBackground = true)
@Composable
fun CategoryChipPreview() {
    CategoryChip(
        category = com.example.expensetracker.data.entity.Category(
            id = "food", 
            name = "Ăn uống", 
            icon = "restaurant", 
            color = "#F59E0B"
        ),
        isSelected = true,
        onClick = {},
        textColor = Color.White
    )
}
