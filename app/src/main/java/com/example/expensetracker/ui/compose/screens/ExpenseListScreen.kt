package com.example.expensetracker.ui.compose.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensetracker.data.entity.Expense
import com.example.expensetracker.ui.viewmodel.ExpenseViewModel
import com.example.expensetracker.ui.viewmodel.ExpenseState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddExpense: () -> Unit = {},
    onNavigateToEditExpense: (String) -> Unit = {},
    isDarkTheme: Boolean = true,
    expenseViewModel: ExpenseViewModel = hiltViewModel()
) {
    val backgroundColor = if (isDarkTheme) Color(0xFF0F0F0F) else Color(0xFFFAFAFA)
    val cardColor = if (isDarkTheme) Color(0xFF1F2937) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
    val mutedTextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    
    // Collect states from ViewModel
    val expenseState by expenseViewModel.expenseState.collectAsState()
    val isLoading by expenseViewModel.isLoading.collectAsState()
    val errorMessage by expenseViewModel.errorMessage.collectAsState()
    val expenses by expenseViewModel.expenses.collectAsState()
    val searchQuery by expenseViewModel.searchQuery.collectAsState()
    val selectedCategory by expenseViewModel.selectedCategory.collectAsState()
    
    // Local state for search input
    var searchInput by remember { mutableStateOf("") }
    
    // Load expenses when screen is first displayed
    LaunchedEffect(Unit) {
        expenseViewModel.loadExpenses()
    }
    
    // Clear error when search input changes
    LaunchedEffect(searchInput) {
        if (errorMessage != null) {
            expenseViewModel.clearError()
        }
    }
    
    val categories = listOf("Tất cả", "Ăn uống", "Giao thông", "Mua sắm", "Thu nhập", "Khác")
    
    // Get filtered expenses
    val filteredExpenses = expenseViewModel.getFilteredExpenses()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header với nút back và tìm kiếm
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
                            text = "Danh sách chi tiêu",
                            style = MaterialTheme.typography.headlineSmall,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Search bar
                    OutlinedTextField(
                        value = searchInput,
                        onValueChange = { 
                            searchInput = it
                            expenseViewModel.searchExpenses(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Tìm kiếm chi tiêu...", color = mutedTextColor) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Tìm kiếm",
                                tint = mutedTextColor
                            )
                        },
                        trailingIcon = {
                            if (searchInput.isNotEmpty()) {
                                IconButton(onClick = { 
                                    searchInput = ""
                                    expenseViewModel.searchExpenses("")
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Xóa",
                                        tint = mutedTextColor
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = mutedTextColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Category filter
                    LazyColumn(
                        modifier = Modifier.height(40.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        item {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                categories.forEach { category ->
                                    FilterChip(
                                        onClick = { 
                                            expenseViewModel.filterByCategory(category)
                                        },
                                        label = { Text(category, color = if (selectedCategory == category) Color.White else textColor) },
                                        selected = selectedCategory == category,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF10B981),
                                            containerColor = cardColor
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Error message
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
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
            
            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF10B981),
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else {
                // Danh sách chi tiêu
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (filteredExpenses.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = mutedTextColor,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = when {
                                        searchInput.isNotEmpty() -> "Không tìm thấy chi tiêu nào"
                                        selectedCategory != "Tất cả" -> "Không có chi tiêu trong danh mục này"
                                        else -> "Chưa có chi tiêu nào"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = mutedTextColor
                                )
                            }
                        }
                    } else {
                        items(filteredExpenses) { expense ->
                            ExpenseItem(
                                expense = expense,
                                cardColor = cardColor,
                                textColor = textColor,
                                mutedTextColor = mutedTextColor,
                                onClick = { onNavigateToEditExpense(expense.id) }
                            )
                        }
                    }
                }
            }
        }
        
        // Floating Action Button
        FloatingActionButton(
            onClick = onNavigateToAddExpense,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = Color(0xFF10B981),
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Thêm chi tiêu",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// Expense Item Component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseItem(
    expense: Expense,
    cardColor: Color,
    textColor: Color,
    mutedTextColor: Color,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = getCategoryColor(expense.category).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(expense.category),
                    contentDescription = expense.category,
                    tint = getCategoryColor(expense.category),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Expense details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = expense.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedTextColor
                )
                if (expense.note.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = expense.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = mutedTextColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${dateFormat.format(expense.date)} • ${timeFormat.format(expense.date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedTextColor
                )
            }
            
            // Amount
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${if (expense.isExpense) "-" else "+"}${formatAmount(expense.amount)}đ",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (expense.isExpense) Color(0xFFEF4444) else Color(0xFF10B981),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (expense.isExpense) "Chi tiêu" else "Thu nhập",
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedTextColor
                )
            }
        }
    }
}

// Helper functions
fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Ăn uống" -> Icons.Default.Restaurant
        "Giao thông" -> Icons.Default.DirectionsCar
        "Mua sắm" -> Icons.Default.ShoppingBag
        "Thu nhập" -> Icons.Default.AccountBalanceWallet
        "Khác" -> Icons.Default.Category
        else -> Icons.Default.Category
    }
}

fun getCategoryColor(category: String): Color {
    return when (category) {
        "Ăn uống" -> Color(0xFFF59E0B)
        "Giao thông" -> Color(0xFF3B82F6)
        "Mua sắm" -> Color(0xFF8B5CF6)
        "Thu nhập" -> Color(0xFF10B981)
        "Khác" -> Color(0xFF6B7280)
        else -> Color(0xFF6B7280)
    }
}

fun formatAmount(amount: Long): String {
    return String.format("%,d", amount)
}

// PREVIEW - Xem giao diện ngay lập tức!
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ExpenseListScreenPreview() {
    ExpenseListScreen(
        onNavigateBack = {}
    )
}

@Preview(showBackground = true)
@Composable
fun ExpenseItemPreview() {
    ExpenseItem(
        expense = Expense(
            id = "1",
            title = "Cà phê",
            amount = 25000L,
            category = "Ăn uống",
            date = Date(),
            note = "Cà phê sáng",
            isExpense = true
        ),
        cardColor = Color(0xFF1F2937),
        textColor = Color.White,
        mutedTextColor = Color(0xFF9CA3AF),
        onClick = {}
    )
}