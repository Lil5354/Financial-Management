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
import java.text.SimpleDateFormat
import java.util.*

// Data class cho Expense
data class Expense(
    val id: String,
    val title: String,
    val amount: Long,
    val category: String,
    val date: Date,
    val note: String = "",
    val isExpense: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddExpense: () -> Unit = {},
    onNavigateToEditExpense: (String) -> Unit = {},
    isDarkTheme: Boolean = true
) {
    val backgroundColor = if (isDarkTheme) Color(0xFF0F0F0F) else Color(0xFFFAFAFA)
    val cardColor = if (isDarkTheme) Color(0xFF1F2937) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
    val mutedTextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    
    // Sample data - sau này sẽ lấy từ database
    val sampleExpenses = remember {
        listOf(
            Expense("1", "Cà phê", 25000, "Ăn uống", Date(), "Cà phê sáng"),
            Expense("2", "Xăng xe", 150000, "Giao thông", Date(), "Đổ xăng"),
            Expense("3", "Ăn trưa", 80000, "Ăn uống", Date(), "Cơm trưa"),
            Expense("4", "Mua sắm", 200000, "Mua sắm", Date(), "Quần áo"),
            Expense("5", "Lương", 5000000, "Thu nhập", Date(), "Lương tháng", false)
        )
    }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tất cả") }
    
    val categories = listOf("Tất cả", "Ăn uống", "Giao thông", "Mua sắm", "Thu nhập", "Khác")
    
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
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
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
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
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
                                        onClick = { selectedCategory = category },
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
            
            // Danh sách chi tiêu
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val filteredExpenses = sampleExpenses.filter { expense ->
                    val matchesSearch = expense.title.contains(searchQuery, ignoreCase = true) ||
                                      expense.note.contains(searchQuery, ignoreCase = true)
                    val matchesCategory = selectedCategory == "Tất cả" || expense.category == selectedCategory
                    matchesSearch && matchesCategory
                }
                
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
                                text = if (searchQuery.isNotEmpty() || selectedCategory != "Tất cả") 
                                    "Không tìm thấy chi tiêu nào" 
                                else "Chưa có chi tiêu nào",
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
            amount = 25000,
            category = "Ăn uống",
            date = Date(),
            note = "Cà phê sáng"
        ),
        cardColor = Color(0xFF1F2937),
        textColor = Color.White,
        mutedTextColor = Color(0xFF9CA3AF),
        onClick = {}
    )
}